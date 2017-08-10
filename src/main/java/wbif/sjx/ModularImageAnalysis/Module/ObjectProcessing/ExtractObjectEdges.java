package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.ZProjector;
import ij.process.LUT;
import inra.ijpb.binary.ChamferWeights3D;
import inra.ijpb.binary.distmap.DistanceTransform3DShort;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Object.LUTs;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by sc13967 on 01/08/2017.
 */
public class ExtractObjectEdges extends HCModule {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String CREATE_EDGE_OBJECTS = "Create edge objects";
    public static final String OUTPUT_EDGE_OBJECTS = "Output edge objects";
    public static final String CREATE_INTERIOR_OBJECTS = "Create interior objects";
    public static final String OUTPUT_INTERIOR_OBJECTS = "Output interior objects";
    public static final String EDGE_MODE = "Edge determination";
    public static final String EDGE_DISTANCE = "Distance";
    public static final String EDGE_PERCENTAGE = "Percentage";

    private static final String DISTANCE_FROM_EDGE = "Distance to edge";
    private static final String PERCENTAGE_FROM_EDGE = "Percentage of maximum distance to edge";
    private static final String[] EDGE_MODES = new String[]{DISTANCE_FROM_EDGE,PERCENTAGE_FROM_EDGE};

    @Override
    public String getTitle() {
        return "Extract object edges";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void run(Workspace workspace, boolean verbose) throws GenericMIAException {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjSet inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        boolean createEdgeObjects = parameters.getValue(CREATE_EDGE_OBJECTS);
        boolean createInteriorObjects = parameters.getValue(CREATE_INTERIOR_OBJECTS);
        String edgeMode = parameters.getValue(EDGE_MODE);
        double edgeDistance = parameters.getValue(EDGE_DISTANCE);
        double edgePercentage = parameters.getValue(EDGE_PERCENTAGE);

        // Initialising output edge objects
        String outputEdgeObjectName = null;
        ObjSet outputEdgeObjects = null;
        if (createEdgeObjects) {
            outputEdgeObjectName = parameters.getValue(OUTPUT_EDGE_OBJECTS);
            outputEdgeObjects = new ObjSet(outputEdgeObjectName);
        }

        // Initialising output interior objects
        String outputInteriorObjectName = null;
        ObjSet outputInteriorObjects = null;
        if (createInteriorObjects) {
            outputInteriorObjectName = parameters.getValue(OUTPUT_INTERIOR_OBJECTS);
            outputInteriorObjects = new ObjSet(outputInteriorObjectName);
        }

        for (Obj inputObject:inputObjects.values()) {
            // Creating new edge object
            Obj outputEdgeObject = null;
            if (createEdgeObjects) {
                outputEdgeObject = new Obj(outputEdgeObjectName, outputEdgeObjects.getNextID());
            }

            // Creating new interior object
            Obj outputInteriorObject = null;
            if (createInteriorObjects) {
                outputInteriorObject = new Obj(outputInteriorObjectName, outputInteriorObjects.getNextID());
            }

            // Getting parent coordinates
            ArrayList<Integer> parentX = inputObject.getCoordinates(Obj.X);
            ArrayList<Integer> parentY = inputObject.getCoordinates(Obj.Y);
            ArrayList<Integer> parentZ = inputObject.getCoordinates(Obj.Z);

            // Creating a Hyperstack to hold the distance transform.  The image is padded by 1px to ensure the distance
            // map knows where the object edges are.  If the image is a single plane there is no z-padding.
            int[][] range = inputObject.getCoordinateRange();
            int zPad = 0;
            if (range[Obj.Z][1] - range[Obj.Z][0] > 0) zPad = 1;

            ImagePlus iplObj = IJ.createHyperStack("Objects", range[Obj.X][1] - range[Obj.X][0] + 3,
                    range[Obj.Y][1] - range[Obj.Y][0] + 3, 1, range[Obj.Z][1] - range[Obj.Z][0] + 1 + 2*zPad, 1, 8);

            // Setting pixels corresponding to the parent object to 1
            for (int i = 0; i < parentX.size(); i++) {
                iplObj.setPosition(1, parentZ.get(i) - range[Obj.Z][0] + 1 + zPad, 1);
                iplObj.getProcessor().set(parentX.get(i) - range[Obj.X][0] + 1, parentY.get(i) - range[Obj.Y][0] + 1, 255);

            }

            // Creating distance map using MorphoLibJ
            short[] weights = ChamferWeights3D.BORGEFORS.getShortWeights();
            DistanceTransform3DShort distTransform = new DistanceTransform3DShort(weights, true);
            iplObj.setStack(distTransform.distanceMap(iplObj.getStack()));

            // If percentage is being used, calculate the current value for edgeDistance
            if (edgeMode.equals(PERCENTAGE_FROM_EDGE)) {
                double maxDist = iplObj.getStatistics().max;
                edgeDistance = (edgePercentage/100)*maxDist;

            }

            // Adding pixel intensities to CumStat
            // Running through all pixels in this object and adding the intensity to the MultiCumStat object
            for (int i = 0; i < parentX.size(); i++) {
                iplObj.setPosition(1, parentZ.get(i) - range[Obj.Z][0] + 2, 1);
                double pixelVal = iplObj.getProcessor().getPixelValue(parentX.get(i) - range[Obj.X][0] + 1, parentY.get(i) - range[Obj.Y][0] + 1);

                if (pixelVal <= edgeDistance && createEdgeObjects) {
                    outputEdgeObject.addCoordinate(Obj.X,parentX.get(i));
                    outputEdgeObject.addCoordinate(Obj.Y,parentY.get(i));
                    outputEdgeObject.addCoordinate(Obj.Z,parentZ.get(i));
                    outputEdgeObject.addCoordinate(Obj.T,inputObject.getPosition(Obj.T));
                    outputEdgeObject.addCoordinate(Obj.C,inputObject.getPosition(Obj.C));

                    outputEdgeObject.addCalibration(Obj.X,inputObject.getCalibration(Obj.X));
                    outputEdgeObject.addCalibration(Obj.Y,inputObject.getCalibration(Obj.Y));
                    outputEdgeObject.addCalibration(Obj.Z,inputObject.getCalibration(Obj.Z));
                    outputEdgeObject.addCalibration(Obj.C,1);
                    outputEdgeObject.addCalibration(Obj.T,1);
                    outputEdgeObject.setCalibratedUnits(inputObject.getCalibratedUnits());

                } else if (pixelVal > edgeDistance && createInteriorObjects) {
                    outputInteriorObject.addCoordinate(Obj.X,parentX.get(i));
                    outputInteriorObject.addCoordinate(Obj.Y,parentY.get(i));
                    outputInteriorObject.addCoordinate(Obj.Z,parentZ.get(i));
                    outputInteriorObject.addCoordinate(Obj.T,inputObject.getPosition(Obj.T));
                    outputInteriorObject.addCoordinate(Obj.C,inputObject.getPosition(Obj.C));

                    outputInteriorObject.addCalibration(Obj.X,inputObject.getCalibration(Obj.X));
                    outputInteriorObject.addCalibration(Obj.Y,inputObject.getCalibration(Obj.Y));
                    outputInteriorObject.addCalibration(Obj.Z,inputObject.getCalibration(Obj.Z));
                    outputInteriorObject.addCalibration(Obj.C,1);
                    outputInteriorObject.addCalibration(Obj.T,1);
                    outputInteriorObject.setCalibratedUnits(inputObject.getCalibratedUnits());

                }
            }

            // If the current object has a size, adding it to the current object collection and assigning relationships
            if (createEdgeObjects) {
                if (outputEdgeObject.getCoordinates(Obj.X) == null) break;

                outputEdgeObjects.add(outputEdgeObject);
                outputEdgeObject.addParent(inputObject);
                inputObject.addChild(outputEdgeObject);
            }

            if (createInteriorObjects) {
                if (outputInteriorObject.getCoordinates(Obj.X) == null) break;

                outputInteriorObjects.add(outputInteriorObject);
                outputInteriorObject.addParent(inputObject);
                inputObject.addChild(outputInteriorObject);
            }
        }

        if (createEdgeObjects) workspace.addObjects(outputEdgeObjects);
        if (createInteriorObjects) workspace.addObjects(outputInteriorObjects);

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.addParameter(new Parameter(CREATE_EDGE_OBJECTS, Parameter.BOOLEAN, true));
        parameters.addParameter(new Parameter(OUTPUT_EDGE_OBJECTS, Parameter.OUTPUT_OBJECTS,null));
        parameters.addParameter(new Parameter(CREATE_INTERIOR_OBJECTS, Parameter.BOOLEAN, true));
        parameters.addParameter(new Parameter(OUTPUT_INTERIOR_OBJECTS, Parameter.OUTPUT_OBJECTS,null));
        parameters.addParameter(new Parameter(EDGE_MODE, Parameter.CHOICE_ARRAY, EDGE_MODES[0], EDGE_MODES));
        parameters.addParameter(new Parameter(EDGE_DISTANCE, Parameter.DOUBLE, 1.0));
        parameters.addParameter(new Parameter(EDGE_PERCENTAGE, Parameter.DOUBLE, 1.0));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(INPUT_OBJECTS));

        returnedParameters.addParameter(parameters.getParameter(CREATE_EDGE_OBJECTS));
        if (parameters.getValue(CREATE_EDGE_OBJECTS)) {
            returnedParameters.addParameter(parameters.getParameter(OUTPUT_EDGE_OBJECTS));
        }

        returnedParameters.addParameter(parameters.getParameter(CREATE_INTERIOR_OBJECTS));
        if (parameters.getValue(CREATE_INTERIOR_OBJECTS)) {
            returnedParameters.addParameter(parameters.getParameter(OUTPUT_INTERIOR_OBJECTS));
        }

        returnedParameters.addParameter(parameters.getParameter(EDGE_MODE));

        if (parameters.getValue(EDGE_MODE).equals(DISTANCE_FROM_EDGE)) {
            returnedParameters.addParameter(parameters.getParameter(EDGE_DISTANCE));

        } else if (parameters.getValue(EDGE_MODE).equals(PERCENTAGE_FROM_EDGE)) {
            returnedParameters.addParameter(parameters.getParameter(EDGE_PERCENTAGE));

        }

        return returnedParameters;

    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {
        String inputObjects = parameters.getValue(INPUT_OBJECTS);

        if (parameters.getValue(CREATE_EDGE_OBJECTS)) {
            String outputEdgeObjects = parameters.getValue(OUTPUT_EDGE_OBJECTS);
            relationships.addRelationship(inputObjects, outputEdgeObjects);
        }

        if (parameters.getValue(CREATE_INTERIOR_OBJECTS)) {
            String outputInteriorObjects = parameters.getValue(OUTPUT_INTERIOR_OBJECTS);
            relationships.addRelationship(inputObjects,outputInteriorObjects);
        }
    }
}
