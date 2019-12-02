/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package ee.mass.epm.stepone.gui;

import core.DTNHost;
import gui.DTNSimGUI;
import gui.InfoPanel;
import org.apache.commons.lang3.reflect.FieldUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Information panel that shows data of selected messages and nodes.
 */
public class StepOneInfoPanel extends InfoPanel implements ActionListener{

	private JButton processInfoButton;

	public StepOneInfoPanel(DTNSimGUI gui) {
		super(gui);
//		reset();
	}

	/**
	 * Show information about a host
	 * @param host Host to show the information of
	 */
	@Override
	public void showInfo(DTNHost host) {
		super.showInfo(host);

		processInfoButton = new JButton("processes info");
		processInfoButton.addActionListener(this);

		this.add(processInfoButton);
		this.revalidate();
	}



	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);

		if (e.getSource() ==  this.processInfoButton) {
			DTNHost selectedHost = null;
			try {
				selectedHost = (DTNHost) FieldUtils.readField(this, "selectedHost", true);
				new ProcessInfoWindow(selectedHost);
			} catch (IllegalAccessException ex) {
				ex.printStackTrace();
			}
		}
	}

}
