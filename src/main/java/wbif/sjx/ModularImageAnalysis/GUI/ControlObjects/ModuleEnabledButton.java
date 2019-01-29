package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Miscellaneous.GUISeparator;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.ModuleCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by sc13967 on 07/06/2017.
 */
public class ModuleEnabledButton extends JButton implements ActionListener {
    private Module module;
    private static final ImageIcon blackIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/power_black_12px.png"), "");
    private static final ImageIcon redIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/power_red_12px.png"), "");
    private static final ImageIcon greenIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/power_brightgreen_12px.png"), "");

    public ModuleEnabledButton(Module module) {
        this.module = module;

        setFocusPainted(false);
        setSelected(false);
        setMargin(new Insets(0,0,0,0));
        setName("ModuleEnabled");
        setToolTipText("Enable/disable module");
        updateState();

        addActionListener(this);

    }

    public void updateState() {
        System.out.println(module.getNickname()+"_"+module.isEnabled()+"_"+module.isRunnable());
        if (module.isEnabled() && module.isRunnable()) setIcon(greenIcon);
        else if (module.isEnabled() &! module.isRunnable()) setIcon(redIcon);
        else setIcon(blackIcon);
    }

    public Module getModule() {
        return module;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Invert state
        module.setEnabled(!module.isEnabled());

        updateState();

        int idx = GUI.getModules().indexOf(module);
        if (idx <= GUI.getLastModuleEval()) GUI.setLastModuleEval(idx-1);

        // If this is a GUISeparator module, disable all modules after it, until the next separator
        if (module.getClass().isInstance(new GUISeparator())) {
            ModuleCollection modules = GUI.getModules();
            for (int i=idx+1;i<modules.size();i++) {
                Module currentModule = modules.get(i);
                if (currentModule.getClass().isInstance(new GUISeparator())) {
                    break;
                } else {
                    currentModule.setEnabled(module.isEnabled());
                }
            }
        }

        GUI.updateModules(true);

    }
}
