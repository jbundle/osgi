/*
 * PathFilter.java
 *
 * Created on May 1, 2000, 4:59 AM
 */
 
package org.jbundle.jbackup.filter;

import java.io.*;
/** 
 * Picture Filter.
 * @author  Administrator
 * @version 
 */
public class PictureFilter extends Object implements FilenameFilter
{

	/**
	  * Creates new PictureFilter.
	  */
	public PictureFilter()
	{
		super();
	}
  	/**
	  * Accept this file?
	  * @param dir The directory this file is in.
	  * @param filename The filename.
	  */
	public boolean accept(File dir, String filename)
	{
		String strPath = dir.getPath();
//		if ((strPath.indexOf("mediumpics") != -1)
//			|| (strPath.indexOf("smallpics") != -1)
//				|| (strPath.indexOf("thumbpics") != -1))
//			return false;
//		if (strPath.indexOf("\\trips\\") != -1)
		if ((strPath.indexOf("\\html\\pics\\pictures\\") != -1)
			&& (filename.indexOf(".html") != -1)
			&& (strPath.indexOf("\\html\\pics\\pictures\\donandannie\\trips") == -1))
				return false;		// Don't replicate the html files, except the trip
		if ((strPath.indexOf("\\html\\pics\\smallpics\\pictures") != -1)
			&& (filename.indexOf(".html") != -1)
			&& (strPath.indexOf("\\html\\pics\\smallpics\\pictures\\donandannie\\trips") == -1))
				return false;		// Don't replicate the html files, except the trip
		return true;
	}
}