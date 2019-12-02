package ee.mass.epm.stepone.gui;


import applications.spe.RoadMonitoringApp;
import core.Coord;
import core.SimClock;
import core.World;
import gui.DTNSimGUI;
import gui.EventLogPanel;
import gui.GUIControls;
import gui.MainWindow;
import gui.playfield.PlayField;
import org.apache.commons.lang3.reflect.FieldUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;

/**
 * Graphical User Interface for simulator
 */
public class StepOneDTNSimGUI extends DTNSimGUI {

    PlayField _field;
    private GUIControls _guiControls;


    private void startGUI() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    initGUI();
                } catch (AssertionError e) {
                    processAssertionError(e);
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @Override
    protected void runSim() {
        double simTime = SimClock.getTime();
        double endTime = scen.getEndTime();

        startGUI();

        // Startup DTN2Manager
        // XXX: Would be nice if this wasn't needed..
        // DTN2Manager.setup(world);

        while (simTime < endTime && !simCancelled){
            if (_guiControls.isPaused()) {

                wait(10); // release CPU resources when paused

            }
            else {
                try {
                    world.update();
                } catch (AssertionError e) {
                    // handles both assertion errors and SimErrors
                    processAssertionError(e);
                }
                simTime = SimClock.getTime();
            }
            this.update(false);
        }

        simDone = true;
        done();
        this.update(true); // force final GUI update

        if (!simCancelled) { // NOT cancelled -> leave the GUI running
            JOptionPane.showMessageDialog(getParentFrame(),
                    "Simulation done");
        }
        else { // was cancelled -> exit immediately
            System.exit(0);
        }
    }

    private class StepOnePlayField extends PlayField {
        public StepOnePlayField(World w, DTNSimGUI gui) {
            super(w, gui);
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            RoadMonitoringApp.drawSegments((Graphics2D)g);
        }
    }

    /**
     * Initializes the GUI
     */
    private void initGUI() {
        try {
            // this.field = new PlayField(world, this);
            World w = (World) FieldUtils.readField(this, "world", true);
            _field = new StepOnePlayField(w, this); //TODO: remove so normal PlayField is used
            FieldUtils.writeField(this, "field", _field, true);

            //this.field.addMouseListener(new PlayfieldMouseHandler());
            // this.field.addMouseWheelListener(new PlayfieldMouseHandler());
            _field.addMouseListener(new PlayfieldMouseHandler());
            _field.addMouseWheelListener(new PlayfieldMouseHandler());

            // this.guiControls = new GUIControls(this,this.field);
            _guiControls = new GUIControls(this, _field);
            FieldUtils.writeField(this, "guiControls", _guiControls, true);

            //        this.eventLogPanel = new EventLogPanel(this);
            EventLogPanel eventLogPanel = new EventLogPanel(this);
            FieldUtils.writeField(this, "eventLogPanel", eventLogPanel, true);

            //        this.infoPanel = new InfoPanel(this);
            StepOneInfoPanel stepOneInfoPanel = new StepOneInfoPanel(this);
            FieldUtils.writeField(this, "infoPanel", stepOneInfoPanel, true);

            //        this.main = new MainWindow(this.scen.getName(), world, field,
            //                guiControls, infoPanel, eventLogPanel, this);
            MainWindow mainWindow = new MainWindow(this.scen.getName(), w, _field, _guiControls, stepOneInfoPanel, eventLogPanel, this);
            FieldUtils.writeField(this, "main", mainWindow, true);

            scen.addMessageListener(eventLogPanel);
            scen.addConnectionListener(eventLogPanel);
            scen.addApplicationListener(eventLogPanel);

            if (scen.getMap() != null ) {
                _field.setMap(scen.getMap());
            }

            // if user closes the main window, call closeSim()
            mainWindow.addWindowListener(new WindowAdapter() {
                private boolean closeAgain = false;
                public void windowClosing(WindowEvent e)  {
                    closeSim();
                    if (closeAgain) {
                        // if method is called again, force closing
                        System.err.println("Forced close. "+
                                "Some reports may have not been finalized.");
                        System.exit(-1);
                    }
                    closeAgain = true;
                }
            });

            mainWindow.setVisible(true);

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


    }

    /**
     * Handler for playfield's mouse clicks.
     */
    private class PlayfieldMouseHandler extends MouseAdapter implements
            MouseWheelListener {
        /**
         * If mouse button is clicked, centers view at that location.
         */
        public void mouseClicked(MouseEvent e) {

            java.awt.Point p = e.getPoint();
            centerViewAt(_field.getWorldPosition(new Coord(p.x, p.y)));
        }

        public void mouseWheelMoved(java.awt.event.MouseWheelEvent e) {
            _guiControls.changeZoom(e.getWheelRotation());
        }
    }

    /**
     * Suspend thread for ms milliseconds
     * @param ms The nrof milliseconds to wait
     */
    private void wait(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // nothing to do here
        }
    }




}
