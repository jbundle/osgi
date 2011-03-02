/*
 * DirScanner.java
 *
 * Created on January 29, 2000, 6:33 AM
 */
 
package org.jbundle.jbackup.source;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.jbundle.jbackup.JBackupConstants;
import org.jbundle.jbackup.util.Util;
import org.jbundle.util.apprunner.PropertyOwner;
import org.jbundle.util.apprunner.PropertyView;


/** 
 * Directory scanner.
 * This class is a utility for scanning through a directory tree.
 * @author  Administrator
 * @version 
 */
public class BaseSource extends Object
	implements SourceFileList, PropertyOwner, JBackupConstants
{
	public static char gchSeparator = '/';

	protected Date m_dateLastBackup = null;		// Date Filter (optional)
	protected FilenameFilter m_Filter = null;	// File filter (optional)
	
	protected SourceFile m_nextPend = null;		// For hasNext support.
	
	/*
	 * Constructor
	 */
	public BaseSource()
	{
		super();
	}
	/*
	 * Constructor
	 */
	public BaseSource(Properties properties)
	{
		this();
		this.init(properties);
	}
	/*
	 * Set up everything to start processing
	 */
	public void init(Properties properties)
	{
	}
	/*
	 * Set up everything to start processing
	 */
	public void initTransfer(Properties properties)
	{
		boolean bIncremental = false;
		String strSelected = properties.getProperty(BACKUP_INCREMENTAL_PARAM);
		if (TRUE.equalsIgnoreCase(strSelected))
			bIncremental = true;
		String strDate = properties.getProperty(BACKUPDATE_PARAM);
		if (bIncremental)	// If incremental, set the "filter date".
			m_dateLastBackup = Util.stringToDate(strDate);
		m_Filter = Util.makeFilter(properties);
	}
	/*
	 * Close everything down after processing.
	 */
	public void finishTransfer(Properties properties)
	{
		String strDateLastBackup = Util.dateToString(new Date());
		properties.setProperty(BACKUPDATE_PARAM, strDateLastBackup);
	}
	/*
	 * Get the panel to change the properties for this object.
	 */
	public PropertyView getPropertyView(Properties properties)
	{
		return new BaseSourcePropertyView(this, properties);
	}
	/*
	 * Set the properties on this object.
	 */
	public void setProperties(final java.util.Properties p1) {
	}
	/** Returns <tt>true</tt> if the iteration has more elements. (In other
	 * words, returns <tt>true</tt> if <tt>next</tt> would return an element
	 * rather than throwing an exception.)
	 *
	 * @return <tt>true</tt> if the iterator has more elements.
	 */
	public boolean hasNext() {
		if (m_nextPend != null)
			return true;
		m_nextPend = this.next();
		if (m_nextPend == null)
			return false;
		else
			return true;
	}
	/** Returns the next element in the interation.
	 * (Returns a SourceFileObject).
	 *
	 * @returns the next element in the interation.
	 * @exception NoSuchElementException iteration has no more elements.
	 */
	public boolean isPend()
	{
		return (m_nextPend != null);
	}
	/** Returns the next element in the interation.
	 * (Returns a SourceFileObject).
	 * <pre>
	 * 	public Object next()
	 * {
	 *	if (this.isPend())
	 *		return this.getPend();
	 *	return null;
	 *}
	 * </pre>
	 *
	 * @returns the next element in the interation.
	 * @exception NoSuchElementException iteration has no more elements.
	 */
	public SourceFile getPend()
	{
		SourceFile nextPend = m_nextPend;
		m_nextPend = null;
		return nextPend;
	}
	/** Returns the next element in the interation.
	 * (Returns a SourceFileObject).
	 */
	public SourceFile next()
	{
		if (this.isPend())
			return this.getPend();
		return null;
	}
	/**
	 * Removes from the underlying collection the last element returned by the
	 * iterator (optional operation).  This method can be called only once per
	 * call to <tt>next</tt>.  The behavior of an iterator is unspecified if
	 * the underlying collection is modified while the iteration is in
	 * progress in any way other than by calling this method.
	 *
	 * @exception UnsupportedOperationException if the <tt>remove</tt>
	 * 		  operation is not supported by this Iterator.
	 *
	 * @exception IllegalStateException if the <tt>next</tt> method has not
	 * 		  yet been called, or the <tt>remove</tt> method has already
	 * 		  been called after the last call to the <tt>next</tt>
	 * 		  method.
	 */
	public void remove() {
		// Not implemented
	}
	/*
	 * Should I skip this file?
	 * Override this.
	 */
	public boolean skipFile(File inFile)
	{
		if (m_dateLastBackup != null)
		{		// Check the date
			Date dateLastMod = new Date(inFile.lastModified());
			if (dateLastMod.before(m_dateLastBackup))
				return true;	// Skip it
		}
		return false;	// Don't skip it
	}
}