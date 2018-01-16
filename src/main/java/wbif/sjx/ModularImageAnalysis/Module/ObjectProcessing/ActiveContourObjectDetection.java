package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.ZProjector;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.ProjectImage;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by sc13967 on 16/01/2018.
 */
public class ActiveContourObjectDetection extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String UPDATE_INPUT_OBJECTS = "Update input objects";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String NODE_DENSITY = "Node density";
    public static final String ELASTIC_ENERGY = "Elastic energy contribution";
    public static final String BENDING_ENERGY = "Bending energy contribution";
    public static final String IMAGE_PATH_ENERGY = "Image path energy contribution";
    public static final String SEARCH_RADIUS = "Search radius (px)";
    public static final String NUMBER_OF_ITERATIONS = "Maximum nmber of iterations";
    public static final String SHOW_CONTOURS = "Show contours";


    @Override
    public String getTitle() {
        return "Active contour-based detection";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected void run(Workspace workspace, boolean verbose) throws GenericMIAException {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting output image name
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        ObjCollection outputObjects = new ObjCollection(outputObjectsName);

        // Getting parameters
        boolean updateInputObjects = parameters.getValue(UPDATE_INPUT_OBJECTS);
        double nodeDensity = parameters.getValue(NODE_DENSITY);
        double elasticEnergy = parameters.getValue(ELASTIC_ENERGY);
        double bendingEnergy = parameters.getValue(BENDING_ENERGY);
        double pathEnergy = parameters.getValue(IMAGE_PATH_ENERGY);
        int searchRadius = parameters.getValue(SEARCH_RADIUS);
        int maxInteractions = parameters.getValue(NUMBER_OF_ITERATIONS);
        boolean showContours = parameters.getValue(SHOW_CONTOURS);

        // Storing the image calibration
        Calibration calibration = inputImagePlus.getCalibration();
        double dppXY = calibration.getX(1);
        double dppZ = calibration.getZ(1);
        String calibrationUnits = calibration.getUnits();

        // Iterating over all objects
        int count = 1;
        int total = inputObjects.size();

        for (Obj inputObject:inputObjects.values()) {
            if (verbose)
                System.out.println("[" + moduleName + "] Processing object " + (count++) + " of " + total);

            // Getting the Roi for the current object
            Roi roi = inputObject.getRoi(inputImage);



        }

        // Resetting the image position
        inputImagePlus.setPosition(1,1,1);

        // If selected, adding new ObjCollection to the Workspace
        if (!updateInputObjects) workspace.addObjects(outputObjects);

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(INPUT_OBJECTS,Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(UPDATE_INPUT_OBJECTS,Parameter.BOOLEAN,true));
        parameters.add(new Parameter(OUTPUT_OBJECTS,Parameter.OUTPUT_OBJECTS,null));
        parameters.add(new Parameter(NODE_DENSITY,Parameter.DOUBLE,1.0));
        parameters.add(new Parameter(ELASTIC_ENERGY,Parameter.DOUBLE,1.0));
        parameters.add(new Parameter(BENDING_ENERGY,Parameter.DOUBLE,1.0));
        parameters.add(new Parameter(IMAGE_PATH_ENERGY,Parameter.DOUBLE,1.0));
        parameters.add(new Parameter(SEARCH_RADIUS,Parameter.INTEGER,1));
        parameters.add(new Parameter(NUMBER_OF_ITERATIONS,Parameter.INTEGER,1000));
        parameters.add(new Parameter(SHOW_CONTOURS,Parameter.BOOLEAN,true));

    }

    @Override
    protected void initialiseMeasurementReferences() {

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(UPDATE_INPUT_OBJECTS));

        if (! (boolean) parameters.getValue(UPDATE_INPUT_OBJECTS)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));
        }

        returnedParameters.add(parameters.getParameter(NODE_DENSITY));
        returnedParameters.add(parameters.getParameter(ELASTIC_ENERGY));
        returnedParameters.add(parameters.getParameter(BENDING_ENERGY));
        returnedParameters.add(parameters.getParameter(IMAGE_PATH_ENERGY));
        returnedParameters.add(parameters.getParameter(SEARCH_RADIUS));
        returnedParameters.add(parameters.getParameter(NUMBER_OF_ITERATIONS));
        returnedParameters.add(parameters.getParameter(SHOW_CONTOURS));

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
