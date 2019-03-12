package wbif.sjx.ModularImageAnalysis.Process.AnalysisHandling;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.InputControl;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.OutputControl;
import wbif.sjx.ModularImageAnalysis.MIA;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.MeasurementRef;
import wbif.sjx.ModularImageAnalysis.Object.ModuleCollection;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;
import wbif.sjx.ModularImageAnalysis.Process.ClassHunter;

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
    public static Analysis loadAnalysis()
            throws SAXException, IllegalAccessException, IOException, InstantiationException, ParserConfigurationException, ClassNotFoundException {
        FileDialog fileDialog = new FileDialog(new Frame(), "Select file to load", FileDialog.LOAD);
        fileDialog.setMultipleMode(false);
        fileDialog.setFile("*.mia");
        fileDialog.setVisible(true);

        if (fileDialog.getFiles().length==0) return null;

        Analysis analysis = loadAnalysis(fileDialog.getFiles()[0]);
        analysis.setAnalysisFilename(fileDialog.getFiles()[0].getAbsolutePath());

        System.out.println("File loaded ("+ FilenameUtils.getName(fileDialog.getFiles()[0].getName())+")");

        return analysis;

    }

    public static Analysis loadAnalysis(File file)
            throws IOException, ClassNotFoundException, ParserConfigurationException, SAXException, IllegalAccessException, InstantiationException {
        String xml = FileUtils.readFileToString(file,"UTF-8");

        return loadAnalysis(xml);

    }

    public static Analysis loadAnalysis(String xml)
            throws IOException, ClassNotFoundException, ParserConfigurationException, SAXException, IllegalAccessException, InstantiationException {
        if (xml.startsWith("\uFEFF")) {
            xml = xml.substring(1);
        }

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document doc = documentBuilder.parse(new InputSource(new ByteArrayInputStream(xml.getBytes("UTF-8"))));
        doc.getDocumentElement().normalize();

        Analysis analysis = new Analysis();
        ModuleCollection modules = analysis.getModules();

        // Creating a list of all available modules (rather than reading their full path, in case they move) using
        // Reflections tool
        Set<Class<? extends Module>> availableModules = new ClassHunter<Module>().getClasses(Module.class,MIA.isDebug());

        NodeList moduleNodes = doc.getElementsByTagName("MODULE");
        for (int i=0;i<moduleNodes.getLength();i++) {
            Node moduleNode = moduleNodes.item(i);

            // Creating an empty Module matching the input type.  If none was found the loop skips to the next Module
            Module module = initialiseModule(moduleNode,availableModules);
            if (module == null) continue;

            // If the module is an input, treat it differently
            if (module.getClass().isInstance(new InputControl())) {
                addInputSpecificComponents(module,moduleNode);
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
                            populateModuleParameters(moduleChildNodes.item(j), module.getAllParameters(), moduleName);
                            foundParameters = true;
                            break;

                        case "MEASUREMENTS":
                            populateModuleMeasurementRefs(moduleChildNodes.item(j), module);
                            break;
                    }
                }

                // Old file formats had parameters loose within MODULE
                if (!foundParameters) populateModuleParameters(moduleNode, module.getAllParameters(),moduleName);

                return module;

            }
        }

        // If no module was found matching that name an error message is displayed
        System.err.println("Module \""+moduleName+"\" not found (skipping)");

        return null;

    }

    public static void addInputSpecificComponents(Module module, Node moduleNode) {
        NamedNodeMap moduleAttributes = moduleNode.getAttributes();

        if (moduleAttributes.getNamedItem("DISABLEABLE") != null) {
            String isDisableable = moduleAttributes.getNamedItem("DISABLEABLE").getNodeValue();
            module.setCanBeDisabled(Boolean.parseBoolean(isDisableable));
        } else {
            module.setCanBeDisabled(false);
        }

        if (moduleAttributes.getNamedItem("NOTES") != null) {
            String notes = moduleAttributes.getNamedItem("NOTES").getNodeValue();
            module.setNotes(notes);
        } else {
            module.setNotes("");
        }
    }

    public static void addOutputSpecificComponents(Module module, Node moduleNode) {
        NamedNodeMap moduleAttributes = moduleNode.getAttributes();

        if (moduleAttributes.getNamedItem("DISABLEABLE") != null) {
            String isDisableable = moduleAttributes.getNamedItem("DISABLEABLE").getNodeValue();
            module.setCanBeDisabled(Boolean.parseBoolean(isDisableable));
        } else {
            module.setCanBeDisabled(false);
        }

        if (moduleAttributes.getNamedItem("NOTES") != null) {
            String notes = moduleAttributes.getNamedItem("NOTES").getNodeValue();
            module.setNotes(notes);
        } else {
            module.setNotes("");
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

    public static void populateModuleParameters(Node moduleNode, ParameterCollection parameters, String moduleName) {
        NodeList parameterNodes = moduleNode.getChildNodes();
        for (int j = 0; j < parameterNodes.getLength(); j++) {
            Node parameterNode = parameterNodes.item(j);

            if (parameterNode.getNodeName().equals("COLLECTIONS")) {
                populateModuleParameterGroups(parameterNode,parameters,moduleName);
                continue;
            }

            NamedNodeMap parameterAttributes = parameterNode.getAttributes();

            String parameterName = parameterAttributes.getNamedItem("NAME").getNodeValue();
            String parameterValue = parameterAttributes.getNamedItem("VALUE").getNodeValue();
            String parameterValueSource = "";
            if (parameterAttributes.getNamedItem("VALUESOURCE") != null) {
                parameterValueSource = parameterAttributes.getNamedItem("VALUESOURCE").getNodeValue();
            }

            try {
                Parameter parameter = parameters.getParameter(parameterName);
                if (parameter instanceof InputImageP) {
                    ((InputImageP) parameters.getParameter(parameterName)).setImageName(parameterValue);
                } else if (parameter instanceof OutputImageP) {
                    ((OutputImageP) parameters.getParameter(parameterName)).setImageName(parameterValue);
                } else if (parameter instanceof InputObjectsP) {
                    ((InputObjectsP) parameters.getParameter(parameterName)).setChoice(parameterValue);
                } else if (parameter instanceof OutputObjectsP) {
                    ((OutputObjectsP) parameters.getParameter(parameterName)).setObjectsName(parameterValue);
                } else if (parameter instanceof RemovedImageP) {
                    ((RemovedImageP) parameters.getParameter(parameterName)).setChoice(parameterValue);
                } else if (parameter instanceof RemovedObjectsP) {
                    ((RemovedObjectsP) parameters.getParameter(parameterName)).setChoice(parameterValue);
                } else if (parameter instanceof StringP) {
                    ((StringP) parameters.getParameter(parameterName)).setValue(parameterValue);
                } else if (parameter instanceof IntegerP) {
                    ((IntegerP) parameters.getParameter(parameterName)).setValueFromString(parameterValue);
                } else if (parameter instanceof DoubleP) {
                    ((DoubleP) parameters.getParameter(parameterName)).setValueFromString(parameterValue);
                } else if (parameter instanceof BooleanP) {
                    ((BooleanP) parameters.getParameter(parameterName)).setValueFromString(parameterValue);
                } else if (parameter instanceof ChoiceP) {
                    ((ChoiceP) parameters.getParameter(parameterName)).setChoice(parameterValue);
                } else if (parameter instanceof ChildObjectsP) {
                    ((ChildObjectsP) parameters.getParameter(parameterName)).setChoice(parameterValue);
                    ((ChildObjectsP) parameters.getParameter(parameterName)).setParentObjectsName(parameterValueSource);
                } else if (parameter instanceof ParentObjectsP) {
                    ((ParentObjectsP) parameters.getParameter(parameterName)).setChoice(parameterValue);
                    ((ParentObjectsP) parameters.getParameter(parameterName)).setChildObjectsName(parameterValueSource);
                } else if (parameter instanceof ImageMeasurementP) {
                    ((ImageMeasurementP) parameters.getParameter(parameterName)).setChoice(parameterValue);
                    ((ImageMeasurementP) parameters.getParameter(parameterName)).setImageName(parameterValueSource);
                } else if (parameter instanceof ObjectMeasurementP) {
                    ((ObjectMeasurementP) parameters.getParameter(parameterName)).setChoice(parameterValue);
                    ((ObjectMeasurementP) parameters.getParameter(parameterName)).setObjectName(parameterValueSource);
                } else if (parameter instanceof FilePathP) {
                    ((FilePathP) parameters.getParameter(parameterName)).setPath(parameterValue);
                } else if (parameter instanceof FolderPathP) {
                    ((FolderPathP) parameters.getParameter(parameterName)).setPath(parameterValue);
                } else if (parameter instanceof FileFolderPathP) {
                    ((FileFolderPathP) parameters.getParameter(parameterName)).setPath(parameterValue);
                } else if (parameter instanceof MetadataItemP) {
                    ((MetadataItemP) parameters.getParameter(parameterName)).setChoice(parameterValue);
                } else if (parameter instanceof TextDisplayP) {
                    ((TextDisplayP) parameters.getParameter(parameterName)).setValue(parameterValue);
                }

                if (parameterAttributes.getNamedItem("VISIBLE") != null) {
                    boolean visible = Boolean.parseBoolean(parameterAttributes.getNamedItem("VISIBLE").getNodeValue());
                    parameter.setVisible(visible);
                }

            } catch (NullPointerException e) {
                System.err.println("Module \""+moduleName+"\" parameter \""+parameterName + "\" ("+parameterValue+") not set");

            }
        }
    }

    public static void populateModuleMeasurementRefs(Node moduleNode, Module module) {
        NodeList referenceNodes = moduleNode.getChildNodes();

        // Iterating over all references of this type
        for (int j=0;j<referenceNodes.getLength();j++) {
            Node referenceNode = referenceNodes.item(j);

            // Getting measurement properties
            NamedNodeMap attributes = referenceNode.getAttributes();
            String measurementName = attributes.getNamedItem("NAME").getNodeValue();
            String type = attributes.getNamedItem("TYPE").getNodeValue();

            // Acquiring the relevant reference
            MeasurementRef measurementReference = null;
            switch (type) {
                case "IMAGE":
                    measurementReference = module.getImageMeasurementRef(measurementName);
                    break;

                case "OBJECTS":
                    measurementReference = module.getObjectMeasurementRef(measurementName);
                    break;

            }

            if (measurementReference == null) continue;

            // Updating the reference's parameters
            String measurementNickName = measurementName;
            if (attributes.getNamedItem("NICKNAME") != null) measurementNickName = attributes.getNamedItem("NICKNAME").getNodeValue();
            measurementReference.setNickname(measurementNickName);
            measurementReference.setImageObjName(attributes.getNamedItem("IMAGE_OBJECT_NAME").getNodeValue());

            boolean exportGlobal = true;
            if (attributes.getNamedItem("EXPORT_GLOBAL") != null) {
                exportGlobal= Boolean.parseBoolean(attributes.getNamedItem("EXPORT_GLOBAL").getNodeValue());
            }
            measurementReference.setExportGlobal(exportGlobal);

            boolean exportIndividual = true;
            if (attributes.getNamedItem("EXPORT_INDIVIDUAL") != null) {
                exportIndividual = Boolean.parseBoolean(attributes.getNamedItem("EXPORT_INDIVIDUAL").getNodeValue());
            }
            measurementReference.setExportIndividual(exportIndividual);

            boolean exportMean = true;
            if (attributes.getNamedItem("EXPORT_MEAN") != null) {
                exportMean = Boolean.parseBoolean(attributes.getNamedItem("EXPORT_MEAN").getNodeValue());
            }
            measurementReference.setExportMean(exportMean);

            boolean exportMin = true;
            if (attributes.getNamedItem("EXPORT_MIN") != null) {
                exportMin = Boolean.parseBoolean(attributes.getNamedItem("EXPORT_MIN").getNodeValue());
            }
            measurementReference.setExportMin(exportMin);

            boolean exportMax = true;
            if (attributes.getNamedItem("EXPORT_MAX") != null) {
                exportMax = Boolean.parseBoolean(attributes.getNamedItem("EXPORT_MAX").getNodeValue());
            }
            measurementReference.setExportMax(exportMax);

            boolean exportSum = true;
            if (attributes.getNamedItem("EXPORT_SUM") != null) {
                exportSum = Boolean.parseBoolean(attributes.getNamedItem("EXPORT_SUM").getNodeValue());
            }
            measurementReference.setExportSum(exportSum);

            boolean exportStd = true;
            if (attributes.getNamedItem("EXPORT_STD") != null) {
                exportStd = Boolean.parseBoolean(attributes.getNamedItem("EXPORT_STD").getNodeValue());
            }
            measurementReference.setExportStd(exportStd);

        }
    }

    public static void populateModuleParameterGroups(Node parameterNode, ParameterCollection parameters, String moduleName) {
        NodeList collectionNodes = parameterNode.getChildNodes();
        String groupName = parameterNode.getAttributes().getNamedItem("NAME").getNodeValue();

        // Loading the ParameterGroup and clearing all previously-initialised parameters
        ParameterGroup group = parameters.getParameter(groupName);
        group.removeAllParameters();

        for (int j = 0; j < collectionNodes.getLength(); j++) {
            ParameterCollection newParameters = group.addParameters();

            Node collectionNode = collectionNodes.item(j);
            Node newParametersNode = collectionNode.getChildNodes().item(0);
            populateModuleParameters(newParametersNode,newParameters,moduleName);

        }
    }
}
