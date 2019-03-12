package wbif.sjx.ModularImageAnalysis.GUI.Panels;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;

public class StatusPanel extends JPanel {
    public StatusPanel() {
        int statusHeight = GUI.getStatusHeight();
        int basicFrameWidth = GUI.getBasicFrameWidth();

        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        setMinimumSize(new Dimension(0,statusHeight+15));
        setMaximumSize(new Dimension(Integer.MAX_VALUE,statusHeight+15));
        setPreferredSize(new Dimension(basicFrameWidth-30,statusHeight+15));
        setOpaque(false);

    }
}
