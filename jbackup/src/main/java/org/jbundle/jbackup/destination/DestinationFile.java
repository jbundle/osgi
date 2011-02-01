/*
 * FileDestination.java
 *
 * Created on April 10, 2000, 3:19 AM
 */
 
package org.jbundle.jbackup.destination;

import java.util.Properties;

import org.jbundle.jbackup.source.SourceFile;


/** 
 * A File Destination is the place where you are adding a bunch of files.
 * @author  Administrator
 * @version 
 */
public interface DestinationFile {

	/*
	 * Add this file to the destination.
	 * Note: Only supply the file or the stream, not both. Supply the object that is easier, given
	 * the source. This dual option is given to allow destinations that require File objects from
	 * (such as FTP or HTTP) Having to write the inStream to a physical file before processing it.
	 * @param file Source file.
	 * @param inStream Source stream.
	 * @param strpath Full path of the source file (including the filename).
	 * @param strFilename Filename.
	 * @param lStreamLength Length of the stream (-1 if unknown).
	 */
	public long addNextFile(SourceFile source);
	/*
	 * Get everything ready.
	 */
	 public void initTransfer(Properties properties);
	/*
	 * Cleanup.
	 */
	 public void finishTransfer(Properties properties);
}
