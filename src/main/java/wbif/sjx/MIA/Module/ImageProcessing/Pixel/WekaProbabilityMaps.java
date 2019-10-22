package wbif.sjx.MIA.Module.ImageProcessing.Pixel;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.plugin.RGBStackConverter;
import ij.plugin.SubHyperstackMaker;
import ij.plugin.SubstackMaker;
import ij.process.ImageProcessor;
import trainableSegmentation.WekaSegmentation;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.ImageTypeConverter;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by sc13967 on 22/03/2018.
 */
public class WekaProbabilityMaps extends Module {
    public static final String INPUT_SEPARATOR = "Image input";
    public static final String INPUT_IMAGE = "Input image";
    public static final String CONVERT_TO_RGB = "Convert to RGB";
    public static final String OUTPUT_SEPARATOR = "Probability image output";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String OUTPUT_BIT_DEPTH = "Output bit depth";
    public static final String OUTPUT_SINGLE_CLASS = "Output single class";
    public static final String OUTPUT_CLASS = "Output class";
    public static final String CLASSIFIER_SEPARATOR = "Classifier settings";
    public static final String CLASSIFIER_FILE = "Classifier file path";
    public static final String BLOCK_SIZE = "Block size (simultaneous slices)";

    public WekaProbabilityMaps(ModuleCollection modules) {
        super("Weka probability maps",modules);
    }


    public interface OutputBitDepths {
        String EIGHT = "8";
        String SIXTEEN = "16";
        String THIRTY_TWO = "32";

        String[] ALL = new String[]{EIGHT,SIXTEEN,THIRTY_TWO};

    }


    public ImagePlus calculateProbabilityMaps(ImagePlus inputImagePlus, String outputImageName, String classifierFilePath, int blockSize, int bitDepth) {
        return calculateProbabilityMaps(inputImagePlus,outputImageName,classifierFilePath,blockSize,bitDepth,-1);
    }

    public ImagePlus calculateProbabilityMaps(ImagePlus inputImagePlus, String outputImageName, String classifierFilePath, int blockSize, int bitDepth, int outputClass) {
        WekaSegmentation wekaSegmentation = new WekaSegmentation();

        // Checking classifier can be loaded
        if (!new File(classifierFilePath).exists()) {
            System.err.println("Can't find classifier ("+classifierFilePath+")");
            return null;
        }

        writeMessage("Loading classifier");
        try {
            wekaSegmentation.loadClassifier(new FileInputStream(classifierFilePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        writeMessage("Classifier loaded");

        int nThreads = Prefs.getThreads();
        int width = inputImagePlus.getWidth();
        int height = inputImagePlus.getHeight();
        int nChannels = inputImagePlus.getNChannels();
        int nSlices = inputImagePlus.getNSlices();
        int nFrames = inputImagePlus.getNFrames();
        int nClasses = wekaSegmentation.getNumOfClasses();
        int nOutputClasses = outputClass == -1 ? wekaSegmentation.getNumOfClasses() : 1;

        int nBlocks = (int) Math.ceil((double) (nChannels*nSlices*nFrames)/(double) blockSize);

        // Creating the new image
        ImagePlus probabilityMaps = IJ.createHyperStack(outputImageName,width,height,nChannels*nOutputClasses,nSlices,nFrames,bitDepth);
        probabilityMaps.setCalibration(inputImagePlus.getCalibration());

        ImageStack inputStack = inputImagePlus.getStack();
        int slices = inputStack.getSize();

        int count = 0;
        for (int b=1;b<=nBlocks;b++) {
            int startingBlock = (b-1)*blockSize+1;
            int endingBlock = Math.min((b-1)*blockSize+blockSize,slices);

            ImagePlus iplSingle = new SubstackMaker().makeSubstack(new ImagePlus("Tempstack",inputStack),startingBlock + "-" + endingBlock);

            wekaSegmentation.setTrainingImage(iplSingle);
            wekaSegmentation.applyClassifier(true);
            iplSingle = wekaSegmentation.getClassifiedImage();

            // Converting probability image to specified bit depth (it will be 32-bit by default)
            ImageTypeConverter.applyConversion(iplSingle,bitDepth,ImageTypeConverter.ScalingModes.SCALE);
            for (int cl = 1; cl <= nOutputClasses; cl++) {
                for (int z = 1; z <= (endingBlock - startingBlock + 1); z++) {
                    int[] pos = inputImagePlus.convertIndexToPosition(startingBlock+z-1);
                    if (outputClass == -1) {
                        // If outputting all classes
                        iplSingle.setPosition(nOutputClasses * (z - 1) + cl);
                        probabilityMaps.setPosition((nOutputClasses * (pos[0] - 1) + cl), pos[1], pos[2]);
                    } else {
                        // If outputting a single class
                        iplSingle.setPosition(nClasses * (z - 1) + outputClass);
                        probabilityMaps.setPosition(1, startingBlock + z - 1, pos[2]);
                    }

                    ImageProcessor iprSingle = iplSingle.getProcessor();
                    ImageProcessor iprProbability = probabilityMaps.getProcessor();

                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            iprProbability.setf(x, y, iprSingle.getf(x, y));
                        }
                    }
                }
            }

            count = count + endingBlock - startingBlock + 1;
            writeMessage("Processed "+count+" of "+slices+" images");

        }

        // Clearing the segmentation model from memory
        wekaSegmentation = null;

        return probabilityMaps;

    }


    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_PIXEL;
    }

