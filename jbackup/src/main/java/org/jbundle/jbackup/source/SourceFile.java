/*
 * FileSource.java
 *
 * Created on April 10, 2000, 3:21 AM
 */
 
package org.jbundle.jbackup.source;

import java.io.File;
import java.io.InputStream;

/** 
 *
 * @author  Administrator
 * @version 
 */
public interface SourceFile
{
	public File getFile();
	public InputStream getInputStream();
	public String getFilePath();
	public String getFileName();
	/*
	 * Get the stream length.
	 * Return the approx length of this stream, or -1 if unknown.
	 */
	public long getStreamLength();
    /**
     * Close the source file.
     */
    public void close();
	public InputStream makeInStream();
	public File makeInFile();
	public File makeInFile(boolean bAllowFilenameChange);
}
