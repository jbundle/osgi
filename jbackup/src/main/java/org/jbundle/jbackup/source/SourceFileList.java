/*
 * FileSource.java
 *
 * Created on April 10, 2000, 3:21 AM
 */
 
package org.jbundle.jbackup.source;

import java.util.Iterator;
import java.util.Properties;

/** 
 * This interface is used to read through the source files.
 * @author  Administrator
 * @version 
 */
public interface SourceFileList
		extends Iterator<SourceFile>
{
	/*
	 * Get everything ready.
	 */
	 public void initTransfer(Properties properties);
	/*
	 * Cleanup.
	 */
	 public void finishTransfer(Properties properties);
}
