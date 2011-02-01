/*
 * DirScanner.java
 *
 * Created on January 29, 2000, 6:33 AM
 */
 
package org.jbundle.jbackup.source;

import java.io.File;
import java.io.InputStream;

/** 
 * Directory scanner.
 * This class is a utility for scanning through a directory tree.
 * @author  Administrator
 * @version 
 */
public class ZipSourceFile extends BaseSourceFile
    implements SourceFile
{
	
	public ZipSourceFile()
	{
        super();
    }
	public ZipSourceFile(InputStream inputStream, String filePath, String fileName)
	{
        this();
		this.init(null, inputStream, filePath, fileName, -1);
	}
	public ZipSourceFile(File inputFile, InputStream inputStream, String filePath, String fileName, long lStreamLength)
    {
        this();
		this.init(inputFile, inputStream, filePath, fileName, lStreamLength);
	}
	public void init(File inputFile, InputStream inputStream, String filePath, String fileName, long lStreamLength)
	{
        super.init(inputFile, inputStream, filePath, fileName, lStreamLength);
	}
    /**
     * Close the source file.
     */
    public void close()
    {
        // Do not close the zip file as it will close the zip archive.
    }
}