/*
 * DirScanner.java
 *
 * Created on January 29, 2000, 6:33 AM
 */
 
package org.jbundle.jbackup.destination;

import java.util.Properties;

import org.jbundle.jbackup.JBackupConstants;
import org.jbundle.jbackup.source.SourceFile;
import org.jbundle.jproperties.PropertyOwner;
import org.jbundle.jproperties.PropertyView;


/** 
 * Directory scanner.
 * This class is a utility for scanning through a directory tree.
 * @author  Administrator
 * @version 
 */
public class BaseDestination extends Object
	implements DestinationFile, JBackupConstants, PropertyOwner
{

	/*
	 * Constructor
	 */
	public BaseDestination()
	{
		super();
	}
	/*
	 * Constructor
	 */
	public BaseDestination(Properties properties)
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
	}
	/*
	 * Close everything down after processing.
	 */
	public void finishTransfer(Properties properties)
	{
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
		long lStreamLength = source.getStreamLength();

		return lStreamLength;
	}
	/*
	 * Get the panel to change the properties for this object.
	 */
	public PropertyView getPropertyView(Properties properties)
	{
		return new BaseDestinationPropertyView(this, properties);
	}
	public void setProperties(Properties properties)
	{
	}
}