/*
 * PathFilter.java
 *
 * Created on May 1, 2000, 4:59 AM
 */
 
package org.jbundle.jbackup.filter;

import java.io.*;
/** 
 * Path Filter.
 * @author  Don Corley
 * @version 1.0.0
 */
public class PathFilter extends Object
	implements FilenameFilter
{
	protected String m_strOkayPath = null;

	/**
	  * Creates new PathFilter
	  */
	public PathFilter(String strOkayPath)
	{
		super();
		String strSeparator = System.getProperty("file.separator");
		char chSeparator = '/';
		if (strSeparator != null)
			if (strSeparator.length() == 1)
				chSeparator = strSeparator.charAt(0);
		m_strOkayPath = strOkayPath.replace('/', chSeparator).toLowerCase();
	}
  	/**
	  * Accept this file?
	  * @param dir The directory this file is in.
	  * @param filename The filename.
	  */
	public boolean accept(File dir, String filename)
	{
		String strPath = dir.getPath().toLowerCase();
		if (strPath.indexOf(m_strOkayPath) != -1)
			return true;
		return false;
	}
}