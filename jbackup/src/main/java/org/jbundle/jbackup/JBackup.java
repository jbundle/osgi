package org.jbundle.jbackup;
/*
 * @(#)ScreenApplet.java	1.13 98/08/28
 *
 */


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Properties;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.jbundle.util.apprunner.PropertyOwner;
import org.jbundle.util.apprunner.AppUtilities;
import org.jbundle.util.apprunner.PropertyView;
import org.jbundle.util.apprunner.AppRunner;

/**
 * An Application to scan files in source directories and
 * move them to a destination.
 *
 * @author Don Corley
 * @version 1.0.0
 */
public class JBackup extends AppRunner
	implements PropertyOwner, ActionListener, JBackupConstants
{
	private static final long serialVersionUID = 1L;

	public String m_strFileName = DEFAULT_PROPERTY_FILENAME;
	
	protected JButton m_buttonGo = null;
	protected JButton m_buttonSave = null;
	protected JProgressBar progressBar = null;
	
	/*
	 * Constructor.
	 */
	public JBackup()
	{
		super();
	}
	
	/*
	 * Constructor.
	 */
	public JBackup(String title)
	{
		this();
		init(title);
	}
	/*
	 * Constructor.
	 */
	public void init(String title)
	{
		super.init(title);
		Container contentPane = this.getContentPane();
		contentPane.setLayout(new BorderLayout());
	
		m_properties = AppUtilities.readProperties(m_strFileName = System.getProperty(PROPERTY_FILENAME_PARAM, DEFAULT_PROPERTY_FILENAME));
		
		JPanel panel = this.getPropertyView(m_properties);
		contentPane.add(panel, BorderLayout.CENTER);
		JPanel panelButtons = new JPanel();
		contentPane.add(panelButtons, BorderLayout.SOUTH);
		panelButtons.setLayout(new BorderLayout());
		panelButtons.add(m_buttonGo = new JButton("GO!"), BorderLayout.EAST);
		m_buttonGo.addActionListener(this);
		panelButtons.add(progressBar = new JProgressBar(0, 1), BorderLayout.CENTER);		
		panelButtons.add(m_buttonSave = new JButton("Save"), BorderLayout.WEST);
		m_buttonSave.addActionListener(this);
	}
    /**
     * APPLET INFO SUPPORT:
     *      The getAppletInfo() method returns a string describing the applet's
     * author, copyright date, or miscellaneous information.
     * @return The applet info.
     */
    public String getAppletInfo()
    {
        return "Name: JBackup\r\n" +
               "Author: Don Corley\r\n" +
               "Version 1.0.0";
    }
	/*
	 * Main method.
	 */
    public static void main(String[] args)
	{
        try {
			JBackup applet = new JBackup("JBackup");
			if (!Boolean.TRUE.toString().equalsIgnoreCase(System.getProperty(PROPERTY_QUIET_PARAM)))
			{
				JFrame frame = applet.addAppToFrame();
				frame.setVisible(true);
			}
			else
			{
				Scanner scanner = new Scanner(applet.m_properties);
				scanner.run();
				
				AppUtilities.writeProperties(applet.m_strFileName, applet.m_properties);				
				System.exit(0);
			}
		} catch (Throwable t) {
			System.out.println("uncaught exception: " + t);
			t.printStackTrace();
		}
//+		System.exit(0);
    }
	/*
	 * User pressed a button.
	 */
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == m_buttonGo)
		{
			Scanner scanner = new Scanner(m_properties);	// Fit them on floppys
			progressBar.setIndeterminate(true);		// For now
			scanner.run();
			progressBar.setIndeterminate(false);
			
			AppUtilities.writeProperties(m_strFileName, m_properties);
		}
		if (e.getSource() == m_buttonSave)
		{
			AppUtilities.writeProperties(m_strFileName, m_properties);
		}
	}
	/**
	 * Screen that is used to change the properties.
	 */
	public PropertyView getPropertyView(Properties properties)
	{
		return new MainPropertyView(this, properties);
	}
}