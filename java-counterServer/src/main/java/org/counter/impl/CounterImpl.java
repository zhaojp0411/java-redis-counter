package org.counter.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;
import org.counter.Counter.Iface;
import org.counter.NotFoundException;
import org.counter.TimedOutException;
import org.counter.UnavailableException;
import org.counter.recovery.BaseRecovery;
import org.counter.recovery.RecoveryLog;

public class CounterImpl extends BaseRecovery implements Iface{
	private HBaseCounter hbaseCounter;
	private MemCounter memCounter;
	private RecoveryLog recoveryLog;
	private StorageStrategy strategy;
	
	public CounterImpl() throws IOException{
		hbaseCounter = new HBaseCounter();
		memCounter = new MemCounter();	
		recoveryLog = RecoveryLog.instance();
		strategy = StorageStrategy.instance();
		
		recoveryLogToMem(recoveryLog, memCounter);
	}
	
	@Override
	public int get(String key) throws NotFoundException,
			UnavailableException, TimedOutException, TException {
		if (strategy.isInMemory(key)){
			return memCounter.get(key);
		}else {
			return hbaseCounter.get(key);
		}
	}

	@Override
	public Map<String, Integer> multiGet(List<String> keys)
			throws NotFoundException, UnavailableException, TimedOutException,
			TException {
		Map<String, Integer> retMap = new HashMap<String,Integer>();
		for (String item : keys) {
			if (strategy.isInMemory(item)) {
				retMap.put(item, memCounter.get(item));
			} else {
				retMap.put(item, hbaseCounter.get(item));
			}
		}
		return retMap;
	}
	
	@Override
	public int add(String key, int value) throws UnavailableException,
			TimedOutException, TException {
		//write to log
		recoveryLog.append(RecoveryLog.OP_ADD, key, value, System.currentTimeMillis());

		if (strategy.isInMemory(key)){
			return memCounter.add(key,value);
		}else {
			return hbaseCounter.add(key,value);
		}
	}

	@Override
	public Map<String,Integer> adds(Map<String, Integer> keys)
			throws UnavailableException, TimedOutException, TException {
		Map<String,Integer> retMap = new HashMap<String,Integer>(keys.size());
		recoveryLog.append(RecoveryLog.OP_ADD, keys, System.currentTimeMillis());

		for (String item : keys.keySet()) {
			if (strategy.isInMemory(item)) {
				retMap.put(item, memCounter.add(item, keys.get(item)));
			} else {
				retMap.put(item, hbaseCounter.add(item, keys.get(item)));
			}
		}
		return retMap;
	}

	@Override
	public int getLong(long key) throws NotFoundException,
			UnavailableException, TimedOutException, TException {
		if (strategy.isInMemory(key)){
			return memCounter.getLong(key);
		}else {
			return hbaseCounter.getLong(key);
		}
	}

	@Override
	public Map<Long, Integer> multiGetLong(List<Long> keys)
			throws NotFoundException, UnavailableException, TimedOutException,
			TException {
		Map<Long, Integer> retMap = new HashMap<Long,Integer>();
		for (Long item : keys) {
			if (strategy.isInMemory(item)) {
				retMap.put(item, memCounter.getLong(item));
			} else {
				retMap.put(item, hbaseCounter.getLong(item));
			}
		}
		return retMap;
	}

	@Override
	public int addLong(long key, int value) throws UnavailableException,
			TimedOutException, TException {
		//write to log
		recoveryLog.appendLong(RecoveryLog.OP_ADD, key, value, System.currentTimeMillis());

		if (strategy.isInMemory(key)){
			return memCounter.addLong(key,value);
		}else {
			return hbaseCounter.addLong(key,value);
		}
	}

	@Override
	public Map<Long, Integer> addsLong(Map<Long, Integer> keys)
			throws UnavailableException, TimedOutException, TException {
		recoveryLog.appendLong(RecoveryLog.OP_ADD, keys, System.currentTimeMillis());
		
		Map<Long,Integer> retMap = new HashMap<Long,Integer>(keys.size());
			
		for (Long item : keys.keySet()) {
			if (strategy.isInMemory(item)) {
				retMap.put(item, memCounter.addLong(item, keys.get(item)));
			} else {
				retMap.put(item, hbaseCounter.addLong(item, keys.get(item)));
			}
		}
		return retMap;
	}

}
