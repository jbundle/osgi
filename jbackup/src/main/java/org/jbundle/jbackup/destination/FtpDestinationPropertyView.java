/*
 * @(#)ScreenApplet.java	1.13 98/08/28
 */
package org.jbundle.jbackup.destination;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jbundle.jbackup.JBackupConstants;
import org.jbundle.jproperties.PropertyOwner;

/**
 * Main property view screen.
 * From this screen the user selects the model, view, and control.
 */
public class FtpDestinationPropertyView extends BaseDestinationPropertyView
	implements ActionListener, JBackupConstants
{
	private static final long serialVersionUID = 1L;

	protected JTextField m_tfHost = null;
	protected JTextField m_tfPort = null;
	protected JTextField m_tfUser = null;
	protected JTextField m_tfPassword = null;
	protected JTextField m_tfDir = null;

	/*
	 * Constructor.
	 */
	public FtpDestinationPropertyView()
	{
		super();
	}
	/*
	 * Constructor.
	 */
	public FtpDestinationPropertyView(PropertyOwner propOwner,Properties properties)
	{
		this();
		this.init(propOwner, properties);
	}
	/*
	 * Initialize.
	 */
	public void init(PropertyOwner propOwner, Properties properties)
	{
		super.init(propOwner, properties);
	}
	/*
	 * Add your property controls to this panel.
	 * Remember to set your own layout manager.
	 * Also, remember to create a new JPanel, and pass it to the super class
	 * so controls of the superclass can be included.
	 * You have a 3 x 3 grid, so add three columns for each control
	 * @param panel This is the panel to add your controls to.
	 */
	public void addControlsToView(JPanel panel)
	{
		panel.setLayout(new BorderLayout());
		JPanel panelMain = this.makeNewPanel(panel, BorderLayout.CENTER);
		
		panelMain.setLayout(new GridLayout(5, 2));

		panelMain.add(new JLabel("Ftp Host: ", JLabel.RIGHT));
		panelMain.add(m_tfHost = new JTextField());

		panelMain.add(new JLabel("Ftp Port: ", JLabel.RIGHT));
		panelMain.add(m_tfPort = new JTextField());

		panelMain.add(new JLabel("User name: ", JLabel.RIGHT));
		panelMain.add(m_tfUser = new JTextField());

		panelMain.add(new JLabel("Password: ", JLabel.RIGHT));
		panelMain.add(m_tfPassword = new JTextField());

		panelMain.add(new JLabel("Initial directory: ", JLabel.RIGHT));
		panelMain.add(m_tfDir = new JTextField());

		JPanel panelSub = this.makeNewPanel(panel, BorderLayout.SOUTH);
		super.addControlsToView(panelSub);
	}
	/*
	 * Set the properties to the current control values.
	 */
	public void controlsToProperties()
	{
		super.controlsToProperties();
		String strHost = m_tfHost.getText();
		m_properties.setProperty(FTP_HOST, strHost);

		String strPort = m_tfPort.getText();
		m_properties.setProperty(FTP_PORT, strPort);

		String strUser = m_tfUser.getText();
		m_properties.setProperty(USER_NAME, strUser);

		String strPassword = m_tfPassword.getText();
		m_properties.setProperty(PASSWORD, strPassword);

		String strDir = m_tfDir.getText();
		m_properties.setProperty(ROOT_DIR, strDir);
	}
	/*
	 * Set the properties to the current control values.
	 */
	public void propertiesToControls()
	{
		super.propertiesToControls();
		String strHost = m_properties.getProperty(FTP_HOST);
		m_tfHost.setText(strHost);

		String strPort = m_properties.getProperty(FTP_PORT);
		m_tfPort.setText(strPort);

		String strUser = m_properties.getProperty(USER_NAME);
		m_tfUser.setText(strUser);

		String strPassword = m_properties.getProperty(PASSWORD);
		m_tfPassword.setText(strPassword);

		String strDir = m_properties.getProperty(ROOT_DIR);
		if (strDir == null)
			strDir = DEFAULT_ROOT_DIR;
		m_tfDir.setText(strDir);
	}
	/*
	 * Get the description of this option panel.
	 */
	public String getDescription()
	{
		return "Base source properties";
	}
}