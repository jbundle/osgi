/*
 * DirScanner.java
 *
 * Created on January 29, 2000, 6:33 AM
 */
 
package org.jbundle.jbackup.destination;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.jbundle.jbackup.JBackupConstants;
import org.jbundle.jbackup.source.SourceFile;
import org.jbundle.util.apprunner.PropertyView;

/** 
 * Directory scanner.
 * //pend(don) NOT FINISHED OR TESTED.
 * @author  Administrator
 * @version 
 */
public class FtpDestination extends BaseDestination
	implements DestinationFile, JBackupConstants
{
	protected FTPClient m_client = null;
	
	protected String m_strRootFTPDirectory = null;
	protected String m_strLastPath = null;		// Current directory

	/*
	 * Constructor
	 */
	public FtpDestination()
	{
		super();
	}
	/**
	 * Creates new DirScanner.
	 * @param lMaxZipFileSize If zip file is going to be larger than this, split it up into smaller files.
	 */
	public FtpDestination(Properties properties)
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
		String strHost = properties.getProperty(FTP_HOST);
		if (strHost == null)
			properties.setProperty(FTP_HOST, "localhost");
		String strUsername = properties.getProperty(USER_NAME);
		if (strUsername == null)
			properties.setProperty(USER_NAME, "anonymous");
		String strPassword = properties.getProperty(PASSWORD);
		if (strPassword == null)
			properties.setProperty(PASSWORD, "name@mailhost.com");
		String m_strRootFTPDirectory = properties.getProperty(ROOT_DIR);
		if (m_strRootFTPDirectory == null)
			properties.setProperty(ROOT_DIR, DEFAULT_ROOT_DIR);
	}
	/*
	 * Set up everything to start processing
	 */
	public void initTransfer(Properties properties)
	{
		super.initTransfer(properties);
		FTPClient client = new FTPClient();
		if (DEBUG)
			System.out.println("Connecting");
		String strHost = properties.getProperty(FTP_HOST, "hd2-12.irv.zyan.com");
		String strPort = properties.getProperty(FTP_PORT, "21");
		int port = Integer.parseInt(strPort);
		String strUsername = properties.getProperty(USER_NAME, "anonymous");
		String strPassword = properties.getProperty(PASSWORD, "doncorley@zyan.com");
		m_strRootFTPDirectory = properties.getProperty(ROOT_DIR, DEFAULT_ROOT_DIR);
		if (m_strRootFTPDirectory.endsWith("/"))
			m_strRootFTPDirectory = m_strRootFTPDirectory.substring(0, m_strRootFTPDirectory.length() - 1);
		if (m_strRootFTPDirectory.startsWith("/"))
			m_strRootFTPDirectory = m_strRootFTPDirectory.substring(1);
	 	properties.setProperty(FTP_HOST, strHost);
	 	properties.setProperty(USER_NAME, strUsername);
	 	properties.setProperty(PASSWORD, strPassword);
	 	properties.setProperty(ROOT_DIR, m_strRootFTPDirectory);
	    try {
		 	client.connect(strHost, port);
			if (DEBUG)
				System.out.println("Connected to " + strHost + ".");
	
		      // After connection attempt, you should check the reply code to verify
		      // success.
		    int reply = client.getReplyCode();
			if(FTPReply.isPositiveCompletion(reply))
			{
					client.login(strUsername, strPassword);
					client.enterLocalPassiveMode();
					m_client = client;	// Flag success
			}
			else
			{ // Error
				if (DEBUG)
					System.out.println("FTP connect Error: " + reply);
				client.disconnect();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/*
	 * Cleanup.
	 */
	 public void finishTransfer(Properties properties)
	{
		if (DEBUG)
			System.out.println("Disconnecting");
		try {
			if (m_client != null)
				m_client.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		m_client = null;
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
		long lStreamLength = source.getStreamLength();

        if (m_client != null)
		{ // Success
			try {
				int reply = this.changeDirectory(strPath);
				if(!FTPReply.isPositiveCompletion(reply))
				{
					System.out.println("Error on change dir: " + reply);
				}
				else
				{
					File fileIn = source.makeInFile(false);
					FileInputStream inStream = new FileInputStream(fileIn);
					if (DEBUG)
						System.out.println("Sending File: " + strFilename);
					if (m_client.storeFile(strFilename, inStream))   // Upload this file
					{	// Success
						
					}
					else
					{	// Error
						
					}
					inStream.close();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return lStreamLength;
	}
	/*
	 * Change to this directory.
	 */
	public int changeDirectory(String strPath) throws IOException
	{
		int iLastSlash = strPath.lastIndexOf('/');
		if (iLastSlash != -1)
			strPath = strPath.substring(0, iLastSlash);
		else
			strPath = "";
		String strFTPPath = "";
		if (m_strRootFTPDirectory.length() > 0)
		{
			if (!strFTPPath.startsWith(File.separator))
			strFTPPath = File.separator + m_strRootFTPDirectory;
			if (strPath.length() > 0)
				if (!strFTPPath.endsWith(File.separator))
					strFTPPath += File.separator;
		}
		strFTPPath += strPath;
		if (strFTPPath.equals(m_strLastPath))
			return FTPReply.COMMAND_OK;		// Already in the current directory.
		if (DEBUG)
			System.out.println("Change working directory to: " + strFTPPath);
		int iError = FTPReply.COMMAND_OK;
		if (!m_client.changeWorkingDirectory(strFTPPath)) 
		{
			if (!m_client.makeDirectory(strFTPPath)) 
				iError = FTPReply.FILE_ACTION_NOT_TAKEN;
			else if (!m_client.changeWorkingDirectory(strFTPPath))
				iError = FTPReply.FILE_ACTION_NOT_TAKEN;
		}
		m_strLastPath = strFTPPath;

		return iError;
	}
	/*
	 * Get the panel to change the properties for this object.
	 */
	public PropertyView getPropertyView(Properties properties)
	{
		return new FtpDestinationPropertyView(this, properties);
	}
}