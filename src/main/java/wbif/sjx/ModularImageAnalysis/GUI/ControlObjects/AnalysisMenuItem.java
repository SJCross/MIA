package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import ij.IJ;
import org.xml.sax.SAXException;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Process.Analysis;
import wbif.sjx.ModularImageAnalysis.Process.AnalysisReader;
import wbif.sjx.ModularImageAnalysis.Process.AnalysisRunner;
import wbif.sjx.ModularImageAnalysis.Process.AnalysisWriter;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Created by stephen on 28/07/2017.
 */
public class AnalysisMenuItem extends JMenuItem implements ActionListener {
    public static final String LOAD_ANALYSIS = "Load pipeline";
    public static final String SAVE_ANALYSIS = "Save pipeline";
    public static final String START_ANALYSIS = "Run analysis";
    public static final String STOP_ANALYSIS = "Stop analysis";
    public static final String CLEAR_PIPELINE = "Remove all modules";
    public static final String ENABLE_ALL = "Enable all modules";
    public static final String DISABLE_ALL = "Disable all modules";
    public static final String OUTPUT_ALL = "Show output for all modules";
    public static final String SILENCE_ALL = "Hide output for all modules";
    public static final String BASIC_VIEW = "Switch to basic view";
    public static final String EDITING_VIEW = "Switch to editing view";

    public AnalysisMenuItem(String command) {
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        setText(command);
        addActionListener(this);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            switch (getText()) {
                case LOAD_ANALYSIS:
                    Analysis analysis = AnalysisReader.loadAnalysis();

                    if (analysis == null) return;

                    GUI.setAnalysis(analysis);

                    if (GUI.isBasicGUI()) {
                        GUI.populateBasicModules();

                    } else {
                        GUI.populateModuleList();
                        GUI.populateModuleParameters();

                    }

                    GUI.setLastModuleEval(-1);

                    break;

                case SAVE_ANALYSIS:
                    AnalysisWriter.saveAnalysis(GUI.getAnalysis());
                    break;

                case START_ANALYSIS:
                    Thread t = new Thread(() -> {
                        try {
                            AnalysisRunner.startAnalysis(GUI.getAnalysis());
                        } catch (IOException | InterruptedException e1) {
                            e1.printStackTrace();
                        } catch (GenericMIAException e1) {
                            IJ.showMessage(e1.getMessage());
                        }
                    });
                    t.start();
                    break;

                case STOP_ANALYSIS:
                    System.out.println("Shutting system down");
                    AnalysisRunner.stopAnalysis();
                    break;

                case CLEAR_PIPELINE:
                    GUI.getAnalysis().removeAllModules();

                    if (GUI.isBasicGUI()) {
                        GUI.populateBasicModules();

                    } else {
                        GUI.populateModuleList();
                        GUI.populateModuleParameters();

                    }

                    GUI.setLastModuleEval(-1);

                    break;

                case ENABLE_ALL:
                    for (Module module:GUI.getModules()) module.setEnabled(true);
                    GUI.populateModuleList();
                    break;

                case DISABLE_ALL:
                    for (Module module:GUI.getModules()) module.setEnabled(false);
                    GUI.populateModuleList();
                    break;

                case OUTPUT_ALL:
                    for (Module module:GUI.getModules()) module.setShowOutput(true);
                    GUI.populateModuleList();
                    break;

                case SILENCE_ALL:
                    for (Module module:GUI.getModules()) module.setShowOutput(false);
                    GUI.populateModuleList();
                    break;

                case BASIC_VIEW:
                    GUI.renderBasicMode();
                    setText(AnalysisMenuItem.EDITING_VIEW);
                    break;

                case EDITING_VIEW:
                    try {
                        GUI.renderEditingMode();
                    } catch (InstantiationException | IllegalAccessException e1) {
                        e1.printStackTrace();
                    }
                    setText(AnalysisMenuItem.BASIC_VIEW);
                    break;
            }

        } catch (IOException | ClassNotFoundException | ParserConfigurationException | SAXException
                | IllegalAccessException | InstantiationException | TransformerException e1) {
            e1.printStackTrace();
        }
    }
}
