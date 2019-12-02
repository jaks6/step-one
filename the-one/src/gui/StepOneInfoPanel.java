//package gui;
//
//import core.DTNHost;
//
//import javax.swing.*;
//import java.awt.event.ActionEvent;
//
//public class StepOneInfoPanel extends InfoPanel {
//    private JButton processInfoButton;
//    private DTNHost selectedHost;
//
//    public StepOneInfoPanel(DTNSimGUI gui) {
//        super(gui);
//    }
//
//    /**
//     * Show information about a host
//     *
//     * @param host Host to show the information of
//     */
//    @Override
//    public void showInfo(DTNHost host) {
//        super.showInfo(host);
//        processInfoButton = new JButton("processes info");
//        processInfoButton.addActionListener(this);
//        this.add(processInfoButton);
//        this.selectedHost = host;
//
//
//    }
//
//
//
//    @Override
//    public void actionPerformed(ActionEvent e) {
//        super.actionPerformed(e);
//
//        if (e.getSource() ==  this.processInfoButton) {
//            new ProcessInfoWindow(this.selectedHost);
//        }
//    }
//
//}
