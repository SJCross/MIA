// TODO: Add controls for all parameter types (hashsets, etc.)
// TODO: Put module list and parameters in scrollable panes
// TODO: If an assigned image/object name is no longer available, flag up the module button in red

package wbif.sjx.ModularImageAnalysis.GUI;

import ij.ImageJ;
import org.apache.commons.io.FilenameUtils;
import org.reflections.Reflections;
import org.w3c.dom.Document;
import wbif.sjx.ModularImageAnalysis.Module.*;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Process.HCExporter;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.security.Key;
import java.util.*;

/**
 * Created by Stephen on 20/05/2017.
 */
public class MainGUI implements ActionListener, FocusListener, MouseListener {
    private static final String addModuleText = "+";
    private static final String removeModuleText = "-";
    private static final String moveModuleUpText = "▲";
    private static final String moveModuleDownText = "▼";
    private static final String startAnalysisText = "✓";
    private static final String stopAnalysisText = "✕";
    private static final String saveAnalysis = "Save";
    private static final String loadAnalysis = "Load";

    private int frameWidth = 1100;
    private int frameHeight = 750;
    private int elementHeight = 30;

    private HCWorkspace testWorkspace = new HCWorkspace(1,null);
    private HCModule activeModule = null;
    private Frame frame = new JFrame();
    private JPanel controlPanel = new JPanel();
    private JPanel modulesPanel = new JPanel();
    private JScrollPane modulesScrollPane = new JScrollPane(modulesPanel);
    private JPanel paramsPanel = new JPanel();
    private JScrollPane paramsScrollPane = new JScrollPane(paramsPanel);
    private JPanel statusPanel = new JPanel();
    private JPopupMenu moduleListMenu = new JPopupMenu();
    private Thread t = null;
    private int lastModuleEval = -1;

    private GUIAnalysis analysis = new GUIAnalysis();
    private HCModuleCollection modules = analysis.modules;
    private String inputFilePath = "";
    private String outputFilePath = "";
    private boolean exportXML = false;
    private boolean exportXLSX = false;

    public static void main(String[] args) throws IllegalAccessException, InstantiationException {
//        new ImageJ();
//        IJ.runMacro("waitForUser");
//
//        ImagePlus ipl = IJ.getImage();
//
//
//        short[] weights = ChamferWeights3D.CITY_BLOCK.getShortWeights();
//        DistanceTransform3DShort distTransform = new DistanceTransform3DShort(weights,false);
//        ImageStack distanceMap = distTransform.distanceMap(ipl.getStack());
//
//        new ImagePlus("dist",distanceMap).show();

//          // Example workflow for loading existing HCAnalysis protocol
//        File input = new File("C:\\Users\\sc13967\\Google Drive\\People\\K\\Abder Kaidi\\2017-03-30 Texture analysis from 3D SIM\\RPE1_gH2AX_53BP1\\2017-06-06 Analysis2.mia");
//        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(input));
//
//        HCAnalysis analysis = (GUIAnalysis) inputStream.readObject();
//        inputStream.close();
//
//        HCWorkspaceCollection workspaces = new HCWorkspaceCollection();
//        HCWorkspace testWorkspace = workspaces.getNewWorkspace(null);
//
//        new ParameterWindow().updateParameters(analysis.modules);
//
//        analysis.execute(testWorkspace);

        new ImageJ();
        new MainGUI();

    }

    public MainGUI() throws InstantiationException, IllegalAccessException {
        frame.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5,5,5,0);
        c.gridx = 0;
        c.gridy = 0;

        // Setting location of panel
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width - frameWidth) / 2, (screenSize.height - frameHeight) / 2);

        // Creating buttons to add and remove modules
        initialiseControlPanel();
        frame.add(controlPanel,c);

        // Initialising the module list
        initialisingModulesPanel();
        c.gridx++;
        frame.add(modulesScrollPane,c);

        // Initialising the parameters panel
        initialiseParametersPanel();
        c.gridx++;
        c.insets = new Insets(5,5,5,5);
        frame.add(paramsScrollPane,c);

        //        // Initialising the status panel
