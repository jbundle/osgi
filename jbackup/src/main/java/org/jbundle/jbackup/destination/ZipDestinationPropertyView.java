/*
 * @(#)ScreenApplet.java	1.13 98/08/28
 */
package org.jbundle.jbackup.destination;

import java.awt.BorderLayout;
import java.awt.GridLayout;
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
public class ZipDestinationPropertyView extends BaseDestinationPropertyView
	implements JBackupConstants
{
	private static final long serialVersionUID = 1L;

	protected JTextField m_tfRootPathname = null;
	protected JTextField m_tfFilename = null;
	protected JTextField m_tfMaxSize = null;

	/*
	 * Constructor.
	 */
	public ZipDestinationPropertyView()
	{
		super();
	}
	/*
	 * Constructor.
	 */
	public ZipDestinationPropertyView(PropertyOwner propOwner,Properties properties)
	{
		this();
		this.init(propOwner, properties);
	}
	/*
	 * Initialize.
	 */
	public void init(PropertyOwner propOwner, Properties properties)
	{
//		String strPathname = properties.getProperty(ZIPOUT_FILENAME_PARAM);
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
		
		panelMain.setLayout(new GridLayout(3, 2));

		panelMain.add(new JLabel("Zip directory: ", JLabel.RIGHT));
		panelMain.add(m_tfRootPathname = new JTextField());

		panelMain.add(new JLabel("Zip filename: ", JLabel.RIGHT));
		panelMain.add(m_tfFilename = new JTextField());

		panelMain.add(new JLabel("Max size: ", JLabel.RIGHT));
		panelMain.add(m_tfMaxSize = new JTextField());

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
		m_properties.setProperty(ZIPOUT_PATHNAME_PARAM, strPathname);
		String strFilename = m_tfFilename.getText();
		m_properties.setProperty(ZIPOUT_FILENAME_PARAM, strFilename);
		String strMaxSize = m_tfMaxSize.getText();
		m_properties.setProperty(MAX_SIZE_PARAM, strMaxSize);
	}
	/*
	 * Set the properties to the current control values.
	 */
	public void propertiesToControls()
	{
		super.propertiesToControls();
		String strPathname = m_properties.getProperty(ZIPOUT_PATHNAME_PARAM);
		m_tfRootPathname.setText(strPathname);

		String strFilename = m_properties.getProperty(ZIPOUT_FILENAME_PARAM);
		m_tfFilename.setText(strFilename);

		String strMaxSize = m_properties.getProperty(MAX_SIZE_PARAM);
		m_tfMaxSize.setText(strMaxSize);
	}
	/*
	 * Get the description of this option panel.
	 */
	public String getDescription()
	{
		return "Base source properties";
	}
}