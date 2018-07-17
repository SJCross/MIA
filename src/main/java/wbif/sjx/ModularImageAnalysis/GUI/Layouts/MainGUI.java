// TODO: Add controls for all parameter types (hashsets, etc.)
// TODO: If an assigned image/object name is no longer available, flag up the module button in red
// TODO: Output panel could allow the user to select which objects and images to output to the spreadsheet

package wbif.sjx.ModularImageAnalysis.GUI.Layouts;

import org.reflections.Reflections;
import wbif.sjx.ModularImageAnalysis.GUI.*;
import wbif.sjx.ModularImageAnalysis.GUI.ControlObjects.*;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.InputControl;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.OutputControl;
import wbif.sjx.ModularImageAnalysis.Module.*;
import wbif.sjx.ModularImageAnalysis.Module.Miscellaneous.GUISeparator;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Process.Analysis;
import wbif.sjx.ModularImageAnalysis.Process.BatchProcessor;
import wbif.sjx.common.FileConditions.ExtensionMatchesString;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;

import java.io.*;
import java.util.*;

/**
 * Created by Stephen on 20/05/2017.
 */
public class MainGUI extends GUI {
    private int mainFrameWidth = 1100;
    private int basicFrameWidth = 375;
    private int frameHeight = 750;
    private int elementHeight = 25;
    private int bigButtonSize = 40;
    private int moduleButtonWidth = 300;

    private ComponentFactory componentFactory;
    private static final JFrame frame = new JFrame();
    private static final JMenuBar menuBar = new JMenuBar();
    private static final JMenu viewMenu = new JMenu("View");
    private static final JPanel basicPanel = new JPanel();
    private static final JPanel editingPanel = new JPanel();
    private static final JPanel modulesPanel = new JPanel();
    private static final JScrollPane modulesScrollPane = new JScrollPane(modulesPanel);
    private static final JPanel paramsPanel = new JPanel();
    private static final JScrollPane paramsScrollPane = new JScrollPane(paramsPanel);
    private static final JProgressBar progressBar = new JProgressBar(0,100);
    private static final JPanel basicModulesPanel = new JPanel();
    private static final StatusTextField textField = new StatusTextField();
    private static final JScrollPane basicModulesScrollPane = new JScrollPane(basicModulesPanel);
    private static final JPopupMenu moduleListMenu = new JPopupMenu();
    private static final JPanel basicStatusPanel = new JPanel();
    private static final JPanel editingStatusPanel = new JPanel();
    private static final JLayeredPane statusPanel = new JLayeredPane();
    private boolean basicGUI = true;
    private boolean debugOn = false;

    public MainGUI(boolean debugOn) throws InstantiationException, IllegalAccessException {
        this.debugOn = debugOn;

        analysis.getInputControl().initialiseParameters();

        componentFactory = new ComponentFactory(this, elementHeight);

        // Setting location of panel
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width - mainFrameWidth) / 2, (screenSize.height - frameHeight) / 2);
        frame.setLayout(new GridBagLayout());
        frame.setTitle("MIA (version " + getClass().getPackage().getImplementationVersion() + ")");

        initialiseStatusPanel();

        // Creating the menu bar
        initialiseMenuBar();
        frame.setJMenuBar(menuBar);
        frame.setResizable(false);

        initialiseEditingMode();
        initialiseBasicMode();

        if (debugOn) {
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            renderEditingMode();
        } else {
            renderBasicMode();
        }

        // Final bits for listeners
