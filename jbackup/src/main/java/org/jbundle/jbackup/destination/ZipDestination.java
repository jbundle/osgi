/*
 * DirScanner.java
 *
 * Created on January 29, 2000, 6:33 AM
 */
 
package org.jbundle.jbackup.destination;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jbundle.jbackup.JBackupConstants;
import org.jbundle.jbackup.source.SourceFile;
import org.jbundle.jbackup.util.Util;
import org.jbundle.util.apprunner.PropertyView;


/** 
 * Directory scanner.
 * This class is a utility for scanning through a directory tree.
 * @author  Administrator
 * @version 
 */
public class ZipDestination extends BaseDestination
	implements DestinationFile, JBackupConstants
{
	protected String m_strZipFilename = null;
	protected ZipOutputStream m_outZip = null;
	protected long m_lMaxZipFileSize = -1;
	protected long m_lCurrentLength = 0;
	protected int m_iFileNumber = 0;

	/*
	 * Constructor
	 */
	public ZipDestination()
	{
		super();
	}
	/*
	 * Constructor
	 */
	public ZipDestination(Properties properties)
	{
		this();
		this.init(properties);
	}
	/*
	 * Get everything ready.
	 */
	public void init(Properties properties)
	{
		super.init(properties);
		String strPathname = properties.getProperty(ZIPOUT_PATHNAME_PARAM);
		if (strPathname == null)
		{
			strPathname = System.getProperties().getProperty("java.io.tmpdir", "c:/Temp");
			properties.setProperty(ZIPOUT_PATHNAME_PARAM, strPathname);
		}

		String strFilename = properties.getProperty(ZIPOUT_FILENAME_PARAM);
		if (strFilename == null)
		{
			strFilename = "[automatic]";
			properties.setProperty(ZIPOUT_FILENAME_PARAM, strFilename);
		}
	}
	/*
	 * Set up everything to start processing
	 */
	public void initTransfer(Properties properties)
	{
		super.initTransfer(properties);
		String strPathname = properties.getProperty(ZIPOUT_PATHNAME_PARAM);
		if ((strPathname != null)
			&& (strPathname.length() > 0)
				&& (strPathname.lastIndexOf(System.getProperties().getProperty("file.separator")) == strPathname.length() - 1)
					&& (strPathname.lastIndexOf('/') == strPathname.length() - 1))
						strPathname += System.getProperties().getProperty("file.separator");

		m_strZipFilename = properties.getProperty(ZIPOUT_FILENAME_PARAM);
		if ((m_strZipFilename == null)
			|| (m_strZipFilename.length() == 0)
			|| (m_strZipFilename.equals("[automatic]")))
				m_strZipFilename = this.getBackupFilename();
		
		if (strPathname != null)
			m_strZipFilename = strPathname + m_strZipFilename;

		String strMaxSize = properties.getProperty(MAX_SIZE_PARAM);
		m_lMaxZipFileSize = 0;
		try	{
			if (strMaxSize != null)
				m_lMaxZipFileSize = Long.parseLong(strMaxSize);
		} catch (NumberFormatException ex)	{
			m_lMaxZipFileSize = 0;
		}
		m_lCurrentLength = 0;
		m_iFileNumber = 0;
		try	{
			FileOutputStream outStream = new FileOutputStream(m_strZipFilename);
			m_outZip = new ZipOutputStream(outStream);
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
			m_outZip.flush();
			m_outZip.close();
		} catch (FileNotFoundException ex)	{
			ex.printStackTrace();
		} catch (IOException ex)	{
			ex.printStackTrace();
		}
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
		long lLength = 0;
		try	{
			if (m_lMaxZipFileSize > 0)
//?				if (lLength != File.OL)
			{	// Check to make sure this file will fit
				if (m_lCurrentLength + lStreamLength > m_lMaxZipFileSize)
				{	// Writing this file would push me past the file length limit
					try	{
						m_outZip.flush();
						m_outZip.close();

						m_lCurrentLength = 0;
						m_iFileNumber++;
						int iPosDot = m_strZipFilename.lastIndexOf('.');
						if (iPosDot == -1)
							iPosDot = m_strZipFilename.length();
						String strZipFilename = m_strZipFilename.substring(0, iPosDot);
						strZipFilename += Integer.toString(m_iFileNumber);
						if (iPosDot != m_strZipFilename.length())
							strZipFilename += m_strZipFilename.substring(iPosDot);
						FileOutputStream outStream = new FileOutputStream(strZipFilename);
						m_outZip = new ZipOutputStream(outStream);
					} catch (FileNotFoundException ex)	{
						ex.printStackTrace();
					} catch (IOException ex)	{
						ex.printStackTrace();
					}
				}
			}
			ZipEntry zipEntry = new ZipEntry(strPath);
			if (DEBUG)
				System.out.println(strPath);
			m_outZip.putNextEntry(zipEntry);
			lLength = Util.copyStream(inStream, m_outZip);
			m_lCurrentLength += lLength;
		} catch (IOException ex)	{
			ex.printStackTrace();
		}
		return lLength;
	}
	/*
	 * Get the panel to change the properties for this object.
	 */
	public PropertyView getPropertyView(Properties properties)
	{
		return new ZipDestinationPropertyView(this, properties);
	}
	/*
	 * Add the files to the backup file
	 */
	public String getBackupFilename()
	{
		Date now = new Date();
		String strDate = Util.dateToString(now);
		for (int i = 0; i < strDate.length(); i++)
		{
			char ch = strDate.charAt(i);
			if (!Character.isLetterOrDigit(ch))
				strDate = strDate.substring(0, i) + '_' + strDate.substring(i + 1, strDate.length());
		}
		return "backup" + strDate + ".zip";
	}
}