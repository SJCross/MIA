// TODO: For image to objects, could add the image ID number as a measurement to the object
// TODO: For image to objects, could create parent object for all instances of that image ID in different frames

package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import ij.process.ImageProcessor;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Object.LUTs;
import wbif.sjx.common.Process.IntensityMinMax;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sc13967 on 04/05/2017.
 */
public class ObjectImageConverter extends HCModule {
    public static final String CONVERSION_MODE = "Conversion mode";
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String TEMPLATE_IMAGE = "Template image";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String COLOUR_MODE = "Colour mode";
    public static final String MEASUREMENT = "Measurement";
    public static final String PARENT_OBJECT_FOR_COLOUR = "Parent object for colour";
    public static final String HIDE_IF_MISSING_PARENT = "Hide points without a parent";
    public static final String SHOW_IMAGE = "Show image";

    public interface ConversionModes {
        String IMAGE_TO_OBJECTS = "Image to objects";
        String OBJECTS_TO_IMAGE = "Objects to image";

        String[] ALL = new String[]{IMAGE_TO_OBJECTS, OBJECTS_TO_IMAGE};

    }

    public interface ColourModes {
        String SINGLE_COLOUR = "Single colour";
        String ID = "ID";
        String RANDOM_COLOUR = "Random colour";
        String MEASUREMENT_VALUE = "Measurement value";
        String PARENT_ID = "Parent ID";

        String[] ALL = new String[]{SINGLE_COLOUR, RANDOM_COLOUR, MEASUREMENT_VALUE, ID, PARENT_ID};

    }

    public static Image convertObjectsToImage(ObjSet objects, String outputImageName, Image templateImage, String colourMode, String colourSource, boolean hideMissing) {
        ImagePlus ipl;

        int bitDepth = 8;
        switch (colourMode){
            case ColourModes.SINGLE_COLOUR:
                bitDepth = 8;
                break;

            case ColourModes.RANDOM_COLOUR:
                bitDepth = 8;
                break;

            case ColourModes.MEASUREMENT_VALUE:
                bitDepth = 32;
                break;

            case ColourModes.ID:
                bitDepth = 16;
                break;

            case ColourModes.PARENT_ID:
                bitDepth = 16;
                break;

        }

        if (templateImage == null) {
            // Getting range of object pixels
            int[][] coordinateRange = new int[4][2];

            for (Obj object : objects.values()) {
                // Getting range of XYZ
                int[][] currCoordinateRange = object.getCoordinateRange();
                for (int dim = 0; dim < currCoordinateRange.length; dim++) {
                    if (currCoordinateRange[dim][0] < coordinateRange[dim][0]) {
                        coordinateRange[dim][0] = currCoordinateRange[dim][0];
                    }

                    if (currCoordinateRange[dim][1] > coordinateRange[dim][1]) {
                        coordinateRange[dim][1] = currCoordinateRange[dim][1];
                    }
                }

                // Getting range of timepoints
                int currTimepoint = object.getT();
                if (currTimepoint < coordinateRange[3][0]) {
                    coordinateRange[3][0] = currTimepoint;
                }

                if (currTimepoint > coordinateRange[3][1]) {
                    coordinateRange[3][1] = currTimepoint;
                }
            }

            // Creating a new image
            ipl = IJ.createHyperStack(objects.getName()+"_Objects", coordinateRange[0][1] + 1,coordinateRange[1][1] + 1,
                    1, coordinateRange[2][1] + 1, coordinateRange[3][1] + 1,bitDepth);

        } else {
            ImagePlus templateIpl = templateImage.getImagePlus();
            ipl = IJ.createHyperStack(objects.getName()+"Objects",templateIpl.getWidth(),templateIpl.getHeight(),
                    templateIpl.getNChannels(),templateIpl.getNSlices(),templateIpl.getNFrames(),bitDepth);
        }

        // If it's a 32-bit image, set all background pixels to NaN
        if (bitDepth == 32) {
            for (int z = 1; z <= ipl.getNSlices(); z++) {
                for (int c = 1; c <= ipl.getNChannels(); c++) {
                    for (int t = 1; t <= ipl.getNFrames(); t++) {
                        for (int x=0;x<ipl.getWidth();x++) {
                            for (int y=0;y<ipl.getHeight();y++) {
                                ipl.setPosition(c,z,t);
                                ipl.getProcessor().putPixelValue(x,y,Double.NaN);
                            }
                        }
                    }
                }
            }
        }

        // Labelling pixels in image
        for (Obj object:objects.values()) {
            ArrayList<Integer> x = object.getXCoords();
            ArrayList<Integer> y = object.getYCoords();
            ArrayList<Integer> z = object.getZCoords();
            Integer tPos = object.getT();

            double valDouble = 1;
            int valInt = 1;
            switch (colourMode){
                case ColourModes.SINGLE_COLOUR:
                    valInt = 1;
                    break;

                case ColourModes.RANDOM_COLOUR:
                    valInt = (int) Math.round(Math.random()*255);
                    break;

                case ColourModes.MEASUREMENT_VALUE:
                    valDouble = object.getMeasurement(colourSource).getValue();
                    break;

                case ColourModes.ID:
                    valInt = object.getID();
                    break;

                case ColourModes.PARENT_ID:
                    if (object.getParent(colourSource) == null) {
                        if (hideMissing) {
                            valInt = 0;
                            break;

                        } else {
                            valInt = Integer.MAX_VALUE;
                            break;

                        }
                    }

                    valInt = object.getParent(colourSource).getID();
                    break;

            }

            for (int i=0;i<x.size();i++) {
                int zPos = z==null ? 0 : z.get(i);

                ipl.setPosition(1,zPos+1,tPos+1);

                if (colourMode.equals(ColourModes.SINGLE_COLOUR) | colourMode.equals(ColourModes.RANDOM_COLOUR) | colourMode.equals(ColourModes.ID) | colourMode.equals(ColourModes.PARENT_ID)) {
                    ipl.getProcessor().putPixel(x.get(i), y.get(i), valInt);

                } else if (colourMode.equals(ColourModes.MEASUREMENT_VALUE)) {
                    ipl.getProcessor().putPixelValue(x.get(i), y.get(i), valDouble);

                }
            }
        }

        // Assigning the spatial calibration from the first object
        Obj referenceObject = objects.values().iterator().next();
        if (referenceObject != null) {
            ipl.getCalibration().pixelWidth = templateImage.getImagePlus().getCalibration().getX(1);
            ipl.getCalibration().pixelHeight = templateImage.getImagePlus().getCalibration().getY(1);
            ipl.getCalibration().pixelDepth = templateImage.getImagePlus().getCalibration().getZ(1);
            ipl.getCalibration().setUnit(templateImage.getImagePlus().getCalibration().getUnit());

        }

        return new Image(outputImageName,ipl);

    }

