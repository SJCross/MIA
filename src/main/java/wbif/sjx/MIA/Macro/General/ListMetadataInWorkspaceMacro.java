package wbif.sjx.MIA.Macro.General;

import ij.macro.MacroExtension;
import ij.measure.ResultsTable;
import wbif.sjx.MIA.Macro.MacroOperation;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.common.Object.HCMetadata;

import java.util.HashMap;

public class ListMetadataInWorkspaceMacro extends MacroOperation {
    public ListMetadataInWorkspaceMacro(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public String getName() {
        return "MIA_ListMetadataInWorkspace";
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[0];
    }

    @Override
    public String action(Object[] objects, Workspace workspace) {
        // Creating a new ResultsTable to hold the Image names
        ResultsTable rt = new ResultsTable();
        int row = 0;

        // Getting a list of Images in the Workspace
        HCMetadata metadata = workspace.getMetadata();
        for (String metadataName:metadata.keySet()) {
            if (row != 0) rt.incrementCounter();

            rt.setValue("Metadata name",row,metadataName);
            rt.setValue("Metadata value",row,metadata.getAsString(metadataName));

            row++;

        }

        rt.show("Metadata in workspace");

        return null;

    }

    @Override
    public String getArgumentsDescription() {
        return "";
    }

    @Override
    public String getDescription() {
        return "Returns a list of metadata values currently in the workspace.";
    }
}
