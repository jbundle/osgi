/*
 * DirScanner.java
 *
 * Created on January 29, 2000, 6:33 AM
 */
 
package org.jbundle.jbackup;

import java.io.FilenameFilter;
import java.util.Date;

/** 
 * Directory scanner.
 * This class is a utility for scanning through a directory tree.
 * @author  Don Corley
 * @version 1.0.0
 */
public class BaseScanner extends Scanner implements Runnable	// In case you want to run as a task
{

	public static char gchSeparator = '/';
	protected String m_strRootPath = null;
	protected Date m_dateLastBackup = null;
	protected FilenameFilter m_Filter = null;

	/** Creates new DirScanner */
	public BaseScanner(String strRootPath)
	{
		this(strRootPath, null, null);
	}
	/** Creates new DirScanner */
	public BaseScanner(String strRootPath,Date dateLastBackup,FilenameFilter filter)
	{
		super();
		m_strRootPath = strRootPath;
		m_dateLastBackup = dateLastBackup;
		m_Filter = filter;
	}
}