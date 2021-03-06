package wbif.sjx.MIA.Macro.ImageMeasurements;

import ij.macro.MacroExtension;
import wbif.sjx.MIA.Macro.MacroOperation;
import wbif.sjx.MIA.Module.ImageMeasurements.MeasureImageIntensity;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Workspace;

public class MIA_MeasureImageIntensity extends MacroOperation {
    public MIA_MeasureImageIntensity(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_NUMBER};
    }

    @Override
    public String action(Object[] objects, Workspace workspace, ModuleCollection modules) {
        MeasureImageIntensity measureImageIntensity = new MeasureImageIntensity(modules);

        measureImageIntensity.updateParameterValue(MeasureImageIntensity.INPUT_IMAGE,objects[0]);
        measureImageIntensity.setShowOutput((double) objects[1] == 1);

        measureImageIntensity.process(workspace);

        return null;

    }

    @Override
    public String getArgumentsDescription() {
        return "String imageName, boolean showResults";
    }

    @Override
    public String getDescription() {
        return "Measure the intensity of the specified image.";
    }
}