    public static ObjSet convertImageToObjects(Image image, String outputObjectsName) {
        // Converting to ImagePlus for this operation
        ImagePlus ipl = image.getImagePlus();

        // Need to get coordinates and convert to a HCObject
        ObjSet outputObjects = new ObjSet(outputObjectsName); //Local ArrayList of objects

        // Getting spatial calibration
        double dppXY = ipl.getCalibration().getX(1);
        double dppZ = ipl.getCalibration().getZ(1);
        String calibratedUnits = ipl.getCalibration().getUnits();

        ImageProcessor ipr = ipl.getProcessor();

        int h = ipl.getHeight();
        int w = ipl.getWidth();
        int nSlices = ipl.getNSlices();
        int nFrames = ipl.getNFrames();
        int nChannels = ipl.getNChannels();

        for (int c=0;c<nChannels;c++) {
            for (int t = 0; t < nFrames; t++) {
                // HashMap linking the ID numbers in the present frame to those used to store the object (this means
                // each frame instance has different ID numbers)
                HashMap<Integer,Integer> IDlink = new HashMap<>();

                for (int z = 0; z < nSlices; z++) {
                    ipl.setPosition(c+1,z+1,t+1);
                    for (int x = 0; x < w; x++) {
                        for (int y = 0; y < h; y++) {
                            int imageID = (int) ipr.getPixelValue(x, y); //Pixel value

                            if (imageID != 0) {
                                IDlink.computeIfAbsent(imageID, k -> outputObjects.getNextID());
                                int outID = IDlink.get(imageID);

                                outputObjects.computeIfAbsent(outID, k -> new Obj(outputObjectsName, outID,dppXY,dppZ,calibratedUnits));
                                outputObjects.get(outID).addCoord(x,y,z);
                                outputObjects.get(outID).setT(t);

                            }
                        }
                    }
                }
            }
        }

        return outputObjects;

    }