    @Override
    public String getDescription() {
        return "Performs pixel classification using the WEKA Trainable Segmentation plugin." +
                "<br><br>This module loads a previously-saved WEKA classifier model and applies it to the input image.  It then returns the multi-channel probability map." +
                "<br><br>Image stacks are processed in 2D, one slice at a time.";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean convertToRGB = parameters.getValue(CONVERT_TO_RGB);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String outputBitDepth = parameters.getValue(OUTPUT_BIT_DEPTH);
        boolean outputSingleClass = parameters.getValue(OUTPUT_SINGLE_CLASS);
        int outputClass = parameters.getValue(OUTPUT_CLASS);
        String classifierFilePath = parameters.getValue(CLASSIFIER_FILE);
        int blockSize = parameters.getValue(BLOCK_SIZE);

        // Converting to RGB if requested
        if (convertToRGB) {
            inputImagePlus = inputImagePlus.duplicate();
            RGBStackConverter.convertToRGB(inputImagePlus);
        }

        // Converting the bit depth to an integer
        int bitDepth = Integer.parseInt(outputBitDepth);

        // If all channels are to be output, set output channel to -1
        if (!outputSingleClass) outputClass = -1;

        // Running the classifier on each individual stack
        ImagePlus probabilityMaps = calculateProbabilityMaps(inputImagePlus,outputImageName,classifierFilePath,blockSize,bitDepth,outputClass);

        // If the classification failed, a null object is returned
        if (probabilityMaps == null) return false;

        // Adding the probability maps to the Workspace
        Image probabilityImage = new Image(outputImageName,probabilityMaps);
        workspace.addImage(probabilityImage);

        if (showOutput) probabilityImage.showImage();

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE,this,"","Image to apply pixel classification to."));
        parameters.add(new BooleanP(CONVERT_TO_RGB,this,false,"Converts a composite image to RGB format.  This should be set to match the image-type used for generation of the model."));

        parameters.add(new ParamSeparatorP(OUTPUT_SEPARATOR,this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE,this, "","Output probability map image."));
        parameters.add(new ChoiceP(OUTPUT_BIT_DEPTH,this,OutputBitDepths.THIRTY_TWO,OutputBitDepths.ALL,"By default images will be saved as floating point 32-bit (probabilities in the range 0-1); however, they can be converted to 8-bit (probabilities in the range 0-255) or 16-bit (probabilities in the range 0-65535).  This is useful for saving memory or if the output probability map will be passed to image threshold module."));
        parameters.add(new BooleanP(OUTPUT_SINGLE_CLASS,this,false,"Allows a single class (image channel) to be output.  This is another feature for reducing memory usage."));
        parameters.add(new IntegerP(OUTPUT_CLASS,this,1,"Class (image channel) to be output.  Channel numbering starts at 1."));

        parameters.add(new ParamSeparatorP(CLASSIFIER_SEPARATOR,this));
        parameters.add(new FilePathP(CLASSIFIER_FILE,this,"","Path to the classifier file (.model extension).  This file needs to be created manually using the WEKA Trainable Segmentation plugin included with Fiji."));
        parameters.add(new IntegerP(BLOCK_SIZE,this,1,"Number of image slices to process at any given time.  This reduces the memory footprint of the module, but can slow down processing."));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(CONVERT_TO_RGB));

        returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_BIT_DEPTH));

        returnedParameters.add(parameters.getParameter(OUTPUT_SINGLE_CLASS));
        if (parameters.getValue(OUTPUT_SINGLE_CLASS)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_CLASS));
        }

        returnedParameters.add(parameters.getParameter(CLASSIFIER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CLASSIFIER_FILE));
        returnedParameters.add(parameters.getParameter(BLOCK_SIZE));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}
