package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing;

import fiji.threshold.Auto_Threshold;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import org.apache.poi.ss.formula.functions.T;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Process.IntensityMinMax;

/**
 * Created by sc13967 on 06/06/2017.
 */
public class ThresholdImage extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String THRESHOLD_MODE = "Threshold mode";
    public static final String THRESHOLD_MULTIPLIER = "Threshold multiplier";
    public static final String USE_LOWER_THRESHOLD_LIMIT = "Use lower threshold limit";
    public static final String LOWER_THRESHOLD_LIMIT = "Lower threshold limit";
    public static final String USE_UPPER_THRESHOLD_LIMIT = "Use upper threshold limit";
    public static final String UPPER_THRESHOLD_LIMIT = "Upper threshold limit";
    public static final String WHITE_BACKGROUND = "Black objects/white background";
    public static final String SHOW_IMAGE = "Show image";

    public interface ThresholdModes {
        String HUANG = "Huang";
        String OTSU = "Otsu";
        String TRIANGLE = "Triangle";

        String[] ALL = new String[]{HUANG, OTSU, TRIANGLE};

    }


    @Override
    public String getTitle() {
        return "Threshold image";
    }

    @Override
    public String getHelp() {
        return "INCOMPLETE";
    }

    @Override
    public void run(Workspace workspace, boolean verbose) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String thresholdMode = parameters.getValue(THRESHOLD_MODE);
        double thrMult = parameters.getValue(THRESHOLD_MULTIPLIER);
        boolean whiteBackground = parameters.getValue(WHITE_BACKGROUND);
        boolean useLowerThresholdLimit = parameters.getValue(USE_LOWER_THRESHOLD_LIMIT);
        double lowerThresholdLimit = parameters.getValue(LOWER_THRESHOLD_LIMIT);
        boolean useUpperThresholdLimit = parameters.getValue(USE_UPPER_THRESHOLD_LIMIT);
        double upperThresholdLimit = parameters.getValue(UPPER_THRESHOLD_LIMIT);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) {inputImagePlus = new Duplicator().run(inputImagePlus);}

        // Image must be 8-bit
        IntensityMinMax.run(inputImagePlus,true);
        IJ.run(inputImagePlus,"8-bit",null);

        // Applying selected threshold
        Auto_Threshold auto_threshold = new Auto_Threshold();
        Object[] results1 = new Object[]{0};

        // Calculating the threshold based on the selected algorithm
        switch (thresholdMode) {
            case ThresholdModes.HUANG:
                if (verbose) System.out.println("["+moduleName+"] Applying global Huang threshold (multplier = "+thrMult+" x)");
                results1 = auto_threshold.exec(inputImagePlus,"Huang",true,false,true,true,false,true);
                break;

            case ThresholdModes.OTSU:
                if (verbose) System.out.println("["+moduleName+"] Applying global Otsu threshold (multplier = "+thrMult+" x)");
                results1 = auto_threshold.exec(inputImagePlus,"Otsu",true,false,true,true,false,true);
                break;

            case ThresholdModes.TRIANGLE:
                if (verbose) System.out.println("["+moduleName+"] Applying global Triangle threshold (multplier = "+thrMult+" x)");
                results1 = auto_threshold.exec(inputImagePlus,"Triangle",true,false,true,true,false,true);
                break;
        }

        // Applying limits, where applicable
        System.out.println("Res1 "+results1[0]+"_"+lowerThresholdLimit);
        if (useLowerThresholdLimit && (int) results1[0] < lowerThresholdLimit) {
            results1[0] = (int) Math.round(lowerThresholdLimit);
        }
        System.out.println("Res1 "+results1[0]+"_"+lowerThresholdLimit);
        System.out.println("Res1 "+results1[0]+"_"+upperThresholdLimit);
        if (useUpperThresholdLimit && (int) results1[0] > upperThresholdLimit) {
            results1[0] = (int) Math.round(upperThresholdLimit);
        }
        System.out.println("Res1 "+results1[0]+"_"+upperThresholdLimit);

        // Applying threshold
        for (int z = 1; z <= inputImagePlus.getNSlices(); z++) {
            for (int c = 1; c <= inputImagePlus.getNChannels(); c++) {
                for (int t = 1; t <= inputImagePlus.getNFrames(); t++) {
                    inputImagePlus.setPosition(c, z, t);
                    inputImagePlus.getProcessor().threshold((int) Math.round((int) results1[0]*thrMult));

                }
            }
        }

        if (whiteBackground) {
            for (int z = 1; z <= inputImagePlus.getNSlices(); z++) {
                for (int c = 1; c <= inputImagePlus.getNChannels(); c++) {
                    for (int t = 1; t <= inputImagePlus.getNFrames(); t++) {
                        inputImagePlus.setPosition(c, z, t);
                        inputImagePlus.getProcessor().invert();
                    }
                }
            }
        }

        inputImagePlus.setPosition(1,1,1);

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            String outputImageName = parameters.getValue(OUTPUT_IMAGE);
            Image outputImage = new Image(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);

            // If selected, displaying the image
            if (parameters.getValue(SHOW_IMAGE)) {
                new Duplicator().run(outputImage.getImagePlus()).show();
            }

        } else {
            // If selected, displaying the image
            if (parameters.getValue(SHOW_IMAGE)) {
                new Duplicator().run(inputImagePlus).show();
            }
        }
    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.addParameter(new Parameter(APPLY_TO_INPUT, Parameter.BOOLEAN,true));
        parameters.addParameter(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.addParameter(new Parameter(THRESHOLD_MODE, Parameter.CHOICE_ARRAY,ThresholdModes.HUANG,ThresholdModes.ALL));
        parameters.addParameter(new Parameter(THRESHOLD_MULTIPLIER, Parameter.DOUBLE,1.0));
        parameters.addParameter(new Parameter(USE_LOWER_THRESHOLD_LIMIT, Parameter.BOOLEAN, false));
        parameters.addParameter(new Parameter(LOWER_THRESHOLD_LIMIT, Parameter.DOUBLE, 0.0));
        parameters.addParameter(new Parameter(USE_UPPER_THRESHOLD_LIMIT, Parameter.BOOLEAN, false));
        parameters.addParameter(new Parameter(UPPER_THRESHOLD_LIMIT, Parameter.DOUBLE, 65535.0));
        parameters.addParameter(new Parameter(WHITE_BACKGROUND, Parameter.BOOLEAN,true));
        parameters.addParameter(new Parameter(SHOW_IMAGE, Parameter.BOOLEAN,false));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.addParameter(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.addParameter(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.addParameter(parameters.getParameter(THRESHOLD_MODE));
        returnedParameters.addParameter(parameters.getParameter(THRESHOLD_MULTIPLIER));
        returnedParameters.addParameter(parameters.getParameter(USE_LOWER_THRESHOLD_LIMIT));

        if (parameters.getValue(USE_LOWER_THRESHOLD_LIMIT)) {
            returnedParameters.addParameter(parameters.getParameter(LOWER_THRESHOLD_LIMIT));
        }

        returnedParameters.addParameter(parameters.getParameter(USE_UPPER_THRESHOLD_LIMIT));

        if (parameters.getValue(USE_UPPER_THRESHOLD_LIMIT)) {
            returnedParameters.addParameter(parameters.getParameter(UPPER_THRESHOLD_LIMIT));
        }

        returnedParameters.addParameter(parameters.getParameter(WHITE_BACKGROUND));
        returnedParameters.addParameter(parameters.getParameter(SHOW_IMAGE));

        return returnedParameters;

    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
