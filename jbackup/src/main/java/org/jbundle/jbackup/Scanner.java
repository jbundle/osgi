/*
 * DirScanner.java
 *
 * Created on January 29, 2000, 6:33 AM
 */
 
package org.jbundle.jbackup;

import java.util.Properties;

import org.jbundle.jbackup.destination.DestinationFile;
import org.jbundle.jbackup.source.SourceFile;
import org.jbundle.jbackup.source.SourceFileList;
import org.jbundle.jbackup.util.Util;
import org.jbundle.util.apprunner.PropertyUtilities;

/** 
 * Directory scanner.
 * This class is a utility to move a bunch of files from one place to another.
 * There are several ways to run this.
 * First, standalone:
 * 1 - Pass the source and destination parameters.
 * Second, as a task or utility:
 * 1 - Pass a property file with the source and destination parameters
 * 2 - run() it if it is not a task, start it if it is.
 * @author  Don Corley
 * @version 1.0.0
 */
public class Scanner extends Object
	implements Runnable, JBackupConstants	// In case you want to run as a task
{
	protected Properties m_properties = null;

	/**
	  * Creates new DirScanner
	  */
	public Scanner()
	{
		super();
	}
	/**
	  * Creates new DirScanner
	  */
	public Scanner(Properties properties)
	{
		this();
		m_properties = properties;
	}
	/**
	 * Standalone support.
	 * Usually you specify a property file to use (ie., property.filename=c:\\temp\\updatesite.properties)
	 */
	public static void main(String[] args)
	{
		Properties properties = new Properties();
		if (args != null)
		{		// Move the args to a property file
			for (int i = 0; i < args.length; i++)
			{
				int iEquals = args[i].indexOf('=');
				if (iEquals != -1)
					if (iEquals < args[i].length() - 1)
						properties.setProperty(args[i].substring(0, iEquals), args[i].substring(iEquals + 1));
			}
		}
		String strPropertyFileName = properties.getProperty(PROPERTY_FILENAME_PARAM);
		if (strPropertyFileName != null)
		{
			Properties propertiesRead = PropertyUtilities.readProperties(strPropertyFileName);
			propertiesRead.putAll(properties);		// Add the read-in properties
			properties = propertiesRead;
		}
		Scanner scanner = new Scanner(properties);
		scanner.run();
		PropertyUtilities.writeProperties(strPropertyFileName, properties);
	}
	/**
	 * Move files from the source to the destination.
	 */
	public void run()
	{
		if (m_properties == null)
		{
			System.out.println("Must supply properties.");
			return;
		}
		String strSourceClass = m_properties.getProperty(SOURCE_PARAM);
		String strDestinationClass = m_properties.getProperty(DESTINATION_PARAM);
		if ((strSourceClass == null) || (strDestinationClass == null))
		{
			System.out.println("Must supply source and destination class names.");
			return;
		}
		SourceFileList sourceList = (SourceFileList)Util.makeObjectFromClassName(Object.class.getName(), "source", strSourceClass);
		DestinationFile destination = (DestinationFile)Util.makeObjectFromClassName(Object.class.getName(), "destination", strDestinationClass);
		this.process(sourceList, destination);
	}
	/**
	 * Move files from the source to the destination.
	 */
	public void process(SourceFileList sourceList, DestinationFile destination)
	{
		sourceList.initTransfer(m_properties);
		destination.initTransfer(m_properties);
		//FilesystemSource("c:\\My Documents");
		//DebugDestination(null);
		//ZipDestination("test.zip", -1);
		while (sourceList.hasNext())
		{
			SourceFile source = sourceList.next();
			this.moveFile(source, destination);
//?                        source.close();
//			System.out.println(source.getFileName());
		}
		destination.finishTransfer(m_properties);
		sourceList.finishTransfer(m_properties);
	}
	/**
	 * Move this file from the source to the destination.
	 */
	public void moveFile(SourceFile source, DestinationFile destination)
	{
		destination.addNextFile(source);
	}
}