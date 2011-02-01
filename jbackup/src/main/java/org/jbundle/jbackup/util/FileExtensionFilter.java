/*
 * FileExtensionFilter.java
 *
 * Created on April 9, 2000, 5:09 AM
 */
 
package org.jbundle.jbackup.util;

import java.io.*;

/** 
 * The FileExtensionFilter filters files in a directory depending on their extension.
 *
 * @author  Don Corley
 * @version 1.0.0
 */
public class FileExtensionFilter extends Object
	implements FileFilter
{
	protected String[] m_rgstrIncludeExtensions = null;
	protected String[] m_rgstrExcludeExtensions = null;

	/**
	  * Creates new FileExtensionFilter
	  */
	public FileExtensionFilter(String[] rgstrIncludeExtensions, String[] rgstrExcludeExtensions)
	{
		super();
		m_rgstrIncludeExtensions = rgstrIncludeExtensions;
		m_rgstrExcludeExtensions = rgstrExcludeExtensions;
	}
	/** Tests whether or not the specified abstract pathname should be
	 * included in a pathname list.
	 *
	 * @param  pathname  The abstract pathname to be tested
	 * @return  <code>true</code> if and only if <code>pathname</code>
	 *          should be included
	 */
	public boolean accept(File pathname)
	{
		String name = pathname.getName();
		int iLastDot = name.lastIndexOf('.');
		String strExtension = "";
		if ((iLastDot != -1)
			&& (iLastDot != name.length() -1 ))
				strExtension = name.substring(iLastDot + 1);
		if (m_rgstrIncludeExtensions != null)
		{
			for (int i = 0; i < m_rgstrIncludeExtensions.length; i++)
			{
				if (m_rgstrIncludeExtensions[i].equalsIgnoreCase(strExtension))
					return true;	// Accept
			}
			return false;	// Not in included - return
		}
		if (m_rgstrExcludeExtensions != null)
		{
			for (int i = 0; i < m_rgstrExcludeExtensions.length; i++)
			{
				if (m_rgstrExcludeExtensions[i].equalsIgnoreCase(strExtension))
					return false;	// Don't accept
			}
		}
		return true;	// Accept this file
	}
}