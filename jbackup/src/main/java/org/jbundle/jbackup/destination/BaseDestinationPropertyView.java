/*
 * @(#)ScreenApplet.java	1.13 98/08/28
 */
package org.jbundle.jbackup.destination;

import java.awt.BorderLayout;
import java.util.Properties;

import javax.swing.JPanel;

import org.jbundle.jbackup.JBackupConstants;
import org.jbundle.jproperties.PropertyOwner;
import org.jbundle.jproperties.PropertyView;

/**
 * Main property view screen.
 * From this screen the user selects the model, view, and control.
 */
public class BaseDestinationPropertyView extends PropertyView
	implements JBackupConstants
{
	private static final long serialVersionUID = 1L;

	/*
	 * Constructor.
	 */
	public BaseDestinationPropertyView()
	{
		super();
	}
	/*
	 * Constructor.
	 */
	public BaseDestinationPropertyView(PropertyOwner propOwner,Properties properties)
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
		this.makeNewPanel(panel, BorderLayout.CENTER);
		

		JPanel panelSub = this.makeNewPanel(panel, BorderLayout.SOUTH);
		super.addControlsToView(panelSub);
	}
	/*
	 * Set the properties to the current control values.
	 */
	public void controlsToProperties()
	{
		super.controlsToProperties();
	}
	/*
	 * Set the properties to the current control values.
	 */
	public void propertiesToControls()
	{
		super.propertiesToControls();
	}
	/*
	 * Get the description of this option panel.
	 */
	public String getDescription()
	{
		return "Base source properties";
	}
}