package org.counter.recovery;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.counter.impl.MemCounter;
import org.counter.utils.Log;

import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.SingleThreadedClaimStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;

public class BaseRecovery implements RecoveryAble {
	private final int RECOVERY_PROCESSORS = 10;
	private final int RING_BUFFER_SIZE = 1024*8;
	private final int BUFFER_READER_SIZE = 1024*10;
	
	private final ExecutorService EXECUTOR = Executors
			.newFixedThreadPool(RECOVERY_PROCESSORS);	
	
	public BaseRecovery(){
	
	}
	
	@Override
	public void recoveryLogToMem(RecoveryLog recovery, MemCounter memCounter) throws IOException {
		
		long start = System.currentTimeMillis();
		
		// publish
		File file = recovery.getRecoveryFile();
		FileReader reader = null;
		BufferedReader bufferReader = null;
		try {
			if (file.length()==0){
				Log.info("recovery.log is empty");
			}else {
				RingBuffer<RecoveryEvent> ringBuffer =
				    new RingBuffer<RecoveryEvent>(RecoveryEvent.EVENT_FACTORY, 
				                               new SingleThreadedClaimStrategy(RING_BUFFER_SIZE),
				                               new YieldingWaitStrategy());
				SequenceBarrier barrier = ringBuffer.newBarrier();    
				
				RecoveryHandler recoveryHandler = new RecoveryHandler(memCounter);
				BatchEventProcessor<RecoveryEvent> recoveryProcessor = 
					new BatchEventProcessor<RecoveryEvent>(ringBuffer,barrier, recoveryHandler);
			
				ringBuffer.setGatingSequences(recoveryProcessor.getSequence()); 
				EXECUTOR.submit(recoveryProcessor);
				
				CountDownLatch latch = new CountDownLatch(1);
				long sequence = recoveryProcessor.getSequence().get();
				
				reader = new FileReader(file);
				bufferReader = new BufferedReader(reader,BUFFER_READER_SIZE);
				while (true) {
					try {
						String line = bufferReader.readLine();
						if (line == null) {
							recoveryHandler.reset(latch,sequence);
							break;				// file end
						}
						RecoveryEvent temp = parseRecoveryLog(line);
						
						// Publishers claim events in sequence
						sequence = ringBuffer.next();
						RecoveryEvent event = ringBuffer.get(sequence);
					
						event.setOp(temp.op);
						event.setPairs(temp.pairs);
						event.setTime(temp.time);
						event.setType(temp.type);

						// make the event available to EventProcessors
						ringBuffer.publish(sequence); 
					}catch (Exception e) {
						Log.error("recoveryFromLog error ", e);
					}
				}
				// wait for recovery ,timeout  
				try {
					latch.await(60, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					Log.warn("recovery from log timeout,some log not recovery to memcounter");
				}
				EXECUTOR.shutdown();
			}
		}catch (FileNotFoundException e) {
			Log.error("file not find error ", e);
		} finally {
			if (bufferReader!=null){
				bufferReader.close();
			}
			if (reader!=null){
				reader.close();
			}
		}
		
		Log.info("recovery form logfile to memcounter end, cost " + (System.currentTimeMillis()-start));
	}

	private RecoveryEvent parseRecoveryLog(String line) throws InvalidRecoveryLogException  {
		if (line == null) {
			Log.warn("log line is null");
			throw new InvalidRecoveryLogException("log line is null");
		}
		RecoveryEvent event = new RecoveryEvent();
		String[] items = line.split(RecoveryLog.LOG_SEPARATOR);
		if (items==null || items.length!=4){
			throw new InvalidRecoveryLogException("log line format invalid," + line);
		}
		event.setOp(items[0]);
		event.setType(items[1]);
		event.setTime(items[3]);
		
		String[] pairs = items[2].split(RecoveryLog.LOG_ITEM_SPARATOR);
		for (String pair : pairs) {
			String[] kv = pair.split(RecoveryLog.LOG_PAIR_SPARATOR);
			if (kv==null || kv.length!=2){
				throw new InvalidRecoveryLogException("log pair format invalid," + line);
			}
			event.getPairs().put(kv[0], Integer.valueOf(kv[1]));
		}
		return event;
	}
	
	public static class InvalidRecoveryLogException extends Exception {
		public InvalidRecoveryLogException() {
			super();
		}

		public InvalidRecoveryLogException(String message) {
			super(message);
		}
	}
	
	public class RecoveryHandler implements EventHandler<RecoveryEvent> {
		private MemCounter memCounter;
		private CountDownLatch latch;
		private long expect = -1;
		public RecoveryHandler(MemCounter counter){
			this.memCounter = counter;
		}
		
		public void reset(CountDownLatch latch,long sequence){
			this.latch = latch;
			this.expect = sequence;
		}
		
		@Override
		public void onEvent(RecoveryEvent event, long sequence, boolean endOfBatch)
				throws Exception {
			Log.debug("recovery sequence " + sequence + event.pairs
				+ " op " + event.getOp() + " time " + event.getTime());
			if (event.op.equalsIgnoreCase(RecoveryLog.OP_ADD)){
				if (event.type.equalsIgnoreCase(RecoveryLog.LOG_PAIR_KEY_STRING)) {
					memCounter.adds(event.pairs);	
				}else {
					Map<Long,Integer> pairs = new HashMap<Long,Integer>(event.pairs.size());
					for(String key : event.pairs.keySet()){
						pairs.put(Long.valueOf(key), event.pairs.get(key));
					}
					memCounter.addsLong(pairs);	
				}
				
			}
			if (expect!= -1 && expect == sequence) {
				latch.countDown();
			}
		}	
	}
	public static class RecoveryEvent {
		private String op;
		private String type;
		private Map<String,Integer> pairs = new HashMap<String,Integer>();	
		private String time;
		
		
		public RecoveryEvent(){
			
		}
		public RecoveryEvent(String op,Map<String,Integer> pairs){
			this.op = op;
			this.pairs.putAll(pairs);
		}
		
		public String getOp() {
			return op;
		}

		public void setOp(String op) {
			this.op = op;
		}

		public Map<String, Integer> getPairs() {
			return pairs;
		}

		public void setPairs(Map<String, Integer> pairs) {
			this.pairs = pairs;
		}
	
		public void setTime(String time) {
			this.time = time;
		}
		public String getTime() {
			return time;
		}

		public void setType(String type) {
			this.type = type;
		}
		public String getType() {
			return type;
		}

		public static EventFactory<RecoveryEvent> EVENT_FACTORY = new EventFactory<RecoveryEvent>() {
			public RecoveryEvent newInstance() {
				return new RecoveryEvent();
			}
		};
	}
}
