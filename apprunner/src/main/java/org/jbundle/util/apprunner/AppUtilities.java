/*
 * Util.java
 *
 * Created on April 10, 2000, 1:38 AM
 */
 
package org.jbundle.util.apprunner;

import java.io.*;
import java.net.URLDecoder;
import java.util.*;

/** 
 * Utilities.
 * @author  Don Corley don@tourgeek.com
 * @version 1.0.0
 */
public class AppUtilities extends Object {
	public static final boolean DEBUG = false;
    public final static String URL_ENCODING = "UTF-8";

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
	// TODO (don) This came straight from jbundle. Have jbundle.thin.util inherit from this
    /**
     * Parse this URL formatted string into properties.
     * @properties The properties object to add the params to.
     * @args The arguments to parse (each formatted as key=value).
     */
    public static Properties parseArgs(Properties properties, String[] args)
    {
    	if (properties == null)
    		properties = new Properties();
        if (args == null)
            return properties;
        for (int i = 0; i < args.length; i++)
        	AppUtilities.addParam(properties, args[i], false);
        return properties;
    }
    /**
     * Parse the param line and add it to this properties object.
     * (ie., key=value).
     * @properties The properties object to add this params to.
     * @param strParam param line in the format param=value
     */
    public static void addParam(Properties properties, String strParams, boolean bDecodeString)
    {
        int iIndex = strParams.indexOf('=');
        int iEndIndex = strParams.length();
        if (iIndex != -1)
        {
            String strParam = strParams.substring(0, iIndex);
            String strValue = strParams.substring(iIndex + 1, iEndIndex);
            if (bDecodeString)
            {
                try {
                    strParam = URLDecoder.decode(strParam, URL_ENCODING);
                    strValue = URLDecoder.decode(strValue, URL_ENCODING);
                } catch (java.io.UnsupportedEncodingException ex) {
                    ex.printStackTrace();
                }
            }
            properties.put(strParam, strValue);
        }
    }
}
