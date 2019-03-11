package wbif.sjx.ModularImageAnalysis.GUI.Panels;

import wbif.sjx.ModularImageAnalysis.GUI.ControlObjects.HelpArea;
import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;

public class HelpPanel extends JPanel {
    public HelpPanel() {
        int basicFrameWidth = GUI.getBasicFrameWidth();
        int bigButtonSize = GUI.getBigButtonSize();

        // Initialising the panel
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        setPreferredSize(new Dimension(basicFrameWidth-45-bigButtonSize, bigButtonSize+15));
        setLayout(new GridBagLayout());

    }

    public void updatePanel() {
        Module activeModule = GUI.getActiveModule();

        removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.insets = new Insets(5,5,0,5);
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;

        // Adding title to help window
        JLabel helpLabel = new JLabel();
        helpLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        if (activeModule != null) helpLabel.setText("About \""+activeModule.getTitle()+"\"");
        add(helpLabel,c);

        // Adding separator
        JSeparator separator = new JSeparator();
        c.gridy++;
        add(separator,c);

        // If no Module is selected, also skip
        HelpArea helpArea = new HelpArea(activeModule);

        JScrollPane jsp = new JScrollPane(helpArea);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.getVerticalScrollBar().setUnitIncrement(10);
        jsp.setBorder(null);
        c.gridy++;
        c.weighty = 1;
        c.insets = new Insets(5,5,5,5);
        add(jsp,c);

        revalidate();
        repaint();

    }

    public void showUsageMessage() {
        Module activeModule = GUI.getActiveModule();

        removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;

        // Adding title to help window
        JTextPane usageMessage = new JTextPane();
        usageMessage.setContentType("text/html");
        usageMessage.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        usageMessage.setText("<html><center><font face=\"sans-serif\" size=\"3\">" +
                "Click a module title to<br>see help and notes about it"+
                "<br><br>" +
                "To hide this, go to<br>View > Toggle help and notes panel" +
                "</font></center></html>");
        usageMessage.setEditable(false);
        usageMessage.setBackground(null);
        add(usageMessage);

        revalidate();
        repaint();

    }

    private static String getHelpText(Module module) {
        StringBuilder sb = new StringBuilder();

        sb.append("<b>DESCRIPTION</b><br>")
                .append(module.getHelp())
                .append("<br><br>")
                .append("<b>PARAMETERS</b><br>");

        for (Parameter parameter:module.getAllParameters()) {
            sb.append("<i>")
                    .append(parameter.getName())
                    .append("</i>: ")
                    .append(parameter.getDescription())
                    .append("<br><br>");
        }

        return sb.toString();

    }
}
