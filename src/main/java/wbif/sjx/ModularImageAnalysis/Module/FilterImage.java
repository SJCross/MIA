package wbif.sjx.ModularImageAnalysis.Module;

import ij.ImagePlus;
import ij.plugin.Duplicator;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by sc13967 on 30/05/2017.
 */
public class FilterImage extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String FILTER_MODE = "Filter mode";
    public static final String FILTER_RADIUS = "Filter radius (px)";
    public static final String CALIBRATED_UNITS = "Calibrated units";

    private static final String[] FILTER_MODES = new String[]{"Test1","Test2"};

    @Override
    public String getTitle() {
        return "Filter image";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void execute(HCWorkspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting input image
        HCName inputImageName = parameters.getValue(INPUT_IMAGE);
        HCImage inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String filterMode = parameters.getValue(FILTER_MODE);
        double filterRadius = parameters.getValue(FILTER_RADIUS);
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) {inputImagePlus = new Duplicator().run(inputImagePlus);}

        
        // STILL NEED TO DO THE PROCESSING


        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            HCName outputImageName = parameters.getValue(OUTPUT_IMAGE);
            HCImage outputImage = new HCImage(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);

        }

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(INPUT_IMAGE,HCParameter.INPUT_IMAGE,null));
        parameters.addParameter(new HCParameter(APPLY_TO_INPUT,HCParameter.BOOLEAN,true));
        parameters.addParameter(new HCParameter(OUTPUT_IMAGE,HCParameter.OUTPUT_IMAGE,null));
        parameters.addParameter(new HCParameter(FILTER_MODE,HCParameter.CHOICE_ARRAY,FILTER_MODES[0],FILTER_MODES));
        parameters.addParameter(new HCParameter(FILTER_RADIUS,HCParameter.DOUBLE,2d));
        parameters.addParameter(new HCParameter(CALIBRATED_UNITS,HCParameter.BOOLEAN,false));

    }

    @Override
    public HCParameterCollection getActiveParameters() {
        HCParameterCollection returnedParameters = new HCParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.addParameter(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.addParameter(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.addParameter(parameters.getParameter(FILTER_MODE));
        returnedParameters.addParameter(parameters.getParameter(FILTER_RADIUS));
        returnedParameters.addParameter(parameters.getParameter(CALIBRATED_UNITS));

        return returnedParameters;

    }

    @Override
    public void addMeasurements(HCMeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(HCRelationshipCollection relationships) {

    }
}
