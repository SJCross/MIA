package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import ij.process.ImageProcessor;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
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
    public static final String SHOW_IMAGE = "Show image";

    private static final String IMAGE_TO_OBJECTS = "Image to objects";
    private static final String OBJECTS_TO_IMAGE = "Objects to image";
    private static final String[] CONVERSION_MODES = new String[]{IMAGE_TO_OBJECTS,OBJECTS_TO_IMAGE};

    private static final String SINGLE_COLOUR = "Single colour";
    private static final String ID = "ID";
    private static final String RANDOM_COLOUR = "Random colour";
    private static final String MEASUREMENT_VALUE = "Measurement value";
    private static final String PARENT_ID = "Parent ID";
    public static final String[] COLOUR_MODES = new String[]{SINGLE_COLOUR,RANDOM_COLOUR,MEASUREMENT_VALUE,ID,PARENT_ID};

    public static Image convertObjectsToImage(ObjSet objects, String outputImageName, Image templateImage, String colourMode, String colourSource) {
        ImagePlus ipl;

        int bitDepth = 8;
        switch (colourMode){
            case SINGLE_COLOUR:
                bitDepth = 8;
                break;

            case RANDOM_COLOUR:
                bitDepth = 8;
                break;

            case MEASUREMENT_VALUE:
                bitDepth = 32;
                break;

            case ID:
                bitDepth = 16;
                break;

            case PARENT_ID:
                bitDepth = 16;
                break;

        }

        if (templateImage == null) {
            // Getting range of object pixels
            int[][] coordinateRange = new int[5][2];

            for (Obj object : objects.values()) {
                // Getting range of XYZ
                int[][] currCoordinateRange = object.getCoordinateRange();
                for (int dim = 0; dim < coordinateRange.length; dim++) {
                    if (currCoordinateRange[dim][0] < coordinateRange[dim][0]) {
                        coordinateRange[dim][0] = currCoordinateRange[dim][0];
                    }

                    if (currCoordinateRange[dim][1] > coordinateRange[dim][1]) {
                        coordinateRange[dim][1] = currCoordinateRange[dim][1];
                    }
                }

                // Getting range of additional dimensions
                for (int dim:object.getPositions().keySet()) {
                    int currValue = object.getPosition(dim);

                    if (currValue < coordinateRange[dim][0]) {
                        coordinateRange[dim][0] = currValue;
                    }

                    if (currValue > coordinateRange[dim][1]) {
                        coordinateRange[dim][1] = currValue;
                    }
                }
            }

            // Creating a new image
            ipl = IJ.createHyperStack("Objects", coordinateRange[Obj.X][1] + 1,
                    coordinateRange[Obj.Y][1] + 1, coordinateRange[Obj.C][1] + 1,
                    coordinateRange[Obj.Z][1] + 1, coordinateRange[Obj.T][1] + 1,bitDepth);

        } else {
            ImagePlus templateIpl = templateImage.getImagePlus();
            ipl = IJ.createHyperStack("Objects",templateIpl.getWidth(),templateIpl.getHeight(),
                    templateIpl.getNChannels(),templateIpl.getNSlices(),templateIpl.getNFrames(),bitDepth);
        }

        // Labelling pixels in image
        for (Obj object:objects.values()) {
            ArrayList<Integer> x = object.getCoordinates(Obj.X);
            ArrayList<Integer> y = object.getCoordinates(Obj.Y);
            ArrayList<Integer> z = object.getCoordinates(Obj.Z);
            Integer cPos = object.getCoordinates(Obj.C);
            Integer tPos = object.getCoordinates(Obj.T);

            double valDouble = 1;
            int valInt = 1;
            switch (colourMode){
                case SINGLE_COLOUR:
                    valInt = 1;
                    break;

                case RANDOM_COLOUR:
                    valInt = (int) Math.round(Math.random()*255);
                    break;

                case MEASUREMENT_VALUE:
                    valDouble = object.getMeasurement(colourSource).getValue();
                    break;

                case ID:
                    System.out.println(object.getID());
                    valInt = object.getID();
                    break;

                case PARENT_ID:
                    // If there is no assigned parent, set the intensity to max integer (better than 1)
                    if (object.getParent(colourSource) == null) {
                        valInt = Integer.MAX_VALUE;
                        break;
                    }

                    valInt = object.getParent(colourSource).getID();
                    break;

            }

            for (int i=0;i<x.size();i++) {
                int zPos = z==null ? 0 : z.get(i);

                ipl.setPosition(cPos+1,zPos+1,tPos+1);

                if (colourMode.equals(SINGLE_COLOUR) | colourMode.equals(RANDOM_COLOUR) | colourMode.equals(ID) | colourMode.equals(PARENT_ID)) {
                    ipl.getProcessor().putPixel(x.get(i), y.get(i), valInt);

                } else if (colourMode.equals(MEASUREMENT_VALUE)) {
                    ipl.getProcessor().putPixelValue(x.get(i), y.get(i), valDouble);

                }
            }
        }

        return new Image(outputImageName,ipl);

    }

    public ObjSet convertImageToObjects(Image image, String outputObjectsName) {
        // Converting to ImagePlus for this operation
        ImagePlus ipl = image.getImagePlus();

        // Need to get coordinates and convert to a HCObject
        ObjSet outputObjects = new ObjSet(outputObjectsName); //Local ArrayList of objects

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

                                outputObjects.computeIfAbsent(outID, k -> new Obj(outputObjectsName, outID));

                                outputObjects.get(outID).addCoordinate(Obj.X, x);
                                outputObjects.get(outID).addCoordinate(Obj.Y, y);
                                outputObjects.get(outID).addCoordinate(Obj.Z, z);
                                outputObjects.get(outID).addCoordinate(Obj.C, c);
                                outputObjects.get(outID).addCoordinate(Obj.T, t);

                            }
                        }
                    }
                }
            }
        }

        // Adding distance calibration to each object
        Calibration calibration = ipl.getCalibration();
        for (Obj object:outputObjects.values()) {
            object.addCalibration(Obj.X,calibration.getX(1));
            object.addCalibration(Obj.Y,calibration.getY(1));
            object.addCalibration(Obj.Z,calibration.getZ(1));
            object.setCalibratedUnits(calibration.getUnits());

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
    public void execute(Workspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        String conversionMode = parameters.getValue(CONVERSION_MODE);

        if (conversionMode.equals(IMAGE_TO_OBJECTS)) {
            String inputImageName = parameters.getValue(INPUT_IMAGE);
            String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

            Image inputImage = workspace.getImages().get(inputImageName);

            ObjSet objects = convertImageToObjects(inputImage, outputObjectsName);

            workspace.addObjects(objects);

        } else if (conversionMode.equals(OBJECTS_TO_IMAGE)) {
            String objectName = parameters.getValue(INPUT_OBJECTS);
            String templateImageName = parameters.getValue(TEMPLATE_IMAGE);
            String outputImageName = parameters.getValue(OUTPUT_IMAGE);
            String colourMode = parameters.getValue(COLOUR_MODE);
            String colourSource = null;
            boolean showImage = parameters.getValue(SHOW_IMAGE);

            if (parameters.getValue(COLOUR_MODE).equals(PARENT_ID)) {
                colourSource = parameters.getValue(PARENT_OBJECT_FOR_COLOUR);

            } else if (parameters.getValue(COLOUR_MODE).equals(MEASUREMENT_VALUE)) {
                colourSource = parameters.getValue(MEASUREMENT);

            }

            ObjSet inputObjects = workspace.getObjects().get(objectName);
            Image templateImage = workspace.getImages().get(templateImageName);

            Image outputImage = convertObjectsToImage(inputObjects,outputImageName,templateImage,colourMode,colourSource);

            workspace.addImage(outputImage);

            if (showImage) {
                ImagePlus ipl = outputImage.getImagePlus();
                IntensityMinMax.run(ipl,ipl.getNSlices() > 1);
                new Duplicator().run(ipl).show();

            }
        }

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(CONVERSION_MODE, Parameter.CHOICE_ARRAY,CONVERSION_MODES[0],CONVERSION_MODES));
        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.addParameter(new Parameter(OUTPUT_OBJECTS, Parameter.OUTPUT_OBJECTS,null));
        parameters.addParameter(new Parameter(TEMPLATE_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.addParameter(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.addParameter(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.addParameter(new Parameter(COLOUR_MODE, Parameter.CHOICE_ARRAY,COLOUR_MODES[0],COLOUR_MODES));
        parameters.addParameter(new Parameter(MEASUREMENT, Parameter.MEASUREMENT,null,null));
        parameters.addParameter(new Parameter(PARENT_OBJECT_FOR_COLOUR, Parameter.PARENT_OBJECTS,null,null));
        parameters.addParameter(new Parameter(SHOW_IMAGE, Parameter.BOOLEAN,true));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(CONVERSION_MODE));

        if (parameters.getValue(CONVERSION_MODE).equals(IMAGE_TO_OBJECTS)) {
            returnedParameters.addParameter(parameters.getParameter(INPUT_IMAGE));
            returnedParameters.addParameter(parameters.getParameter(OUTPUT_OBJECTS));

        } else if(parameters.getValue(CONVERSION_MODE).equals(OBJECTS_TO_IMAGE)) {
            returnedParameters.addParameter(parameters.getParameter(TEMPLATE_IMAGE));
            returnedParameters.addParameter(parameters.getParameter(INPUT_OBJECTS));
            returnedParameters.addParameter(parameters.getParameter(OUTPUT_IMAGE));

            returnedParameters.addParameter(parameters.getParameter(COLOUR_MODE));
            if (parameters.getValue(COLOUR_MODE).equals(MEASUREMENT_VALUE)) {
                // Use measurement
                returnedParameters.addParameter(parameters.getParameter(MEASUREMENT));

                if (parameters.getValue(INPUT_OBJECTS) != null) {
                    parameters.updateValueRange(MEASUREMENT,parameters.getValue(INPUT_OBJECTS));

                }

            } else if (parameters.getValue(COLOUR_MODE).equals(PARENT_ID)) {
                // Use Parent ID
                returnedParameters.addParameter(parameters.getParameter(PARENT_OBJECT_FOR_COLOUR));

                String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
                parameters.updateValueRange(PARENT_OBJECT_FOR_COLOUR,inputObjectsName);

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