//        frame.addMouseListener(this);
        frame.setVisible(true);

        // Populating the list containing all available modules
        listAvailableModules();
        moduleListMenu.show(frame, 0, 0);
        moduleListMenu.setVisible(false);

    }

    private void initialiseMenuBar() {
        // Creating the file menu
        JMenu menu = new JMenu("File");
        menuBar.add(menu);
        menu.add(new AnalysisMenuItem(this, AnalysisMenuItem.LOAD_ANALYSIS));
        menu.add(new AnalysisMenuItem(this, AnalysisMenuItem.SAVE_ANALYSIS));

        // Creating the edit menu
        menu = new JMenu("Edit");
        menuBar.add(menu);
        menu.add(new AnalysisMenuItem(this, AnalysisMenuItem.CLEAR_PIPELINE));
        menu.add(new AnalysisMenuItem(this, AnalysisMenuItem.ENABLE_ALL));
        menu.add(new AnalysisMenuItem(this, AnalysisMenuItem.DISABLE_ALL));
        menu.add(new AnalysisMenuItem(this, AnalysisMenuItem.OUTPUT_ALL));
        menu.add(new AnalysisMenuItem(this, AnalysisMenuItem.SILENCE_ALL));

        // Creating the analysis menu
        menu = new JMenu("Analysis");
        menuBar.add(menu);
//        menu.add(new AnalysisMenuItem(this, AnalysisMenuItem.SET_FILE_TO_ANALYSE));
        menu.add(new AnalysisMenuItem(this, AnalysisMenuItem.START_ANALYSIS));
        menu.add(new AnalysisMenuItem(this, AnalysisMenuItem.STOP_ANALYSIS));

        // Creating the new menu
        menuBar.add(viewMenu);
        ButtonGroup group = new ButtonGroup();
        ViewControlButton rbMenuItem = new ViewControlButton(this, ViewControlButton.BASIC_MODE);
        rbMenuItem.setSelected(!debugOn);
        group.add(rbMenuItem);
        viewMenu.add(rbMenuItem);

        rbMenuItem = new ViewControlButton(this, ViewControlButton.EDITING_MODE);
        rbMenuItem.setSelected(debugOn);
        group.add(rbMenuItem);
        viewMenu.add(rbMenuItem);

    }

    public void render() throws IllegalAccessException, InstantiationException {
        if (basicGUI) {
            renderBasicMode();
        } else {
            renderEditingMode();
        }
    }

    public void initialiseBasicMode() {
        basicPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 0, 5);
        c.gridx = 0;
        c.gridy = 0;

        // Initialising the control panel
        basicPanel.add(initialiseBasicControlPanel(), c);

        // Initialising the parameters panel
        initialiseBasicModulesPanel();
        c.gridy++;
        basicPanel.add(basicModulesScrollPane, c);

        // Initialising the status panel
        if (!debugOn) {
            c.gridy++;
            c.insets = new Insets(0,0,0,0);
            basicStatusPanel.setBorder(null);
            basicPanel.add(basicStatusPanel,c);
        }
    }

    public void initialiseEditingMode() {
        editingPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 0);
        c.gridx = 0;
        c.gridy = 0;

        // Creating buttons to add and remove modules
        JPanel controlPanel = initialiseControlPanel();
        c.gridheight = 3;
        editingPanel.add(controlPanel, c);

        // Initialising the status panel
        if (!debugOn) {
            c.gridheight = 1;
            c.gridy++;
            c.gridy++;
            c.gridy++;
            c.gridwidth = 3;
            c.insets = new Insets(0,0,0,0);
            editingStatusPanel.setBorder(null);
            editingPanel.add(editingStatusPanel, c);
        }

        // Initialising the input enable panel
        initialiseInputEnablePanel();
        c.gridy = 0;
        c.gridx++;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 0, 0);
        editingPanel.add(initialiseInputEnablePanel(), c);

        // Initialising the module list panel
        initialisingModulesPanel();
        c.gridy++;
        c.insets = new Insets(5, 5, 5, 0);
        editingPanel.add(modulesScrollPane, c);

        // Initialising the output enable panel
        initialiseOutputEnablePanel();
        c.gridy++;
        c.gridheight = 1;
        c.insets = new Insets(0, 5, 5, 0);
        editingPanel.add(initialiseOutputEnablePanel(), c);

        // Initialising the parameters panel
        initialiseParametersPanel();
        c.gridx++;
        c.gridy = 0;
        c.gridheight = 3;
        c.insets = new Insets(5, 5, 5, 5);
        editingPanel.add(paramsScrollPane, c);

    }

    public void renderBasicMode() {
        basicGUI = true;

        frame.remove(editingPanel);
        frame.add(basicPanel);
        updateStatusPanel(basicFrameWidth);
        basicStatusPanel.add(statusPanel);

        basicPanel.setVisible(true);
        basicPanel.validate();
        basicPanel.repaint();

        frame.pack();
        frame.revalidate();
        frame.repaint();

        populateBasicModules();
        updateTestFile();

    }

    public void renderEditingMode() throws InstantiationException, IllegalAccessException {
        basicGUI = false;

        frame.remove(basicPanel);
        frame.add(editingPanel);
        updateStatusPanel(1080);
        editingStatusPanel.add(statusPanel);

        editingPanel.setVisible(true);
        editingPanel.validate();
        editingPanel.repaint();

        frame.pack();
        frame.revalidate();
        frame.repaint();

        populateModuleList();
        populateModuleParameters();
        updateTestFile();

    }

    private JPanel initialiseControlPanel() {
        JPanel controlPanel = new JPanel();

        controlPanel.setPreferredSize(new Dimension(bigButtonSize + 15, frameHeight - 50));
        controlPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        controlPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.PAGE_START;

        // Add module button
        ModuleControlButton.setButtonSize(bigButtonSize);
        ModuleControlButton addModuleButton = new ModuleControlButton(this, ModuleControlButton.ADD_MODULE);
        addModuleButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        controlPanel.add(addModuleButton, c);

        // Remove module button
        ModuleControlButton removeModuleButton = new ModuleControlButton(this, ModuleControlButton.REMOVE_MODULE);
        removeModuleButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        c.gridy++;
        controlPanel.add(removeModuleButton, c);

        // Move module up button
        ModuleControlButton moveModuleUpButton = new ModuleControlButton(this, ModuleControlButton.MOVE_MODULE_UP);
        moveModuleUpButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        c.gridy++;
        controlPanel.add(moveModuleUpButton, c);

        // Move module down button
        ModuleControlButton moveModuleDownButton = new ModuleControlButton(this, ModuleControlButton.MOVE_MODULE_DOWN);
        moveModuleDownButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        c.gridy++;
        controlPanel.add(moveModuleDownButton, c);

        // Load analysis protocol button
        AnalysisControlButton.setButtonSize(bigButtonSize);
        AnalysisControlButton loadAnalysisButton = new AnalysisControlButton(this, AnalysisControlButton.LOAD_ANALYSIS);
        c.gridy++;
        c.weighty = 1;
        c.anchor = GridBagConstraints.PAGE_END;
        controlPanel.add(loadAnalysisButton, c);

        // Save analysis protocol button
        AnalysisControlButton saveAnalysisButton = new AnalysisControlButton(this, AnalysisControlButton.SAVE_ANALYSIS);
        c.gridy++;
        c.weighty = 0;
        controlPanel.add(saveAnalysisButton, c);

        // Start analysis button
        AnalysisControlButton startAnalysisButton = new AnalysisControlButton(this, AnalysisControlButton.START_ANALYSIS);
        c.gridy++;
        controlPanel.add(startAnalysisButton, c);

        // Stop analysis button
        AnalysisControlButton stopAnalysisButton = new AnalysisControlButton(this, AnalysisControlButton.STOP_ANALYSIS);
        c.gridy++;
        controlPanel.add(stopAnalysisButton, c);

        controlPanel.validate();
        controlPanel.repaint();

        return controlPanel;

    }

    private JPanel initialiseInputEnablePanel() {
        JPanel inputEnablePanel = new JPanel();

        // Initialising the panel
        inputEnablePanel.setPreferredSize(new Dimension(moduleButtonWidth + 15, bigButtonSize + 15));
        inputEnablePanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        inputEnablePanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.PAGE_START;

        ModuleButton inputButton = new ModuleButton(this,analysis.getInputControl());
        inputButton.setPreferredSize(new Dimension(moduleButtonWidth,bigButtonSize));
        inputEnablePanel.add(inputButton, c);

        inputEnablePanel.validate();
        inputEnablePanel.repaint();

        return inputEnablePanel;

    }

    private JPanel initialiseOutputEnablePanel() {
        JPanel outputEnablePanel = new JPanel();

        // Initialising the panel
        outputEnablePanel.setPreferredSize(new Dimension(moduleButtonWidth + 15, bigButtonSize + 15));
        outputEnablePanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        outputEnablePanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.PAGE_START;

        ModuleButton outputButton = new ModuleButton(this,analysis.getOutputControl());
        outputButton.setPreferredSize(new Dimension(moduleButtonWidth,bigButtonSize));
        outputEnablePanel.add(outputButton, c);

        outputEnablePanel.validate();
        outputEnablePanel.repaint();

        return outputEnablePanel;

    }

    private void initialisingModulesPanel() {
        // Initialising the scroll panel
        modulesScrollPane.setPreferredSize(new Dimension(moduleButtonWidth + 15, frameHeight - 2 * bigButtonSize - 90));
        modulesScrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        modulesScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        modulesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        modulesScrollPane.getVerticalScrollBar().setUnitIncrement(10);

        // Initialising the panel for module buttons
        modulesPanel.setLayout(new GridBagLayout());
        modulesPanel.validate();
        modulesPanel.repaint();

        modulesScrollPane.validate();
        modulesScrollPane.repaint();

    }

    private void initialiseParametersPanel() {
        // Initialising the scroll panel
        paramsScrollPane.setPreferredSize(new Dimension(700, frameHeight - 50));
        paramsScrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        paramsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        paramsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        paramsPanel.removeAll();

        paramsPanel.setLayout(new GridBagLayout());

        // Adding placeholder text
        JTextField textField = new JTextField("Select a module to edit its parameters");
        textField.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        textField.setBorder(null);
        textField.setEditable(false);
        paramsPanel.add(textField);

        paramsPanel.validate();
        paramsPanel.repaint();

        paramsScrollPane.validate();
        paramsScrollPane.repaint();

        // Displaying the input controls
        activeModule = analysis.getInputControl();
        updateModules();

    }

    private void initialiseStatusPanel() {
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        progressBar.setString("");
        progressBar.setForeground(new Color(200,200,200));
        statusPanel.add(progressBar,new Integer(1));

        textField.setBackground(null);
        textField.setBorder(null);
        textField.setOpaque(false);
        textField.setText("MIA (version " + getClass().getPackage().getImplementationVersion() + ")");
        textField.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        textField.setEditable(false);
        textField.setToolTipText(textField.getText());
        statusPanel.add(textField, new Integer(2));

//        progressBar.validate();
//        progressBar.repaint();
//        textField.validate();
//        textField.repaint();
//        statusPanel.validate();
//        statusPanel.repaint();

        OutputStreamTextField outputStreamTextField = new OutputStreamTextField(textField);
        PrintStream printStream = new PrintStream(outputStreamTextField);
        System.setOut(printStream);

    }

    private void updateStatusPanel(int width) {
        statusPanel.setPreferredSize(new Dimension(width, 40));
        progressBar.setBounds(0,0,width,40);
        textField.setBounds(12,9,width-20,20);

    }

    private JPanel initialiseBasicControlPanel() {
        JPanel basicControlPanel = new JPanel();
        int buttonSize = 50;

        basicControlPanel.setPreferredSize(new Dimension(basicFrameWidth, bigButtonSize + 15));
        basicControlPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        basicControlPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.FIRST_LINE_START;

        // Load analysis protocol button
        AnalysisControlButton.setButtonSize(bigButtonSize);
        AnalysisControlButton loadAnalysisButton
                = new AnalysisControlButton(this, AnalysisControlButton.LOAD_ANALYSIS);
        c.gridx++;
        c.anchor = GridBagConstraints.PAGE_END;
        basicControlPanel.add(loadAnalysisButton, c);

        // Save analysis protocol button
        AnalysisControlButton saveAnalysisButton
                = new AnalysisControlButton(this, AnalysisControlButton.SAVE_ANALYSIS);
        c.gridx++;
        basicControlPanel.add(saveAnalysisButton, c);

        // Start analysis button
        AnalysisControlButton startAnalysisButton
                = new AnalysisControlButton(this, AnalysisControlButton.START_ANALYSIS);
        c.gridx++;
        c.weightx = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_END;
        basicControlPanel.add(startAnalysisButton, c);

        // Stop analysis button
        AnalysisControlButton stopAnalysisButton
                = new AnalysisControlButton(this, AnalysisControlButton.STOP_ANALYSIS);
        c.gridx++;
        c.weightx = 0;
        basicControlPanel.add(stopAnalysisButton, c);

        basicControlPanel.validate();
        basicControlPanel.repaint();

        return basicControlPanel;

    }

    private void initialiseBasicModulesPanel() {
        int elementWidth = basicFrameWidth;

        // Initialising the scroll panel
        basicModulesScrollPane.setPreferredSize(new Dimension(elementWidth, frameHeight-110));
        Border margin = new EmptyBorder(0,0,0,0);
        Border border = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
        basicModulesScrollPane.setBorder(new CompoundBorder(margin,border));
        basicModulesScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        basicModulesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Initialising the panel for module buttons
        basicModulesPanel.setLayout(new GridBagLayout());
        basicModulesPanel.validate();
        basicModulesPanel.repaint();

        basicModulesScrollPane.validate();
        basicModulesScrollPane.repaint();

    }

    public void populateModuleList() {
        modulesPanel.removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.FIRST_LINE_START;

        ButtonGroup group = new ButtonGroup();

        // Adding module buttons
        ModuleCollection modules = getModules();
        for (int i=0;i<modules.size();i++) {
            Module module = modules.get(i);
            int idx = modules.indexOf(module);
            if (idx == modules.size() - 1) c.weighty = 1;

            JPanel modulePanel = componentFactory
                    .createAdvancedModuleControl(module, group, activeModule, moduleButtonWidth - 25);

            // If this is the final module, add a gap at the bottom
            if (i==modules.size()-1) modulePanel.setBorder(new EmptyBorder(0,0,5,0));

            modulesPanel.add(modulePanel, c);
            c.gridy++;

        }

        modulesPanel.validate();
        modulesPanel.repaint();
        modulesScrollPane.validate();
        modulesScrollPane.repaint();

    }

    public void populateModuleParameters() {
        paramsPanel.removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        c.insets = new Insets(5, 5, 20, 5);
        c.anchor = GridBagConstraints.WEST;

        // If the active module is set to null (i.e. we're looking at the analysis options panel) exit this method
        if (activeModule == null) return;

        boolean isInput = activeModule.getClass().isInstance(new InputControl());
        boolean isOutput = activeModule.getClass().isInstance(new OutputControl());

        JPanel topPanel = componentFactory.createParametersTopRow(activeModule);
        c.gridwidth = 2;
        paramsPanel.add(topPanel,c);

        // If it's an input/output control, get the current version
        if (activeModule.getClass().isInstance(new InputControl())) activeModule = analysis.getInputControl();
        if (activeModule.getClass().isInstance(new OutputControl())) activeModule = analysis.getOutputControl();

        // If the active module hasn't got parameters enabled, skip it
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridwidth = 1;
        c.insets = new Insets(2, 5, 0, 0);
        if (activeModule.updateAndGetParameters() != null) {
            Iterator<Parameter> iterator = activeModule.updateAndGetParameters().values().iterator();
            while (iterator.hasNext()) {
                Parameter parameter = iterator.next();
                c.insets = new Insets(2, 5, 0, 0);
                c.gridx = 0;
                c.gridy++;
                JPanel paramPanel = componentFactory.createParameterControl(parameter, getModules(), activeModule, 630);
                paramsPanel.add(paramPanel, c);

                c.insets = new Insets(2, 0, 0, 0);
                c.gridx++;
                c.weightx=1;
                VisibleCheck visibleCheck = new VisibleCheck(parameter);
                visibleCheck.setPreferredSize(new Dimension(elementHeight,elementHeight));
                paramsPanel.add(visibleCheck, c);

            }
        }

        // If selected, adding the measurement selector for output control
        if (activeModule.getClass().isInstance(new OutputControl())
                && analysis.getOutputControl().isEnabled()
                && (boolean) analysis.getOutputControl().getParameterValue(OutputControl.SELECT_MEASUREMENTS)) {

            LinkedHashSet<Parameter> imageNameParameters = getModules().getParametersMatchingType(Parameter.OUTPUT_IMAGE);
            for (Parameter imageNameParameter:imageNameParameters) {
                String imageName = imageNameParameter.getValue();

                JPanel measurementHeader = componentFactory.createMeasurementHeader(imageName+" (Image)",635);
                c.gridx = 0;
                c.gridy++;
                paramsPanel.add(measurementHeader,c);

                MeasurementReferenceCollection measurementReferences = getModules().getImageMeasurementReferences(imageName);
                // Iterating over the measurements for the current image, adding a control for each
                for (MeasurementReference measurementReference:measurementReferences.values()) {
                    if (!measurementReference.isCalculated()) continue;

                    // Adding measurement control
                    JPanel currentMeasurementPanel = componentFactory.createMeasurementControl(measurementReference,635);
                    c.gridy++;
                    paramsPanel.add(currentMeasurementPanel,c);

                }
            }

            LinkedHashSet<Parameter> objectNameParameters = getModules().getParametersMatchingType(Parameter.OUTPUT_OBJECTS);
            for (Parameter objectNameParameter:objectNameParameters) {
                String objectName = objectNameParameter.getValue();

                JPanel measurementHeader = componentFactory.createMeasurementHeader(objectName+" (Object)",635);
                c.gridx = 0;
                c.gridy++;
                paramsPanel.add(measurementHeader,c);

                MeasurementReferenceCollection measurementReferences = getModules().getObjectMeasurementReferences(objectName);
                // Iterating over the measurements for the current object, adding a control for each
                for (MeasurementReference measurementReference:measurementReferences.values()) {
                    if (!measurementReference.isCalculated()) continue;

                    // Adding measurement control
                    JPanel currentMeasurementPanel = componentFactory.createMeasurementControl(measurementReference,635);
                    c.gridy++;
                    paramsPanel.add(currentMeasurementPanel,c);

                }
            }
        }

        // Creating the notes/help field at the bottom of the panel
        if (!isInput &! isOutput) {
            JTabbedPane notesHelpPane = new JTabbedPane();
            notesHelpPane.setPreferredSize(new Dimension(-1, elementHeight * 3));

            String help = activeModule.getHelp();
            JTextArea helpArea = new JTextArea(help);
            helpArea.setEditable(false);
            notesHelpPane.addTab("Help", null, helpArea);

            String notes = activeModule.getNotes();
            NotesArea notesArea = new NotesArea(this, notes);
            notesHelpPane.addTab("Notes", null, notesArea);

            c.anchor = GridBagConstraints.LAST_LINE_START;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy++;
            c.weighty = 1;
            c.gridwidth = 3;
            c.insets = new Insets(5, 5, 5, 10);
            paramsPanel.add(notesHelpPane, c);

        } else {
            JSeparator separator = new JSeparator();
            separator.setSize(new Dimension(0,15));
            c.weighty = 1;
            paramsPanel.add(separator,c);
        }

        paramsPanel.validate();
        paramsPanel.repaint();

        paramsScrollPane.validate();
        paramsScrollPane.repaint();

    }

    private void updateEvalButtonStates() {
        if (basicGUI) {
            for (Component component : basicModulesScrollPane.getComponents()) {
                if (component.getClass() == EvalButton.class) {
                    ((EvalButton) component).updateColour();

                }
            }
        } else {
            for (Component component : modulesScrollPane.getComponents()) {
                if (component.getClass() == EvalButton.class) {
                    ((EvalButton) component).updateColour();

                }
            }
        }
    }

    public void populateBasicModules() {
        basicModulesPanel.removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0;

        // Check if there are no modules
        if (analysis.modules.size()==0) return;

        // Adding a separator between the input and main modules
        GUISeparator separatorModule = new GUISeparator();
        separatorModule.updateParameterValue(GUISeparator.TITLE,"File loading");
        c.gridy++;
        basicModulesPanel.add(componentFactory.getSeparator(separatorModule,basicFrameWidth-40),c);

        JSeparator separator = new JSeparator();
        separator.setPreferredSize(new Dimension(0,20));
        basicModulesPanel.add(separator,c);

        // Adding input control options
        c.gridy++;
        JPanel inputPanel =
                componentFactory.createBasicModuleControl(analysis.getInputControl(),basicFrameWidth-40);

        if (inputPanel != null) basicModulesPanel.add(inputPanel,c);

        // Adding module buttons
        c.insets = new Insets(0,0,0,0);
        ModuleCollection modules = getModules();
        for (Module module : modules) {
            int idx = modules.indexOf(module);
            if (idx == modules.size() - 1) c.weighty = 1;
            c.gridy++;

            JPanel modulePanel = componentFactory.createBasicModuleControl(module,basicFrameWidth-40);
            if (modulePanel!=null) basicModulesPanel.add(modulePanel,c);

        }

        c.gridy++;
        JPanel outputPanel =
                componentFactory.createBasicModuleControl(analysis.getOutputControl(),basicFrameWidth-40);

        if (outputPanel != null) {
            c.gridy++;
            c.insets = new Insets(0,0,0,0);
            basicModulesPanel.add(outputPanel,c);

        }

        c.gridy++;
        c.weighty = 100;
        basicModulesPanel.add(separator, c);

        basicModulesPanel.validate();
        basicModulesPanel.repaint();
        basicModulesScrollPane.validate();
        basicModulesScrollPane.repaint();

    }

    private void listAvailableModules() throws IllegalAccessException, InstantiationException {
        // Using Reflections tool to get list of classes extending Module
        Reflections.log = null;
        Reflections reflections = new Reflections("wbif.sjx.ModularImageAnalysis");
        Set<Class<? extends Module>> availableModules = reflections.getSubTypesOf(Module.class);

        // Creating an alphabetically-ordered list of all modules
        TreeMap<String,Class> modules = new TreeMap<>();
        for (Class clazz : availableModules) {
            if (clazz != InputControl.class && clazz != OutputControl.class) {
                String[] names = clazz.getPackage().getName().split("\\.");
                StringBuilder stringBuilder = new StringBuilder();
                for (String name:names) stringBuilder.append(name);
                modules.put(stringBuilder.toString()+clazz.getSimpleName(),clazz);
            }
        }

        LinkedHashSet<ModuleListMenu> topList = new LinkedHashSet<>();
        for (Class clazz : modules.values()) {
            // ActiveList starts at the top list
            LinkedHashSet<ModuleListMenu> activeList = topList;
            ModuleListMenu activeItem = null;

            String[] names = clazz.getPackage().getName().split("\\.");
            for (int i=4;i<names.length;i++) {
                boolean found = false;
                for (ModuleListMenu listItemm:activeList) {
                    if (listItemm.getName().equals(names[i])) {
                        activeItem = listItemm;
                        found = true;
                    }
                }

                if (!found) {
                    ModuleListMenu newItem = new ModuleListMenu(this, names[i], new ArrayList<>());
                    newItem.setName(names[i]);
                    activeList.add(newItem);
                    if (activeItem != null) activeItem.add(newItem);
                    activeItem = newItem;
                }

                activeList = activeItem.getChildren();

            }
            activeItem.addMenuItem((Module) clazz.newInstance());
        }

        for (ModuleListMenu listMenu:topList) moduleListMenu.add(listMenu);
    }

    public void addModule() {
        moduleListMenu.setLocation(MouseInfo.getPointerInfo().getLocation());
        moduleListMenu.setVisible(true);

    }

    public void removeModule() {
        if (activeModule != null) {
            ModuleCollection modules = getModules();
            // Removing a module resets all the current evaluation
            int idx = modules.indexOf(activeModule);

            if (idx <= lastModuleEval) lastModuleEval = idx - 1;

            modules.remove(activeModule);
            activeModule = null;

            populateModuleList();
            populateModuleParameters();

        }
    }

    public void moveModuleUp() {
        if (activeModule != null) {
            ModuleCollection modules = getModules();
            int idx = modules.indexOf(activeModule);

            if (idx != 0) {
                if (idx - 2 <= lastModuleEval) lastModuleEval = idx - 2;

                modules.remove(activeModule);
                modules.add(idx - 1, activeModule);
                populateModuleList();

            }
        }
    }

    public void moveModuleDown() {
        if (activeModule != null) {
            ModuleCollection modules = getModules();
            int idx = modules.indexOf(activeModule);

            if (idx != modules.size()) {
                if (idx <= lastModuleEval) lastModuleEval = idx - 1;

                modules.remove(activeModule);
                modules.add(idx + 1, activeModule);
                populateModuleList();
            }
        }
    }

    public JFrame getFrame() {
        return frame;
    }

    public JPopupMenu getModuleListMenu() {
        return moduleListMenu;
    }

    public JMenu getViewMenu() {
        return viewMenu;
    }

    public boolean isBasicGUI() {
        return basicGUI;
    }

    public void setAnalysis(Analysis analysis) {
        this.analysis = analysis;
    }

    public ModuleCollection getModules() {
        return analysis.getModules();
    }

    public static void setProgress(int val) {
        progressBar.setValue(val);
    }

    public static int getProgress() {
        return progressBar.getValue();
    }

    @Override
    public void updateModules() {
        updateEvalButtonStates();
        if (!isBasicGUI()) {
            populateModuleParameters();
        } else {
            populateBasicModules();
        }
        populateModuleList();
    }

    @Override
    public void updateTestFile() {
        // Ensuring the input file specified in the InputControl is active in the test workspace
        InputControl inputControl = getAnalysis().getInputControl();
        String inputMode = inputControl.getParameterValue(InputControl.INPUT_MODE);
        String singleFile = inputControl.getParameterValue(InputControl.SINGLE_FILE_PATH);
        String batchFolder = inputControl.getParameterValue(InputControl.BATCH_FOLDER_PATH);
        String extension = inputControl.getParameterValue(InputControl.FILE_EXTENSION);
        int nThreads = inputControl.getParameterValue(InputControl.NUMBER_OF_THREADS);
        boolean useFilenameFilter1 = inputControl.getParameterValue(InputControl.USE_FILENAME_FILTER_1);
        String filenameFilter1 = inputControl.getParameterValue(InputControl.FILENAME_FILTER_1);
        String filenameFilterType1 = inputControl.getParameterValue(InputControl.FILENAME_FILTER_TYPE_1);
        boolean useFilenameFilter2 = inputControl.getParameterValue(InputControl.USE_FILENAME_FILTER_2);
        String filenameFilter2 = inputControl.getParameterValue(InputControl.FILENAME_FILTER_2);
        String filenameFilterType2 = inputControl.getParameterValue(InputControl.FILENAME_FILTER_TYPE_2);
        boolean useFilenameFilter3 = inputControl.getParameterValue(InputControl.USE_FILENAME_FILTER_3);
        String filenameFilter3 = inputControl.getParameterValue(InputControl.FILENAME_FILTER_3);
        String filenameFilterType3 = inputControl.getParameterValue(InputControl.FILENAME_FILTER_TYPE_3);

        Units.setUnits(inputControl.getParameterValue(InputControl.SPATIAL_UNITS));

        String inputFile = "";
        switch (inputMode) {
            case InputControl.InputModes.SINGLE_FILE:
                inputFile = singleFile;
                break;

            case InputControl.InputModes.BATCH:
                // Initialising BatchProcessor
                if (batchFolder == null) return;

                BatchProcessor batchProcessor = new BatchProcessor(new File(batchFolder));
                batchProcessor.setnThreads(nThreads);

                // Adding extension filter
                batchProcessor.addFileCondition(new ExtensionMatchesString(new String[]{extension}));

                // Adding filename filters
                if (useFilenameFilter1) batchProcessor.addFilenameFilter(filenameFilterType1,filenameFilter1);
                if (useFilenameFilter2) batchProcessor.addFilenameFilter(filenameFilterType2,filenameFilter2);
                if (useFilenameFilter3) batchProcessor.addFilenameFilter(filenameFilterType3,filenameFilter3);

                // Running the analysis
                File nextFile = batchProcessor.getNextValidFileInStructure();
                if (nextFile == null) {
                    inputFile = null;
                } else {
                    inputFile = nextFile.getAbsolutePath();
                }
                break;
        }

        if (inputFile == null) return;

        if (getTestWorkspace().getMetadata().getFile() == null) {
            lastModuleEval = -1;
            setTestWorkspace(new Workspace(1, new File(inputFile),1));
        }

        // If the input path isn't the same assign this new file
        if (!getTestWorkspace().getMetadata().getFile().getAbsolutePath().equals(inputFile)) {
            lastModuleEval = -1;
            setTestWorkspace(new Workspace(1, new File(inputFile),1));

        }

        switch ((String) analysis.getInputControl().getParameterValue(InputControl.SERIES_MODE)) {
            case InputControl.SeriesModes.ALL_SERIES:
                getTestWorkspace().getMetadata().setSeriesNumber(1);
                getTestWorkspace().getMetadata().setSeriesName("");
                break;

            case InputControl.SeriesModes.SINGLE_SERIES:
                int seriesNumber = analysis.getInputControl().getParameterValue(InputControl.SERIES_NUMBER);
                getTestWorkspace().getMetadata().setSeriesNumber(seriesNumber);
                getTestWorkspace().getMetadata().setSeriesName("");
        }
    }
}