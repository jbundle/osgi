package org.jbundle.util.apprunner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Properties;

import javax.swing.JApplet;
import javax.swing.JFrame;

public abstract class AppRunner extends JApplet
	implements PropertyOwner
{
	protected Properties m_properties = null;

	/**
	 * Set the application properties.
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void setProperties(Properties properties)
	{
		m_properties = properties;
	}

	/**
	 * Add this applet to a frame and initialize.
	 * @return
	 */
	public JFrame addAppToFrame()
	{
		JFrame frame = new JFrame();
		frame.setTitle("JBackup");
		frame.setBackground(Color.lightGray);
		frame.getContentPane().setLayout(new BorderLayout());

		frame.getContentPane().add(this, BorderLayout.CENTER);
		frame.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		frame.pack();
		frame.setSize(frame.getPreferredSize().width, frame.getPreferredSize().height);
		return frame;
	}
}
