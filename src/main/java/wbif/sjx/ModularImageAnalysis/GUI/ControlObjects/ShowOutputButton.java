package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import wbif.sjx.ModularImageAnalysis.Module.Module;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by sc13967 on 07/06/2017.
 */
public class ShowOutputButton extends JButton implements ActionListener {
    private Module module;
    private boolean state = true;
    private static final ImageIcon blackClosedIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/eyeclosed_black_12px.png"), "");
    private static final ImageIcon blackOpenIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/eyeopen_black_12px.png"), "");
    private static final ImageIcon greyClosedIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/eyeclosed_grey_12px.png"), "");
    private static final ImageIcon greyOpenIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/eyeopen_grey_12px.png"), "");


    public ShowOutputButton(Module module) {
        this.module = module;

        state = module.canShowOutput();

        addActionListener(this);
        setFocusPainted(false);
        setSelected(false);
        setMargin(new Insets(0,0,0,0));
        setName("Show output");
        setToolTipText("Show output from module");
        setIcon();

    }

    public void setIcon() {
        if (state && module.isEnabled()) setIcon(blackOpenIcon);
        else if (state &! module.isEnabled()) setIcon(greyOpenIcon);
        else if (!state && module.isEnabled()) setIcon(blackClosedIcon);
        else if (!state &! module.isEnabled()) setIcon(greyClosedIcon);
    }

    public Module getModule() {
        return module;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Invert state
        state = !state;

        setIcon();
        module.setShowOutput(state);

    }
}
