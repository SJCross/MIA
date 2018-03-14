package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by sc13967 on 07/06/2017.
 */
public class ModuleEnabledCheck extends JCheckBox implements ActionListener {
    private GUI gui;
    private Module module;

    public ModuleEnabledCheck(GUI gui, Module module) {
        this.gui = gui;
        this.module = module;

        setSelected(module.isEnabled());
        setName("ModuleEnabledCheck");
        setEnabled(module.canBeDisabled());

        addActionListener(this);

    }

    public Module getModule() {
        return module;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        module.setEnabled(isSelected());

        int idx = gui.getModules().indexOf(module);
        if (idx <= gui.getLastModuleEval()) {
            gui.setLastModuleEval(idx-1);
        }

        gui.updateModules();
    }
}
