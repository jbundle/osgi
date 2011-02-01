/*
 * DirScanner.java
 *
 * Created on January 29, 2000, 6:33 AM
 */
 
package org.jbundle.jbackup.source;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.jbundle.jbackup.JBackupConstants;
import org.jbundle.jproperties.PropertyView;


/** 
 * Directory scanner.
 * This class is a utility for scanning through a directory tree.
 * @author  Administrator
 * @version 
 */
public class FilesystemSource extends BaseSource
	implements SourceFileList, JBackupConstants
{
	protected String m_strRootPath = null;		// Initial path to start at
	
	protected File[][] m_rgfileCurrentFilelist = new File[MAX_LEVELS][];
	protected String[] m_rgstrCurrentPath = new String[MAX_LEVELS];
	protected int[] m_rgiCurrentFile = new int[MAX_LEVELS];
	protected int m_iCurrentLevel = -1;

	/*
	 * Constructor
	 */
	public FilesystemSource()
	{
		super();
	}
	/*
	 * Constructor
	 */
	public FilesystemSource(Properties properties)
	{
		this();
		this.init(properties);
	}
	/*
	 * Get ready to start processing.
	 */
	public void init(Properties properties)
	{
		super.init(properties);
		String strPathname = properties.getProperty(SOURCE_ROOT_PATHNAME_PARAM);
		if (strPathname == null)
		{
			strPathname = "c:\\My Documents";
			properties.setProperty(SOURCE_ROOT_PATHNAME_PARAM, strPathname);
		}
	}
	/*
	 * Set up everything to start processing
	 */
	public void initTransfer(Properties properties)
	{
		super.initTransfer(properties);
		m_strRootPath = properties.getProperty(SOURCE_ROOT_PATHNAME_PARAM);
		m_iCurrentLevel = -1;
	}
	/*
	 * Close everything down after processing.
	 */
	public void finishTransfer(Properties properties)
	{
		super.finishTransfer(properties);
	}
	/** Returns the next element in the iteration.
	 * (Returns a SourceFileObject).
	 *
	 * @returns the next element in the iteration.
	 * @exception NoSuchElementException iteration has no more elements.
	 */
	public SourceFile next() {
		if (this.isPend())
			return this.getPend();
		if (m_iCurrentLevel == -1)
		{		// First time
			File fileDir = new File(m_strRootPath);
			m_iCurrentLevel++;
			if (!fileDir.isDirectory())
				return null;		// Never	// pend(don) Return 1 file?
			m_rgfileCurrentFilelist[m_iCurrentLevel] = fileDir.listFiles();
			m_rgstrCurrentPath[m_iCurrentLevel] = JBackupConstants.BLANK;	// Root (relative)
			m_rgiCurrentFile[m_iCurrentLevel] = 0;
		}

		while (true)
		{
			File[] fileList = m_rgfileCurrentFilelist[m_iCurrentLevel];		// Current list
			int iCurrentIndex = m_rgiCurrentFile[m_iCurrentLevel];
			if (iCurrentIndex >= fileList.length)
			{		// End of directory, go up a level
				m_rgfileCurrentFilelist[m_iCurrentLevel] = null;	// Free
				m_rgstrCurrentPath[m_iCurrentLevel] = null;
				m_rgiCurrentFile[m_iCurrentLevel] = 0;
				m_iCurrentLevel--;	// End of directory
				if (m_iCurrentLevel < 0)
					return null;		// End of files!
			}
			else
			{
				File file = fileList[iCurrentIndex];
				String strPath = m_rgstrCurrentPath[m_iCurrentLevel];
				m_rgiCurrentFile[m_iCurrentLevel]++;	// Bump for next time
				if (file.isDirectory())
				{	// This is a directory, go down a level
					strPath += file.getName() + gchSeparator;

					m_iCurrentLevel++;
					if (m_Filter == null)
						fileList = file.listFiles();
					else
						fileList = file.listFiles(m_Filter);
					m_rgfileCurrentFilelist[m_iCurrentLevel] = fileList;
					m_rgstrCurrentPath[m_iCurrentLevel] = strPath;	// Relative path to this directory
					m_rgiCurrentFile[m_iCurrentLevel] = 0;
					if (fileList == null)
						m_iCurrentLevel--;	// Special case - skip windows linked directory
				}
				else if (file.isFile())
				{
					String strName = file.getName();
					if (m_Filter != null)
					{
						if (!m_Filter.accept(file, strName))
							continue;		// HACK - The filter should do this
					}
					if (this.skipFile(file))
						continue;
					String strRelativeFileName = strPath + strName;
					long lStreamLength = file.length();

					return new StreamSourceFile(file, null, strRelativeFileName, strName, lStreamLength);		// Return the file
				}
			}
		}
	}
	/*
	 * Get the panel to change the properties for this object.
	 */
	public PropertyView getPropertyView(Properties properties) {
		return new FilesystemSourcePropertyView(this, properties);
	}
}