//        initialiseStatusPanel();
//        c.gridx = 0;
//        c.gridy++;
//        c.gridwidth = 3;
//        c.insets = new Insets(0,5,5,5);
//        frame.add(statusPanel,c);

        // Final bits for listeners
        frame.addMouseListener(this);
        frame.setVisible(true);
        frame.pack();

        // Populating the list containing all available modules
        listAvailableModules();
        moduleListMenu.show(frame,0,0);
        moduleListMenu.setVisible(false);

    }

    private void initialiseControlPanel() {
        int buttonSize = 50;

        controlPanel = new JPanel();
        controlPanel.setPreferredSize(new Dimension(buttonSize + 15, frameHeight-50));
        controlPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        controlPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.PAGE_START;

        // Add module button
        JButton addModuleButton = new JButton(addModuleText);
        addModuleButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        addModuleButton.addActionListener(this);
        addModuleButton.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,20));
        addModuleButton.setMargin(new Insets(0,0,0,0));
        addModuleButton.setFocusPainted(false);
        addModuleButton.setName("ControlButton");
        addModuleButton.setMargin(new Insets(0,0,0,0));
        controlPanel.add(addModuleButton, c);

        // Remove module button
        JButton removeModuleButton = new JButton(removeModuleText);
        removeModuleButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        removeModuleButton.addActionListener(this);
        removeModuleButton.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,20));
        removeModuleButton.setMargin(new Insets(0,0,0,0));
        removeModuleButton.setFocusPainted(false);
        removeModuleButton.setName("ControlButton");
        c.gridy++;
        controlPanel.add(removeModuleButton, c);

        // Move module up button
        JButton moveModuleUpButton = new JButton(moveModuleUpText);
        moveModuleUpButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        moveModuleUpButton.addActionListener(this);
        moveModuleUpButton.setFont(new Font(Font.SANS_SERIF,Font.BOLD,16));
        moveModuleUpButton.setMargin(new Insets(0,0,0,0));
        moveModuleUpButton.setFocusPainted(false);
        moveModuleUpButton.setName("ControlButton");
        c.gridy++;
        controlPanel.add(moveModuleUpButton, c);

        // Move module up button
        JButton moveModuleDownButton = new JButton(moveModuleDownText);
        moveModuleDownButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        moveModuleDownButton.addActionListener(this);
        moveModuleDownButton.setFont(new Font(Font.SANS_SERIF,Font.BOLD,16));
        moveModuleDownButton.setMargin(new Insets(0,0,0,0));
        moveModuleDownButton.setFocusPainted(false);
        moveModuleDownButton.setName("ControlButton");
        c.gridy++;
        controlPanel.add(moveModuleDownButton, c);

        // Load analysis protocol button
        JButton loadAnalysisButton = new JButton(loadAnalysis);
        loadAnalysisButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        loadAnalysisButton.addActionListener(this);
        loadAnalysisButton.setFocusPainted(false);
        loadAnalysisButton.setMargin(new Insets(0,0,0,0));
        loadAnalysisButton.setName("ControlButton");
        c.gridy++;
        c.weighty = 1;
        c.anchor = GridBagConstraints.PAGE_END;
        controlPanel.add(loadAnalysisButton, c);

        // Save analysis protocol button
        JButton saveAnalysisButton = new JButton(saveAnalysis);
        saveAnalysisButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        saveAnalysisButton.addActionListener(this);
        saveAnalysisButton.setFocusPainted(false);
        saveAnalysisButton.setMargin(new Insets(0,0,0,0));
        saveAnalysisButton.setName("ControlButton");
        c.gridy++;
        c.weighty = 0;
        controlPanel.add(saveAnalysisButton, c);

        // Start analysis button
        JButton startAnalysisButton = new JButton(startAnalysisText);
        startAnalysisButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        startAnalysisButton.addActionListener(this);
        startAnalysisButton.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,16));
        startAnalysisButton.setMargin(new Insets(0,0,0,0));
        startAnalysisButton.setFocusPainted(false);
        startAnalysisButton.setName("ControlButton");
        c.gridy++;
        controlPanel.add(startAnalysisButton, c);

        // Stop analysis button
        JButton stopAnalysisButton = new JButton(stopAnalysisText);
        stopAnalysisButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        stopAnalysisButton.addActionListener(this);
        stopAnalysisButton.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,16));
        stopAnalysisButton.setMargin(new Insets(0,0,0,0));
        stopAnalysisButton.setFocusPainted(false);
        stopAnalysisButton.setName("ControlButton");
        c.gridy++;
        controlPanel.add(stopAnalysisButton, c);

        controlPanel.validate();
        controlPanel.repaint();

    }

    private void initialisingModulesPanel() {
        int buttonWidth = 300;

        // Initialising the scroll panel
        modulesScrollPane.setPreferredSize(new Dimension(buttonWidth + 15, frameHeight-50));
        modulesScrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        modulesScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        modulesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Initialising the panel for module buttons
        modulesPanel.setLayout(new GridBagLayout());
        modulesPanel.validate();
        modulesPanel.repaint();

        modulesScrollPane.validate();
        modulesScrollPane.repaint();

    }

    private void initialiseParametersPanel() {
        // Initialising the scroll panel
        paramsScrollPane.setPreferredSize(new Dimension(700, frameHeight-50));
        paramsScrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        paramsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        paramsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        paramsPanel.removeAll();

        paramsPanel.setLayout(new GridBagLayout());

        // Adding placeholder text
        JTextField textField = new JTextField("Select a module to edit its parameters");
        textField.setFont(new Font(Font.SANS_SERIF,Font.BOLD,12));
        textField.setBorder(null);
        textField.setEditable(false);
        paramsPanel.add(textField);

        paramsPanel.validate();
        paramsPanel.repaint();

        paramsScrollPane.validate();
        paramsScrollPane.repaint();

    }

    private void initialiseStatusPanel() {
        statusPanel.setPreferredSize(new Dimension(1090,40));
        statusPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        statusPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5,5,5,5);

        JTextField textField = new JTextField();
        textField.setBackground(null);
        textField.setPreferredSize(new Dimension(1070,25));
        textField.setBorder(null);
        textField.setText("Modular image analysis (version "+getClass().getPackage().getImplementationVersion()+")");
        textField.setFont(new Font(Font.SANS_SERIF,Font.BOLD,12));
        statusPanel.add(textField,c);


        OutputStreamTextField outputStreamTextField = new OutputStreamTextField(textField);
        PrintStream printStream = new PrintStream(outputStreamTextField);
        System.setOut(printStream);

    }

    private void updateEvaluationButtons() {
        for (Component component:modulesPanel.getComponents()) {
            if (component.getName().equals("EvalButton")) {
                int idx = modules.indexOf(((EvalButton) component).getModule());

                if (idx == lastModuleEval+1) {
                    component.setForeground(Color.getHSBColor(0.08f,1f,1f));
                } else if (idx <= lastModuleEval) {
                    component.setForeground(Color.getHSBColor(0.27f,1f,0.6f));
                } else {
                    component.setForeground(Color.getHSBColor(0f,1f,0.6f));
                }

            }
        }
    }

    private void populateModuleList() {
        modulesPanel.removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;

        // Adding module buttons
        Iterator<HCModule> iterator = modules.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            HCModule module = iterator.next();

            // Adding the module enabled checkbox
            c.gridx = 0;
            c.gridy++;
            c.weightx = 0;
            c.insets = new Insets(5, 5, 5, 0);
            c.anchor = GridBagConstraints.BASELINE_LEADING;
            ModuleEnabledCheck enabledCheck = new ModuleEnabledCheck(module);
            enabledCheck.addActionListener(this);
            modulesPanel.add(enabledCheck,c);

            // Adding the main module button
            c.gridx++;
            c.weightx = 1;
            c.insets = new Insets(5, 5, 5, 0);
            c.anchor = GridBagConstraints.FIRST_LINE_START;
            ModuleButton button = new ModuleButton(module);
            button.setPreferredSize(new Dimension(-1, elementHeight));
            button.addActionListener(this);
            if (!module.isEnabled()) button.setForeground(Color.GRAY);
            if (activeModule != null) {
                if (module == activeModule) button.setSelected(true);
            }
            modulesPanel.add(button, c);

            // Adding the state/evaluate button
            c.gridx++;
            c.weightx = 0;
            c.insets = new Insets(5, 0, 5, 5);
            c.anchor = GridBagConstraints.FIRST_LINE_END;
            EvalButton evalButton = new EvalButton(module);
            evalButton.setPreferredSize(new Dimension(elementHeight, elementHeight));
            evalButton.addActionListener(this);
            modulesPanel.add(evalButton, c);

            count++;

        }

        // Adding an invisible separator to prevent the checkboxes sitting in the middle of empty space
        c.weighty = 1;
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        JSeparator separator = new JSeparator();
        separator.setPreferredSize(new Dimension(0,0));
        separator.setName("Separator");
        modulesPanel.add(separator,c);

        modulesPanel.validate();
        modulesPanel.repaint();
        modulesScrollPane.validate();
        modulesScrollPane.repaint();

        updateEvaluationButtons();

    }

    private void populateModuleParameters() {
        paramsPanel.removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.FIRST_LINE_START;

        // If the active module is set to null (i.e. we're looking at the analysis options panel) exit this method
        if (activeModule == null) {
            return;
        }

        // If the active module hasn't got parameters enabled, skip it
        if (activeModule.getActiveParameters() != null) {
            Iterator<HCParameter> iterator = activeModule.getActiveParameters().getParameters().values().iterator();
            while (iterator.hasNext()) {
                HCParameter parameter = iterator.next();

                c.gridx = 0;
                c.insets = new Insets(5, 5, 5, 5);
                c.anchor = GridBagConstraints.FIRST_LINE_START;

                JTextField parameterName = new JTextField(parameter.getName());
                parameterName.setPreferredSize(new Dimension(315, elementHeight));
                parameterName.setEditable(false);
                parameterName.setBorder(null);
                paramsPanel.add(parameterName, c);

                JComponent parameterControl = null;

                if (parameter.getType() == HCParameter.INPUT_IMAGE) {
                    // Getting a list of available images
                    ArrayList<HCParameter> images = modules.getParametersMatchingType(HCParameter.OUTPUT_IMAGE,
                            activeModule);

                    parameterControl = new HCNameInputParameter(parameter);
                    for (HCParameter image : images) {
                        ((HCNameInputParameter) parameterControl).addItem(image.getValue());

                    }
                    ((HCNameInputParameter) parameterControl).setSelectedItem(parameter.getValue());
                    parameterControl.addFocusListener(this);
                    parameterControl.setName("InputParameter");

                } else if (parameter.getType() == HCParameter.INPUT_OBJECTS) {
                    // Getting a list of available images
                    ArrayList<HCParameter> images = modules.getParametersMatchingType(HCParameter.OUTPUT_OBJECTS,
                            activeModule);

                    parameterControl = new HCNameInputParameter(parameter);
                    for (HCParameter image : images) {
                        ((HCNameInputParameter) parameterControl).addItem(image.getValue());

                    }
                    ((HCNameInputParameter) parameterControl).setSelectedItem(parameter.getValue());
                    parameterControl.addFocusListener(this);
                    parameterControl.setName("InputParameter");

                } else if (parameter.getType() == HCParameter.INTEGER | parameter.getType() == HCParameter.DOUBLE
                        | parameter.getType() == HCParameter.STRING | parameter.getType() == HCParameter.OUTPUT_IMAGE
                        | parameter.getType() == HCParameter.OUTPUT_OBJECTS) {

                    parameterControl = new TextParameter(parameter);
                    String name = parameter.getValue() == null ? "" : parameter.getValue().toString();
                    ((TextParameter) parameterControl).setText(name);
                    parameterControl.addFocusListener(this);
                    parameterControl.setName("TextParameter");

                } else if (parameter.getType() == HCParameter.BOOLEAN) {
                    parameterControl = new BooleanParameter(parameter);
                    ((BooleanParameter) parameterControl).setSelected(parameter.getValue());
                    ((BooleanParameter) parameterControl).addActionListener(this);
                    parameterControl.setName("BooleanParameter");

                } else if (parameter.getType() == HCParameter.FILE_PATH) {
                    parameterControl = new FileParameter(parameter);
                    ((FileParameter) parameterControl).setText(FilenameUtils.getName(parameter.getValue()));
                    ((FileParameter) parameterControl).addActionListener(this);
                    parameterControl.setName("FileParameter");

                } else if (parameter.getType() == HCParameter.CHOICE_ARRAY) {
                    String[] valueSource = (String[]) parameter.getValueSource();
                    parameterControl = new ChoiceArrayParameter(parameter, valueSource);
                    if (parameter.getValue() != null) {
                        ((ChoiceArrayParameter) parameterControl).setSelectedItem(parameter.getValue());

                    }
                    ((ChoiceArrayParameter) parameterControl).addActionListener(this);

                    parameterControl.setName("ChoiceArrayParameter");

                } else if (parameter.getType() == HCParameter.MEASUREMENT) {
                    HCMeasurementCollection measurements = modules.getMeasurements(activeModule);
                    String[] measurementChoices = measurements.getMeasurementNames((HCName) parameter.getValueSource());
                    Arrays.sort(measurementChoices);

                    parameterControl = new ChoiceArrayParameter(parameter, measurementChoices);
                    if (parameter.getValue() != null) {
                        ((ChoiceArrayParameter) parameterControl).setSelectedItem(parameter.getValue());
                    }
                    ((ChoiceArrayParameter) parameterControl).addActionListener(this);
                    parameterControl.setName("ChoiceArrayParameter");

                } else if (parameter.getType() == HCParameter.CHILD_OBJECTS) {
                    HCRelationshipCollection relationships = modules.getRelationships(activeModule);
                    HCName[] relationshipChoices = relationships.getChildNames((HCName) parameter.getValueSource());
                    parameterControl = new HCNameInputParameter(parameter);
                    if (relationshipChoices != null) {
                        for (HCName relationship : relationshipChoices) {
                            ((HCNameInputParameter) parameterControl).addItem(relationship);

                        }
                        ((HCNameInputParameter) parameterControl).setSelectedItem(parameter.getValue());
                    }
                    ((HCNameInputParameter) parameterControl).addActionListener(this);
                    parameterControl.setName("InputParameter");

                }

                // Adding the input component
                c.gridx++;
                c.weightx=1;
                c.anchor = GridBagConstraints.EAST;
                if (parameterControl != null) {
                    paramsPanel.add(parameterControl, c);
                    parameterControl.setPreferredSize(new Dimension(320, elementHeight));

                }

                // Adding a checkbox to determine if the parameter should be visible to the user
                c.gridx++;
                c.insets = new Insets(5, 0, 5, 5);
                c.anchor = GridBagConstraints.BASELINE_TRAILING;
                VisibleCheck visibleCheck = new VisibleCheck(parameter);
                visibleCheck.addActionListener(this);
                paramsPanel.add(visibleCheck,c);

                c.gridy++;

            }
        }

        // Creating the notes/help field at the bottom of the panel
        JTabbedPane notesHelpPane = new JTabbedPane();
        notesHelpPane.setPreferredSize(new Dimension(-1, elementHeight*3));

        String help = activeModule.getHelp();
        JTextArea helpArea = new JTextArea(help);
        helpArea.setEditable(false);
        notesHelpPane.addTab("Help", null, helpArea);

        String notes = activeModule.getNotes();
        JTextArea notesArea = new JTextArea(notes);
        notesArea.setName("NotesArea");
        notesArea.addFocusListener(this);
        notesHelpPane.addTab("Notes", null, notesArea);

        c.anchor = GridBagConstraints.LAST_LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.weighty = 1;
        c.gridwidth = 3;
        c.insets = new Insets(5, 5, 5, 5);
        paramsPanel.add(notesHelpPane,c);

        paramsPanel.validate();
        paramsPanel.repaint();

        paramsScrollPane.validate();
        paramsScrollPane.repaint();

    }

    private void populateAnalysisParameters() {
        paramsPanel.removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.FIRST_LINE_START;

//        // Getting analysis mode
//        String[] analysisModes = new String[]{"Use image open in ImageJ","Load single image from file","Batch mode"};
//        JComboBox analysisMode = new JComboBox(analysisModes);
//        analysisMode.setName("AnalysisMode");
//        analysisMode.addActionListener(this);
//        paramsPanel.add(analysisMode,c);
//
//        // If loading from ImageJ do nothing
//        if (analysisMode.getSelectedItem().equals("Use image open in ImageJ"));


        // Select file export location
        JTextField exportFileName = new JTextField("Export location");
        exportFileName.setPreferredSize(new Dimension(200, elementHeight));
        exportFileName.setEditable(false);
        exportFileName.setBorder(null);
        c.gridy++;
        paramsPanel.add(exportFileName, c);

        JButton exportFileButton = new JButton(outputFilePath);
        exportFileButton.addActionListener(this);
        exportFileButton.setPreferredSize(new Dimension(200, elementHeight));
        exportFileButton.setName("OutputFilePath");
        c.gridx++;
        c.weightx = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_END;
        paramsPanel.add(exportFileButton, c);

        // Select export type
        JCheckBox xmlCheck = new JCheckBox("Export XML");
        xmlCheck.addActionListener(this);
        xmlCheck.setSelected(exportXML);
        xmlCheck.setName("XMLCheck");
        c.gridx = 0;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.gridy++;
        paramsPanel.add(xmlCheck, c);

        JCheckBox xlsxCheck = new JCheckBox("Export XLSX");
        xlsxCheck.addActionListener(this);
        xlsxCheck.setSelected(exportXLSX);
        xlsxCheck.setName("XLSXCheck");
        c.gridy++;
        c.weighty = 1;
        paramsPanel.add(xlsxCheck, c);

        paramsPanel.validate();
        paramsPanel.repaint();

    }

    private void listAvailableModules() throws IllegalAccessException, InstantiationException {
        // Using Reflections tool to get list of classes extending HCModule
        Reflections.log = null;
        Reflections reflections = new Reflections("wbif.sjx.ModularImageAnalysis");
        Set<Class<? extends HCModule>> availableModules = reflections.getSubTypesOf(HCModule.class);

        // Creating new instances of these classes and adding to ArrayList
        TreeMap<String,ArrayList<HCModule>> availableModulesList = new TreeMap<>();
        for (Class clazz : availableModules) {
            String[] names = clazz.getPackage().getName().split("\\.");
            String pkg = names[names.length-1];

            availableModulesList.putIfAbsent(pkg,new ArrayList<>());
            availableModulesList.get(pkg).add((HCModule) clazz.newInstance());

        }

        // Sorting the ArrayList based on module title
        for (ArrayList<HCModule> modules:availableModulesList.values()) {
            Collections.sort(modules, Comparator.comparing(HCModule::getTitle));
        }

        // Adding the modules to the list
        for (String pkgName:availableModulesList.keySet()) {
            ArrayList<HCModule> modules = availableModulesList.get(pkgName);
            JMenu packageMenu = new JMenu(pkgName);
            packageMenu.setName("ModuleListPackage");
            packageMenu.addMouseListener(this);

            for (HCModule module : modules) {
                PopupMenuItem menuItem = new PopupMenuItem(module);
                menuItem.addActionListener(this);
                menuItem.setName("ModuleName");
                packageMenu.add(menuItem);

            }

            moduleListMenu.add(packageMenu);

        }
    }

    private void addModule() {
        moduleListMenu.setLocation(MouseInfo.getPointerInfo().getLocation());
        moduleListMenu.setVisible(true);

    }

    private void removeModule() {
        if (activeModule != null) {
            // Removing a module resets all the current evaluation
            lastModuleEval = -1;
            testWorkspace = new HCWorkspace(1,null);

            modules.remove(activeModule);
            activeModule = null;

            populateModuleList();
            initialiseParametersPanel();

        }
    }

    private void moveModuleUp() {
        if (activeModule != null) {
            int idx = modules.indexOf(activeModule);
            if (idx != 0) {
                if (idx-1 <= lastModuleEval) lastModuleEval = idx-2;

                modules.remove(activeModule);
                modules.add(idx - 1, activeModule);
                populateModuleList();

            }
        }
    }

    private void moveModuleDown() {
        if (activeModule != null) {
            int idx = modules.indexOf(activeModule);
            if (idx != modules.size()) {
                if (idx <= lastModuleEval) lastModuleEval = idx-1;

                modules.remove(activeModule);
                modules.add(idx + 1, activeModule);
                populateModuleList();
            }
        }
    }

    private void saveAnalysis() throws IOException, ParserConfigurationException, TransformerException {
        FileDialog fileDialog = new FileDialog(new Frame(), "Select file to save", FileDialog.SAVE);
        fileDialog.setMultipleMode(false);
        fileDialog.setVisible(true);

        String outputFileName = fileDialog.getFiles()[0].getAbsolutePath();
        if (!FilenameUtils.getExtension(outputFileName).equals("mia")) {
            outputFileName = FilenameUtils.removeExtension(outputFileName)+".mia";
        }

        // Creating the outputStream
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(outputFileName));

        // Adding the analysis object to the output stream
        outputStream.writeObject(analysis);

        // Adding an XML formatted summary of the modules and their values
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        doc.appendChild(HCExporter.prepareParametersXML(doc,analysis.getModules()));
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(new DOMSource(doc), new StreamResult(outputStream));

        outputStream.close();

        System.out.println("File saved ("+FilenameUtils.getName(outputFileName)+")");

    }

    private void loadAnalysis() throws IOException, ClassNotFoundException {
        FileDialog fileDialog = new FileDialog(new Frame(), "Select file to save", FileDialog.LOAD);
        fileDialog.setMultipleMode(false);
        fileDialog.setFile("*.mia");
        fileDialog.setVisible(true);

        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(fileDialog.getFiles()[0]));

        analysis = (GUIAnalysis) inputStream.readObject();
        inputStream.close();

        modules = analysis.getModules();

        populateModuleList();
        populateModuleParameters();

        System.out.println("File loaded ("+FilenameUtils.getName(fileDialog.getFiles()[0].getName())+")");

    }

    private void startAnalysis() throws IOException {
        // Initialising the testWorkspace
        HCWorkspaceCollection workspaces = new HCWorkspaceCollection();
        HCWorkspace workspace;
        if (!inputFilePath.equals("")) {
            workspace = workspaces.getNewWorkspace(new File(inputFilePath));

        } else {
            workspace = workspaces.getNewWorkspace(null);

        }

        // Running the analysis
        analysis.execute(workspace, true);

        // Exporting XLSX
        if (exportXLSX & !outputFilePath.equals("")) {
            HCExporter exporter = new HCExporter(outputFilePath, HCExporter.XLSX_EXPORT);
            exporter.exportResults(workspaces, analysis);

        }

        // Exporting XML
        if (exportXML & !outputFilePath.equals("")) {
            HCExporter exporter = new HCExporter(outputFilePath, HCExporter.XML_EXPORT);
            exporter.exportResults(workspaces, analysis);

        }

    }

    private void selectModule(HCModule module) {
        // Clearing the previous module
        ModuleButton prevModuleButton = getModuleButton(activeModule);
        if (prevModuleButton != null) prevModuleButton.setSelected(false);

        // Getting new button and setting enabled
        ModuleButton activeModuleButton = getModuleButton(module);
        if (activeModuleButton != null) activeModuleButton.setSelected(true);
        activeModule = module;

        // Updating the displayed parameters
        populateModuleParameters();

    }

    private ModuleButton getModuleButton(HCModule module) {
        for (Component component : modulesPanel.getComponents()) {
            if (component.getName().equals("ModuleButton")) {
                if (((ModuleButton) component).getModule() == module) {
                    return (ModuleButton) component;

                }
            }
        }

        return null;

    }

    private void evaluateModule(HCModule module) {
        module.execute(testWorkspace,true);
        populateModuleList();

    }

    private void reactToAction(Object object)
            throws IllegalAccessException, InstantiationException, IOException, ClassNotFoundException, TransformerException, ParserConfigurationException {

        if (((JComponent) object).getName().equals("ControlButton")) {
            if (((JButton) object).getText().equals(addModuleText)) {
                addModule();

            } else if (((JButton) object).getText().equals(removeModuleText)) {
                removeModule();

            } else if (((JButton) object).getText().equals(moveModuleUpText)) {
                moveModuleUp();

            } else if (((JButton) object).getText().equals(moveModuleDownText)) {
                moveModuleDown();

            } else if (((JButton) object).getText().equals(saveAnalysis)) {
                saveAnalysis();

            } else if (((JButton) object).getText().equals(loadAnalysis)) {
                loadAnalysis();

            } else if (((JButton) object).getText().equals(startAnalysisText)) {
                t = new Thread(() -> {
                    try {
                        startAnalysis();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                t.start();

            } else if (((JButton) object).getText().equals(stopAnalysisText)) {
                System.out.println("Shutting system down");
                analysis.shutdown();

            }

        } else if (((JComponent) object).getName().equals("ModuleName")) {
            moduleListMenu.setVisible(false);

            if (((PopupMenuItem) object).getModule() == null) return;

            // Adding it after the currently-selected module
            HCModule newModule = ((PopupMenuItem) object).getModule().getClass().newInstance();
            if (activeModule != null) {
                int idx = modules.indexOf(activeModule);
                modules.add(++idx,newModule);

            } else {
                modules.add(newModule);

            }

            // Adding to the list of modules
            populateModuleList();

            // Selecting the added module
            selectModule(newModule);

        } else if (((JComponent) object).getName().equals("EvalButton")) {
            HCModule evalModule = ((EvalButton) object).getModule();
            int idx = modules.indexOf(evalModule);
            if (idx<=lastModuleEval+1) {
                lastModuleEval = idx;
                new Thread(() -> evaluateModule(((EvalButton) object).getModule())).start();

            }


        } else if (((JComponent) object).getName().equals("AnalysisOptionsButton")) {
            // Selecting the added module
            selectModule(null);

            populateAnalysisParameters();

        } else if (((JComponent) object).getName().equals("ModuleEnabledCheck")) {
            ((ModuleEnabledCheck) object).getModule().setEnabled(((ModuleEnabledCheck) object).isSelected());
            populateModuleList();
            populateModuleParameters();

        } else if (((JComponent) object).getName().equals("ModuleButton")) {
            selectModule(((ModuleButton) object).getModule());

        } else if (((JComponent) object).getName().equals("InputParameter")) {
            HCParameter parameter = ((HCNameInputParameter) object).getParameter();
            parameter.setValue(((HCNameInputParameter) object).getSelectedItem());
            populateModuleParameters();

            int idx = modules.indexOf(activeModule);
            if (idx <= lastModuleEval) lastModuleEval = idx-1;
            populateModuleList();

        } else if (((JComponent) object).getName().equals("TextParameter")) {
            HCParameter parameter = ((TextParameter) object).getParameter();
            String text = ((TextParameter) object).getText();

            if (parameter.getType() == HCParameter.OUTPUT_IMAGE | parameter.getType() == HCParameter.OUTPUT_OBJECTS) {
                parameter.setValue(new HCName(text));

            } else if (parameter.getType() == HCParameter.INTEGER) {
                parameter.setValue(Integer.valueOf(text));

            } else if (parameter.getType() == HCParameter.DOUBLE) {
                parameter.setValue(Double.valueOf(text));

            } else if (parameter.getType() == HCParameter.STRING) {
                parameter.setValue(text);

            }

            int idx = modules.indexOf(activeModule);
            if (idx <= lastModuleEval) lastModuleEval = idx-1;
            updateEvaluationButtons();

        } else if (((JComponent) object).getName().equals("BooleanParameter")) {
            HCParameter parameter = ((BooleanParameter) object).getParameter();

            parameter.setValue(((BooleanParameter) object).isSelected());
            populateModuleParameters();

            int idx = modules.indexOf(activeModule);
            if (idx <= lastModuleEval) lastModuleEval = idx-1;
            updateEvaluationButtons();

        } else if (((JComponent) object).getName().equals("FileParameter")) {
            HCParameter parameter = ((FileParameter) object).getParameter();

            FileDialog fileDialog = new FileDialog(new Frame(), "Select image to load", FileDialog.LOAD);
            fileDialog.setMultipleMode(false);
            fileDialog.setVisible(true);

            parameter.setValue(fileDialog.getFiles()[0].getAbsolutePath());
            ((FileParameter) object).setText(FilenameUtils.getName(parameter.getValue()));

            int idx = modules.indexOf(activeModule);
            if (idx <= lastModuleEval) lastModuleEval = idx-1;
            updateEvaluationButtons();

        } else if (((JComponent) object).getName().equals("ChoiceArrayParameter")) {
            HCParameter parameter = ((ChoiceArrayParameter) object).getParameter();
            parameter.setValue(((ChoiceArrayParameter) object).getSelectedItem());

            populateModuleParameters();

            int idx = modules.indexOf(activeModule);
            if (idx <= lastModuleEval) lastModuleEval = idx-1;
            updateEvaluationButtons();

        } else if (((JComponent) object).getName().equals("OutputFilePath")) {
            FileDialog fileDialog = new FileDialog(new Frame(), "Select file to save", FileDialog.SAVE);
            fileDialog.setMultipleMode(false);
            fileDialog.setVisible(true);

            outputFilePath = fileDialog.getFiles()[0].getAbsolutePath();
            populateAnalysisParameters();

        } else if (((JComponent) object).getName().equals("VisibleCheck")) {
            HCParameter parameter = ((VisibleCheck) object).getParameter();
            parameter.setVisible(((VisibleCheck) object).isSelected());

        } else if (((JComponent) object).getName().equals("NotesArea")) {
            activeModule.setNotes(((JTextArea) object).getText());

        } else if (((JComponent) object).getName().equals("XMLCheck")) {
            exportXML = ((JCheckBox) object).isSelected();
            populateAnalysisParameters();

        } else if (((JComponent) object).getName().equals("XLSXCheck")) {
            exportXLSX = ((JCheckBox) object).isSelected();
            populateAnalysisParameters();

        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            reactToAction(e.getSource());
        } catch (IllegalAccessException | InstantiationException | IOException | ClassNotFoundException | ParserConfigurationException | TransformerException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        try {
            reactToAction(e.getSource());
        } catch (IllegalAccessException | InstantiationException | IOException | ClassNotFoundException | TransformerException | ParserConfigurationException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        moduleListMenu.setVisible(false);

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getComponent().getName().equals("ModuleListPackage")) {
            // Adding the mouse listener to show the relevant sub-menu
            moduleListMenu.show(frame, e.getX(), e.getY());

        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
