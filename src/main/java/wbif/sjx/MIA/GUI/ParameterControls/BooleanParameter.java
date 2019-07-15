package wbif.sjx.MIA.GUI.ParameterControls;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Object.Parameters.Abstract.BooleanType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Stephen on 20/05/2017.
 */
public class BooleanParameter extends ParameterControl implements ActionListener {
    private BooleanType parameter;
    private JCheckBox control;

    public BooleanParameter(BooleanType parameter) {
        this.parameter = parameter;

        control = new JCheckBox();

        control.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        control.setSelected(parameter.isSelected());
        control.addActionListener(this);
        control.setOpaque(false);

    }

    public BooleanType getParameter() {
        return parameter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.addUndo();

        parameter.setSelected(control.isSelected());

        int idx = GUI.getModules().indexOf(parameter.getModule());
        if (idx <= GUI.getLastModuleEval()) GUI.setLastModuleEval(idx-1);

        GUI.updateModuleStates(true);
        GUI.updateModules();
        GUI.updateParameters();

        updateControl();

    }
    @Override
    public JComponent getComponent() {
        return control;
    }

    @Override
    public void updateControl() {
        control.setSelected(parameter.isSelected());
    }
}
