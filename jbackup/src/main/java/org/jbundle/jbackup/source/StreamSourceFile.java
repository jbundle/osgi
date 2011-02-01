/*
 * DirScanner.java
 *
 * Created on January 29, 2000, 6:33 AM
 */
 
package org.jbundle.jbackup.source;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/** 
 * Directory scanner.
 * This class is a utility for scanning through a directory tree.
 * @author  Administrator
 * @version 
 */
public class StreamSourceFile extends BaseSourceFile
	implements SourceFile {
        
	public StreamSourceFile()
	{
        super();
    }
	public StreamSourceFile(File inputFile, String filePath, String fileName)
	{
        this();
		this.init(inputFile, null, filePath, fileName, -1);
	}
	public StreamSourceFile(InputStream inputStream, String filePath, String fileName)
	{
        this();
		this.init(null, inputStream, filePath, fileName, -1);
	}
	public StreamSourceFile(File inputFile, InputStream inputStream, String filePath, String fileName, long lStreamLength)
	{
        this();
		this.init(inputFile, inputStream, filePath, fileName, lStreamLength);
	}
	public void init(File inputFile, InputStream inputStream, String filePath, String fileName, long lStreamLength)
	{
        super.init(inputFile, inputStream, filePath, fileName, lStreamLength);
	}
    /**
     * If there is no input stream, use the file to create one.
     */
	public InputStream makeInStream()
	{
        return m_InputStream = super.makeInStream();
	}
    /**
     * Close the source file.
     */
    public void close()
    {
        if (m_InputStream != null)
        {
            try {
                m_InputStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}