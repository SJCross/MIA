package wbif.sjx.MIA.Macro.General;

import ij.macro.MacroExtension;
import ij.measure.ResultsTable;
import wbif.sjx.MIA.Macro.MacroOperation;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Workspace;

import java.util.HashMap;

public class ListImagesInWorkspaceMacro extends MacroOperation {
    public ListImagesInWorkspaceMacro(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public String getName() {
        return "MIA_ListImagesInWorkspace";
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
        HashMap<String,Image<?>> images = workspace.getImages();
        for (String imageName:images.keySet()) {
            if (row != 0) rt.incrementCounter();

            String measurementsOnly = Boolean.toString(images.get(imageName).getImagePlus() == null);

            rt.setValue("Image name",row,imageName);
            rt.setValue("Measurements only",row,measurementsOnly);

            row++;

        }

        rt.show("Images in workspace");

        return null;

    }

    @Override
    public String getArgumentsDescription() {
        return "";
    }

    @Override
    public String getDescription() {
        return "Returns a list of images currently in the workspace.  \"Measurements only\" is true if the image data"+
                " has been removed, leaving measurements only";
    }
}
