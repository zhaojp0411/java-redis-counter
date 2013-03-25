package org.counter.impl;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.counter.Counter;
import org.counter.utils.TimeStatUtil;

public class CounterClient {

	private String host;
	private int port;
	
	// connection
	private Counter.Client client = null;
	private TTransport transport = null;
	
	private static List<Integer> elapseTime = new Vector<Integer>();
	private static Thread printThread;
	private static AtomicLong errorCount = new AtomicLong(0);
	static AtomicLong successCount = new AtomicLong(0);
	static {
		synchronized (elapseTime) {
			if (printThread == null) {
				printThread = new Thread("check-count-timestat") {
					public void run() {
						TimeStatUtil.printStat(successCount, errorCount, elapseTime);
					}
				};
				printThread.start();
			}
		}
	}
	
	public CounterClient(){
		
	}
	public CounterClient(final String host,final int port){
		this.host = host;
		this.port = port;
	}
    
	public void init() throws TException {
		
		try {
			transport = new TFramedTransport(new TSocket(host, port));
			TProtocol protocol = new TBinaryProtocol(transport);

			client = new Counter.Client(protocol);
			transport.open();
		} catch (TTransportException e) {
			throw new TException(e);
		}
	}

	public void closeClient() {
		if (transport != null){
			transport.close();
		}
	}
		
    public void add(String key,int value) throws TException{ 	
        try {
        	long start = System.currentTimeMillis();

			client.add(key,value);
			
			successCount.incrementAndGet();
			TimeStatUtil.addElapseTimeStat(elapseTime, (int) (System.currentTimeMillis() - start));
		
		} catch (TException e) {
			errorCount.incrementAndGet();
			throw e;
		}
    }
    
    public int get(String key) throws TException{ 	
        try {
        	long start = System.currentTimeMillis();

			int ret = client.get(key);
				
			successCount.incrementAndGet();
			TimeStatUtil.addElapseTimeStat(elapseTime, (int) (System.currentTimeMillis() - start));
			return ret;
		} catch (TException e) {
			errorCount.incrementAndGet();
			throw e;
		}
    }
    
    public void addLong(Long key,int value) throws TException{ 	
        try {
        	long start = System.currentTimeMillis();

			client.addLong(key,value);
			
			successCount.incrementAndGet();
			TimeStatUtil.addElapseTimeStat(elapseTime, (int) (System.currentTimeMillis() - start));
		
		} catch (TException e) {
			errorCount.incrementAndGet();
			throw e;
		}
    }
    
    public int getLong(Long key) throws TException{ 	
        try {
        	long start = System.currentTimeMillis();

			int ret = client.getLong(key);
				
			successCount.incrementAndGet();
			TimeStatUtil.addElapseTimeStat(elapseTime, (int) (System.currentTimeMillis() - start));
			return ret;
		} catch (TException e) {
			errorCount.incrementAndGet();
			throw e;
		}
    }
    
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
