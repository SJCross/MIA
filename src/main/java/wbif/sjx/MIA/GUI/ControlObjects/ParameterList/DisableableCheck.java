package wbif.sjx.MIA.GUI.ControlObjects.ParameterList;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Module;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DisableableCheck extends JCheckBox implements ActionListener {
    private Module module;

    public DisableableCheck(Module module) {
        this.module = module;

        this.setSelected(module.canBeDisabled());
        setText("Can be disabled  ");
        addActionListener(this);

    }

    public Module getModule() {
        return module;
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.addUndo();

        module.setCanBeDisabled(isSelected());

    }
}
