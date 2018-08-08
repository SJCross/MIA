// TODO: Add true 3D local thresholds (local auto thresholding works slice-by-slice)

package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel;

import fiji.threshold.Auto_Local_Threshold;
import fiji.threshold.Auto_Threshold;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.plugin.Duplicator;
import ij.process.AutoThresholder;
import ij.process.ByteProcessor;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Filters.AutoLocalThreshold3D;
import wbif.sjx.common.Process.IntensityMinMax;

/**
 * Created by sc13967 on 06/06/2017.
 */
public class ThresholdImage extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String THRESHOLD_TYPE = "Threshold type";
    public static final String GLOBAL_ALGORITHM = "Global threshold algorithm";
    public static final String LOCAL_ALGORITHM = "Local threshold algorithm";
    public static final String THRESHOLD_MULTIPLIER = "Threshold multiplier";
    public static final String USE_LOWER_THRESHOLD_LIMIT = "Use lower threshold limit";
    public static final String LOWER_THRESHOLD_LIMIT = "Lower threshold limit";
    public static final String LOCAL_RADIUS = "Local radius";
    public static final String SPATIAL_UNITS = "Spatial units";
    public static final String USE_GLOBAL_Z = "Use full Z-range (\"Global Z\")";
    public static final String WHITE_BACKGROUND = "Black objects/white background";

    public interface ThresholdTypes {
        String GLOBAL_TYPE = "Global";
        String LOCAL_TYPE = "Local";

        String[] ALL = new String[]{GLOBAL_TYPE,LOCAL_TYPE};

    }

    public interface GlobalAlgorithms {
        String HUANG = "Huang";
        String INTERMODES = "Intermodes";
        String ISO_DATA = "IsoData";
        String LI = "Li";
        String MAX_ENTROPY = "MaxEntropy";
        String MEAN = "Mean";
        String MIN_ERROR = "MinError";
        String MINIMUM = "Minimum";
        String MOMENTS = "Moments";
        String OTSU = "Otsu";
        String PERCENTILE = "Percentile";
        String RENYI_ENTROPY = "RenyiEntropy";
        String SHANBHAG = "Shanbhag";
        String TRIANGLE = "Triangle";
        String YEN = "Yen";

        String[] ALL = new String[]{HUANG, INTERMODES, ISO_DATA, LI, MAX_ENTROPY, MEAN, MIN_ERROR, MINIMUM, MOMENTS, OTSU, PERCENTILE, RENYI_ENTROPY, SHANBHAG, TRIANGLE, YEN};

    }

    public interface LocalAlgorithms {
        String BERNSEN_3D = "Bernsen (3D)";
        String CONTRAST_3D = "Contrast (3D)";
        String MEAN_3D = "Mean (3D)";
        String MEDIAN_3D = "Median (3D)";
        String PHANSALKAR_3D = "Phansalkar (3D)";
        String PHANSALKAR_SLICE = "Phansalkar (slice-by-slice)";

        String[] ALL = new String[]{BERNSEN_3D,CONTRAST_3D,MEAN_3D,MEDIAN_3D,PHANSALKAR_3D,PHANSALKAR_SLICE};

    }

    public interface SpatialUnits {
        String CALIBRATED = "Calibrated";
        String PIXELS = "Pixel";

        String[] ALL = new String[]{CALIBRATED,PIXELS};

    }

    public void applyGlobalThresholdToStack(ImagePlus inputImagePlus, String algorithm, double thrMult,
                                            boolean useLowerLim, double lowerLim) {
        // Compiling stack histogram
        int[] histogram = null;
        for (int z = 1; z <= inputImagePlus.getNSlices(); z++) {
            for (int c = 1; c <= inputImagePlus.getNChannels(); c++) {
                for (int t = 1; t <= inputImagePlus.getNFrames(); t++) {
                    inputImagePlus.setPosition(c, z, t);
                    if (histogram == null) {
                        histogram = inputImagePlus.getProcessor().getHistogram();
                    } else {
                        int[] tempHist = inputImagePlus.getProcessor().getHistogram();
                        for (int i=0;i<histogram.length;i++) histogram[i] = histogram[i] + tempHist[i];
                    }
                }
            }
        }

        int threshold = new AutoThresholder().getThreshold(algorithm,histogram);

        // Applying limits, where applicable
        if (useLowerLim && threshold < lowerLim) {
            threshold = (int) Math.round(lowerLim);
        }

        // Applying threshold
        for (int z = 1; z <= inputImagePlus.getNSlices(); z++) {
            for (int c = 1; c <= inputImagePlus.getNChannels(); c++) {
                for (int t = 1; t <= inputImagePlus.getNFrames(); t++) {
                    inputImagePlus.setPosition(c, z, t);
                    inputImagePlus.getProcessor().threshold((int) Math.round(threshold*thrMult));
                }
            }
        }

        inputImagePlus.setPosition(1,1,1);

    }

    public void applyLocalThresholdToStack(ImagePlus inputImagePlus, String algorithm, double localRadius) {
        // Applying threshold
        for (int z = 1; z <= inputImagePlus.getNSlices(); z++) {
            for (int c = 1; c <= inputImagePlus.getNChannels(); c++) {
                for (int t = 1; t <= inputImagePlus.getNFrames(); t++) {
                    inputImagePlus.setPosition(c, z, t);
                    Object[] results = new Auto_Local_Threshold().exec(inputImagePlus,algorithm,(int) localRadius,0,0,true);
                    inputImagePlus.setProcessor(((ImagePlus) results[0]).getProcessor());

                }
            }
        }
        inputImagePlus.setPosition(1,1,1);
    }

    public void applyLocalThreshold3D(ImagePlus inputImagePlus, String algorithm, double localRadius, double thrMult,
                                      boolean useLowerLim, double lowerLim, boolean globalZ) {

        double localRadiusZ;
        if (globalZ) {
            localRadiusZ = inputImagePlus.getNSlices()/2;
        } else {
            localRadiusZ = localRadius*inputImagePlus.getCalibration().getX(1) / inputImagePlus.getCalibration().getZ(1);
        }

        AutoLocalThreshold3D alt3D = new AutoLocalThreshold3D();
        if (useLowerLim) alt3D.setLowerThreshold((int) lowerLim);

        alt3D.exec(inputImagePlus,algorithm,(int) Math.round(localRadius),(int) Math.round(localRadiusZ),thrMult,0,0,true);

    }

    @Override
    public String getTitle() {
        return "Threshold image";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_PIXEL;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public void run(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String thresholdType = parameters.getValue(THRESHOLD_TYPE);
        String globalThresholdAlgorithm = parameters.getValue(GLOBAL_ALGORITHM);
        String localThresholdAlgorithm = parameters.getValue(LOCAL_ALGORITHM);
        double thrMult = parameters.getValue(THRESHOLD_MULTIPLIER);
        boolean whiteBackground = parameters.getValue(WHITE_BACKGROUND);
        boolean useLowerLim = parameters.getValue(USE_LOWER_THRESHOLD_LIMIT);
        double lowerLim = parameters.getValue(LOWER_THRESHOLD_LIMIT);
        double localRadius = parameters.getValue(LOCAL_RADIUS);
        String spatialUnits = parameters.getValue(SPATIAL_UNITS);
        boolean useGlobalZ = parameters.getValue(USE_GLOBAL_Z);

        if (spatialUnits.equals(SpatialUnits.CALIBRATED)) {
            localRadius = inputImagePlus.getCalibration().getRawX(localRadius);
        }

        Prefs.blackBackground = !whiteBackground;

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) {inputImagePlus = new Duplicator().run(inputImagePlus);}

        // Image must be 8-bit
        if (inputImagePlus.getBitDepth() != 8) {
            System.err.println("[ThresholdImage] Image \""+inputImageName+"\" converted to 8-bit with normalised intensity");
            IntensityMinMax.run(inputImagePlus, true);
            IJ.run(inputImagePlus, "8-bit", null);
        }

        // Calculating the threshold based on the selected algorithm
        switch (thresholdType) {
            case ThresholdTypes.GLOBAL_TYPE:
                writeMessage("Applying global "+globalThresholdAlgorithm+" threshold (multplier = "+thrMult+" x)");
                applyGlobalThresholdToStack(inputImagePlus,globalThresholdAlgorithm,thrMult,useLowerLim,lowerLim);
                break;

            case ThresholdTypes.LOCAL_TYPE:
                switch (localThresholdAlgorithm) {
                    case LocalAlgorithms.BERNSEN_3D:
                        writeMessage("Applying local Bernsen threshold (radius = "+localRadius+" px)");
                        applyLocalThreshold3D(inputImagePlus,AutoLocalThreshold3D.BERNSEN,localRadius,thrMult,
                                useLowerLim,lowerLim,useGlobalZ);
                        break;

                    case LocalAlgorithms.CONTRAST_3D:
                        writeMessage("Applying local Contrast threshold (radius = "+localRadius+" px)");
                        applyLocalThreshold3D(inputImagePlus,AutoLocalThreshold3D.CONTRAST,localRadius,thrMult,
                                useLowerLim,lowerLim,useGlobalZ);
                        break;

                    case LocalAlgorithms.MEAN_3D:
                        writeMessage("Applying local Mean threshold (radius = "+localRadius+" px)");
                        applyLocalThreshold3D(inputImagePlus,AutoLocalThreshold3D.MEAN,localRadius,thrMult,useLowerLim,
                                lowerLim,useGlobalZ);
                        break;

                    case LocalAlgorithms.MEDIAN_3D:
                        writeMessage("Applying local Median threshold (radius = "+localRadius+" px)");
                        applyLocalThreshold3D(inputImagePlus,AutoLocalThreshold3D.MEDIAN,localRadius,thrMult,useLowerLim,
                                lowerLim,useGlobalZ);
                        break;

                    case LocalAlgorithms.PHANSALKAR_3D:
                        writeMessage("Applying local Phansalkar threshold (radius = "+localRadius+" px)");
                        applyLocalThreshold3D(inputImagePlus,AutoLocalThreshold3D.PHANSALKAR,localRadius,thrMult,
                                useLowerLim,lowerLim,useGlobalZ);
                        break;

                    case LocalAlgorithms.PHANSALKAR_SLICE:
                        writeMessage("Applying local Phansalkar threshold (radius = "+localRadius+" px)");
                        applyLocalThresholdToStack(inputImagePlus,"Phansalkar",localRadius);
                        break;

                }
                break;

        }

        if (whiteBackground) InvertIntensity.process(inputImagePlus);

        // If the image is being saved as a new image, adding it to the workspace
        if (applyToInput) {
            inputImage.setImagePlus(inputImagePlus);
            // If selected, displaying the image
            if (showOutput) {
                ImagePlus showIpl = new Duplicator().run(inputImagePlus);
                showIpl.setTitle(inputImageName);
                showIpl.show();
            }

        } else {
            String outputImageName = parameters.getValue(OUTPUT_IMAGE);
            Image outputImage = new Image(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);

            // If selected, displaying the image
            if (showOutput) {
                ImagePlus showIpl = new Duplicator().run(outputImage.getImagePlus());
                showIpl.setTitle(outputImageName);
                showIpl.show();
            }
        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(APPLY_TO_INPUT, Parameter.BOOLEAN,true));
        parameters.add(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.add(
                new Parameter(THRESHOLD_TYPE,Parameter.CHOICE_ARRAY,ThresholdTypes.GLOBAL_TYPE,ThresholdTypes.ALL));
        parameters.add(
                new Parameter(GLOBAL_ALGORITHM,Parameter.CHOICE_ARRAY,GlobalAlgorithms.HUANG,GlobalAlgorithms.ALL));
        parameters.add(
                new Parameter(LOCAL_ALGORITHM,Parameter.CHOICE_ARRAY,LocalAlgorithms.PHANSALKAR_3D,LocalAlgorithms.ALL));
        parameters.add(new Parameter(THRESHOLD_MULTIPLIER, Parameter.DOUBLE,1.0));
        parameters.add(new Parameter(USE_LOWER_THRESHOLD_LIMIT, Parameter.BOOLEAN, false));
        parameters.add(new Parameter(LOWER_THRESHOLD_LIMIT, Parameter.DOUBLE, 0.0));
        parameters.add(new Parameter(LOCAL_RADIUS, Parameter.DOUBLE, 1.0));
        parameters.add(
                new Parameter(SPATIAL_UNITS, Parameter.CHOICE_ARRAY, SpatialUnits.PIXELS, SpatialUnits.ALL));
        parameters.add(new Parameter(USE_GLOBAL_Z,Parameter.BOOLEAN,false));
        parameters.add(new Parameter(WHITE_BACKGROUND, Parameter.BOOLEAN,true));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(THRESHOLD_TYPE));
        returnedParameters.add(parameters.getParameter(THRESHOLD_MULTIPLIER));

        switch ((String) parameters.getValue(THRESHOLD_TYPE)) {
            case ThresholdTypes.GLOBAL_TYPE:
                returnedParameters.add(parameters.getParameter(GLOBAL_ALGORITHM));

                break;

            case ThresholdTypes.LOCAL_TYPE:
                returnedParameters.add(parameters.getParameter(LOCAL_ALGORITHM));
                returnedParameters.add(parameters.getParameter(LOCAL_RADIUS));
                returnedParameters.add(parameters.getParameter(SPATIAL_UNITS));
                returnedParameters.add(parameters.getParameter(USE_GLOBAL_Z));

                break;

        }

        returnedParameters.add(parameters.getParameter(USE_LOWER_THRESHOLD_LIMIT));

        if (parameters.getValue(USE_LOWER_THRESHOLD_LIMIT)) {
            returnedParameters.add(parameters.getParameter(LOWER_THRESHOLD_LIMIT));
        }

        returnedParameters.add(parameters.getParameter(WHITE_BACKGROUND));

        return returnedParameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
