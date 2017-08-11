// TODO: Get measurements to export from analysis.getModules().getMeasurements().get(String) for each object

package wbif.sjx.ModularImageAnalysis.Process;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Object.HCMetadata;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Created by sc13967 on 12/05/2017.
 */
public class Exporter {
    public static final int XML_EXPORT = 0;
    public static final int XLSX_EXPORT = 1;
    public static final int JSON_EXPORT = 2;

    private int exportMode = XLSX_EXPORT;

    private String exportFilePath;
    private boolean verbose = false;

    private boolean addMetadataToObjects = true;


    // CONSTRUCTOR

    public Exporter(String exportFilePath, int exportMode) {
        this.exportFilePath = exportFilePath;
        this.exportMode = exportMode;

    }


    // PUBLIC METHODS

    public void exportResults(WorkspaceCollection workspaces, Analysis analysis) throws IOException {
        if (exportMode == XML_EXPORT) {
            exportXML(workspaces,analysis);

        } else if (exportMode == XLSX_EXPORT) {
            exportXLSX(workspaces,analysis);

        } else if (exportMode == JSON_EXPORT) {
            exportJSON(workspaces,analysis);

        }
    }

    private void exportXML(WorkspaceCollection workspaces, Analysis analysis) {
        // Initialising DecimalFormat
        DecimalFormat df = new DecimalFormat("0.000E0");

        // Getting modules
        ModuleCollection modules = analysis.getModules();

        try {
            // Initialising the document
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = doc.createElement("ROOT");
            doc.appendChild(root);

            // Getting parameters as Element and adding to the main file
            Element parametersElement = prepareParametersXML(doc,modules);
            root.appendChild(parametersElement);

            // Running through each workspace (each corresponds to a file) adding file information
            for (Workspace workspace:workspaces) {
                Element setElement =  doc.createElement("SET");

                // Adding metadata from the workspace
                HCMetadata metadata = workspace.getMetadata();
                for (String key:metadata.keySet()) {
                    String attrName = key.toUpperCase();
                    Attr attr = doc.createAttribute(attrName);
                    attr.appendChild(doc.createTextNode(metadata.getAsString(key)));
                    setElement.setAttributeNode(attr);

                }

                // Creating new elements for each image in the current workspace with at least one measurement
                for (String imageName:workspace.getImages().keySet()) {
                    Image image = workspace.getImages().get(imageName);

                    if (image.getSingleMeasurements() != null) {
                        Element imageElement = doc.createElement("IMAGE");

                        Attr nameAttr = doc.createAttribute("NAME");
                        nameAttr.appendChild(doc.createTextNode(String.valueOf(imageName)));
                        imageElement.setAttributeNode(nameAttr);

                        for (MIAMeasurement measurement : image.getSingleMeasurements().values()) {
                            String attrName = measurement.getName().toUpperCase().replaceAll(" ", "_");
                            Attr measAttr = doc.createAttribute(attrName);
                            String attrValue = df.format(measurement.getValue());
                            measAttr.appendChild(doc.createTextNode(attrValue));
                            imageElement.setAttributeNode(measAttr);
                        }

                        setElement.appendChild(imageElement);

                    }
                }

                // Creating new elements for each object in the current workspace
                for (String objectNames:workspace.getObjects().keySet()) {
                    for (Obj object:workspace.getObjects().get(objectNames).values()) {
                        Element objectElement =  doc.createElement("OBJECT");

                        // Setting the ID number
                        Attr idAttr = doc.createAttribute("ID");
                        idAttr.appendChild(doc.createTextNode(String.valueOf(object.getID())));
                        objectElement.setAttributeNode(idAttr);

                        Attr nameAttr = doc.createAttribute("NAME");
                        nameAttr.appendChild(doc.createTextNode(String.valueOf(objectNames)));
                        objectElement.setAttributeNode(nameAttr);

                        for (int dim:object.getPositions().keySet()) {
                            String dimName = dim==3 ? "CHANNEL" : dim == 4 ? "TIME" : "DIM_"+dim;
                            Attr positionAttr = doc.createAttribute(dimName);
                            positionAttr.appendChild(doc.createTextNode(String.valueOf(dim)));
                            objectElement.setAttributeNode(positionAttr);

                        }

                        for (MIAMeasurement measurement:object.getMeasurements().values()) {
                            Element measElement = doc.createElement("MEAS");

                            String name = measurement.getName().toUpperCase().replaceAll(" ", "_");
                            measElement.setAttribute("NAME",name);

                            String value = df.format(measurement.getValue());
                            measElement.setAttribute("VALUE",value);

                            // Adding the measurement as a child of that object
                            objectElement.appendChild(measElement);

                        }

                        setElement.appendChild(objectElement);

                    }
                }

                root.appendChild(setElement);

            }

            // Preparing the filepath and filename
            String outPath = FilenameUtils.removeExtension(exportFilePath) +".xml";

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(outPath);

            transformer.transform(source, result);

            if (verbose) System.out.println("Saved "+ outPath);


        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
    }

    public static Element prepareParametersXML(Document doc, ModuleCollection modules) {
        Element parametersElement =  doc.createElement("PARAMETERS");

        // Running through each parameter set (one for each module
        for (HCModule module:modules) {
            LinkedHashMap<String,Parameter> parameters = module.getActiveParameters();

            boolean first = true;
            Element moduleElement =  doc.createElement("MODULE");
            for (Parameter currParam:parameters.values()) {
                // For the first parameter in a module, adding the name
                if (first) {
                    Attr nameAttr = doc.createAttribute("NAME");
                    nameAttr.appendChild(doc.createTextNode(module.getClass().getName()));
                    moduleElement.setAttributeNode(nameAttr);

                    first = false;

                }

                // Adding the name and value of the current parameter
                Element parameterElement =  doc.createElement("PARAMETER");

                Attr nameAttr = doc.createAttribute("NAME");
                nameAttr.appendChild(doc.createTextNode(currParam.getName()));
                parameterElement.setAttributeNode(nameAttr);

                Attr valueAttr = doc.createAttribute("VALUE");
                valueAttr.appendChild(doc.createTextNode(currParam.getValue().toString()));
                parameterElement.setAttributeNode(valueAttr);

                if (currParam.getType() == Parameter.CHILD_OBJECTS | currParam.getType() == Parameter.PARENT_OBJECTS) {
                    if (currParam.getValueSource() != null) {
                        Attr valueSourceAttr = doc.createAttribute("VALUESOURCE");
                        valueSourceAttr.appendChild(doc.createTextNode(currParam.getValueSource().toString()));
                        parameterElement.setAttributeNode(valueSourceAttr);
                    }
                }

                moduleElement.appendChild(parameterElement);

            }

            // Adding current module to parameters
            parametersElement.appendChild(moduleElement);

        }

        return parametersElement;

    }

    private void exportXLSX(WorkspaceCollection workspaces, Analysis analysis) throws IOException {
        // Getting modules
        ModuleCollection modules = analysis.getModules();

        // Initialising the workbook
        SXSSFWorkbook workbook = new SXSSFWorkbook();

        // Adding relevant sheets
        prepareParametersXLSX(workbook,modules);
        prepareMetadataXLSX(workbook,workspaces);
        prepareImagesXLSX(workbook,workspaces,modules);
        prepareObjectsXLSX(workbook,workspaces,modules);

        // Writing the workbook to file
        String outPath = FilenameUtils.removeExtension(exportFilePath) +".xlsx";
        FileOutputStream outputStream = new FileOutputStream(outPath);
        workbook.write(outputStream);
        workbook.close();

        if (verbose) System.out.println("Saved "+ outPath);

    }

    private void prepareParametersXLSX(SXSSFWorkbook workbook, ModuleCollection modules) {
        // Creating a sheet for parameters
        Sheet paramSheet = workbook.createSheet("Parameters");

        // Adding a header row for the parameter titles
        int paramRow = 0;
        int paramCol = 0;
        Row parameterHeader = paramSheet.createRow(paramRow++);

        Cell nameHeaderCell = parameterHeader.createCell(paramCol++);
        nameHeaderCell.setCellValue("PARAMETER");

        Cell valueHeaderCell = parameterHeader.createCell(paramCol++);
        valueHeaderCell.setCellValue("VALUE");

        Cell moduleHeaderCell = parameterHeader.createCell(paramCol);
        moduleHeaderCell.setCellValue("MODULE");

        // Adding a new parameter to each row
        for (HCModule module:modules) {
            LinkedHashMap<String,Parameter> parameters = module.getActiveParameters();

            paramRow++;

            for (Parameter currParam : parameters.values()) {
                paramCol = 0;
                Row row = paramSheet.createRow(paramRow++);

                Cell nameValueCell = row.createCell(paramCol++);
                nameValueCell.setCellValue(currParam.getName());

                Cell valueValueCell = row.createCell(paramCol++);
                valueValueCell.setCellValue(currParam.getValue().toString());

                Cell moduleValueCell = row.createCell(paramCol);
                moduleValueCell.setCellValue(module.getClass().getSimpleName());

            }

        }
    }

    private void prepareMetadataXLSX(SXSSFWorkbook workbook, WorkspaceCollection workspaces) {
        // Basing column names on the first workspace in the WorkspaceCollection
        Workspace exampleWorkspace = workspaces.iterator().next();

        if (exampleWorkspace != null) {
            HCMetadata exampleMetadata = exampleWorkspace.getMetadata();

            if (exampleMetadata.size() != 0) {
                // Adding header rows for the metadata sheet.
                Sheet metaSheet = workbook.createSheet("Metadata");

                // Creating the header row
                int metaRow = 0;
                int metaCol = 0;
                Row metaHeaderRow = metaSheet.createRow(metaRow++);

                // Setting the analysis ID.  This is the same value on each sheet
                Cell IDHeaderCell = metaHeaderRow.createCell(metaCol++);
                IDHeaderCell.setCellValue("ANALYSIS_ID");

                // Running through all the metadata values, adding them as new columns
                for (String name : exampleMetadata.keySet()) {
                    Cell metaHeaderCell = metaHeaderRow.createCell(metaCol++);
                    String metadataName = name.toUpperCase().replaceAll(" ", "_");
                    metaHeaderCell.setCellValue(metadataName);

                }

                // Running through each workspace, adding the relevant values.  Metadata is stored as a LinkedHashMap, so values
                // should always come off in the same order for the same analysis
                for (Workspace workspace : workspaces) {
                    HCMetadata metadata = workspace.getMetadata();

                    metaCol = 0;
                    Row metaValueRow = metaSheet.createRow(metaRow++);

                    // Setting the analysis ID.  This is the same value on each sheet
                    Cell metaValueCell = metaValueRow.createCell(metaCol++);
                    metaValueCell.setCellValue(workspace.getID());

                    // Running through all the metadata values, adding them as new columns
                    for (String name : metadata.keySet()) {
                        metaValueCell = metaValueRow.createCell(metaCol++);
                        metaValueCell.setCellValue(metadata.getAsString(name));

                    }
                }
            }
        }
    }

    private void prepareImagesXLSX(SXSSFWorkbook workbook, WorkspaceCollection workspaces, ModuleCollection modules) {
        // Basing column names on the first workspace in the WorkspaceCollection
        Workspace exampleWorkspace = workspaces.iterator().next();

        if (exampleWorkspace.getImages() != null) {
            // Creating a new sheet for each image.  Each analysed file will have its own row.
            HashMap<String, Sheet> imageSheets = new HashMap<>();
            HashMap<String, Integer> imageRows = new HashMap<>();

            // Using the first workspace in the WorkspaceCollection to initialise column headers
            for (String imageName : exampleWorkspace.getImages().keySet()) {
                Image image = exampleWorkspace.getImages().get(imageName);

                if (image.getSingleMeasurements().size() != 0) {
                    // Creating relevant sheet prefixed with "IM"
                    imageSheets.put(imageName, workbook.createSheet("IM_" + imageName));

                    // Adding headers to each column
                    int col = 0;

                    imageRows.put(imageName, 1);
                    Row imageHeaderRow = imageSheets.get(imageName).createRow(0);

                    // Creating a cell holding the path to the analysed file
                    Cell IDHeaderCell = imageHeaderRow.createCell(col++);
                    IDHeaderCell.setCellValue("ANALYSIS_ID");

                    String[] measurementNames = modules.getMeasurements().getMeasurementNames(imageName);
                    // Adding measurement headers
                    for (String measurementName : measurementNames) {
                        Cell measHeaderCell = imageHeaderRow.createCell(col++);
                        measHeaderCell.setCellValue(measurementName);

                    }
                }
            }

            // Running through each Workspace, adding rows
            for (Workspace workspace : workspaces) {
                for (String imageName : workspace.getImages().keySet()) {
                    Image image = workspace.getImages().get(imageName);

                    if (image.getSingleMeasurements().size() != 0) {
                        // Adding the measurements from this image
                        int col = 0;

                        Row imageValueRow = imageSheets.get(imageName).createRow(imageRows.get(imageName));
                        imageRows.compute(imageName, (k, v) -> v = v + 1);

                        // Creating a cell holding the path to the analysed file
                        Cell IDValueCell = imageValueRow.createCell(col++);
                        IDValueCell.setCellValue(workspace.getID());

                        for (MIAMeasurement measurement : image.getSingleMeasurements().values()) {
                            Cell measValueCell = imageValueRow.createCell(col++);
                            if (Double.isNaN(measurement.getValue())) {
                                measValueCell.setCellValue("");
                            } else {
                                measValueCell.setCellValue(measurement.getValue());
                            }
                        }
                    }
                }
            }
        }
    }

    private void prepareObjectsXLSX(SXSSFWorkbook workbook, WorkspaceCollection workspaces, ModuleCollection modules) {
        // Basing column names on the first workspace in the WorkspaceCollection
        Workspace exampleWorkspace = workspaces.iterator().next();

        if (exampleWorkspace != null) {
            // Creating a new sheet for each object.  Each analysed file has its own set of rows (one for each object)
            HashMap<String, Sheet> objectSheets = new HashMap<>();
            HashMap<String, Integer> objectRows = new HashMap<>();

            // Creating a LinkedHashMap that links relationship ID names to column numbers.  This keeps the correct
            // relationships in the correct columns
            LinkedHashMap<String, LinkedHashMap<Integer,String>> parentNames = new LinkedHashMap<>();
            LinkedHashMap<String, LinkedHashMap<Integer,String>> childNames = new LinkedHashMap<>();

            // Creating a LinkedHashMap that links measurement names to column numbers.  This keeps the correct
            // measurements in the correct columns
            LinkedHashMap<String, LinkedHashMap<Integer,String>> measurementNames = new LinkedHashMap<>();

            // Using the first workspace in the WorkspaceCollection to initialise column headers
            for (String objectName : exampleWorkspace.getObjects().keySet()) {
                ObjSet objects = exampleWorkspace.getObjects().get(objectName);

                // Skipping this object if there are none in the set
                if (!objects.values().iterator().hasNext()) {
                    continue;
                }

                // Getting an example object
                Obj object = objects.values().iterator().next();

                // Creating relevant sheet prefixed with "IM"
                objectSheets.put(objectName, workbook.createSheet("OBJ_" + objectName));

                // Adding headers to each column
                int col = 0;

                objectRows.put(objectName, 1);
                Row objectHeaderRow = objectSheets.get(objectName).createRow(0);

                // Creating a cell holding the path to the analysed file
                Cell IDHeaderCell = objectHeaderRow.createCell(col++);
                IDHeaderCell.setCellValue("ANALYSIS_ID");

                Cell objectIDHeaderCell = objectHeaderRow.createCell(col++);
                objectIDHeaderCell.setCellValue("OBJECT_ID");

                // Adding metadata headers (if enabled)
                if (addMetadataToObjects) {
                    // Running through all the metadata values, adding them as new columns
                    HCMetadata exampleMetadata = exampleWorkspace.getMetadata();
                    for (String name : exampleMetadata.keySet()) {
                        Cell metaHeaderCell = objectHeaderRow.createCell(col++);
                        String metadataName = name.toUpperCase().replaceAll(" ", "_");
                        metaHeaderCell.setCellValue(metadataName);

                    }
                }

                // Adding parent IDs
                RelationshipCollection relationships = modules.getRelationships();
                String[] parents = relationships.getParentNames(objectName);
                if (parents != null) {
                    for (String parent : parents) {
                        parentNames.putIfAbsent(objectName, new LinkedHashMap<>());
                        parentNames.get(objectName).put(col, parent);
                        Cell parentHeaderCell = objectHeaderRow.createCell(col++);
                        parentHeaderCell.setCellValue("PARENT_" + parent + "_ID");

                    }
                }

                // Adding number of children for each child type
                String[] children = relationships.getChildNames(objectName);
                if (children != null) {
                    for (String child : children) {
                        childNames.putIfAbsent(objectName, new LinkedHashMap<>());
                        childNames.get(objectName).put(col, child);
                        Cell childHeaderCell = objectHeaderRow.createCell(col++);
                        childHeaderCell.setCellValue("NUMBER_OF_" + child + "_CHILDREN");

                    }
                }

                // Adding single-valued position headers
                for (int dim:object.getPositions().keySet()) {
                    Cell positionsHeaderCell = objectHeaderRow.createCell(col++);
                    String dimName = dim==3 ? "CHANNEL" : dim == 4 ? "FRAME" : "DIM_"+dim;
                    positionsHeaderCell.setCellValue(dimName);

                }

                // Adding measurement headers
                String[] measurements = modules.getMeasurements().getMeasurementNames(objectName);
                if (measurements != null) {
                    for (String measurement : measurements) {
                        measurementNames.putIfAbsent(objectName, new LinkedHashMap<>());
                        measurementNames.get(objectName).put(col, measurement);
                        Cell measHeaderCell = objectHeaderRow.createCell(col++);
                        measHeaderCell.setCellValue(measurement);

                    }
                }
            }

            // Running through each Workspace, adding rows
            for (Workspace workspace : workspaces) {
                for (String objectName : workspace.getObjects().keySet()) {
                    ObjSet objects = workspace.getObjects().get(objectName);

                    if (objects.values().iterator().hasNext()) {
                        for (Obj object : objects.values()) {
                            // Adding the measurements from this image
                            int col = 0;

                            Row objectValueRow = objectSheets.get(objectName).createRow(objectRows.get(objectName));
                            objectRows.compute(objectName, (k, v) -> v = v + 1);

                            // Creating a cell holding the path to the analysed file
                            Cell IDValueCell = objectValueRow.createCell(col++);
                            IDValueCell.setCellValue(workspace.getID());

                            Cell objectIDValueCell = objectValueRow.createCell(col++);
                            objectIDValueCell.setCellValue(object.getID());

                            // Adding metadata (if enabled)
                            if (addMetadataToObjects) {
                                HCMetadata metadata = workspace.getMetadata();
                                for (String name : metadata.keySet()) {
                                    Cell metaValueCell = objectValueRow.createCell(col++);
                                    metaValueCell.setCellValue(metadata.getAsString(name));

                                }
                            }

                            // Adding parents to the columns specified in parentNames
                            if (parentNames.get(objectName) != null) {
                                for (int column : parentNames.get(objectName).keySet()) {
                                    Cell parentValueCell = objectValueRow.createCell(column);
                                    String parentName = parentNames.get(objectName).get(column);
                                    Obj parent = object.getParent(parentName);
                                    if (parent != null) {
                                        parentValueCell.setCellValue(parent.getID());
                                    } else {
                                        parentValueCell.setCellValue("");
                                    }
                                    col++;
                                }
                            }

                            // Adding number of children to the columns specified in childNames
                            if (childNames.get(objectName) != null) {
                                for (int column : childNames.get(objectName).keySet()) {
                                    Cell childValueCell = objectValueRow.createCell(column);
                                    String childName = childNames.get(objectName).get(column);
                                    ObjSet children = object.getChildren(childName);
                                    if (children != null) {
                                        childValueCell.setCellValue(children.size());
                                    } else {
                                        childValueCell.setCellValue("0");
                                    }
                                    col++;
                                }
                            }

                            // Adding extra dimension positions
                            for (int dim : object.getPositions().keySet()) {
                                Cell positionsValueCell = objectValueRow.createCell(col++);
                                positionsValueCell.setCellValue(object.getPosition(dim));

                            }

                            // Adding measurements to the columns specified in measurementNames
                            for (int column : measurementNames.get(objectName).keySet()) {
                                Cell measValueCell = objectValueRow.createCell(column);
                                String measurementName = measurementNames.get(objectName).get(column);
                                MIAMeasurement measurement = object.getMeasurement(measurementName);

                                // If there isn't a corresponding value for this object, set a blank cell
                                if (measurement == null) {
                                    measValueCell.setCellValue("");
                                    continue;
                                }

                                // If the value is a NaN, also set a blank cell
                                if (Double.isNaN(measurement.getValue())) {
                                    measValueCell.setCellValue("");
                                    continue;

                                }

                                measValueCell.setCellValue(measurement.getValue());

                            }
                        }
                    }
                }
            }
        }
    }

    private void exportJSON(WorkspaceCollection workspaces, Analysis analysis) {
        System.out.println("[WARN] No JSON export currently implemented.  File not saved.");

    }


    // GETTERS AND SETTERS

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

}
