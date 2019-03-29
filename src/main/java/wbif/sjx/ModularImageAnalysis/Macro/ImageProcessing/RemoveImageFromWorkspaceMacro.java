package wbif.sjx.ModularImageAnalysis.Macro.ImageProcessing;

import ij.macro.MacroExtension;
import wbif.sjx.ModularImageAnalysis.Macro.MacroOperation;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack.RemoveImage;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

public class RemoveImageFromWorkspaceMacro extends MacroOperation {
    public RemoveImageFromWorkspaceMacro(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public String getName() {
        return "MIA_RemoveImageFromWorkspace";
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_NUMBER};
    }

    @Override
    public String action(Object[] objects, Workspace workspace) {
        RemoveImage removeImage = new RemoveImage();

        removeImage.updateParameterValue(RemoveImage.INPUT_IMAGE,objects[0]);
        removeImage.updateParameterValue(RemoveImage.RETAIN_MEASUREMENTS,(double) objects[1] == 1);

        removeImage.process(workspace);

        return null;

    }

    @Override
    public String getArgumentsDescription() {
        return "String imageName, boolean retainMeasurements";
    }

    @Override
    public String getDescription() {
        return "Removes the specified image from the workspace.  If \"Retain measurements\" is true, any measurements"+
                " will be left available for export.";
    }
}
