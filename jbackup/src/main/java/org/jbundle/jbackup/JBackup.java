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

import org.jbundle.jproperties.PropertyOwner;
import org.jbundle.jproperties.PropertyUtilities;
import org.jbundle.jproperties.PropertyView;


/**
 * An Application to scan files in source directories and
 * move them to a destination.
 *
 * @author Don Corley
 * @version 1.0.0
 */
public class JBackup extends JApplet
	implements PropertyOwner, ActionListener, JBackupConstants
{
	private static final long serialVersionUID = 1L;

	public String m_strFileName = DEFAULT_PROPERTY_FILENAME;

	protected Properties m_properties = null;
	
	protected JButton m_buttonGo = null;
	protected JButton m_buttonSave = null;
	protected JProgressBar progressBar = null;
	
	/*
	 * Constructor.
	 */
	public JBackup()
	{
		super();
	
		Container contentPane = this.getContentPane();
		contentPane.setLayout(new BorderLayout());
	
		m_properties = PropertyUtilities.readProperties(m_strFileName = System.getProperty(PROPERTY_FILENAME_PARAM, DEFAULT_PROPERTY_FILENAME));
		
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
			JBackup applet = new JBackup();
			JFrame frame = new JFrame();
			frame.setTitle("JBackup");
			frame.setBackground(Color.lightGray);
			frame.getContentPane().setLayout(new BorderLayout());
	
			frame.getContentPane().add(applet, BorderLayout.CENTER);
			frame.addWindowListener(new WindowAdapter()
			{
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});
			frame.pack();
			frame.setSize(frame.getPreferredSize().width, frame.getPreferredSize().height);
			if (!Boolean.TRUE.toString().equalsIgnoreCase(System.getProperty(PROPERTY_QUIET_PARAM)))
				frame.setVisible(true);
			else
			{
				Scanner scanner = new Scanner(applet.m_properties);
				scanner.run();
				
				PropertyUtilities.writeProperties(applet.m_strFileName, applet.m_properties);				
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
			
			PropertyUtilities.writeProperties(m_strFileName, m_properties);
		}
		if (e.getSource() == m_buttonSave)
		{
			PropertyUtilities.writeProperties(m_strFileName, m_properties);
		}
	}
	/**
	 * Set this control up to implement these new properties.
	 */
	public void setProperties(Properties properties)
	{
		m_properties = properties;
	}
	/**
	 * Screen that is used to change the properties.
	 */
	public PropertyView getPropertyView(Properties properties)
	{
		return new MainPropertyView(this, properties);
	}
}