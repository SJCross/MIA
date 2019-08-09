package wbif.sjx.MIA.GUI.ParameterControls;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Miscellaneous.GlobalVariables;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.TextType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Created by Stephen on 20/05/2017.
 */
public class TextParameter extends ParameterControl implements FocusListener {
    protected TextType parameter;
    protected JTextField control;

    public TextParameter(TextType parameter) {
        this.parameter = parameter;

        control = new JTextField();

        control.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        control.setText(parameter.getRawStringValue());
        control.addFocusListener(this);

    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        GUI.addUndo();

        parameter.setValueFromString(control.getText());

        int idx = GUI.getModules().indexOf(parameter.getModule());
        if (idx <= GUI.getLastModuleEval()) GUI.setLastModuleEval(idx-1);

        // If this module is a GlobalVariable, update the GlobalVariables
        if (parameter.getModule() instanceof GlobalVariables) {
            System.out.println("Before "+GlobalVariables.count());
            GlobalVariables.resetCollection();
            System.out.println("Reset "+GlobalVariables.count());
            for (Module module:parameter.getModule().getModules().values()) module.updateAndGetParameters();
            System.out.println("After "+GlobalVariables.count());
        }

        updateControl();

        GUI.updateModuleStates(true);

    }

    @Override
    public JComponent getComponent() {
        return control;
    }

    @Override
    public void updateControl() {
        control.setText(parameter.getRawStringValue());
    }
}
