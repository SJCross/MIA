package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Created by Stephen on 28/07/2017.
 */
public class NotesArea extends JTextArea implements FocusListener {
    public NotesArea(Module module) {
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        if (module == null) {
            setText("");
        } else {
            setText(module.getNotes());
        }

        setLineWrap(true);
        setWrapStyleWord(true);
        addFocusListener(this);
        setCaretPosition(0);

    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        GUI.getActiveModule().setNotes(getText());
    }
}
