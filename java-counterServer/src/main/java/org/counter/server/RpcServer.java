package org.counter.server;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.counter.Counter;
import org.counter.impl.CounterImpl;
import org.counter.impl.StorageStrategy;

public class RpcServer {
	static RpcServer rpcServer = new RpcServer();
	
	protected TServer server = null;
	protected TProcessor processor = null;
	protected String host = "localhost";
	protected int port = 8203;
	protected int selectThread = 8;
	protected int workThread = 20;
	
	private Thread thriftThread;
	private static AtomicInteger threadID = new AtomicInteger(0);

	public RpcServer() {
	}

	public RpcServer(String host, int port, int selectThread,
			int workThread) {
		this.host = host;
		this.port = port;
		this.selectThread = selectThread;
		this.workThread = workThread;
	}

	public String stats() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(" host: ").append(host);
		buffer.append(" port: ").append(port);
		buffer.append(" selectThread: ").append(selectThread);
		buffer.append(" workThread: ").append(workThread);
		buffer.append(" isServing: ").append(isServeing());

		return buffer.toString();
	}

	public boolean isServeing() {
		if (server != null) {
			return server.isServing();
		} else {
			return false;
		}
	}
	
	public void init() {
		try {
			TNonblockingServerTransport serverTransport = new TNonblockingServerSocket(port);
			TThreadedSelectorServer.Args tThreadArgs = new TThreadedSelectorServer.Args(
					serverTransport);
			tThreadArgs.selectorThreads(selectThread);
			tThreadArgs.workerThreads(workThread);
			server = new TThreadedSelectorServer(tThreadArgs.processor(processor));
			//System.out.println(stats());
		} catch (TTransportException e) {
			e.printStackTrace();
		}		
	}
	
	public void start(){
		threadID.incrementAndGet();
		thriftThread = new Thread("thriftServerThread"+threadID) {
			@Override
			public void run() {
				server.serve();
			}
		};
		thriftThread.start();
	}

	public void halt() {
		if (server != null && server.isServing()) {
			server.stop();
		}
		System.out.println("thrift server halt : " + stats());
	}

	public TServer getServer() {
		return server;
	}

	public void setServer(TServer server) {
		this.server = server;
	}

	public TProcessor getProcessor() {
		return processor;
	}

	public void setProcessor(TProcessor processor) {
		this.processor = processor;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String Host) {
		this.host = Host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int Port) {
		this.port = Port;
	}

	public int getSelectThread() {
		return selectThread;
	}

	public void setSelectThread(int SelectThread) {
		this.selectThread = SelectThread;
	}

	public int getWorkThread() {
		return workThread;
	}

	public void setWorkThread(int WorkThread) {
		this.workThread = WorkThread;
	}
	

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Args obj = Args.parse(args);
		StorageStrategy.inMemory = obj.inMemory;

		CounterImpl counterImpl = new CounterImpl();
		TProcessor processor = new Counter.Processor<CounterImpl>(counterImpl);
		rpcServer.setProcessor(processor);
		rpcServer.init();
		Thread t = new Thread("start_bloom_server") {
			@Override
			public void run() {
				rpcServer.start();
				safeSleep(1000);
				System.out.println(rpcServer.stats() + " start listening. ");
			}
		};
		t.start();
	}
	static class Args {
		public boolean inMemory = false;

		public static Args parse(String[] args) {
			Args obj = new Args();

			for (int i = 0; i < args.length; i++) {
				if (StringUtils.equals(args[i], "-in")) {
					i++;
					obj.inMemory = Boolean.valueOf(args[i]);
				}
			}
			return obj;
		}
	}
	public static void safeSleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
