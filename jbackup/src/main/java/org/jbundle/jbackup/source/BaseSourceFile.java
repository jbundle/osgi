/*
 * DirScanner.java
 *
 * Created on January 29, 2000, 6:33 AM
 */
 
package org.jbundle.jbackup.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jbundle.jbackup.JBackupConstants;
import org.jbundle.jbackup.util.Util;


/** 
 * Directory scanner.
 * This class is a utility for scanning through a directory tree.
 * @author  Administrator
 * @version 
 */
public class BaseSourceFile extends Object
    implements SourceFile {

	public File m_inputFile = null;
	public InputStream m_InputStream = null;
	public String m_FilePath = null;
	public String m_FileName = null;
	public long m_lStreamLength = -1;
	
	public BaseSourceFile()
	{
		super();
	}
	public void init(File inputFile, InputStream inputStream, String filePath, String fileName, long lStreamLength)
	{
		m_inputFile = inputFile;
		m_InputStream = inputStream;
		m_FilePath = filePath;
		m_FileName = fileName;
		m_lStreamLength = lStreamLength;
	}

	public InputStream getInputStream()
	{
		return m_InputStream;
	}
	public String getFilePath()
	{
		return m_FilePath;
	}
	public String getFileName()
	{
		return m_FileName;
	}
	/*
	 * Get the stream length.
	 * Return the approx length of this stream, or -1 if unknown.
	 */
	public long getStreamLength() {
		return m_lStreamLength;
	}
	public File getFile() {
		return m_inputFile;
	}
    /**
     * Close the source file.
     */
    public void close()
    {
        // You will have to override this
    }
    /**
     * If there is no input stream, use the file to create one.
     */
	public InputStream makeInStream()
	{
		if (m_InputStream != null)
			return m_InputStream;
		try	{
			return new FileInputStream(m_inputFile);
		} catch (FileNotFoundException ex)	{
			System.out.println("Warning: scanned file does not exist: " + m_inputFile.getPath());		// Skip this file
		}
		return null;
	}
	public File makeInFile()
	{
		return this.makeInFile(true);
	}
	public File makeInFile(boolean bAllowFilenameChange)
	{
        File fileIn = m_inputFile;
        InputStream inStream = m_InputStream;
        String strFilename = m_FileName;
		if (fileIn != null)
			return fileIn;
		int iPosDot = strFilename.lastIndexOf('.');
		String strFileExtension = JBackupConstants.BLANK;
		if ((iPosDot != -1)
			&& (iPosDot < strFilename.length() - 1))
		{
			strFileExtension = strFilename.substring(iPosDot);
			strFilename = strFilename.substring(0, iPosDot);
		}
		try	{
			if (bAllowFilenameChange)
				fileIn = File.createTempFile(strFilename, strFileExtension);
			else
			{
				String strTempPath = System.getProperty("java.io.tmpdir") + '/';
				int iLastSlash = strFilename.lastIndexOf('/');
				if (iLastSlash != -1)
					strFilename = strFilename.substring(iLastSlash);
				strFilename = strTempPath + strFilename + strFileExtension;
				fileIn = new File(strFilename);
				if (fileIn.exists())
					fileIn.delete();
			}
			OutputStream outStream = new FileOutputStream(fileIn);
			Util.copyStream(inStream, outStream);
			outStream.flush();
			outStream.close();
			fileIn.deleteOnExit();
		} catch (IOException ex)	{
			ex.printStackTrace();
		}
		return fileIn;
	}
}