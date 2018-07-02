package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.MainGUI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by steph on 28/07/2017.
 */
public class ViewControlButton extends JRadioButton implements ActionListener {
    public static final String BASIC_MODE = "Basic mode";
    public static final String EDITING_MODE = "Editing mode";

    private MainGUI gui;

    public ViewControlButton(MainGUI gui, String command) {
        this.gui = gui;

        setFocusPainted(false);
        setText(command);
        setSelected(true);
        addActionListener(this);


    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (getText()) {
            case BASIC_MODE:
                gui.renderBasicMode();
                break;

            case EDITING_MODE:
                try {
                    gui.renderEditingMode();
                } catch (InstantiationException | IllegalAccessException e1) {
                    e1.printStackTrace();
                }
                break;
        }

        gui.getViewMenu().setSelected(false);
        gui.getViewMenu().setPopupMenuVisible(false);
    }
}
