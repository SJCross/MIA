package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Process.IntensityMinMax;

/**
 * Created by sc13967 on 07/06/2017.
 */
public class ImageTypeConverter extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String OUTPUT_TYPE = "Output image type";
    public static final String SCALE_INTENSITIES = "Scale intensities to full range";

    public interface OutputTypes {
        String INT8 = "8-bit integer";
        String INT16 = "16-bit integer";
        String FLOAT32 = "32-bit float";

        String[] ALL = new String[]{INT8, INT16, FLOAT32};

    }

    @Override
    public String getTitle() {
        return "Image type converter";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void run(Workspace workspace, boolean verbose) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputType = parameters.getValue(OUTPUT_TYPE);
        boolean scaleIntensities = parameters.getValue(SCALE_INTENSITIES);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) {inputImagePlus = new Duplicator().run(inputImagePlus);}

        // If necessary, stretching input image intensities to full range
        if (scaleIntensities) IntensityMinMax.run(inputImagePlus,true);

        // Converting to requested type
        switch (outputType) {
            case OutputTypes.INT8:
                IJ.run(inputImagePlus, "8-bit", null);
                break;
            case OutputTypes.INT16:
                IJ.run(inputImagePlus, "16-bit", null);
                break;
            case OutputTypes.FLOAT32:
                IJ.run(inputImagePlus, "32-bit", null);
                break;
        }

        // Adding output image to workspace if necessary
        if (!applyToInput) {
            String outputImageName = parameters.getValue(OUTPUT_IMAGE);
            if (verbose) System.out.println("["+moduleName+"] Adding image ("+outputImageName+") to workspace");
            Image outputImage = new Image(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);

        }
    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.addParameter(new Parameter(APPLY_TO_INPUT, Parameter.BOOLEAN,true));
        parameters.addParameter(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.addParameter(new Parameter(OUTPUT_TYPE, Parameter.CHOICE_ARRAY,OutputTypes.INT8,OutputTypes.ALL));
        parameters.addParameter(new Parameter(SCALE_INTENSITIES, Parameter.BOOLEAN,false));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.addParameter(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.addParameter(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.addParameter(parameters.getParameter(OUTPUT_TYPE));
        returnedParameters.addParameter(parameters.getParameter(SCALE_INTENSITIES));

        return returnedParameters;

    }

    @Override
    public void initialiseReferences() {

    }

    @Override
    public ReferenceCollection updateAndGetImageReferences() {
        return null;
    }

    @Override
    public ReferenceCollection updateAndGetObjectReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
