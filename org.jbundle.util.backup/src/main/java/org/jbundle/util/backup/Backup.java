package org.jbundle.util.backup;

import org.jbundle.jbackup.JBackup;

/**
 * Standalone backup program.
 * This version of jbackup includes all the dependent classes in one runnable jar.
 */
public class Backup extends JBackup
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void main( String[] args )
    {
    	JBackup.main(args);
    }
}
