/*
 * @(#)ScreenApplet.java	1.13 98/08/28
 */
package org.jbundle.jbackup.source;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jbundle.jbackup.JBackupConstants;
import org.jbundle.util.apprunner.PropertyOwner;
import org.jbundle.util.apprunner.PropertyView;

/**
 * Main property view screen.
 * From this screen the user selects the model, view, and control.
 */
public class BaseSourcePropertyView extends PropertyView
	implements ActionListener, JBackupConstants
{
	private static final long serialVersionUID = 1L;

	protected JTextField m_tfDate = null;
	protected JTextField m_tfFilter = null;
	protected JCheckBox m_cbIncremental = null;

	/*
	 * Constructor.
	 */
	public BaseSourcePropertyView()
	{
		super();
	}
	/*
	 * Constructor.
	 */
	public BaseSourcePropertyView(PropertyOwner propOwner, Properties properties)
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
		
		panelMain.setLayout(new GridLayout(3, 2));

		panelMain.add(new JLabel("Last backup: ", JLabel.RIGHT));
		panelMain.add(m_tfDate = new JTextField());

		panelMain.add(new JLabel("Filter: ", JLabel.RIGHT));
		panelMain.add(m_tfFilter = new JTextField());

		panelMain.add(new JPanel());
		panelMain.add(m_cbIncremental = new JCheckBox("Incremental"));

		JPanel panelSub = this.makeNewPanel(panel, BorderLayout.SOUTH);
		super.addControlsToView(panelSub);
	}
	/*
	 * Set the properties to the current control values.
	 */
	public void controlsToProperties()
	{
		super.controlsToProperties();
		String strDate = m_tfDate.getText();
		m_properties.setProperty(BACKUPDATE_PARAM, strDate);

		String strFilter = m_tfFilter.getText();
		m_properties.setProperty(FILTER_PARAM, strFilter);
		
		boolean bSelected = m_cbIncremental.isSelected();
		if (bSelected)
			m_properties.setProperty(BACKUP_INCREMENTAL_PARAM, TRUE);
		else
			m_properties.setProperty(BACKUP_INCREMENTAL_PARAM, FALSE);
	}
	/*
	 * Set the properties to the current control values.
	 */
	public void propertiesToControls()
	{
		super.propertiesToControls();
		String strDate = m_properties.getProperty(BACKUPDATE_PARAM);
		m_tfDate.setText(strDate);

		String strFilter = m_properties.getProperty(FILTER_PARAM);
		m_tfFilter.setText(strFilter);

		boolean bSelected = false;
		String strSelected = m_properties.getProperty(BACKUP_INCREMENTAL_PARAM);
		if (TRUE.equalsIgnoreCase(strSelected))
			bSelected = true;
		m_cbIncremental.setSelected(bSelected);
	}
	/*
	 * Get the description of this option panel.
	 */
	public String getDescription()
	{
		return "Base source properties";
	}
}