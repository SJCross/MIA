package wbif.sjx.MIA.GUI.Panels;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.border.EtchedBorder;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.GUI.ControlObjects.NotesArea;
import wbif.sjx.MIA.Module.Module;

public class NotesPanel extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = -3629896187837314617L;

    public NotesPanel() {
        // Initialising the panel
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        setLayout(new GridBagLayout());

    }

    public void updatePanel() {
        Module activeModule = GUI.getFirstSelectedModule();

        removeAll();

        if (activeModule == null) return;

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.insets = new Insets(5,5,0,5);
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;

        // Adding title to help window
        JLabel notesLabel = new JLabel();
        notesLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        notesLabel.setText("Notes for \""+activeModule.getNickname()+"\"");
        add(notesLabel,c);

        // Adding separator
        JSeparator separator = new JSeparator();
        c.gridy++;
        add(separator,c);

        NotesArea notesArea = new NotesArea(activeModule);
        c.gridy++;
        c.weighty = 1;
        c.insets = new Insets(5,5,5,5);

        JScrollPane jsp = new JScrollPane(notesArea);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.getVerticalScrollBar().setUnitIncrement(10);
        add(jsp,c);

        validate();
        repaint();

    }

    public void showUsageMessage() {
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
                "Click a module title to<br>see an editable notes panel."+
                "<br><br>" +
                "To hide this, go to<br>View > Show notes panel" +
                "</font></center></html>");
        usageMessage.setEditable(false);
        usageMessage.setBackground(null);
        usageMessage.setOpaque(false);
        add(usageMessage);

        revalidate();
        repaint();

    }
}
