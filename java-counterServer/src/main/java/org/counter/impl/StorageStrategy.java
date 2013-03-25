package org.counter.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.cliffc.high_scale_lib.Counter;
import org.cliffc.high_scale_lib.NonBlockingHashMapLong;


public class StorageStrategy {
	
	public static boolean inMemory = false;

	public static int HASH_SIZE = 10; 
	private List<ConcurrentHashMap<String,AtomicInteger>> countersMapList = 
		new ArrayList<ConcurrentHashMap<String,AtomicInteger>>(HASH_SIZE);
	
	private List<NonBlockingHashMapLong<AtomicInteger>> countersMapLongList = 
		new ArrayList<NonBlockingHashMapLong<AtomicInteger>>(HASH_SIZE);
	
	private List<NonBlockingHashMapLong<Counter>> countersMapLongCounterList = 
		new ArrayList<NonBlockingHashMapLong<Counter>>(HASH_SIZE);
	
	private static StorageStrategy stroageStrategy = new StorageStrategy();
	
	public static StorageStrategy instance(){
		return stroageStrategy;
	}
	
	private StorageStrategy(){
		for (int i=0; i < HASH_SIZE; i++) {
			countersMapList.add(i,new ConcurrentHashMap<String,AtomicInteger>());
			countersMapLongList.add(i,new NonBlockingHashMapLong<AtomicInteger>());
			countersMapLongCounterList.add(i,new NonBlockingHashMapLong<Counter>());
		}
	}
	
	/**
	 * 判断key在内存中还是HBase中
	 * @param key
	 * @return
	 */
	public boolean isInMemory(String key) {
		return inMemory;
	}
	
	public boolean isInMemory(long key) {
		return inMemory;
	}
	
	public ConcurrentHashMap<String,AtomicInteger> getShardingHashMap(String key) {
		return countersMapList.get(getHashIndex(key));
	}
	
	public NonBlockingHashMapLong<AtomicInteger> getShardingHashMap(long key) {
		return countersMapLongList.get(getHashIndex(key));
	}
	
	public NonBlockingHashMapLong<Counter> getShardingCounter(long key) {
		return countersMapLongCounterList.get(getHashIndex(key));
	}
	
	private int getHashIndex(String key) {
		return key.hashCode()%HASH_SIZE;
	}
	
	private int getHashIndex(long key) {
		return (int) (key%HASH_SIZE);
	}
}
