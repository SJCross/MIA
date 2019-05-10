package wbif.sjx.MIA.GUI.ControlObjects;

import org.xml.sax.SAXException;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Process.AnalysisHandling.*;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by steph on 28/07/2017.
 */
public class AnalysisControlButton extends JButton implements ActionListener {
    public static final String LOAD_ANALYSIS = "Load";
    public static final String SAVE_ANALYSIS = "Save";
    public static final String START_ANALYSIS = "Run";
    public static final String STOP_ANALYSIS = "Stop";


    public AnalysisControlButton(String command, int buttonSize) {
        addActionListener(this);
        setFocusPainted(false);
        setMargin(new Insets(0,0,0,0));
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        setText(command);
        setPreferredSize(new Dimension(buttonSize, buttonSize));

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            switch (getText()) {
                case LOAD_ANALYSIS:
                    Analysis newAnalysis = AnalysisReader.loadAnalysis();
                    if (newAnalysis == null) return;
                    GUI.setAnalysis(newAnalysis);
                    GUI.populateModuleList();
                    GUI.populateModuleParameters();
                    GUI.populateHelpNotes();

                    GUI.setLastModuleEval(-1);
                    GUI.updateTestFile();
                    GUI.updateModules();
                    GUI.updateModuleStates(true);

                    break;

                case SAVE_ANALYSIS:
                    AnalysisWriter.saveAnalysis(GUI.getAnalysis());
                    break;

                case START_ANALYSIS:
                    Thread t = new Thread(() -> {
                        try {
                            AnalysisRunner.startAnalysis(GUI.getAnalysis());
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    });
                    t.start();
                    break;

                case STOP_ANALYSIS:
                    System.out.println("Shutting system down");
                    AnalysisRunner.stopAnalysis();
                    break;
            }

        } catch (IOException | ClassNotFoundException | ParserConfigurationException | SAXException
                | IllegalAccessException | InstantiationException | TransformerException | NoSuchMethodException
                | InvocationTargetException e1) {
            e1.printStackTrace();
        }
    }
}
