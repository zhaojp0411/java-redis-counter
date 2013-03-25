package org.counter.recovery;

import java.io.IOException;

import org.counter.impl.MemCounter;

public interface RecoveryAble {
	public void recoveryLogToMem(RecoveryLog log,MemCounter memCounter) 
		throws IOException;
}
