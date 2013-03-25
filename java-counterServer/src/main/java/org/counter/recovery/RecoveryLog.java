package org.counter.recovery;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

import org.apache.thrift.TException;
import org.counter.utils.Log;

public class RecoveryLog {
		
	public static final String OP_ADD = "add";
	public static final String LOG_SEPARATOR = ",";
	public static final String LOG_ITEM_SPARATOR = ";";
	public static final String LOG_PAIR_SPARATOR = "_";
	
	public static final String LOG_PAIR_KEY_STRING = "string";
	public static final String LOG_PAIR_KEY_LONG = "long";

	private File logFile = null;
	private BufferedWriter bufferedWriter = null;

	private final String FILE_NAME = "./recovery.log";
	private static RecoveryLog recoveryLog = null;
	
	private RecoveryLog() throws IOException {
		init();
	}
	
	public static RecoveryLog instance() throws IOException{
		if (recoveryLog == null) {
			recoveryLog = new RecoveryLog();
		}
		return recoveryLog;
	}

	/**
	 * open log file for write, if file does not exist ,create new file
	 * @throws IOException 
	 */
	public void init() throws IOException {
		logFile = createLogFile(FILE_NAME);
		openHotLogFile();
	}
	
	private void openHotLogFile() throws IOException{
		try {			
			FileWriter fileWriter = new FileWriter(logFile, true);
			bufferedWriter = new BufferedWriter(fileWriter);
		} catch (IOException e) {
			Log.error("open file error : " + logFile.getAbsolutePath(),e);
			throw e;
		}
	}
	
	private File createLogFile(String filename) throws IOException{
		File logFile = new File(filename);
		if (!logFile.getParentFile().exists()) {
			logFile.getParentFile().mkdirs();
		}
		if (!logFile.exists()) {
			logFile.createNewFile();
			System.out.println("create new recovery log file " + filename);
		} else {
			Log.info("open log file " + filename);
		}
		return logFile;
	}


	public void append(String op,Map<String,Integer> map,long time) throws TException {
		try {			
			StringBuilder builder = new StringBuilder();
			for(String item : map.keySet()){
				builder.append(item).append(LOG_PAIR_SPARATOR);
				builder.append(map.get(item)).append(LOG_ITEM_SPARATOR);
			}
			String info = builder.substring(0, builder.length()-LOG_ITEM_SPARATOR.length());
			
			bufferedWriter.write(op + LOG_SEPARATOR +LOG_PAIR_KEY_STRING+LOG_SEPARATOR
					+ info + LOG_SEPARATOR  + time + "\n");
			bufferedWriter.flush();
		} catch (IOException e) {
			Log.error("append to recovery log file error : " + map, e);
			throw new TException(e);
		}
	}
	
	public void append(String op,String key,int value,long time) throws TException {
		StringBuilder builder = new StringBuilder();
		builder.append(key).append(LOG_PAIR_SPARATOR);
		builder.append(value);
		
		try {			
			bufferedWriter.write(op + LOG_SEPARATOR + LOG_PAIR_KEY_STRING + LOG_SEPARATOR
					+ builder + LOG_SEPARATOR  + time + "\n");			
			bufferedWriter.flush();
		} catch (IOException e) {
			Log.error("append to recovery log file error : " + builder, e);
			throw new TException(e);
		}
	}
	
	public void appendLong(String op,long key,int value,long time) throws TException {
		StringBuilder builder = new StringBuilder();
		builder.append(key).append(LOG_PAIR_SPARATOR);
		builder.append(value);
		
		try {			
			bufferedWriter.write(op + LOG_SEPARATOR + LOG_PAIR_KEY_LONG + LOG_SEPARATOR
					+ builder + LOG_SEPARATOR  + time + "\n");
			bufferedWriter.flush();
		} catch (IOException e) {
			Log.error("append to recovery log file error : " + builder, e);
			throw new TException(e);
		}
	}
	
	public void appendLong(String op,Map<Long,Integer> map,long time) throws TException {
		try {			
			StringBuilder builder = new StringBuilder();
			for(Long item : map.keySet()){
				builder.append(item).append(LOG_PAIR_SPARATOR);
				builder.append(map.get(item)).append(LOG_ITEM_SPARATOR);
			}
			String info = builder.substring(0, builder.length()-LOG_ITEM_SPARATOR.length());
			
			bufferedWriter.write(op + LOG_SEPARATOR +LOG_PAIR_KEY_LONG+LOG_SEPARATOR
					+ info + LOG_SEPARATOR  + time + "\n");
			bufferedWriter.flush();
		} catch (IOException e) {
			Log.error("append to recovery log file error : " + map, e);
			throw new TException(e);
		}
	}
	
	public File getRecoveryFile() {
		return logFile;
	}
}
