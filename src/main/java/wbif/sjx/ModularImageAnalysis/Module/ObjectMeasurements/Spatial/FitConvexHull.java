//package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial;
//
//import org.apache.commons.math3.exception.InsufficientDataException;
//import org.apache.commons.math3.geometry.Point;
//import org.apache.commons.math3.geometry.hull.ConvexHull;
//import org.apache.commons.math3.geometry.partitioning.Region;
//import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
//import wbif.sjx.ModularImageAnalysis.Module.Module;
//import wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial.MeasureObjectShape;
//import wbif.sjx.ModularImageAnalysis.Object.*;
//import wbif.sjx.common.Analysis.ConvexHullCalculator;
//import wbif.sjx.common.Object.Volume;
//
///**
// * Created by sc13967 on 18/06/2018.
// */
//public class FitConvexHull extends Module {
//    public static final String INPUT_OBJECTS = "Input objects";
//    public static final String OBJECT_OUTPUT_MODE = "Object output mode";
//    public static final String OUTPUT_OBJECTS = "Output objects";
//    public static final String FITTING_MODE = "Fitting mode";
//    public static final String TOL1 = "Tol1";
//    public static final String TOL2 = "Tol2";
//
//
//    public interface OutputModes {
//        String DO_NOT_STORE = "Do not store";
//        String CREATE_NEW_OBJECT = "Create new objects";
//        String UPDATE_INPUT = "Update input objects";
//
//        String[] ALL = new String[]{DO_NOT_STORE,CREATE_NEW_OBJECT,UPDATE_INPUT};
//
//    }
//
//    public interface FittingModes {
//        String CENTROIDS = "Pixel centroids";
//        String CORNERS = "Pixel corners";
//
//        String[] ALL = new String[]{CENTROIDS,CORNERS};
//
//    }
//
//    public interface Measurements {
//        String HULL_VOLUME_PX = "CONVEX_HULL // VOLUME_(PX^3)";
//        String HULL_VOLUME_CAL = "CONVEX_HULL // VOLUME_(${CAL}^3)";
//        String HULL_SURFACE_AREA_PX = "CONVEX_HULL // SURFACE_AREA_(PX^2)";
//        String HULL_SURFACE_AREA_CAL = "CONVEX_HULL // SURFACE_AREA_(${CAL}^2)";
//        String SPHERICITY = "CONVEX_HULL // SPHERICITY";
//        String SOLIDITY = "CONVEX_HULL // SOLIDITY";
//
//    }
//
//    public void processObject(Obj inputObject, int mode, ObjCollection outputObjects, String objectOutputMode) {
//        ConvexHullCalculator calculator = new ConvexHullCalculator(inputObject, mode,parameters.getValue(TOL1),parameters.getValue(TOL2));
//
//        // Adding measurements
//        addMeasurements(inputObject,calculator);
//
//        // If the hull can't be fit, terminating the current iteration
//        if (!calculator.canFitHull()) return;
//
//        Volume hull = !objectOutputMode.equals(OutputModes.DO_NOT_STORE) ? calculator.getContainedPoints() : null;
//
//        switch (objectOutputMode) {
//            case OutputModes.CREATE_NEW_OBJECT:
//                Obj hullObject = createNewObject(inputObject,hull,outputObjects);
//                if (hullObject != null) outputObjects.add(hullObject);
//                break;
//            case OutputModes.UPDATE_INPUT:
//                updateInputObject(inputObject,hull);
//                break;
//        }
//    }
//
//    public int getFitMode(String fittingMode) {
//        // Getting the mode with which to run the ConvexHullCalculator
//        switch (fittingMode) {
//            case FittingModes.CENTROIDS:
//                return ConvexHullCalculator.CENTROID;
//            case FittingModes.CORNERS:
//                return ConvexHullCalculator.CORNER;
//        }
//
//        return 0;
//
//    }
//
//    public void addMeasurements(Obj inputObject, ConvexHullCalculator calculator) {
//        // If the convex hull can't be fit add blank measurements
//        if (!calculator.canFitHull()) {
//            inputObject.addMeasurement(new Measurement(Measurements.HULL_VOLUME_PX, Double.NaN));
//            inputObject.addMeasurement(new Measurement(Units.replace(Measurements.HULL_VOLUME_CAL), Double.NaN));
//            inputObject.addMeasurement(new Measurement(Measurements.HULL_SURFACE_AREA_PX, Double.NaN));
//            inputObject.addMeasurement(new Measurement(Units.replace(Measurements.HULL_SURFACE_AREA_CAL), Double.NaN));
//            inputObject.addMeasurement(new Measurement(Measurements.SPHERICITY, Double.NaN));
//            inputObject.addMeasurement(new Measurement(Measurements.SOLIDITY, Double.NaN));
//            return;
//        }
//
//        // Hull volume was calculated using
//        double hullVolumePx = calculator.getHullVolume(true);
//        double hullVolumeCal = calculator.getHullVolume(false);
//        double hullSurfaceAreaPx = calculator.getHullSurfaceArea(true);
//        double hullSurfaceAreaCal = calculator.getHullSurfaceArea(false);
//        double sphericity = calculator.getSphericity();
//        double solidity = calculator.getSolidity();
//
//        inputObject.addMeasurement(new Measurement(Measurements.HULL_VOLUME_PX, hullVolumePx));
//        inputObject.addMeasurement(new Measurement(Units.replace(Measurements.HULL_VOLUME_CAL), hullVolumeCal));
//        inputObject.addMeasurement(new Measurement(Measurements.HULL_SURFACE_AREA_PX, hullSurfaceAreaPx));
//        inputObject.addMeasurement(new Measurement(Units.replace(Measurements.HULL_SURFACE_AREA_CAL), hullSurfaceAreaCal));
//        inputObject.addMeasurement(new Measurement(Measurements.SPHERICITY, sphericity));
//        inputObject.addMeasurement(new Measurement(Measurements.SOLIDITY, solidity));
//    }
//
//    public Obj createNewObject (Obj inputObject, Volume hull, ObjCollection outputObjects) {
//        if (hull == null) return null;
//
//        double dppXY = inputObject.getDistPerPxXY();
//        double dppZ = inputObject.getDistPerPxZ();
//        String units = inputObject.getCalibratedUnits();
//        boolean is2D = inputObject.is2D();
//
//        Obj hullObject = new Obj(outputObjects.getName(),outputObjects.getNextID(),dppXY,dppZ,units,is2D);
//        hullObject.setPoints(hull.getPoints());
//
//        hullObject.addParent(inputObject);
//        inputObject.addChild(hullObject);
//        outputObjects.add(hullObject);
//
//        return hullObject;
//
//    }
//
//    public void updateInputObject(Obj inputObject, Volume hull) {
//        inputObject.setPoints(hull.getPoints());
//    }
//
//
//    @Override
//    public String getTitle() {
//        return "Fit convex hull";
//    }
//
//    @Override
//    public String getHelp() {
//        return "Uses QuickHull3D to fit a convex hull." +
//                "\nhttps://github.com/Quickhull3d/quickhull3d";
//    }
//
//    @Override
//    protected void run(Workspace workspace) throws GenericMIAException {
//        // Getting input objects
//        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
//        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);
//
//        // Getting parameters
//        String objectOutputMode = parameters.getValue(OBJECT_OUTPUT_MODE);
//        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
//        String fittingMode = parameters.getValue(FITTING_MODE);
//        int mode = getFitMode(fittingMode);
//
//        // If necessary, creating a new ObjCollection and adding it to the Workspace
//        ObjCollection outputObjects = null;
//        if (objectOutputMode.equals(OutputModes.CREATE_NEW_OBJECT)) {
//            outputObjects = new ObjCollection(outputObjectsName);
//            workspace.addObjects(outputObjects);
//        }
//
//        // Running through each object, taking measurements and adding new object to the workspace where necessary
//        int count = 0;
//        int nTotal = inputObjects.size();
//        for (Obj inputObject:inputObjects.values()) {
//            processObject(inputObject,mode,outputObjects,objectOutputMode);
//            writeMessage("Processed object "+(++count)+" of "+nTotal);
//        }
//    }
//
//    @Override
//    protected void initialiseParameters() {
//        parameters.add(new Parameter(INPUT_OBJECTS,Parameter.INPUT_OBJECTS,null));
//        parameters.add(new Parameter(OBJECT_OUTPUT_MODE,Parameter.CHOICE_ARRAY,OutputModes.DO_NOT_STORE,OutputModes.ALL));
//        parameters.add(new Parameter(OUTPUT_OBJECTS,Parameter.OUTPUT_OBJECTS,""));
//        parameters.add(new Parameter(FITTING_MODE,Parameter.CHOICE_ARRAY,FittingModes.CENTROIDS,FittingModes.ALL));
//        parameters.add(new Parameter(TOL1,Parameter.DOUBLE,1E-2));
//        parameters.add(new Parameter(TOL2,Parameter.DOUBLE,1E-2));
//
//    }
//
//    @Override
//    public ParameterCollection updateAndGetParameters() {
//        ParameterCollection returnedParameters = new ParameterCollection();
//
//        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
//
//        returnedParameters.add(parameters.getParameter(OBJECT_OUTPUT_MODE));
//        switch ((String) parameters.getValue(OBJECT_OUTPUT_MODE)) {
//            case OutputModes.CREATE_NEW_OBJECT:
//                returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));
//                break;
//        }
//
//        returnedParameters.add(parameters.getParameter(FITTING_MODE));
//        returnedParameters.add(parameters.getParameter(TOL1));
//        returnedParameters.add(parameters.getParameter(TOL2));
//
//        return returnedParameters;
//
//    }
//
//    @Override
//    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
//        return null;
//    }
//
//    @Override
//    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
//        objectMeasurementReferences.setAllCalculated(false);
//
//        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
//
//        MeasurementReference reference = objectMeasurementReferences.getOrPut(Measurements.HULL_VOLUME_PX);
//        reference.setCalculated(true);
//        reference.setImageObjName(inputObjectsName);
//
//        reference = objectMeasurementReferences.getOrPut(Units.replace(Measurements.HULL_VOLUME_CAL));
//        reference.setCalculated(true);
//        reference.setImageObjName(inputObjectsName);
//
//        reference = objectMeasurementReferences.getOrPut(Measurements.HULL_SURFACE_AREA_PX);
//        reference.setCalculated(true);
//        reference.setImageObjName(inputObjectsName);
//
//        reference = objectMeasurementReferences.getOrPut(Units.replace(Measurements.HULL_SURFACE_AREA_CAL));
//        reference.setCalculated(true);
//        reference.setImageObjName(inputObjectsName);
//
//        reference = objectMeasurementReferences.getOrPut(Measurements.SPHERICITY);
//        reference.setCalculated(true);
//        reference.setImageObjName(inputObjectsName);
//
//        reference = objectMeasurementReferences.getOrPut(Measurements.SOLIDITY);
//        reference.setCalculated(true);
//        reference.setImageObjName(inputObjectsName);
//
//        return objectMeasurementReferences;
//
//    }
//
//    @Override
//    public void addRelationships(RelationshipCollection relationships) {
//
//    }
//}
