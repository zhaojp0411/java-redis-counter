package org.counter.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.client.Result;
import org.apache.thrift.TException;
import org.counter.Counter.Iface;
import org.counter.NotFoundException;
import org.counter.TimedOutException;
import org.counter.UnavailableException;
import org.counter.utils.Log;
import org.counter.utils.Util;

public class HBaseCounter implements Iface {

	public static final String defaultStrTableName = "counter";

	public static final byte[] defaultFamilyName = new byte[] { 'f' };
	public static final String defaultStrFamilyName = "f";

	public static final byte[] defaultColumnName = new byte[] { 'c' };
	public static final String defaultStrColumnName = "c";
	
	public static final byte[] defaultQualifierName = new byte[] {'q'};

	HTable counterTable;

	public HBaseCounter() throws IOException {
		Configuration config = HBaseConfiguration.create();
		counterTable = new HTable(config, defaultStrTableName);
	}

	@Override
	public int get(String key) throws NotFoundException,
			UnavailableException, TimedOutException, TException {
		return getFromHBase(key.getBytes());
	}
	
	private int getFromHBase(byte[] key) throws NotFoundException,
	UnavailableException, TimedOutException{
		Get getOpt = new Get(key);
		getOpt.addColumn(defaultFamilyName, defaultQualifierName);
		try {
			Result ret = counterTable.get(getOpt);
			byte[] bRet = ret.getValue(defaultFamilyName, defaultQualifierName);
			return Util.safeResult(bRet);
		} catch (IOException e) {
			Log.error("get from hbase error");
			throw new UnavailableException();
		}
	}

	@Override
	public Map<String, Integer> multiGet(List<String> keys)
			throws NotFoundException, UnavailableException, TimedOutException,
			TException {
		Map<String, Integer> resultMap = new HashMap<String,Integer>();
		for(String item : keys) {
			int ret = getFromHBase(item.getBytes());
			resultMap.put(item, ret);
		}
		return resultMap;
	}

	@Override
	public int add(String key, int value) throws UnavailableException,
			TimedOutException, TException {
		return addToHBase(key.getBytes(),value);
	}
	
	private int addToHBase(byte[] key,int value) throws UnavailableException,
	TimedOutException {
		Increment incrOpt = new Increment(key);
		incrOpt.addColumn(defaultFamilyName, defaultQualifierName, value);
		try {
			Result result = counterTable.increment(incrOpt);
			byte[] bRet = result.getValue(defaultFamilyName, defaultQualifierName);
			return Util.safeResult(bRet);
		} catch (IOException e) {
			Log.error("incrment to hbase eror");
			throw new UnavailableException();
		}
	}

	@Override
	public Map<String, Integer> adds(Map<String, Integer> keys)
			throws UnavailableException, TimedOutException, TException {
		Map<String, Integer> retMap = new HashMap<String,Integer>();
		for(String item : keys.keySet()) {
			retMap.put(item,addToHBase(item.getBytes(),keys.get(item)));
		}
		return retMap;
	}

	@Override
	public int getLong(long key) throws NotFoundException,
			UnavailableException, TimedOutException, TException {
		return getFromHBase(Util.long2bytearray(key));
	}

	@Override
	public Map<Long, Integer> multiGetLong(List<Long> keys)
			throws NotFoundException, UnavailableException, TimedOutException,
			TException {
		Map<Long, Integer> resultMap = new HashMap<Long,Integer>();
		for(Long item : keys) {
			int ret = getFromHBase(Util.long2bytearray(item));
			resultMap.put(item, ret);
		}
		return resultMap;
	}

	@Override
	public int addLong(long key, int value) throws UnavailableException,
			TimedOutException, TException {
		return addToHBase(Util.long2bytearray(key),value);
	}

	@Override
	public Map<Long, Integer> addsLong(Map<Long, Integer> keys)
			throws UnavailableException, TimedOutException, TException {
		Map<Long, Integer> retMap = new HashMap<Long,Integer>();
		for(Long item : keys.keySet()) {
			retMap.put(item,addToHBase(Util.long2bytearray(item),keys.get(item)));
		}
		return retMap;
	}

}
