package wbif.sjx.ModularImageAnalysis.Process;

import org.apache.commons.io.FilenameUtils;
import org.reflections.Reflections;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.InputControl;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.OutputControl;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.*;
import java.util.Set;

/**
 * Created by sc13967 on 23/06/2017.
 */
public class AnalysisReader {
    public static Analysis loadAnalysis() throws SAXException, IllegalAccessException, IOException, InstantiationException, ParserConfigurationException, ClassNotFoundException {
        FileDialog fileDialog = new FileDialog(new Frame(), "Select file to load", FileDialog.LOAD);
        fileDialog.setMultipleMode(false);
        fileDialog.setFile("*.mia");
        fileDialog.setVisible(true);

        if (fileDialog.getFiles().length==0) return null;

        Analysis analysis = loadAnalysis(new FileInputStream(fileDialog.getFiles()[0]));

        System.out.println("File loaded ("+ FilenameUtils.getName(fileDialog.getFiles()[0].getName())+")");

        return analysis;

    }

    public static Analysis loadAnalysis(InputStream analysisFileStream)
            throws IOException, ClassNotFoundException, ParserConfigurationException, SAXException, IllegalAccessException, InstantiationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document doc = documentBuilder.parse(analysisFileStream);
        doc.getDocumentElement().normalize();

        Analysis analysis = new Analysis();
        ModuleCollection modules = analysis.getModules();

        // Creating a list of all available modules (rather than reading their full path, in case they move) using
        // Reflections tool
        Reflections.log = null;
        Reflections reflections = new Reflections("wbif.sjx.ModularImageAnalysis");
        Set<Class<? extends Module>> availableModules = reflections.getSubTypesOf(Module.class);

        NodeList moduleNodes = doc.getElementsByTagName("MODULE");
        for (int i=0;i<moduleNodes.getLength();i++) {
            Node moduleNode = moduleNodes.item(i);

            // Creating an empty Module matching the input type.  If none was found the loop skips to the next Module
            Module module = initialiseModule(moduleNode,availableModules);
            if (module == null) continue;

            // If the module is an input, treat it differently
            if (module.getClass().isInstance(new InputControl())) {
                analysis.setInputControl((InputControl) module);

            } else if (module.getClass().isInstance(new OutputControl())) {
                addOutputSpecificComponents(module,moduleNode);
                analysis.setOutputControl((OutputControl) module);

            } else {
                addStandardModuleSpecificComponents(module, moduleNode);
                modules.add(module);
            }
        }

