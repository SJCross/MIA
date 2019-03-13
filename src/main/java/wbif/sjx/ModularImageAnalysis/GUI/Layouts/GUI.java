// TODO: Add controls for all parameter types (hashsets, etc.)
// TODO: If an assigned image/object name is no longer available, flag up the module button in red
// TODO: Output panel could allow the user to select which objects and images to output to the spreadsheet

package wbif.sjx.ModularImageAnalysis.GUI.Layouts;

import ij.Prefs;
import org.apache.commons.io.output.TeeOutputStream;
import wbif.sjx.ModularImageAnalysis.GUI.ComponentFactory;
import wbif.sjx.ModularImageAnalysis.GUI.ControlObjects.*;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.InputControl;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.OutputControl;
import wbif.sjx.ModularImageAnalysis.GUI.Panels.*;
import wbif.sjx.ModularImageAnalysis.MIA;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;
import wbif.sjx.ModularImageAnalysis.Process.AnalysisHandling.Analysis;
import wbif.sjx.ModularImageAnalysis.Process.AnalysisHandling.AnalysisTester;
import wbif.sjx.ModularImageAnalysis.Process.BatchProcessor;
import wbif.sjx.ModularImageAnalysis.Process.ClassHunter;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Stephen on 20/05/2017.
 */
public class GUI {
    private static Analysis analysis = new Analysis();
    private static Module activeModule = null;
    private static Module lastEditingHelpNotesModule = null;
    private static Module lastBasicHelpNotesModule = null;
    private static int lastModuleEval = -1;
    private static int moduleBeingEval = -1;
    private static Workspace testWorkspace = new Workspace(1, null,1);
    private static final MeasurementRef globalMeasurementRef = new MeasurementRef("Global");

    private static int editingFrameWidth = 1100;
    private static int minimumEditingFrameWidth = 800;
    private static int basicFrameWidth = 400;
    private static int minimumFrameHeight = 600;
    private static int frameHeight = 800;
    private static int elementHeight = 26;
    private static int bigButtonSize = 45;
    private static int moduleButtonWidth = 295;
    private static int statusHeight = 20;

    private static boolean initialised = false;
    private static boolean basicGUI = true;
    private static boolean showEditingHelpNotes = Prefs.get("MIA.showEditingHelpNotes",true);
    private static boolean showBasicHelpNotes = Prefs.get("MIA.showBasicHelpNotes",true);

    private static ComponentFactory componentFactory = new ComponentFactory(elementHeight);
    private static final JFrame frame = new JFrame();
    private static final JMenuBar menuBar = new JMenuBar();
    private static final ButtonGroup moduleGroup = new ButtonGroup();
    private static final JPopupMenu moduleListMenu = new JPopupMenu();
    private static final StatusTextField textField = new StatusTextField();

    private static final JPanel basicPanel = new JPanel();
    private static final JPanel editingPanel = new JPanel();

    private static final StatusPanel basicStatusPanel = new StatusPanel();
    private static final BasicControlPanel basicControlPanel = new BasicControlPanel();
    private static final ProgressBarPanel editingProgressBarPanel = new ProgressBarPanel();
    private static final ProgressBarPanel basicProgressBarPanel = new ProgressBarPanel();
    private static final InputOutputPanel editingInputPanel = new InputOutputPanel();
    private static final InputOutputPanel editingOutputPanel = new InputOutputPanel();
    private static final ModulesPanel editingModulesPanel = new ModulesPanel();
    private static final ParametersPanel editingParametersPanel = new ParametersPanel();
    private static final JPanel basicHelpNotesPanel = new JPanel();
    private static final JPanel helpNotesPanel = new JPanel();
    private static final HelpPanel editingHelpPanel = new HelpPanel();
    private static final HelpPanel basicHelpPanel = new HelpPanel();
    private static final NotesPanel editingNotesPanel = new NotesPanel();
    private static final NotesPanel basicNotesPanel = new NotesPanel();
    private static final StatusPanel statusPanel = new StatusPanel();
    private static final ModuleControlButton addModuleButton = new ModuleControlButton(ModuleControlButton.ADD_MODULE,bigButtonSize);


