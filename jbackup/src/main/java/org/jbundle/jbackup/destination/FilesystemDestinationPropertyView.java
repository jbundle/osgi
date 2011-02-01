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
public class FilesystemDestinationPropertyView extends BaseDestinationPropertyView
	implements ActionListener, JBackupConstants
{
	private static final long serialVersionUID = 1L;

	protected JTextField m_tfRootPathname = null;

	/*
	 * Constructor.
	 */
	public FilesystemDestinationPropertyView()
	{
		super();
	}
	/*
	 * Constructor.
	 */
	public FilesystemDestinationPropertyView(PropertyOwner propOwner,Properties properties)
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
		
		panelMain.setLayout(new GridLayout(1, 2));

		panelMain.add(new JLabel("Root pathname: ", JLabel.RIGHT));
		panelMain.add(m_tfRootPathname = new JTextField());

		JPanel panelSub = this.makeNewPanel(panel, BorderLayout.SOUTH);
		super.addControlsToView(panelSub);
	}
	/*
	 * Set the properties to the current control values.
	 */
	public void controlsToProperties()
	{
		super.controlsToProperties();
		String strPathname = m_tfRootPathname.getText();
		m_properties.setProperty(DEST_ROOT_PATHNAME_PARAM, strPathname);
	}
	/*
	 * Set the properties to the current control values.
	 */
	public void propertiesToControls()
	{
		super.propertiesToControls();
		String strPathname = m_properties.getProperty(DEST_ROOT_PATHNAME_PARAM);
		m_tfRootPathname.setText(strPathname);
	}
	/*
	 * Get the description of this option panel.
	 */
	public String getDescription()
	{
		return "Filesystem destination properties";
	}
}