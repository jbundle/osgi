/*
 * Util.java
 *
 * Created on April 10, 2000, 1:38 AM
 */
 
package org.jbundle.jproperties;

import java.io.*;
import java.util.*;

/** 
 * Utilities.
 * @author  Don Corley don@tourgeek.com
 * @version 1.0.0
 */
public class PropertyUtilities extends Object {
	public static final boolean DEBUG = false;

	/**
	  * Creates new PropertyUtilities
	  */
	public PropertyUtilities()
	{
	}
	/*
	 * Open and read the properties file.
	 * @param strFileName The name of the properties file.
	 * @return The properties.
	 */
	public static Properties readProperties(String strFileName)
	{
		Properties properties = new Properties();
		File fileProperties = new File(strFileName);
		try	{
			if (!fileProperties.exists())
				fileProperties.createNewFile();
			InputStream inStream = new FileInputStream(fileProperties);
			properties.load(inStream);
			inStream.close();
		} catch (IOException ex)	{
			ex.printStackTrace();
		}
		return properties;
	}
	/*
	 * Write out the properties.
	 * @param strFileName The name of the properties file.
	 * @param properties The properties file.
	 */
	public static void writeProperties(String strFileName, Properties properties)
	{
		try	{
			OutputStream out = new FileOutputStream(strFileName);
			properties.store(out, "JBackup preferences");
			out.flush();
			out.close();
		} catch (IOException ex)	{
			ex.printStackTrace();
		}
	}
}
