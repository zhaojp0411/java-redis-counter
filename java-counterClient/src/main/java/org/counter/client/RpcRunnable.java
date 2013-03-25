package org.counter.client;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.thrift.TException;
import org.counter.impl.CounterClient;

/**
 * Basic Rpc Task : bench test,call name()
 * 
 * @author zhiguo4
 * 
 */
public class RpcRunnable implements Runnable {

	private int count = 1;
	private CounterClient client;
	private String op = "get";
	private static AtomicLong sequence = new AtomicLong(1000000000L);

	public RpcRunnable(String host,int port,String op, int count) {
		client = new CounterClient(host, port);
		try {
			client.init();
		} catch (TException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			client = null;
		}
		this.op = op;
		this.count = count;
	}
	

	@Override
	public void run() {
		try {
			for (int i = 0; i < count; i++) {
				if (op.equals("get")){
					int ret = client.get("key" + sequence.incrementAndGet());
				}else if (op.equals("add")){
					client.add("key"+sequence.incrementAndGet(), i);
				}else if (op.equals("getlong")){
					int ret = client.getLong(sequence.incrementAndGet());
				}else if (op.equals("addlong")){
					client.addLong(sequence.incrementAndGet(), i);
				}
			}
		} catch (TException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
