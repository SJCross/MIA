package wbif.sjx.ModularImageAnalysis.GUI.Panels;

import wbif.sjx.ModularImageAnalysis.GUI.ControlObjects.ModuleButton;
import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;

public class InputOutputPanel extends JPanel {
    private ModuleButton button;

    public InputOutputPanel() {
        int basicFrameWidth = GUI.getBasicFrameWidth();
        int bigButtonSize = GUI.getBigButtonSize();

        // Initialising the panel
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        setPreferredSize(new Dimension(basicFrameWidth-45-bigButtonSize, bigButtonSize+15));
        setLayout(new GridBagLayout());

        validate();
        repaint();

    }

    public void updatePanel(Module module) {
        ButtonGroup group = GUI.getModuleGroup();

        removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.PAGE_START;
        c.fill = GridBagConstraints.BOTH;

        button = new ModuleButton(module);
        group.add(button);
        add(button, c);

        revalidate();
        repaint();

    }

    public void updateButtonState() {
        if (button == null) return;
        button.updateState();
    }
}
