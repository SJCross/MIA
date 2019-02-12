package wbif.sjx.ModularImageAnalysis.Macro.ObjectProcessing;

import ij.macro.MacroExtension;
import wbif.sjx.ModularImageAnalysis.Macro.MacroOperation;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification.IdentifyObjects;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

public class IdentifyObjectsMacro extends MacroOperation {
    public IdentifyObjectsMacro(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public String getName() {
        return "MIA_IdentifyObjects";
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_STRING,ARG_NUMBER,ARG_NUMBER,ARG_NUMBER,ARG_NUMBER};
    }

    @Override
    public String action(Object[] objects, Workspace workspace) {
        IdentifyObjects identifyObjects = new IdentifyObjects();

        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,objects[0]);
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,objects[1]);
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,((double) objects[2] == 1));
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT,((double) objects[3] == 1));

        if ((double) objects[4] == 6) {
            identifyObjects.updateParameterValue(IdentifyObjects.CONNECTIVITY,IdentifyObjects.Connectivity.SIX);
        } else if ((double) objects[4] == 26) {
            identifyObjects.updateParameterValue(IdentifyObjects.CONNECTIVITY,IdentifyObjects.Connectivity.TWENTYSIX);
        } else {
            System.err.println("Connectivity must be set to either 6 or 26.");
            return null;
        }

        identifyObjects.setShowOutput((double) objects[5] == 1);

        identifyObjects.run(workspace);

        return null;

    }

    @Override
    public String getArgumentsDescription() {
        return "String inputImageName, String outputObjectsName, boolean whiteBackground, boolean singleObject, "+
                "int connectivity, boolean showObjects";
    }

    @Override
    public String getDescription() {
        return "Uses connected component labelling to convert a binary image to objects.  Connectivity must be set to "+
                "either 6 or 26.";
    }
}
