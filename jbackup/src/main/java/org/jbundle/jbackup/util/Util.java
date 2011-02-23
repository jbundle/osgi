/*
 * Util.java
 *
 * Created on April 10, 2000, 1:38 AM
 */
 
package org.jbundle.jbackup.util;

import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Properties;

import org.jbundle.jbackup.JBackupConstants;
import org.jbundle.jbackup.filter.PathFilter;


/** 
 * Utilities.
 * @author  Don Corley don@tourgeek.com
 * @version 1.0.0
 */
public class Util extends Object {
	public static final boolean DEBUG = false;

	/**
	  * Creates new Util
	  */
	public Util()
	{
	}
	/*
	 * Copy the input stream to the output stream.
	 * @param inStream Input stream.
	 * @param outStream Output stream.
	 * @return Error code, or 0 if no errors.
	 */
	public static int copyStream(InputStream inStream, OutputStream outStream)
		throws IOException
	{
		return Util.copyStream(inStream, outStream, false, false);
	}
	/*
	 * Copy the input stream to the output stream.
	 * @param inStream Input stream.
	 * @param outStream Output stream.
	 * @return Error code, or 0 if no errors.
	 */
	public static int copyStream(InputStream inStream, OutputStream outStream, boolean bCountInBytes, boolean bCountOutBytes)
		throws IOException
	{
		byte rgBytes[] = new byte[8192];
		int iTotalLength = 0;
		int iStartInByte = 0;
		int iStartOutByte = 0;
		while (true)
		{
			int iLength = rgBytes.length;
			if (bCountInBytes)
				iStartInByte = iTotalLength;
			iLength = inStream.read(rgBytes, iStartInByte, iLength);
			if (DEBUG)
				System.out.println("inLen = " + iLength);
			if (iLength <= 0)
				break;
			if (bCountOutBytes)
				iStartOutByte = iTotalLength;
			outStream.write(rgBytes, iStartOutByte, iLength);
			iTotalLength += iLength;
		}
		return iTotalLength;
	}
	/*
	 * Create a new object using this class name.
	 * Conform to the standardized classname format: com.tourapp.terminal.src/dest.name.namesrc/dest
	 */
	public static Object makeObjectFromClassName(String interfaceName, String strPackage, String strClassName)
	{
		if (strClassName == null)
			return null;
		if (strClassName.indexOf('.') == -1)
			if (strPackage != null)
		{	// Use default structure
			strClassName = org.jbundle.jbackup.JBackupConstants.ROOT_PACKAGE + "jbackup." + strPackage.toLowerCase() + '.' + strClassName + strPackage.substring(0, 1).toUpperCase() + strPackage.substring(1);
		}
		Object objClass = null;
		try
		{
			if (strClassName.indexOf('.') == 0)
				strClassName = org.jbundle.jbackup.JBackupConstants.ROOT_PACKAGE + strClassName.substring(1);
			Class<?> c = Class.forName(strClassName);
			if (c != null)
			{
				objClass = c.newInstance();
			}
		}
		catch (Exception ex) {
			System.out.println("Error on attempt to make class: " + strClassName);
			ex.printStackTrace();
			System.exit(0);
		}
		return objClass;
	}	
	/*
	 * Convert this date string to a date object.
	 * @param strDate the date to parse.
	 * @return The date or null if unparseable.
	 */
	public static Date stringToDate(String strDate)
	{
		Date dateLastBackup = null;
		if ((strDate != null) && (strDate.length() > 0))
		{
			try	{
				dateLastBackup = DateFormat.getInstance().parse(strDate);
			} catch (ParseException ex)	{
				dateLastBackup = null;
			}
		}
		return dateLastBackup;
    }
	/*
	 * Convert this date object to a string.
	 * @param The date or null if unparseable.
	 * @return strDate the date to parse.
	 */
	public static String dateToString(Date date)
	{
		if (date == null)
			return "";
		try	{
			return DateFormat.getInstance().format(date);
		} catch (Exception ex) {
		}
		return "";
    }
	/*
	 * Create a filter from these properties.
	 * @param The properties.
	 * @return The filter.
	 */
	public static FilenameFilter makeFilter(Properties properties)
	{
		String strFilter = properties.getProperty(JBackupConstants.FILTER_PARAM);
		if (strFilter != null)
		if (strFilter.indexOf('.') != -1)
			return (FilenameFilter)Util.makeObjectFromClassName(Object.class.getName(), null, strFilter);
		else
			return new PathFilter(strFilter);
		return null;	// Add+++ Make the filename filter!
	}
}