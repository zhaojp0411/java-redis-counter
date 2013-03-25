package org.counter.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TException;
import org.counter.impl.CounterClient;


public class RpcClient {

	public static void main(String[] args) {
			
		Args obj = Args.parse(args);
		long start = System.currentTimeMillis();

		processCall(obj);	
		
		long resumetime = System.currentTimeMillis()-start;
		System.out.println("rpc call " + obj.number + " resumetime(s) : " + resumetime/1000);
	}
	
	private static void processCall(Args obj){

		ExecutorService executor = Executors.newFixedThreadPool(obj.threadNumber);
		for (int i = 0; i < obj.threadNumber; i++) {
			int count = obj.number/obj.threadNumber;
			RpcRunnable rpcCmd = new RpcRunnable(obj.host, obj.port, obj.op ,count);
			executor.execute(rpcCmd);
		}
		
		(executor).shutdownNow();
		try {
			executor.awaitTermination(24, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	static class Args {
		private String host="localhost";
		private int port=8203;
		private int number=10000;
		private int threadNumber=1;
		private String op = "get";

		public static Args parse(String[] args) {
			Args obj = new Args();

			for (int i = 0; i < args.length; i++) {
				if (StringUtils.equals(args[i], "-host")) {
					i++;
					obj.host = args[i];
				} else if (StringUtils.equals(args[i], "-port")) {
					i++;
					obj.port = Integer.valueOf(args[i]);
				} else if (StringUtils.equals(args[i], "-c")) {
					i++;
					obj.threadNumber = Integer.parseInt(args[i]);
				} else if (StringUtils.equals(args[i], "-n")) {
					i++;
					obj.number = Integer.parseInt(args[i]);
				} else if (StringUtils.equals(args[i], "-op")) {
					i++;
					obj.op = args[i];
				}  
			}
			return obj;
		}
	}

}
