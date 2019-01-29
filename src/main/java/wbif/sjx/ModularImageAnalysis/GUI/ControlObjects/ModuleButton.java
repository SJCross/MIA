package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Stephen on 20/05/2017.
 */
public class ModuleButton extends JToggleButton implements ActionListener {
    private Module module;


    // CONSTRUCTOR

    public ModuleButton(Module module) {
        this.module = module;
        setFocusPainted(false);
        setSelected(false);
        addActionListener(this);
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        setText(module.getNickname());
        updateState();
    }


    // PUBLIC METHODS

    public void updateState() {
        if (module.isEnabled() && module.isRunnable()) {
            setForeground(Color.BLACK);
        } else if (module.isEnabled() &! module.isRunnable()) {
            setForeground(Color.RED);
        } else {
            setForeground(Color.GRAY);
        }
    }


    // GETTERS

    public Module getModule() {
        return module;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.setActiveModule(module);
        GUI.updateModules(false);
    }
}