        return analysis;

    }

    public static Module initialiseModule(Node moduleNode, Set<Class<? extends Module>> availableModules)
            throws IllegalAccessException, InstantiationException {

        NamedNodeMap moduleAttributes = moduleNode.getAttributes();
        String fullModuleName = moduleAttributes.getNamedItem("NAME").getNodeValue();
        String moduleName = FilenameUtils.getExtension(fullModuleName);

        for (Class<?> clazz:availableModules) {
            if (moduleName.equals(clazz.getSimpleName())) {
                Module module = (Module) clazz.newInstance();

                if (moduleAttributes.getNamedItem("NICKNAME") != null) {
                    String moduleNickname = moduleAttributes.getNamedItem("NICKNAME").getNodeValue();
                    module.setNickname(moduleNickname);
                } else {
                    module.setNickname(module.getTitle());
                }

                // Populating parameters
                NodeList moduleChildNodes = moduleNode.getChildNodes();
                boolean foundParameters = false;
                for (int j=0;j<moduleChildNodes.getLength();j++) {
                    switch (moduleChildNodes.item(j).getNodeName()) {
                        case "PARAMETERS":
                            populateModuleParameters(moduleChildNodes.item(j), module);
                            foundParameters = true;
                            break;

                        case "MEASUREMENTS":
                            populateModuleMeasurementReferences(moduleChildNodes.item(j), module);
                            break;
                    }
                }

                // Old file formats had parameters loose within MODULE
                if (!foundParameters) populateModuleParameters(moduleNode, module);

                return module;

            }
        }

        // If no module was found matching that name an error message is displayed
        System.err.println("Module \""+moduleName+"\" not found (skipping)");

        return null;

    }

    public static void addOutputSpecificComponents(Module module, Node moduleNode) {
        NamedNodeMap moduleAttributes = moduleNode.getAttributes();

        if (moduleAttributes.getNamedItem("DISABLEABLE") != null) {
            String isDisableable = moduleAttributes.getNamedItem("DISABLEABLE").getNodeValue();
            module.setCanBeDisabled(Boolean.parseBoolean(isDisableable));
        } else {
            module.setCanBeDisabled(false);
        }
    }

    public static void addStandardModuleSpecificComponents(Module module, Node moduleNode) {
        NamedNodeMap moduleAttributes = moduleNode.getAttributes();

        if (moduleAttributes.getNamedItem("ENABLED") != null) {
            String isEnabled = moduleAttributes.getNamedItem("ENABLED").getNodeValue();
            module.setEnabled(Boolean.parseBoolean(isEnabled));
        } else {
            module.setEnabled(true);
        }

        if (moduleAttributes.getNamedItem("DISABLEABLE") != null) {
            String isDisableable = moduleAttributes.getNamedItem("DISABLEABLE").getNodeValue();
            module.setCanBeDisabled(Boolean.parseBoolean(isDisableable));
        } else {
            module.setCanBeDisabled(false);
        }

        if (moduleAttributes.getNamedItem("SHOW_OUTPUT") != null) {
            String canShowOutput = moduleAttributes.getNamedItem("SHOW_OUTPUT").getNodeValue();
            module.setShowOutput(Boolean.parseBoolean(canShowOutput));
        } else {
            module.setShowOutput(false);
        }

        if (moduleAttributes.getNamedItem("NOTES") != null) {
            String notes = moduleAttributes.getNamedItem("NOTES").getNodeValue();
            module.setNotes(notes);
        } else {
            module.setNotes("");
        }
    }

    public static void populateModuleParameters(Node moduleNode, Module module) {
        NodeList parameterNodes = moduleNode.getChildNodes();
        for (int j = 0; j < parameterNodes.getLength(); j++) {
            Node parameterNode = parameterNodes.item(j);
            NamedNodeMap parameterAttributes = parameterNode.getAttributes();
            String parameterName = parameterAttributes.getNamedItem("NAME").getNodeValue();
            String parameterValue = parameterAttributes.getNamedItem("VALUE").getNodeValue();

            boolean parameterVisible = false;
            if (parameterAttributes.getNamedItem("VISIBLE") != null) {
                parameterVisible = Boolean.parseBoolean(parameterAttributes.getNamedItem("VISIBLE").getNodeValue());
            }

            try {
                int parameterType = module.getParameterType(parameterName);

                switch (parameterType) {
                    case Parameter.INPUT_IMAGE:
                    case Parameter.OUTPUT_IMAGE:
                    case Parameter.INPUT_OBJECTS:
                    case Parameter.OUTPUT_OBJECTS:
                    case Parameter.REMOVED_IMAGE:
                    case Parameter.REMOVED_OBJECTS:
                    case Parameter.STRING:
                    case Parameter.CHOICE_ARRAY:
                    case Parameter.FILE_PATH:
                    case Parameter.FOLDER_PATH:
                    case Parameter.IMAGE_MEASUREMENT:
                    case Parameter.OBJECT_MEASUREMENT:
                    case Parameter.CHILD_OBJECTS:
                    case Parameter.PARENT_OBJECTS:
                    case Parameter.METADATA_ITEM:
                        module.updateParameterValue(parameterName, parameterValue);
                        break;

                    case Parameter.INTEGER:
                        module.updateParameterValue(parameterName, Integer.parseInt(parameterValue));
                        break;

                    case Parameter.DOUBLE:
                        module.updateParameterValue(parameterName, Double.parseDouble(parameterValue));
                        break;

                    case Parameter.BOOLEAN:
                        module.updateParameterValue(parameterName, Boolean.parseBoolean(parameterValue));
                        break;

                }

                module.setParameterVisibility(parameterName,parameterVisible);

            } catch (NullPointerException e) {
                System.err.println("Module \""+module.getTitle()
                        +"\" parameter \""+parameterName + "\" ("+parameterValue+") not set");

            }
        }
    }

    public static void populateModuleMeasurementReferences(Node moduleNode, Module module) {
        NodeList referenceNodes = moduleNode.getChildNodes();

        // Iterating over all references of this type
        for (int j=0;j<referenceNodes.getLength();j++) {
            Node referenceNode = referenceNodes.item(j);

            // Getting measurement properties
            NamedNodeMap attributes = referenceNode.getAttributes();
            String measurementName = attributes.getNamedItem("NAME").getNodeValue();
            boolean isExportable = Boolean.parseBoolean(attributes.getNamedItem("IS_EXPORTABLE").getNodeValue());
            String type = attributes.getNamedItem("TYPE").getNodeValue();
            String imageObjectName = attributes.getNamedItem("IMAGE_OBJECT_NAME").getNodeValue();

            // Acquiring the relevant reference
            MeasurementReference measurementReference = null;
            switch (type) {
                case "IMAGE":
                    measurementReference = module.getImageMeasurementReference(measurementName);
                    break;

                case "OBJECTS":
                    measurementReference = module.getObjectMeasurementReference(measurementName);
                    break;

            }

            if (measurementReference == null) continue;

            // Updating the reference's parameters
            measurementReference.setExportable(isExportable);
            measurementReference.setImageObjName(imageObjectName);

        }
    }

}
