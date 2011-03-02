/*
 * @(#)PropertyView.java	1.13 98/08/28
 */
package org.jbundle.util.apprunner;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

/**
 * Basic Property View Screen.
 */
public class PropertyView extends JPanel
{
	private static final long serialVersionUID = 1344323492l;
	
	protected Properties m_properties = null;
	protected PropertyOwner m_propOwner = null;

	/*
	 * Constructor.
	 */
	public PropertyView()
	{
		super();
	}
	/*
	 * Constructor.
	 */
	public PropertyView(PropertyOwner propOwner, Properties properties)
	{
		this();
		this.init(propOwner, properties);
	}
	/*
	 * Constructor.
	 */
	public void init(PropertyOwner propOwner, Properties properties)
	{
		m_propOwner = propOwner;
		m_properties = properties;
		this.setLayout(new BorderLayout());		// Default
		this.addControlsToView(this);	// Add the controls to this view
		this.propertiesToControls();
		this.addListeners();
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
		if (panel == this)
			if (panel.getComponentCount() == 0)
				panel.add(new Label("No Properties to change for this object"));
	}
	/*
	 * Create a combobox with all the values in this string array.
	 */
	public JComponent makeControlPopup(String[] rgstrValue, String strControl)
	{
		JComboBox comboBox = new JComboBox();
		comboBox.setEditable(true);
		this.addItems(comboBox, rgstrValue, strControl);
		return comboBox;
	}
	/*
	 * Add all the items in this array to the combobox and set the default.
	 */
	public void addItems(JComboBox comboBox, String[] rgstrValue, String strDefault)
	{
		for (int i = 0; i < rgstrValue.length; i++)
		{
			comboBox.addItem(rgstrValue[i]);
			if (rgstrValue[i].equalsIgnoreCase(strDefault))
				strDefault = rgstrValue[i];
		}
		comboBox.setSelectedItem(strDefault);
	}
	/*
	 * Set the properties to the current control values.
	 */
	public void controlsToProperties()
	{	// Override this
	}
	/*
	 * Set the controls to the current property values.
	 */
	public void propertiesToControls()
	{	// Override this
	}
	/*
	 * Get the description of this option panel.
	 */
	public String getDescription()
	{
		return "Default properties";
	}
	/*
	 * Get the owner of this property key.
	 * @return The owner of this property key.
	 */
	public PropertyOwner getPropertyOwner()
	{
		return m_propOwner;
	}
	/*
	 * Utility to create a new panel and add it to this parent panel.
	 */
	public JPanel makeNewPanel(JPanel panel, Object constraints)
	{
		JPanel panelNew = new JPanel();
		panelNew.setLayout(new BorderLayout());
		panel.add(panelNew, constraints);
		return panelNew;
	}
	/*
	 * Add any listeners.
	 * We do this after setting up the control and moving the properties to the controls
	 * to avoid echos.
	 */
	public void addListeners()
	{
	}
	/**
	 * Event handler for changes in the current selection of the Choices.
	 * If a port is open the port can not be changed.
	 * If the choice is unsupported on the platform then the user will
	 * be notified and the settings will revert to their pre-selection
	 * state.
	 */
	public void itemStateChanged(ItemEvent e)
	{
	}
	/*
	 * User pressed a button. Remember to implement ActionListener in the overriding
	 * class and add "this" as an action listener.
	 */
	public void actionPerformed(ActionEvent e)
	{
	}
}
