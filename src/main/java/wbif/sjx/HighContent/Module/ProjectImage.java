package wbif.sjx.HighContent.Module;

import ij.ImagePlus;
import ij.plugin.ZProjector;
import wbif.sjx.HighContent.Object.*;
import wbif.sjx.HighContent.Object.HCParameterCollection;

/**
 * Created by sc13967 on 04/05/2017.
 */
public class ProjectImage extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public HCImage projectImageInZ(HCImage inputImage, HCName outputImageName) {
        ZProjector z_projector = new ZProjector(inputImage.getImagePlus());
        z_projector.setMethod(ZProjector.MAX_METHOD);
        z_projector.doProjection();
        ImagePlus iplOut = z_projector.getProjection();

        return new HCImage(outputImageName,iplOut);

    }

    @Override
    public String getTitle() {
        return "Project image";

    }

    @Override
    public void execute(HCWorkspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Loading image into workspace
        HCName inputImageName = parameters.getValue(INPUT_IMAGE);
        HCImage inputImage = workspace.getImages().get(inputImageName);

        // Getting output image name
        HCName outputImageName = parameters.getValue(OUTPUT_IMAGE);

        // Create max projection image
        HCImage outputImage = projectImageInZ(inputImage,outputImageName);

        // Adding projected image to workspace
        workspace.addImage(outputImage);

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(INPUT_IMAGE, HCParameter.INPUT_IMAGE,null));
        parameters.addParameter(new HCParameter(OUTPUT_IMAGE, HCParameter.OUTPUT_IMAGE,null));

    }

    @Override
    public HCParameterCollection getActiveParameters() {
        return parameters;
    }

    @Override
    public void addMeasurements(HCMeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(HCRelationshipCollection relationships) {

    }
}
