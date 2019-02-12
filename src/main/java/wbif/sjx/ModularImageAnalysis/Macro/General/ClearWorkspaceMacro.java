package wbif.sjx.ModularImageAnalysis.Macro.General;

import ij.macro.MacroExtension;
import wbif.sjx.ModularImageAnalysis.Macro.MacroOperation;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

public class ClearWorkspaceMacro extends MacroOperation {
    public ClearWorkspaceMacro(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public String getName() {
        return "MIA_ClearWorkspace";
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[0];
    }

    @Override
    public String action(Object[] objects, Workspace workspace) {
        workspace.clearAllImages(false);
        workspace.clearAllObjects(false);

        return null;
    }

    @Override
    public String getArgumentsDescription() {
        return "";
    }

    @Override
    public String getDescription() {
        return "Removes all images and objects from the workspace.  This should be run at the beginning of a macro.";
    }
}