    public GUI() throws InstantiationException, IllegalAccessException {
        // Only create a GUI if one hasn't already been created
        if (initialised) {
            frame.setVisible(true);
            return;
        }
        initialised = true;

        // Starting this process, as it takes longest
        new Thread(GUI::listAvailableModules).start();

        // Setting location of panel
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width - editingFrameWidth) / 2, (screenSize.height - frameHeight) / 2);
        frame.setTitle("MIA (version " + MIA.getVersion() + ")");

        initialiseStatusTextField();

        // Creating the menu bar
        initialiseMenuBar();
        frame.setJMenuBar(menuBar);

        initialiseBasicMode();
        initialiseEditingMode();

        if (MIA.isDebug()) {
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            renderEditingMode();
        } else {
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            renderBasicMode();
        }

        // Final bits for listeners
        frame.setVisible(true);

        // Populating the list containing all available modules
        moduleListMenu.show(frame, 0, 0);
        moduleListMenu.setVisible(false);

    }

    private static void initialiseMenuBar() {
        // Creating the file menu
        JMenu menu = new JMenu("File");
        menu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        menuBar.add(menu);
        menu.add(new AnalysisMenuItem(AnalysisMenuItem.LOAD_ANALYSIS));
        menu.add(new AnalysisMenuItem(AnalysisMenuItem.SAVE_ANALYSIS));

        // Creating the edit menu
        menu = new JMenu("Edit");
        menu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        menuBar.add(menu);
        menu.add(new AnalysisMenuItem(AnalysisMenuItem.CLEAR_PIPELINE));
        menu.add(new AnalysisMenuItem(AnalysisMenuItem.ENABLE_ALL));
        menu.add(new AnalysisMenuItem(AnalysisMenuItem.DISABLE_ALL));
        menu.add(new AnalysisMenuItem(AnalysisMenuItem.OUTPUT_ALL));
        menu.add(new AnalysisMenuItem(AnalysisMenuItem.SILENCE_ALL));

        // Creating the analysis menu
        menu = new JMenu("Analysis");
        menu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        menuBar.add(menu);
        menu.add(new AnalysisMenuItem(AnalysisMenuItem.START_ANALYSIS));
        menu.add(new AnalysisMenuItem(AnalysisMenuItem.STOP_ANALYSIS));

        // Creating the new menu
        menu = new JMenu("View");
        menu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        menuBar.add(menu);
        if (MIA.isDebug()) {
            menu.add(new AnalysisMenuItem(AnalysisMenuItem.BASIC_VIEW));
        } else {
            menu.add(new AnalysisMenuItem(AnalysisMenuItem.EDITING_VIEW));
        }
        menu.add(new AnalysisMenuItem(AnalysisMenuItem.TOGGLE_HELP_NOTES));

    }

    public static void render() throws IllegalAccessException, InstantiationException {
        if (basicGUI) {
            renderBasicMode();
        } else {
            renderEditingMode();
        }
    }

    public static void initialiseBasicMode() {
        basicPanel.setLayout(new GridBagLayout());
        basicPanel.setPreferredSize(new Dimension(basicFrameWidth-20,frameHeight));
        basicPanel.setMinimumSize(new Dimension(basicFrameWidth-20,frameHeight));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 0, 5);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;

        // Initialising the control panel
        basicPanel.add(initialiseBasicControlPanel(), c);

        // Initialising the parameters panel
        c.gridy++;
        c.weighty = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.BOTH;
        basicPanel.add(basicControlPanel, c);

        // Initialising the help and notes panels
        initialiseBasicHelpNotesPanels();
        c.gridx++;
        c.insets = new Insets(5, 0, 0, 5);
        basicPanel.add(basicHelpNotesPanel,c);

        // Initialising the status panel
        c.gridx = 0;
        c.gridy++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0;
        c.gridwidth = 2;
        c.insets = new Insets(5, 5, 0, 5);
        basicPanel.add(basicStatusPanel,c);

        // Initialising the progress bar
        c.gridy++;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5,5,5,5);
        basicPanel.add(basicProgressBarPanel,c);

    }

    public static void initialiseEditingMode() {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 0);
        c.gridx = 0;
        c.gridy = 0;

        editingPanel.setLayout(new GridBagLayout());

        // Creating buttons to add and remove modules
        JPanel controlPanel = initialiseControlPanel();
        c.weightx = 0;
        c.weighty = 1;
        c.gridheight = 3;
        c.fill = GridBagConstraints.VERTICAL;
        editingPanel.add(controlPanel, c);

        // Initialising the status panel
        c.gridheight = 1;
        c.gridy++;
        c.gridy++;
        c.gridy++;
        c.weighty = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 4;
        c.insets = new Insets(0,5,5,5);
        editingPanel.add(statusPanel, c);

        // Initialising the progress bar
        c.gridy++;
        c.insets = new Insets(0,5,5,5);
        editingPanel.add(editingProgressBarPanel,c);

        // Initialising the input enable panel
        c.gridx++;
        c.gridy = 0;
        c.weightx = 0;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 0, 0);
        editingPanel.add(editingInputPanel, c);

        // Initialising the module list panel
        c.gridy++;
        c.weighty = 1;
        c.fill = GridBagConstraints.VERTICAL;
        editingPanel.add(editingModulesPanel, c);

        // Initialising the output enable panel
        c.gridy++;
        c.gridheight = 1;
        c.weighty = 0;
        c.insets = new Insets(5, 5, 5, 0);
        editingPanel.add(editingOutputPanel, c);

        // Initialising the parameters panel
        c.gridx++;
        c.gridy = 0;
        c.gridheight = 3;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5, 5, 5, 5);
        editingPanel.add(editingParametersPanel, c);

        initialiseHelpNotesPanels();
        c.gridx++;
        c.weightx = 0;
        c.insets = new Insets(5,0,5,5);
        editingPanel.add(helpNotesPanel,c);

        // Setting the active module to InputControl
        activeModule = analysis.getInputControl();

    }

    public static void renderBasicMode() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(0,5,0,0);
        c.anchor = GridBagConstraints.WEST;

        basicGUI = true;

        frame.remove(editingPanel);
        frame.add(basicPanel);
        basicStatusPanel.add(textField,c);

        basicHelpNotesPanel.setVisible(showBasicHelpNotes);

        basicPanel.setVisible(true);
        basicPanel.validate();
        basicPanel.repaint();

        int frameWidth = basicFrameWidth;
        if (showBasicHelpNotes) frameWidth = frameWidth + 319;
        frame.setPreferredSize(new Dimension(frameWidth,frameHeight));
        frame.setMinimumSize(new Dimension(frameWidth,minimumFrameHeight));

        frame.pack();
        frame.validate();
        frame.repaint();

        if (showBasicHelpNotes) populateBasicHelpNotes();
        populateBasicModules();
        updateTestFile();

    }

    public static void renderEditingMode() throws InstantiationException, IllegalAccessException {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(0,5,0,0);
        c.anchor = GridBagConstraints.WEST;

        basicGUI = false;

        frame.remove(basicPanel);
        frame.add(editingPanel);

        statusPanel.add(textField,c);

        helpNotesPanel.setVisible(showEditingHelpNotes);

        editingPanel.setVisible(true);
        editingPanel.validate();
        editingPanel.repaint();

        int frameWidth = editingFrameWidth;
        if (showEditingHelpNotes) frameWidth = frameWidth + 315;
        frame.setPreferredSize(new Dimension(frameWidth,frameHeight));
        frame.setMinimumSize(new Dimension(minimumEditingFrameWidth,minimumFrameHeight));

        frame.pack();
        frame.revalidate();
        frame.repaint();

        populateModuleList();
        populateModuleParameters();
        if (showEditingHelpNotes) populateHelpNotes();
        updateTestFile();

    }

    private static JPanel initialiseControlPanel() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0;
        c.insets = new Insets(5, 5, 0, 5);
        c.anchor = GridBagConstraints.PAGE_START;

        JPanel controlPanel = new JPanel();
        controlPanel.setMaximumSize(new Dimension(bigButtonSize + 20, Integer.MAX_VALUE));
        controlPanel.setMinimumSize(new Dimension(bigButtonSize + 20, frameHeight - statusHeight-350));
        controlPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        controlPanel.setLayout(new GridBagLayout());

        // Add module button
        addModuleButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        controlPanel.add(addModuleButton, c);

        // Remove module button
        ModuleControlButton removeModuleButton = new ModuleControlButton(ModuleControlButton.REMOVE_MODULE,bigButtonSize);
        removeModuleButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        c.gridy++;
        controlPanel.add(removeModuleButton, c);

        // Move module up button
        ModuleControlButton moveModuleUpButton = new ModuleControlButton(ModuleControlButton.MOVE_MODULE_UP,bigButtonSize);
        moveModuleUpButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        c.gridy++;
        controlPanel.add(moveModuleUpButton, c);

        // Move module down button
        ModuleControlButton moveModuleDownButton = new ModuleControlButton(ModuleControlButton.MOVE_MODULE_DOWN,bigButtonSize);
        moveModuleDownButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        c.gridy++;
        controlPanel.add(moveModuleDownButton, c);

        // Load analysis protocol button
        AnalysisControlButton loadAnalysisButton = new AnalysisControlButton(AnalysisControlButton.LOAD_ANALYSIS,bigButtonSize);
        c.gridy++;
        c.weighty = 1;
        c.anchor = GridBagConstraints.PAGE_END;
        controlPanel.add(loadAnalysisButton, c);

        // Save analysis protocol button
        AnalysisControlButton saveAnalysisButton = new AnalysisControlButton(AnalysisControlButton.SAVE_ANALYSIS,bigButtonSize);
        c.gridy++;
        c.weighty = 0;
        controlPanel.add(saveAnalysisButton, c);

        // Start analysis button
        AnalysisControlButton startAnalysisButton = new AnalysisControlButton(AnalysisControlButton.START_ANALYSIS,bigButtonSize);
        c.gridy++;
        controlPanel.add(startAnalysisButton, c);

        // Stop analysis button
        AnalysisControlButton stopAnalysisButton = new AnalysisControlButton(AnalysisControlButton.STOP_ANALYSIS,bigButtonSize);
        c.gridy++;
        c.insets = new Insets(5, 5, 5, 5);
        controlPanel.add(stopAnalysisButton, c);

        controlPanel.validate();
        controlPanel.repaint();

        return controlPanel;

    }

    private static void initialiseHelpNotesPanels() {
        // Adding panels to combined JPanel
        helpNotesPanel.setLayout(new GridBagLayout());
        GridBagConstraints cc = new GridBagConstraints();

        cc.fill = GridBagConstraints.BOTH;
        cc.gridx = 0;
        cc.gridy = 0;
        cc.weightx = 1;
        cc.weighty = 2;
        cc.insets = new Insets(0,0,5,0);
        helpNotesPanel.add(editingHelpPanel,cc);

        cc.gridy++;
        cc.weighty = 1;
        cc.insets = new Insets(0,0,0,0);
        helpNotesPanel.add(editingNotesPanel,cc);

    }

    private static void initialiseBasicHelpNotesPanels() {
        // Adding panels to combined JPanel
        basicHelpNotesPanel.setLayout(new GridBagLayout());
        GridBagConstraints cc = new GridBagConstraints();

        cc.fill = GridBagConstraints.BOTH;
        cc.gridx = 0;
        cc.gridy = 0;
        cc.weightx = 1;
        cc.weighty = 2;
        cc.insets = new Insets(0,0,5,0);
        basicHelpNotesPanel.add(basicHelpPanel,cc);

        cc.gridy++;
        cc.weighty = 1;
        cc.insets = new Insets(0,0,0,0);
        basicHelpNotesPanel.add(basicNotesPanel,cc);

    }

    private static void initialiseStatusTextField() {
        textField.setPreferredSize(new Dimension(Integer.MAX_VALUE,statusHeight));
        textField.setBorder(null);
        textField.setText("MIA (version " + MIA.getVersion() + ")");
        textField.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        textField.setToolTipText(textField.getText());
        textField.setOpaque(false);

        OutputStreamTextField outputStreamTextField = new OutputStreamTextField(textField);
        PrintStream guiPrintStream = new PrintStream(outputStreamTextField);

        if (MIA.isDebug()) {
            TeeOutputStream teeOutputStream = new TeeOutputStream(System.out,guiPrintStream);
            PrintStream printStream = new PrintStream(teeOutputStream);
            System.setOut(printStream);
        } else {
            System.setOut(guiPrintStream);
        }
    }

    private static JPanel initialiseBasicControlPanel() {
        JPanel basicControlPanel = new JPanel();

        basicControlPanel.setPreferredSize(new Dimension(basicFrameWidth-30, bigButtonSize + 15));
        basicControlPanel.setMinimumSize(new Dimension(basicFrameWidth-30, bigButtonSize + 15));
        basicControlPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        basicControlPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.FIRST_LINE_START;

        // Load analysis protocol button
        AnalysisControlButton loadAnalysisButton
                = new AnalysisControlButton(AnalysisControlButton.LOAD_ANALYSIS,bigButtonSize);
        c.gridx++;
        c.anchor = GridBagConstraints.PAGE_END;
        basicControlPanel.add(loadAnalysisButton, c);

        // Save analysis protocol button
        AnalysisControlButton saveAnalysisButton
                = new AnalysisControlButton(AnalysisControlButton.SAVE_ANALYSIS,bigButtonSize);
        c.gridx++;
        basicControlPanel.add(saveAnalysisButton, c);

        // Start analysis button
        AnalysisControlButton startAnalysisButton
                = new AnalysisControlButton(AnalysisControlButton.START_ANALYSIS,bigButtonSize);
        c.gridx++;
        c.weightx = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_END;
        basicControlPanel.add(startAnalysisButton, c);

        // Stop analysis button
        AnalysisControlButton stopAnalysisButton
                = new AnalysisControlButton(AnalysisControlButton.STOP_ANALYSIS,bigButtonSize);
        c.gridx++;
        c.weightx = 0;
        basicControlPanel.add(stopAnalysisButton, c);

        basicControlPanel.validate();
        basicControlPanel.repaint();

        return basicControlPanel;

    }

    public static void populateModuleList() {
        editingInputPanel.updatePanel(analysis.getInputControl());
        editingOutputPanel.updatePanel(analysis.getOutputControl());
        editingModulesPanel.updatePanel();
    }

    public static void populateModuleParameters() {
        editingParametersPanel.updatePanel(activeModule);
    }

    public static void populateHelpNotes() {
        // Only update the help and notes if the module has changed
        if (activeModule != lastEditingHelpNotesModule) {
            lastEditingHelpNotesModule = activeModule;
        } else {
            return;
        }

        editingHelpPanel.updatePanel();
        editingNotesPanel.updatePanel();

    }

    public static void populateBasicHelpNotes() {
        // If null, show a special message
        if (activeModule == null) {
            basicHelpPanel.showUsageMessage();
            basicNotesPanel.updatePanel();
            return;
        }

        // Only update the help and notes if the module has changed
        if (activeModule != lastBasicHelpNotesModule) {
            lastBasicHelpNotesModule = activeModule;
        } else {
            return;
        }

        basicHelpPanel.updatePanel();
        basicNotesPanel.updatePanel();

    }

    public static void updateModuleParameters(Module module) {
        for (Parameter parameter:module.updateAndGetParameters()) {
            parameter.getControl().updateControl();
        }
    }

    public static void populateBasicModules() {
        basicControlPanel.updatePanel();
    }

    private static void listAvailableModules() {
        try {
            addModuleButton.setEnabled(false);
            addModuleButton.setToolTipText("Loading modules");

            Set<Class<? extends Module>> availableModules = new ClassHunter<Module>().getClasses(Module.class,MIA.isDebug());

            // Creating an alphabetically-ordered list of all modules
            TreeMap<String, Class> modules = new TreeMap<>();
            for (Class clazz : availableModules) {
                if (clazz != InputControl.class && clazz != OutputControl.class) {
                    Module module = (Module) clazz.newInstance();
                    String packageName = module.getPackageName();
                    String moduleName = module.getTitle();
                    modules.put(packageName+moduleName, clazz);
                }
            }

            LinkedHashSet<ModuleListMenu> topList = new LinkedHashSet<>();
            for (String name : modules.keySet()) {
                // ActiveList starts at the top list
                LinkedHashSet<ModuleListMenu> activeList = topList;
                ModuleListMenu activeItem = null;

                String[] names = name.split("\\\\");
                for (int i = 0; i < names.length-1; i++) {
                    boolean found = false;
                    for (ModuleListMenu listItemm : activeList) {
                        if (listItemm.getName().equals(names[i])) {
                            activeItem = listItemm;
                            found = true;
                        }
                    }

                    if (!found) {
                        ModuleListMenu newItem = new ModuleListMenu(names[i], new ArrayList<>());
                        newItem.setName(names[i]);
                        activeList.add(newItem);
                        if (activeItem != null) activeItem.add(newItem);
                        activeItem = newItem;
                    }

                    activeList = activeItem.getChildren();

                }

                Module module = (Module) modules.get(name).newInstance();
                if (module != null && activeItem != null) activeItem.addMenuItem(module);

            }

            for (ModuleListMenu listMenu : topList) moduleListMenu.add(listMenu);

        } catch (IllegalAccessException | InstantiationException e){
            e.printStackTrace(System.err);
        }

        addModuleButton.setToolTipText("Add module");
        addModuleButton.setEnabled(true);

    }

    public static void addModule() {
        moduleListMenu.setLocation(MouseInfo.getPointerInfo().getLocation());
        moduleListMenu.setVisible(true);
        updateModules(true);

    }

    public static void removeModule() {
        if (activeModule != null) {
            ModuleCollection modules = getModules();
            // Removing a module resets all the current evaluation
            int idx = modules.indexOf(activeModule);

            if (idx <= lastModuleEval) lastModuleEval = idx - 1;

            modules.remove(activeModule);
            activeModule = null;

            populateModuleList();
            populateModuleParameters();
            populateHelpNotes();
            updateModules(true);

        }
    }

    public static void moveModuleUp() {
        if (activeModule != null) {
            ModuleCollection modules = getModules();
            int idx = modules.indexOf(activeModule);

            if (idx != 0) {
                if (idx - 2 <= lastModuleEval) lastModuleEval = idx - 2;

                modules.remove(activeModule);
                modules.add(idx - 1, activeModule);
                populateModuleList();
                updateModules(true);
            }
        }
    }

    public static void moveModuleDown() {
        if (activeModule != null) {
            ModuleCollection modules = getModules();
            int idx = modules.indexOf(activeModule);

            if (idx < modules.size()-1) {
                if (idx <= lastModuleEval) lastModuleEval = idx - 1;

                modules.remove(activeModule);
                modules.add(idx + 1, activeModule);
                populateModuleList();
                updateModules(true);
            }
        }
    }

    public static ComponentFactory getComponentFactory() {
        return componentFactory;
    }

    public static JFrame getFrame() {
        return frame;
    }

    public static JPopupMenu getModuleListMenu() {
        return moduleListMenu;
    }

    public static boolean isBasicGUI() {
        return basicGUI;
    }

    public static void setAnalysis(Analysis analysis) {
        GUI.analysis = analysis;
    }

    public static ModuleCollection getModules() {
        return analysis.getModules();
    }

    public static void setProgress(int val) {
        editingProgressBarPanel.setValue(val);
        basicProgressBarPanel.setValue(val);
    }

    public static void updateModules(boolean verbose) {
        int nRunnable = AnalysisTester.testModules(analysis.getModules());
        int nActive = 0;
        for (Module module:analysis.getModules()) if (module.isEnabled()) nActive++;
        int nModules = analysis.getModules().size();
        if (verbose && nModules > 0) System.out.println(nRunnable+" of "+nActive+" active modules are runnable");

        boolean runnable = AnalysisTester.testModule(analysis.getInputControl(),analysis.getModules());
        analysis.getInputControl().setRunnable(runnable);
        editingInputPanel.updateButtonState();

        runnable = AnalysisTester.testModule(analysis.getOutputControl(),analysis.getModules());
        analysis.getInputControl().setRunnable(runnable);
        editingOutputPanel.updateButtonState();

        if (basicGUI) {
            populateBasicModules();
        } else {
            editingModulesPanel.updateButtonStates();
            populateModuleParameters();
        }

        populateHelpNotes();

    }

    public static void updateTestFile() {
        // Ensuring the input file specified in the InputControl is active in the test workspace
        InputControl inputControl = getAnalysis().getInputControl();
        String inputPath = ((FileFolderPathP) inputControl.getParameter(InputControl.INPUT_PATH)).getPath();
        int nThreads = ((IntegerP) inputControl.getParameter(InputControl.SIMULTANEOUS_JOBS)).getValue();
        Units.setUnits(((ChoiceP) inputControl.getParameter(InputControl.SPATIAL_UNITS)).getChoice());

        if (inputPath == null) return;

        String inputFile = "";
        if (new File(inputPath).isFile()) {
            inputFile = inputPath;
        } else {
            BatchProcessor batchProcessor = new BatchProcessor(new File(inputPath));
            batchProcessor.setnThreads(nThreads);

            // Adding filename filters
            inputControl.addFilenameExtensionFilter(batchProcessor);
            inputControl.addFilenameFilters(batchProcessor);

            // Running the analysis
            File nextFile = batchProcessor.getNextValidFileInStructure();
            if (nextFile == null) {
                inputFile = null;
            } else {
                inputFile = nextFile.getAbsolutePath();
            }
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

        ChoiceP seriesMode = (ChoiceP) analysis.getInputControl().getParameter(InputControl.SERIES_MODE);
        switch (seriesMode.getChoice()) {
            case InputControl.SeriesModes.ALL_SERIES:
                getTestWorkspace().getMetadata().setSeriesNumber(1);
                getTestWorkspace().getMetadata().setSeriesName("");
                break;

            case InputControl.SeriesModes.SERIES_LIST:
                SeriesListSelectorP listParameter = analysis.getInputControl().getParameter(InputControl.SERIES_LIST);
                int[] seriesList = listParameter.getSeriesList();
                getTestWorkspace().getMetadata().setSeriesNumber(seriesList[0]);
                getTestWorkspace().getMetadata().setSeriesName("");
                break;

        }
    }

    public static int getLastModuleEval(){
        return lastModuleEval;
    }

    public static void setLastModuleEval(int lastModuleEval) {
        GUI.lastModuleEval = Math.max(lastModuleEval,-1);
    }

    public static int getModuleBeingEval() {
        return moduleBeingEval;
    }

    public static void setModuleBeingEval(int moduleBeingEval) {
        GUI.moduleBeingEval = moduleBeingEval;
    }

    public static Module getActiveModule() {
        return activeModule;
    }

    public static void setActiveModule(Module activeModule) {
        GUI.activeModule = activeModule;
    }

    public static void evaluateModule(Module module) {
        // Setting the index to the previous module.  This will make the currently-evaluated module go red
        lastModuleEval = getModules().indexOf(module) - 1;
        moduleBeingEval = getModules().indexOf(module);
        updateModules(false);

        Module.setVerbose(true);
        module.execute(testWorkspace);
        lastModuleEval = getModules().indexOf(module);
        moduleBeingEval = -1;

        updateModules(false);

    }

    public static void setTestWorkspace(Workspace testWorkspace) {
        GUI.testWorkspace = testWorkspace;
    }

    public static Analysis getAnalysis() {
        return analysis;
    }

    public static Workspace getTestWorkspace() {
        return testWorkspace;
    }

    public static MeasurementRef getGlobalMeasurementRef() {
        return globalMeasurementRef;
    }

    public static boolean isShowEditingHelpNotes() {
        return showEditingHelpNotes;
    }

    public static void setShowEditingHelpNotes(boolean showEditingHelpNotes) {
        GUI.showEditingHelpNotes = showEditingHelpNotes;
    }

    public static boolean isShowBasicHelpNotes() {
        return showBasicHelpNotes;
    }

    public static void setShowBasicHelpNotes(boolean showBasicHelpNotes) {
        GUI.showBasicHelpNotes = showBasicHelpNotes;
    }

    public static int getEditingFrameWidth() {
        return editingFrameWidth;
    }

    public static void setEditingFrameWidth(int editingFrameWidth) {
        GUI.editingFrameWidth = editingFrameWidth;
    }

    public static Module getLastEditingHelpNotesModule() {
        return lastEditingHelpNotesModule;
    }

    public static void setLastEditingHelpNotesModule(Module lastEditingHelpNotesModule) {
        GUI.lastEditingHelpNotesModule = lastEditingHelpNotesModule;
    }

    public static Module getLastBasicHelpNotesModule() {
        return lastBasicHelpNotesModule;
    }

    public static void setLastBasicHelpNotesModule(Module lastBasicHelpNotesModule) {
        GUI.lastBasicHelpNotesModule = lastBasicHelpNotesModule;
    }

    public static ButtonGroup getModuleGroup() {
        return moduleGroup;
    }


    // COMPONENT SIZE GETTERS

    public static int getMinimumEditingFrameWidth() {
        return minimumEditingFrameWidth;
    }

    public static int getBasicFrameWidth() {
        return basicFrameWidth;
    }

    public static int getMinimumFrameHeight() {
        return minimumFrameHeight;
    }

    public static int getFrameHeight() {
        return frameHeight;
    }

    public static int getElementHeight() {
        return elementHeight;
    }

    public static int getBigButtonSize() {
        return bigButtonSize;
    }

    public static int getModuleButtonWidth() {
        return moduleButtonWidth;
    }

    public static int getStatusHeight() {
        return statusHeight;
    }
}