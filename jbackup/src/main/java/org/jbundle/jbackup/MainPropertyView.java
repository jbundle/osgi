/*
 * @(#)MainPropertyView.java	1.13 98/08/28
 */
package org.jbundle.jbackup;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jbundle.jbackup.destination.BaseDestination;
import org.jbundle.jbackup.source.BaseSource;
import org.jbundle.jbackup.util.Util;
import org.jbundle.jproperties.PropertyOwner;
import org.jbundle.jproperties.PropertyView;


/**
 * Main property view screen.
 * From this screen the user selects the model, view, and control.
 */
public class MainPropertyView extends PropertyView
	implements ActionListener, JBackupConstants, ItemListener
{
	private static final long serialVersionUID = 1L;

	protected static final String[] m_rgstrSources = {
		"Filesystem",
		"Zip"};
	protected static final String[] m_rgstrDestinations = {
		"Filesystem",
		"Zip",
		"Ftp",
		"Http",
		"Debug"};
	protected JComboBox m_comboSource = null;
	protected JComboBox m_comboDestination = null;
	protected JButton m_buttonSource = null;
	protected JButton m_buttonDestination = null;

	/*
	 * Constructor.
	 */
	public MainPropertyView()
	{
		super();
	}
	/*
	 * Constructor.
	 */
	public MainPropertyView(PropertyOwner propOwner, Properties properties)
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
		
		panelMain.setLayout(new GridLayout(2, 3));

		String strSource = m_properties.getProperty(SOURCE_PARAM);
		if (strSource == null)
			strSource = m_rgstrSources[0];
		panelMain.add(new JLabel("Source: ", JLabel.RIGHT));
		panelMain.add(m_comboSource = (JComboBox)this.makeControlPopup(m_rgstrSources, strSource));
		panelMain.add(m_buttonSource = (JButton)new JButton("Change settings..."));

		String strDestination = m_properties.getProperty(DESTINATION_PARAM);
		if (strDestination == null)
			strDestination = m_rgstrDestinations[0];
		panelMain.add(new JLabel("Destination: ", JLabel.RIGHT));
		panelMain.add(m_comboDestination = (JComboBox)this.makeControlPopup(m_rgstrDestinations, strDestination));
		panelMain.add(m_buttonDestination = (JButton)new JButton("Change settings..."));

		JPanel panelSub = this.makeNewPanel(panel, BorderLayout.SOUTH);
		super.addControlsToView(panelSub);
	}
	/*
	 * Add listeners to the controls.
	 */
	public void addListeners()
	{
		super.addListeners();
		m_buttonSource.addActionListener(this);
		m_comboSource.addItemListener(this);
		m_buttonDestination.addActionListener(this);
		m_comboDestination.addItemListener(this);
	}
	/*
	 * Set the properties to the current control values.
	 */
	public void controlsToProperties()
	{
		String strSelection = (String)m_comboSource.getSelectedItem();
		m_properties.setProperty(SOURCE_PARAM, strSelection);
		strSelection = (String)m_comboDestination.getSelectedItem();
		m_properties.setProperty(DESTINATION_PARAM, strSelection);
		super.controlsToProperties();
	}
	/*
	 * Set the properties to the current control values.
	 */
	public void propertiesToControls()
	{
		m_comboSource.setSelectedItem(m_properties.getProperty(SOURCE_PARAM));
		if (m_comboSource.getSelectedIndex() == -1)
		{
			m_comboSource.setSelectedIndex(0);
			m_properties.setProperty(SOURCE_PARAM, m_comboSource.getSelectedItem().toString());
		}
		m_comboDestination.setSelectedItem(m_properties.getProperty(DESTINATION_PARAM));
		if (m_comboDestination.getSelectedIndex() == -1)
		{
			m_comboDestination.setSelectedIndex(0);
			m_properties.setProperty(DESTINATION_PARAM, m_comboDestination.getSelectedItem().toString());
		}
		super.propertiesToControls();
	}
	/*
	 * User pressed a button.
	 */
	public void actionPerformed(ActionEvent e)
	{
		PropertyOwner propOwner = null;
		String strClass = null;
		Object objClass = null;
		if (e.getSource() == m_buttonSource)
		{
			strClass = m_properties.getProperty(Scanner.SOURCE_PARAM);
			objClass = Util.makeObjectFromClassName(Object.class.getName(), SOURCE_PARAM, strClass);
			if (objClass instanceof BaseSource)	// Always
				((BaseSource)objClass).init(m_properties);
		}
		else if (e.getSource() == m_buttonDestination)
		{
			strClass = m_properties.getProperty(Scanner.DESTINATION_PARAM);
			objClass = Util.makeObjectFromClassName(Object.class.getName(), DESTINATION_PARAM, strClass);
			if (objClass instanceof BaseDestination)	// Always
				((BaseDestination)objClass).init(m_properties);
		}
		if (objClass instanceof PropertyOwner)
			propOwner = (PropertyOwner)objClass;
		if (propOwner != null)
		{
			this.controlsToProperties();
			PropertyView panel = propOwner.getPropertyView(m_properties);
			if (panel == null)	// Default
				panel = new PropertyView(m_propOwner, m_properties);
			if (JOptionPane.showConfirmDialog(null, panel, panel.getDescription(), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
			{
				panel.controlsToProperties();
				propOwner.setProperties(m_properties);	// Send the property owner the new settings
			}
		}
	}
	/*
	 * One of the popup's changed
	 */
	public void itemStateChanged(final java.awt.event.ItemEvent p1)
	{
		super.itemStateChanged(p1);
		this.controlsToProperties();	// Make sure they are up-to-date
	}
	/*
	 * Get the description of this option panel.
	 */
	public String getDescription()
	{
		return "Main properties";
	}
}