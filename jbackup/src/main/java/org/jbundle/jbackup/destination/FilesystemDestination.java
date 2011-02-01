/*
 * DirScanner.java
 *
 * Created on January 29, 2000, 6:33 AM
 */
 
package org.jbundle.jbackup.destination;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.jbundle.jbackup.source.SourceFile;
import org.jbundle.jbackup.util.Util;
import org.jbundle.jproperties.PropertyView;


/** 
 * Directory scanner.
 * This class is a utility for scanning through a directory tree.
 * @author  Administrator
 * @version 
 */
public class FilesystemDestination extends BaseDestination
	implements DestinationFile
{
	protected String m_strPathname = null;

	/*
	 * Constructor
	 */
	public FilesystemDestination()
	{
		super();
	}
	/**
	 * Creates new DirScanner.
	 * @param lMaxZipFileSize If zip file is going to be larger than this, split it up into smaller files.
	 */
	public FilesystemDestination(Properties properties)
	{
		this();
		this.init(properties);
	}
	/*
	 * Set up everything to start processing
	 */
	public void init(Properties properties)
	{
		super.init(properties);
		String strPathname = properties.getProperty(DEST_ROOT_PATHNAME_PARAM);
		if (strPathname == null)
		{
			strPathname = System.getProperties().getProperty("java.io.tmpdir", "c:/Temp");
			if (strPathname != null)
				if (!strPathname.endsWith(File.separator))
					strPathname += File.separator;
			properties.setProperty(DEST_ROOT_PATHNAME_PARAM, strPathname);
		}
	}
	/*
	 * Set up everything to start processing
	 */
	public void initTransfer(Properties properties)
	{
		super.initTransfer(properties);
		m_strPathname = properties.getProperty(DEST_ROOT_PATHNAME_PARAM);
		if ((m_strPathname == null)
			|| (m_strPathname.length() == 0))
				m_strPathname = "";		// No prefix
		else if ((m_strPathname.lastIndexOf(System.getProperties().getProperty("file.separator")) != m_strPathname.length() - 1)
			&& (m_strPathname.lastIndexOf('/') != m_strPathname.length() - 1))
				m_strPathname += System.getProperties().getProperty("file.separator");	// Must end in a path separator
	}
	/*
	 * Close everything down after processing.
	 */
	public void finishTransfer(Properties properties)
	{
		super.finishTransfer(properties);
	}
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
	public long addNextFile(SourceFile source)
	{
		String strPath = source.getFilePath();
		long lStreamLength = source.getStreamLength();

        InputStream inStream = source.makeInStream();
		try	{
			strPath = m_strPathname + strPath;
			File file = new File(strPath);
			file.mkdirs();
			if (file.exists())
				file.delete();
			file.createNewFile();
			OutputStream outStream = new FileOutputStream(file);
			lStreamLength = Util.copyStream(inStream, outStream);
			outStream.close();
		} catch (IOException ex)	{
			ex.printStackTrace();
		}
		return lStreamLength;
	}
	/*
	 * Get the panel to change the properties for this object.
	 */
	public PropertyView getPropertyView(Properties properties)
	{
		return new FilesystemDestinationPropertyView(this, properties);
	}
}