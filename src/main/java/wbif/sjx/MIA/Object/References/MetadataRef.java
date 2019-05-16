package wbif.sjx.MIA.Object.References;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.w3c.dom.NamedNodeMap;
import wbif.sjx.MIA.Object.References.Abstract.Ref;
import wbif.sjx.MIA.Object.References.Abstract.SpreadsheetWriter;
import wbif.sjx.MIA.Object.Workspace;

import java.util.LinkedHashMap;

public class MetadataRef extends Ref implements SpreadsheetWriter {
    public MetadataRef(NamedNodeMap attributes) {
        super(attributes);
    }

    public MetadataRef(String name) {
        super(name);
    }

    @Override
    public void addSummaryXLSX(Sheet sheet, LinkedHashMap<Integer,Workspace> workspaces) {
        if (!isExportIndividual() && !isExportGlobal()) return;

        // Getting the column number for this reference
        Row titleRow = sheet.getRow(0);
        int col = titleRow.getLastCellNum();
        if (col == -1) col++;

        // Adding the heading to the title row
        Cell cell = titleRow.createCell(col);
        cell.setCellValue("META // "+getNickname());

        // Adding to each row
        for (int rowN:workspaces.keySet()) {
            Row row = sheet.getRow(rowN);
            cell = row.createCell(col);
            Workspace workspace = workspaces.get(rowN);
            cell.setCellValue(workspace.getMetadata().getAsString(getNickname()));
        }
    }
}
