package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.Binary;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.process.ImageProcessor;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.watershed.ExtendedMinimaWatershed;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;

public class Watershed extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String USE_MARKERS = "Use markers";
    public static final String MARKER_IMAGE = "Input marker image";
    public static final String INTENSITY_MODE = "Intensity mode";
    public static final String INTENSITY_IMAGE = "Intensity image";
    public static final String DYNAMIC = "Dynamic";
    public static final String CONNECTIVITY = "Connectivity";
    public static final String MATCH_Z_TO_X= "Match Z to XY";

    public interface IntensityModes {
        String DISTANCE = "Distance";
        String INPUT_IMAGE = "Input image intensity";

        String[] ALL = new String[]{DISTANCE,INPUT_IMAGE};

    }

    public interface Connectivity {
        String SIX = "6";
        String TWENTYSIX = "26";

        String[] ALL = new String[]{SIX,TWENTYSIX};

    }


    public void process(ImagePlus intensityIpl, ImagePlus markerIpl, ImagePlus maskIpl, int dynamic, int connectivity) {
        // Expected inputs for binary images (marker and mask) are black objects on a white background.  These need to
        // be inverted before using as MorphoLibJ uses the opposite convention.
        IJ.run(maskIpl,"Invert","stack");
        if (markerIpl != null) {
            markerIpl = new Duplicator().run(markerIpl);
            IJ.run(markerIpl, "Invert", "stack");
        }

        int nFrames = maskIpl.getNFrames();
        for (int t = 1; t <= nFrames; t++) {
            // Getting maskIpl for this timepoint
            ImagePlus timepointMaskIpl = getTimepoint(maskIpl,t);
            ImagePlus timepointIntensityIpl = getTimepoint(intensityIpl,t);

            if (markerIpl == null) {
                timepointMaskIpl.setStack(ExtendedMinimaWatershed.extendedMinimaWatershed(timepointIntensityIpl.getStack(), timepointMaskIpl.getStack(), dynamic, connectivity, false));

            } else {
                ImagePlus timepointMarkerIpl = getTimepoint(markerIpl,t);
                timepointMarkerIpl = BinaryImages.componentsLabeling(timepointMarkerIpl, connectivity, 32);
                timepointMaskIpl.setStack(inra.ijpb.watershed.Watershed.computeWatershed(timepointIntensityIpl, timepointMarkerIpl, timepointMaskIpl, connectivity, true, false).getStack());

            }

            // The image produced by MorphoLibJ's watershed function is labelled.  Converting to binary and back to 8-bit.
            IJ.setRawThreshold(timepointMaskIpl, 0, 0, null);
            IJ.run(timepointMaskIpl, "Convert to Mask", "method=Default background=Light");
            IJ.run(timepointMaskIpl, "Invert LUT", "");
            IJ.run(timepointMaskIpl, "8-bit", null);

            //  Replacing the maskIpl intensity
            overwriteTimepoint(maskIpl,timepointMaskIpl,t);

            writeMessage("Processed "+t+" of "+nFrames+" frames");

        }
    }

    private static ImagePlus getTimepoint(ImagePlus inputImagePlus, int timepoint) {
        ImagePlus outputImagePlus = IJ.createImage("Output",inputImagePlus.getWidth(),inputImagePlus.getHeight(),inputImagePlus.getNSlices(),inputImagePlus.getBitDepth());

        for (int z = 1; z <= inputImagePlus.getNSlices(); z++) {
            inputImagePlus.setPosition(1,z,timepoint);
            outputImagePlus.setPosition(z);

            ImageProcessor inputImageProcessor = inputImagePlus.getProcessor();
            ImageProcessor outputImageProcessor = outputImagePlus.getProcessor();

            for (int y=0;y<inputImagePlus.getHeight();y++) {
                for (int x = 0; x < inputImagePlus.getWidth(); x++) {
                    outputImageProcessor.set(x,y,inputImageProcessor.get(x,y));
                }
            }
        }

        inputImagePlus.setPosition(1,1,1);
        outputImagePlus.setPosition(1);

        return outputImagePlus;

    }

    private static  void overwriteTimepoint(ImagePlus inputImagePlus, ImagePlus timepointImagePlus, int timepoint) {
        for (int z = 1; z <= inputImagePlus.getNSlices(); z++) {
            inputImagePlus.setPosition(1,z,timepoint);
            timepointImagePlus.setPosition(z);

            ImageProcessor inputImageProcessor = inputImagePlus.getProcessor();
            ImageProcessor timepointImageProcessor = timepointImagePlus.getProcessor();

            for (int y=0;y<inputImagePlus.getHeight();y++) {
                for (int x = 0; x < inputImagePlus.getWidth(); x++) {
                    inputImageProcessor.set(x,y,timepointImageProcessor.get(x,y));
                }
            }

        }

        inputImagePlus.setPosition(1,1,1);
        timepointImagePlus.setPosition(1);

    }


    @Override
    public String getTitle() {
        return "Watershed transform";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_PIXEL_BINARY;
    }

    @Override
    public String getHelp() {
        return "Uses MorphoLibJ implementation of watershed transform.";
    }

    @Override
    protected void run(Workspace workspace) {
// Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        boolean useMarkers = parameters.getValue(USE_MARKERS);
        String markerImageName = parameters.getValue(MARKER_IMAGE);
        String intensityMode = parameters.getValue(INTENSITY_MODE);
        String intensityImageName = parameters.getValue(INTENSITY_IMAGE);
        int dynamic = parameters.getValue(DYNAMIC);
        int connectivity = Integer.parseInt(parameters.getValue(CONNECTIVITY));
        boolean matchZToXY = parameters.getValue(MATCH_Z_TO_X);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) inputImagePlus = new Duplicator().run(inputImagePlus);

        ImagePlus markerIpl = null;
        if (useMarkers) markerIpl = workspace.getImage(markerImageName).getImagePlus();

        ImagePlus intensityIpl = null;
        switch (intensityMode) {
            case IntensityModes.DISTANCE:
                intensityIpl = new Duplicator().run(inputImagePlus);
                intensityIpl = DistanceMap.getDistanceMap(intensityIpl,matchZToXY);
                IJ.run(intensityIpl,"Invert","stack");
                break;

            case IntensityModes.INPUT_IMAGE:
                intensityIpl = workspace.getImage(intensityImageName).getImagePlus();
                break;

        }

        process(intensityIpl,markerIpl,inputImagePlus,dynamic,connectivity);

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            writeMessage("Adding image ("+outputImageName+") to workspace");
            Image outputImage = new Image(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);
            if (showOutput) showImage(outputImage);

        } else {
            if (showOutput) showImage(inputImage);

        }
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(APPLY_TO_INPUT, Parameter.BOOLEAN,true));
        parameters.add(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.add(new Parameter(USE_MARKERS, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(MARKER_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(INTENSITY_MODE, Parameter.CHOICE_ARRAY,IntensityModes.DISTANCE,IntensityModes.ALL));
        parameters.add(new Parameter(INTENSITY_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(DYNAMIC, Parameter.INTEGER,1));
        parameters.add(new Parameter(CONNECTIVITY, Parameter.CHOICE_ARRAY,Connectivity.SIX,Connectivity.ALL));
        parameters.add(new Parameter(MATCH_Z_TO_X, Parameter.BOOLEAN, true));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(USE_MARKERS));
        if (parameters.getValue(USE_MARKERS)) {
            returnedParameters.add(parameters.getParameter(MARKER_IMAGE));
        } else {
            returnedParameters.add(parameters.getParameter(DYNAMIC));
        }

        returnedParameters.add(parameters.getParameter(INTENSITY_MODE));
        switch ((String) parameters.getValue(INTENSITY_MODE)) {
            case IntensityModes.DISTANCE:
                returnedParameters.add(parameters.getParameter(MATCH_Z_TO_X));
                break;

            case IntensityModes.INPUT_IMAGE:
                returnedParameters.add(parameters.getParameter(INTENSITY_IMAGE));
                break;
        }
        returnedParameters.add(parameters.getParameter(CONNECTIVITY));

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
    public MetadataReferenceCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
