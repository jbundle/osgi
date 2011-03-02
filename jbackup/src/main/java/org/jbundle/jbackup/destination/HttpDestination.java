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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Properties;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.jbundle.jbackup.JBackupConstants;
import org.jbundle.jbackup.source.SourceFile;
import org.jbundle.util.apprunner.PropertyView;

/** 
 * Directory scanner.
 * @author  Administrator
 * @version 
 */
public class HttpDestination extends BaseDestination
	implements DestinationFile, JBackupConstants
{
	protected String m_strBaseURL = null;
	
	protected HttpClient client = null;

	/*
	 * Constructor
	 */
	public HttpDestination()
	{
		super();
	}
	/**
	 * Creates new DirScanner.
	 * @param lMaxZipFileSize If zip file is going to be larger than this, split it up into smaller files.
	 */
	public HttpDestination(Properties properties)
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
		if (client == null)
			client = new HttpClient();
		String strBaseURL = properties.getProperty(BASE_URL_PARAM);
		if (strBaseURL == null)
		{
			strBaseURL = "http://localhost/uploads/";
			properties.setProperty(BASE_URL_PARAM, strBaseURL);
		}
	}
	/*
	 * Set up everything to start processing
	 */
	public void initTransfer(Properties properties)
	{
		super.initTransfer(properties);
		String strBaseURL = properties.getProperty(BASE_URL_PARAM);
		if (strBaseURL.length() > 0)
			if (strBaseURL.lastIndexOf('/') != strBaseURL.length() - 1)
				strBaseURL += '/';
		m_strBaseURL = strBaseURL;
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
		
		HttpClient client = new HttpClient();

		File fileIn = source.makeInFile();

		String furl = m_strBaseURL + this.encodeURL(strPath);
		PutMethod put = new PutMethod(furl);

		try	{
			RequestEntity entity = new InputStreamRequestEntity(new FileInputStream(fileIn));
			put.setRequestEntity(entity);

			int response = client.executeMethod(put);
			
			if (response != 201)
				System.out.println("Error Response: " + response);
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (FileNotFoundException ex)	{
			ex.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return lStreamLength;
	}
	/*
	 * Convert the URL to the proper format.
	 */
	public String encodeURL(String strURL)
	{
		String furl = JBackupConstants.BLANK;
		try {
			int iStart = 0;
			for (int i = iStart; i <= strURL.length(); i++)
			{
				if ((i == strURL.length())
					|| (strURL.charAt(i) == '/'))
				{
					if (iStart != i)
						furl += URLEncoder.encode(strURL.substring(iStart, i), ENCODING);
					if (i != strURL.length())
						furl += '/';
					iStart = i + 1;
				}
			}
		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
		}
		return furl;
	}
	public static final String ENCODING = "UTF-8";
	/*
	 * Get the panel to change the properties for this object.
	 */
	public PropertyView getPropertyView(Properties properties)
	{
		return new HttpDestinationPropertyView(this, properties);
	}
}
