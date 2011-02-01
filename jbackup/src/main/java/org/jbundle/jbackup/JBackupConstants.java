/*
 * Constants.java
 *
 * Created on April 24, 2000, 4:31 AM
 */
 
package org.jbundle.jbackup;

/** 
 * Constants for the backup program.
 * @author  Administrator
 * @version 
 */
public interface JBackupConstants {

	public static final boolean DEBUG = false;
	
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	
	public static final String BLANK = "";
	public static final String ROOT_PACKAGE = "org.jbundle.";
	
	public static final String PROPERTY_FILENAME_PARAM = "property.filename";
	public static final String DEFAULT_PROPERTY_FILENAME = "JBackup.properties";
	public static final String PROPERTY_QUIET_PARAM = "property.quiet";

	public static final String SOURCE_PARAM = "source";
	public static final String DESTINATION_PARAM = "destination";
	
	public static final String BACKUP_INCREMENTAL_PARAM = "source.incremental";
	public static final String BACKUPDATE_PARAM = "source.lastbackupdate";
	public static final String FILTER_PARAM = "source.filtername";
		// Filesytem
	public static final String DEST_ROOT_PATHNAME_PARAM = "destination.filesystem.rootpathname";
		// Debug
	public static final String LOG_FILENAME_PARAM = "destination.debug.logfilename";
	public static final String CALC_FILE_LENGTH_PARAM = "destination.debug.calcfilelength";
		// Ftp
	public static final String FTP_HOST = "destination.ftp.ftphost";
	public static final String FTP_PORT = "destination.ftp.port";
	public static final String ROOT_DIR = "destination.ftp.root";
	public static final String DEFAULT_ROOT_DIR = "";
	public static final String USER_NAME = "destination.ftp.username";
	public static final String PASSWORD = "destination.ftp.password";
		// Http
	public static final String BASE_URL_PARAM = "destination.http.baseurl";
	public static final String HTTP = "http:";
		// Zip
	public static final String ZIPOUT_PATHNAME_PARAM = "destination.zip.pathname";
	public static final String ZIPOUT_FILENAME_PARAM = "destination.zip.filename";
	public static final String MAX_SIZE_PARAM = "maxsize";
		// Filesystem
	public static final int MAX_LEVELS = 50;
	public static final String SOURCE_ROOT_PATHNAME_PARAM = "source.filesystem.rootpathname";
		// Zip
	public static final String ZIPIN_FILENAME_PARAM = "source.zip.filename";
}
