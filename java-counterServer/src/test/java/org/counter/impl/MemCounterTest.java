package org.counter.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.thrift.TException;
import org.counter.NotFoundException;
import org.counter.TimedOutException;
import org.counter.UnavailableException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MemCounterTest {

	private MemCounter counter;
	
	List<String> keys = new ArrayList<String>(10);

	Map<String,Integer> pairs = new HashMap<String,Integer>(10);
	
	Map<String,Integer> pairs2 = new HashMap<String,Integer>(10);

	@Before
	public void setUp() throws Exception {
		counter = new MemCounter();
		for(int i=0;i<10;i++){
			keys.add((("item"+i)));
		}
		
		for(int i=0;i<10;i++){
			pairs.put((("item"+i)),1);
		}
		
		for(int i=0;i<10;i++){
			pairs2.put((("item"+i)),-1);
		}
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGet() throws NotFoundException, UnavailableException, TimedOutException, TException {
		int result = counter.get(("item"));
		Assert.assertEquals(0, result);
	}

	@Test
	public void testMultiGet() throws NotFoundException, UnavailableException, TimedOutException, TException {
		
		Map<String,Integer> mapResult = counter.multiGet(keys);
		for(String item : mapResult.keySet()){
			Assert.assertEquals(0, mapResult.get(item).intValue());
		}
	}

	@Test
	public void testAdd() throws UnavailableException, TimedOutException, TException {
		int result = counter.get(("item"));
		counter.add(("item"),1);
		int result2 = counter.get(("item"));
		Assert.assertEquals(result+1, result2);
		
		counter.add(("item"),-1);
		int result3 = counter.get(("item"));
		Assert.assertEquals(result, result3);
	}

	@Test
	public void testAdds() throws NotFoundException, UnavailableException, TimedOutException, TException {
		Map<String,Integer> mapResult = counter.multiGet(keys);
		
		counter.adds(pairs);
		Map<String,Integer> mapResult2 = counter.multiGet(keys);
		for(String item : pairs.keySet()){
			//System.out.println(mapResult.get(item)+1 + " " + mapResult2.get(item).intValue());
			Assert.assertEquals(mapResult.get(item)+1, mapResult2.get(item).intValue());
		}	

		counter.adds(pairs2);
		Map<String,Integer> mapResult3 = counter.multiGet(keys);
		for(String item : pairs.keySet()){
			//System.out.println(mapResult.get(item)+1 + " " + mapResult2.get(item).intValue());
			Assert.assertEquals(mapResult.get(item).intValue(), mapResult3.get(item).intValue());
		}
	}

}
