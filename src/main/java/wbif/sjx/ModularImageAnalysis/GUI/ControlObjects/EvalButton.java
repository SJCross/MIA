package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import ij.IJ;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.InputControl;
import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;
import wbif.sjx.ModularImageAnalysis.Process.BatchProcessor;
import wbif.sjx.common.FileConditions.ExtensionMatchesString;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Created by Stephen on 08/06/2017.
 */
public class EvalButton extends JButton implements ActionListener {
    private GUI gui;
    private Module module;


    // CONSTRUCTOR

    public EvalButton(GUI gui, Module module) {
        this.gui = gui;
        this.module = module;

        setFocusPainted(false);
        setSelected(false);
        setMargin(new Insets(0,0,0,0));
        setName("EvalButton");
        setText("⇩");
        setFont(new Font(Font.SERIF,Font.BOLD,14));
//        setEnabled(module.isEnabled());
        addActionListener(this);
        updateColour();

    }

    public void updateColour() {
        Color color;
        int idx = gui.getModules().indexOf(module);
        if (!module.isEnabled()) {
            color = Color.LIGHT_GRAY;
        } else {
            if (idx <= gui.getLastModuleEval()) {
                color = Color.getHSBColor(0.27f,1f,0.6f);
            } else {
                color = Color.getHSBColor(0f,1f,0.6f);
            }
        }

        setForeground(color);

    }


    // GETTERS

    public Module getModule() {
        return module;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int idx = gui.getModules().indexOf(module);

        // If the module is ready to be evaluated
        if (idx <= gui.getLastModuleEval()) new Thread(() -> {
            try {
                gui.evaluateModule(module);
            } catch (GenericMIAException ex) {
                IJ.showMessage(ex.getMessage());
            }
        }).start();

        // If multiple modules will need to be evaluated first
        new Thread(() -> {
            for (int i = gui.getLastModuleEval()+1;i<=idx;i++) {
                Module module = gui.getModules().get(i);
                if (module.isEnabled()) try {
                    gui.evaluateModule(module);
                } catch (GenericMIAException ex) {
                    IJ.showMessage(ex.getMessage());
                }

            }
        }).start();
    }
}
