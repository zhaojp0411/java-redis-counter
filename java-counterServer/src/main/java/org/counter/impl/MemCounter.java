package org.counter.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.thrift.TException;
import org.cliffc.high_scale_lib.Counter;
import org.cliffc.high_scale_lib.NonBlockingHashMapLong;
import org.counter.Counter.Iface;
import org.counter.NotFoundException;
import org.counter.TimedOutException;
import org.counter.UnavailableException;
import org.counter.utils.Util;

public class MemCounter implements Iface {
	  
	private StorageStrategy strategy = StorageStrategy.instance();
	
	@Override
	public int get(String key) throws NotFoundException,
			UnavailableException, TimedOutException, TException {
		ConcurrentHashMap<String,AtomicInteger> countersMap = strategy.getShardingHashMap(key);
		AtomicInteger result = countersMap.get(key);
		return Util.safeResult(result);
	}
	@Override
	public Map<String, Integer> multiGet(List<String> keys)
			throws NotFoundException, UnavailableException, TimedOutException,
			TException {
		Map<String, Integer> resultMap = new HashMap<String,Integer>();
		for(String item : keys) {
			int ret = get(item);
			resultMap.put(item, ret);
		}

		return resultMap;
	}

	@Override
	public int add(String key, int value) throws UnavailableException,
			TimedOutException, TException {
		ConcurrentHashMap<String,AtomicInteger> countersMap = strategy.getShardingHashMap(key);
		countersMap.putIfAbsent(key,  new AtomicInteger(Util.DEFAULT_VALUE));
		return countersMap.get(key).addAndGet(value);
	}
	
	@Override
	public Map<String, Integer> adds(Map<String, Integer> keys)
			throws UnavailableException, TimedOutException, TException {
		Map<String, Integer> retMap = new HashMap<String,Integer>();
		for (String item : keys.keySet()) {
			retMap.put(item, add(item,keys.get(item)));
		}
		return retMap;
	}

//	@Override
//	public int getLong(long key) throws NotFoundException,
//			UnavailableException, TimedOutException, TException {
//		NonBlockingHashMapLong<AtomicInteger> countersMapLong = strategy.getShardingHashMap(key);
//		AtomicInteger result = countersMapLong.get(key);
//		return Util.safeResult(result);
//	}
//
//	@Override
//	public Map<Long, Integer> multiGetLong(List<Long> keys)
//			throws NotFoundException, UnavailableException, TimedOutException,
//			TException {
//		Map<Long, Integer> resultMap = new HashMap<Long,Integer>();
//		for(Long item : keys) {
//			int ret = getLong(item);
//			resultMap.put(item, ret);
//		}
//
//		return resultMap;
//	}
//
//	@Override
//	public int addLong(long key, int value) throws UnavailableException,
//			TimedOutException, TException {
//		NonBlockingHashMapLong<AtomicInteger> countersMapLong = strategy.getShardingHashMap(key);
//		if (countersMapLong.containsKey(key)){
//			return countersMapLong.get(key).addAndGet(value);
//		}else {
//			countersMapLong.putIfAbsent(key,  new AtomicInteger(value));
//			return value;
//		}	
//	}
//
//	@Override
//	public Map<Long, Integer> addsLong(Map<Long, Integer> keys)
//			throws UnavailableException, TimedOutException, TException {
//		Map<Long, Integer> retMap = new HashMap<Long,Integer>();
//		for (Long item : keys.keySet()) {
//			retMap.put(item, addLong(item,keys.get(item)));
//		}
//		return retMap;
//	}	
	
	
	@Override
	public int getLong(long key) throws NotFoundException,
			UnavailableException, TimedOutException, TException {
		NonBlockingHashMapLong<Counter> countersMapLong = strategy.getShardingCounter(key);
		Counter result = countersMapLong.get(key);
		return Util.safeResult(result);
	}

	@Override
	public Map<Long, Integer> multiGetLong(List<Long> keys)
			throws NotFoundException, UnavailableException, TimedOutException,
			TException {
		Map<Long, Integer> resultMap = new HashMap<Long,Integer>();
		for(Long item : keys) {
			int ret = getLong(item);
			resultMap.put(item, ret);
		}
		return resultMap;
	}

	@Override
	public int addLong(long key, int value) throws UnavailableException,
			TimedOutException, TException {
		NonBlockingHashMapLong<Counter> countersMapLong = strategy.getShardingCounter(key);
		if (countersMapLong.containsKey(key)){
			countersMapLong.get(key).add(value);
			//return (int) countersMapLong.get(key).get();
			return value;
		}else {
			Counter count = new Counter();
			count.set((long)value);
			countersMapLong.putIfAbsent(key, count);
			return value;
		}	
	}

	@Override
	public Map<Long, Integer> addsLong(Map<Long, Integer> keys)
			throws UnavailableException, TimedOutException, TException {
		Map<Long, Integer> retMap = new HashMap<Long,Integer>();
		for (Long item : keys.keySet()) {
			retMap.put(item, addLong(item,keys.get(item)));
		}
		return retMap;
	}	
}
