/*
 * DebugDestination.java
 *
 * Created on April 10, 2000, 4:56 AM
 */
 
package org.jbundle.jbackup.destination;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;

import org.jbundle.jbackup.source.SourceFile;
import org.jbundle.jproperties.PropertyView;


/** 
 * This is a special destination that does nothing except print the name and path, and scream through the Stream.
 * @author  Administrator
 * @version 
 */
public class DebugDestination extends BaseDestination
		implements DestinationFile
{
	protected PrintStream streamOut = null;
	protected boolean getFileLength = true;

	/*
	 * Constructor
	 */
	public DebugDestination()
	{
		super();
	}
	/*
	 * Constructor
	 */
	public DebugDestination(Properties properties)
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
		String strPathname = properties.getProperty(LOG_FILENAME_PARAM);
		if (strPathname == null)
		{
			strPathname = "";
			properties.setProperty(LOG_FILENAME_PARAM, strPathname);
		}
		String strGetFileLength = properties.getProperty(CALC_FILE_LENGTH_PARAM);
		if (strGetFileLength == null)
		{
			strGetFileLength = TRUE;
			properties.setProperty(CALC_FILE_LENGTH_PARAM, strGetFileLength);
		}
	}
	/*
	 * Set up everything to start processing
	 */
	public void initTransfer(Properties properties)
	{
		super.initTransfer(properties);
		String strPathname = properties.getProperty(LOG_FILENAME_PARAM);
		if (strPathname != null)
			if (strPathname.length() > 0)
			{
				try {
					FileOutputStream fileOut = new FileOutputStream(strPathname);
					streamOut = new PrintStream(fileOut);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		if (streamOut == null)
			streamOut = System.out;
		
		String strSelected = properties.getProperty(CALC_FILE_LENGTH_PARAM);
		if (FALSE.equalsIgnoreCase(strSelected))
			getFileLength = false;
		else
			getFileLength = true;
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
		String strFilename = source.getFileName();
		long lTotalLength = 0;

		if (getFileLength)
		{
			byte rgBytes[] = new byte[8192];
			InputStream inStream = source.makeInStream();
			try	{
				while (true)
				{
					int iLength = rgBytes.length;
					iLength = inStream.read(rgBytes, 0, iLength);
					if (iLength <= 0)
						break;
					lTotalLength += iLength;
				}
			} catch (IOException ex)	{
				streamOut.println("Error on next file: " + ex.getMessage());
			}
		}
		streamOut.println("Filename: " + strFilename + " Path: " + strPath + " length: " + lTotalLength);
		return lTotalLength;
	}
	/*
	 * Get the panel to change the properties for this object.
	 */
	public PropertyView getPropertyView(Properties properties)
	{
		return new DebugDestinationPropertyView(this, properties);
	}
}