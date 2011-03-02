/*
 * DirScanner.java
 *
 * Created on January 29, 2000, 6:33 AM
 */
 
package org.jbundle.jbackup.source;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jbundle.jbackup.JBackupConstants;
import org.jbundle.util.apprunner.PropertyView;


/** 
 * Directory scanner.
 * This class is a utility for scanning through a directory tree.
 * @author  Administrator
 * @version 
 */
public class ZipSource extends BaseSource
	implements SourceFileList, JBackupConstants
{
	protected String m_strZipFilename = null;
	protected ZipInputStream m_inZip = null;

	/*
	 * Constructor
	 */
	public ZipSource()
	{
		super();
	}
	/*
	 * Constructor
	 */
	public ZipSource(Properties properties)
	{
		this();
		this.init(properties);
	}
	/*
	 * Get ready to start processing.
	 */
	public void init(Properties properties)
	{
		super.init(properties);
		String strPathname = properties.getProperty(ZIPIN_FILENAME_PARAM);
		if ((strPathname == null) || (strPathname.length() == 0))
		{
			strPathname = "in.zip";
			properties.setProperty(ZIPIN_FILENAME_PARAM, strPathname);
		}
	}
	/*
	 * Set up everything to start processing
	 */
	public void initTransfer(Properties properties)
	{
		super.initTransfer(properties);
		m_strZipFilename = properties.getProperty(ZIPIN_FILENAME_PARAM);
		try	{
			FileInputStream inStream = new FileInputStream(m_strZipFilename);
			m_inZip = new ZipInputStream(inStream);
		} catch (IOException ex)	{
			ex.printStackTrace();
		}
	}
	/*
	 * Close everything down after processing.
	 */
	public void finishTransfer(Properties properties)
	{
		try	{
			m_inZip.close();
		} catch (FileNotFoundException ex)	{
			ex.printStackTrace();
		} catch (IOException ex)	{
			ex.printStackTrace();
		}
		super.finishTransfer(properties);
	}
	/** Returns the next element in the interation.
	 * (Returns a SourceFileObject).
	 *
	 * @returns the next element in the interation.
	 * @exception NoSuchElementException iteration has no more elements.
	 */
	public SourceFile next()
	{
		if (this.isPend())
			return this.getPend();
		try	{
			ZipEntry entry = m_inZip.getNextEntry();
			if (entry == null)
				return null;		// EOF
			String strPath = entry.getName();
			String strFilename = strPath;
			if (strPath.lastIndexOf(gchSeparator) != -1)
				if (strPath.lastIndexOf(gchSeparator) + 1 < strPath.length())
					strFilename = strPath.substring(strPath.lastIndexOf(gchSeparator) + 1);
			long lStreamLength = entry.getSize();
			if (DEBUG)
				System.out.println("Name: " + entry.getName() + " size: " + entry.getSize());
			return new StreamSourceFile(null, m_inZip, strPath, strFilename, lStreamLength);		// Return the file
		} catch (IOException ex)	{
			ex.printStackTrace();
		}
		return null;	// pend(don) Don't do this!
	}
	/*
	 * Get the panel to change the properties for this object.
	 */
	public PropertyView getPropertyView(Properties properties)
	{
		return new ZipSourcePropertyView(this, properties);
	}
}