    @Override
    public String getTitle() {
        return "Image-object converter";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void run(Workspace workspace, boolean verbose) {
        String conversionMode = parameters.getValue(CONVERSION_MODE);

        if (conversionMode.equals(ConversionModes.IMAGE_TO_OBJECTS)) {
            String inputImageName = parameters.getValue(INPUT_IMAGE);
            String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

            Image inputImage = workspace.getImages().get(inputImageName);

            ObjSet objects = convertImageToObjects(inputImage, outputObjectsName);

            workspace.addObjects(objects);

        } else if (conversionMode.equals(ConversionModes.OBJECTS_TO_IMAGE)) {
            String objectName = parameters.getValue(INPUT_OBJECTS);
            String templateImageName = parameters.getValue(TEMPLATE_IMAGE);
            String outputImageName = parameters.getValue(OUTPUT_IMAGE);
            String colourMode = parameters.getValue(COLOUR_MODE);
            String colourSource = null;
            boolean showImage = parameters.getValue(SHOW_IMAGE);
            boolean hideMissing = parameters.getValue(HIDE_IF_MISSING_PARENT);

            if (parameters.getValue(COLOUR_MODE).equals(ColourModes.PARENT_ID)) {
                colourSource = parameters.getValue(PARENT_OBJECT_FOR_COLOUR);

            } else if (parameters.getValue(COLOUR_MODE).equals(ColourModes.MEASUREMENT_VALUE)) {
                colourSource = parameters.getValue(MEASUREMENT);

            }

            ObjSet inputObjects = workspace.getObjects().get(objectName);
            Image templateImage = workspace.getImages().get(templateImageName);

            Image outputImage = convertObjectsToImage(inputObjects,outputImageName,templateImage,colourMode,colourSource,hideMissing);

            workspace.addImage(outputImage);

            if (showImage) {
                ImagePlus ipl = outputImage.getImagePlus();
                IntensityMinMax.run(ipl,ipl.getNSlices() > 1);
                ImagePlus iplShow = new Duplicator().run(ipl);
                iplShow.setLut(LUTs.Random(true));
                iplShow.show();

            }
        }
    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(CONVERSION_MODE, Parameter.CHOICE_ARRAY,ConversionModes.IMAGE_TO_OBJECTS,ConversionModes.ALL));
        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.addParameter(new Parameter(OUTPUT_OBJECTS, Parameter.OUTPUT_OBJECTS,null));
        parameters.addParameter(new Parameter(TEMPLATE_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.addParameter(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.addParameter(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.addParameter(new Parameter(COLOUR_MODE, Parameter.CHOICE_ARRAY,ColourModes.SINGLE_COLOUR,ColourModes.ALL));
        parameters.addParameter(new Parameter(MEASUREMENT, Parameter.MEASUREMENT,null,null));
        parameters.addParameter(new Parameter(PARENT_OBJECT_FOR_COLOUR, Parameter.PARENT_OBJECTS,null,null));
        parameters.addParameter(new Parameter(HIDE_IF_MISSING_PARENT,Parameter.BOOLEAN,true));
        parameters.addParameter(new Parameter(SHOW_IMAGE, Parameter.BOOLEAN,true));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(CONVERSION_MODE));

        if (parameters.getValue(CONVERSION_MODE).equals(ConversionModes.IMAGE_TO_OBJECTS)) {
            returnedParameters.addParameter(parameters.getParameter(INPUT_IMAGE));
            returnedParameters.addParameter(parameters.getParameter(OUTPUT_OBJECTS));

        } else if(parameters.getValue(CONVERSION_MODE).equals(ConversionModes.OBJECTS_TO_IMAGE)) {
            returnedParameters.addParameter(parameters.getParameter(TEMPLATE_IMAGE));
            returnedParameters.addParameter(parameters.getParameter(INPUT_OBJECTS));
            returnedParameters.addParameter(parameters.getParameter(OUTPUT_IMAGE));

            returnedParameters.addParameter(parameters.getParameter(COLOUR_MODE));
            if (parameters.getValue(COLOUR_MODE).equals(ColourModes.MEASUREMENT_VALUE)) {
                // Use measurement
                returnedParameters.addParameter(parameters.getParameter(MEASUREMENT));

                if (parameters.getValue(INPUT_OBJECTS) != null) {
                    parameters.updateValueSource(MEASUREMENT,parameters.getValue(INPUT_OBJECTS));

                }

            } else if (parameters.getValue(COLOUR_MODE).equals(ColourModes.PARENT_ID)) {
                // Use Parent ID
                returnedParameters.addParameter(parameters.getParameter(PARENT_OBJECT_FOR_COLOUR));
                returnedParameters.addParameter(parameters.getParameter(HIDE_IF_MISSING_PARENT));

                String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
                parameters.updateValueSource(PARENT_OBJECT_FOR_COLOUR,inputObjectsName);

            }

            returnedParameters.addParameter(parameters.getParameter(SHOW_IMAGE));

        }

        return returnedParameters;
    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }

}