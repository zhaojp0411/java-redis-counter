package org.counter.utils;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import org.cliffc.high_scale_lib.Counter;

public class Util {
	public static final int DEFAULT_VALUE = 0;
	
	public static String getRowKey(ByteBuffer key) {
		return new String(key.array());
	}

	public static int safeResult(AtomicInteger result) {
		return result==null?DEFAULT_VALUE:result.get();
	}
	
	public static int safeResult(Counter result) {
		return (int) (result==null?DEFAULT_VALUE:result.get());
	}
	
	/*
	 * 此处采用 bytearray2long ： 
	 */
	public static int safeResult(byte[] result) {
		return (int) ((result==null)?DEFAULT_VALUE:bytearray2long(result));
	}

	private static long byte2Long(byte[] b) {
		int mask = 0xff;
		int temp = 0;
		int n = 0;
		for (int i = 0; i < 8 && i < b.length; i++) {
			n <<= 8;
			temp = b[i] & mask;
			n |= temp;
		}
		return n;
	}

	public static byte[] long2bytearray(long l) {
		byte b[] = new byte[8];

		ByteBuffer buf = ByteBuffer.wrap(b);
		buf.putLong(l);
		return b;
	}

	public static byte[] int2bytearray(int i) {
		byte b[] = new byte[4];

		ByteBuffer buf = ByteBuffer.wrap(b);
		buf.putInt(i);
		return b;
	}

	public static long bytearray2long(byte[] b) {
		ByteBuffer buf = ByteBuffer.wrap(b);
		return buf.getLong();
	}

	public static int bytearray2int(byte[] b) {
		ByteBuffer buf = ByteBuffer.wrap(b);
		return buf.getInt();
	}

}
