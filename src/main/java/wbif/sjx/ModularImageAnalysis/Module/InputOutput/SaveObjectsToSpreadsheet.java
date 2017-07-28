package wbif.sjx.ModularImageAnalysis.Module.InputOutput;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;

/**
 * Created by sc13967 on 26/05/2017.
 */
public class SaveObjectsToSpreadsheet extends HCModule {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_FILE = "Output file";

    @Override
    public String getTitle() {
        return "Save objects to spreadsheet";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void execute(Workspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjSet inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting file to save to
        String exportFilePath = parameters.getValue(OUTPUT_FILE);

        // Initialising the workbook
        XSSFWorkbook workbook = new XSSFWorkbook();

        int row = 0;

        // Creating column headers
        if (inputObjects.values().iterator().next().getMeasurements().size() != 0) {
            // Creating relevant sheet prefixed with "IM"
            XSSFSheet sheet = workbook.createSheet("OBJ_" + inputObjectsName);

            // Adding headers to each column
            int col = 0;

            Row objectHeaderRow = sheet.createRow(row++);

            // Creating a cell holding the path to the analysed file
            Cell IDHeaderCell = objectHeaderRow.createCell(col++);
            IDHeaderCell.setCellValue("ANALYSIS_ID");

            Cell objectIDHeaderCell = objectHeaderRow.createCell(col++);
            objectIDHeaderCell.setCellValue("OBJECT_ID");

            Obj object = inputObjects.values().iterator().next();

            // Getting parents
            LinkedHashMap<String,Obj> parents = object.getParents();
            for (String parent:parents.keySet()) {
                Cell parentHeaderCell = objectHeaderRow.createCell(col++);
                String name = (parent+"_ID").toUpperCase();
                parentHeaderCell.setCellValue(name);
            }

            // Adding single-valued position headers
            for (int dim : object.getPositions().keySet()) {
                Cell positionsHeaderCell = objectHeaderRow.createCell(col++);
                String dimName = dim == 3 ? "CHANNEL" : dim == 4 ? "FRAME" : "DIM_" + dim;
                positionsHeaderCell.setCellValue(dimName);

            }

            // Adding measurement headers
            for (MIAMeasurement measurement : object.getMeasurements().values()) {
                Cell measHeaderCell = objectHeaderRow.createCell(col++);
                String measurementName = measurement.getName().toUpperCase().replaceAll(" ", "_");
                measHeaderCell.setCellValue(measurementName);

            }

            // Running through each object, adding a new row
            for (Obj inputObject : inputObjects.values()) {
                col = 0;
                Row objectValueRow = sheet.createRow(row++);

                // Creating a cell holding the path to the analysed file
                Cell IDValueCell = objectValueRow.createCell(col++);
                IDValueCell.setCellValue(workspace.getID());

                Cell objectIDValueCell = objectValueRow.createCell(col++);
                objectIDValueCell.setCellValue(inputObject.getID());

                parents = inputObject.getParents();
                for (String parent:parents.keySet()) {
                    Cell parentValueCell = objectValueRow.createCell(col++);
                    parentValueCell.setCellValue(parents.get(parent).getID());
                }

                for (int dim : inputObject.getPositions().keySet()) {
                    Cell positionsValueCell = objectValueRow.createCell(col++);
                    positionsValueCell.setCellValue(inputObject.getPosition(dim));

                }

                for (MIAMeasurement measurement : inputObject.getMeasurements().values()) {
                    Cell measValueCell = objectValueRow.createCell(col++);
                    measValueCell.setCellValue(measurement.getValue());
                }
            }

            // Writing the workbook to file
            String outPath = FilenameUtils.removeExtension(exportFilePath) +".xlsx";

            try {
                FileOutputStream outputStream = new FileOutputStream(outPath);
                workbook.write(outputStream);
                workbook.close();

                if (verbose) System.out.println("["+moduleName+"] Saved "+ outPath);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.addParameter(new Parameter(OUTPUT_FILE, Parameter.FILE_PATH,null));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        return parameters;
    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
