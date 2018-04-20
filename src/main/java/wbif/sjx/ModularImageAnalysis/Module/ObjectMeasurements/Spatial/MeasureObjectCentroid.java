// TODO: Add measurements

package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.util.ArrayList;

/**
 * Created by sc13967 on 11/05/2017.
 */
public class MeasureObjectCentroid extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String CENTROID_METHOD = "Centroid method";

    public interface Methods {
        String MEAN = "Mean";
        String MEDIAN = "Median";
        String BOTH = "Both";

        String[] ALL = new String[]{MEAN, MEDIAN, BOTH};

    }

    public interface Measurements {
        String MEAN_X_PX = "CENTROID//MEAN_X_(PX)";
        String MEAN_Y_PX = "CENTROID//MEAN_Y_(PX)";
        String MEAN_Z_SLICE = "CENTROID//MEAN_Z_(SLICE)";
        String MEAN_X_CAL = "CENTROID//MEAN_X_(${CAL})";
        String MEAN_Y_CAL = "CENTROID//MEAN_Y_(${CAL})";
        String MEAN_Z_CAL = "CENTROID//MEAN_Z_(${CAL})";
        String MEDIAN_X_PX = "CENTROID//MEDIAN_X_(PX)";
        String MEDIAN_Y_PX = "CENTROID//MEDIAN_Y_(PX)";
        String MEDIAN_Z_SLICE = "CENTROID//MEDIAN_Z_(SLICE)";
        String MEDIAN_X_CAL = "CENTROID//MEDIAN_X_(${CAL})";
        String MEDIAN_Y_CAL = "CENTROID//MEDIAN_Y_(${CAL})";
        String MEDIAN_Z_CAL = "CENTROID//MEDIAN_Z_(${CAL})";

    }


    @Override
    public String getTitle() {
        return "Measure object centroid";

    }

    @Override
    public String getHelp() {
        return "Z-coordinates are specified in terms of slices (not pixels)";
    }

    @Override
    public void run(Workspace workspace) {
        // Getting current objects
        String inputObjectName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectName);

        // Getting which centroid measures to calculate
        String choice = parameters.getValue(CENTROID_METHOD);
        boolean useMean = choice.equals(Methods.MEAN) | choice.equals(Methods.BOTH);
        boolean useMedian = choice.equals(Methods.MEDIAN) | choice.equals(Methods.BOTH);
        writeMessage("Calculating centroid as "+choice);

        // Getting the centroids of each and saving them to the objects
        for (Obj object:inputObjects.values()) {
            ArrayList<Integer> x = object.getXCoords();
            ArrayList<Integer> y = object.getYCoords();
            ArrayList<Integer> z = object.getZCoords();

            if (useMean) {
                if (x != null) {
                    object.addMeasurement(new Measurement(Measurements.MEAN_X_PX,object.getXMean(true)));
                    object.addMeasurement(new Measurement(Units.replace(Measurements.MEAN_X_CAL),object.getXMean(false)));
                }
                if (y!= null) {
                    object.addMeasurement(new Measurement(Measurements.MEAN_Y_PX,object.getYMean(true)));
                    object.addMeasurement(new Measurement(Units.replace(Measurements.MEAN_Y_CAL),object.getYMean(false)));
                }
                if (z!= null) {
                    object.addMeasurement(new Measurement(Measurements.MEAN_Z_SLICE,object.getZMean(true,false)));
                    object.addMeasurement(new Measurement(Units.replace(Measurements.MEAN_Z_CAL),object.getZMean(false,false)));
                }
            }

            if (useMedian) {
                if (x != null) {
                    object.addMeasurement(new Measurement(Measurements.MEDIAN_X_PX,object.getXMedian(true)));
                    object.addMeasurement(new Measurement(Units.replace(Measurements.MEDIAN_X_CAL),object.getXMedian(false)));
                }
                if (y!= null) {
                    object.addMeasurement(new Measurement(Measurements.MEDIAN_Y_PX,object.getYMedian(true)));
                    object.addMeasurement(new Measurement(Measurements.MEDIAN_Y_CAL,object.getYMedian(false)));
                }
                if (z!= null) {
                    object.addMeasurement(new Measurement(Measurements.MEDIAN_Z_SLICE,object.getZMedian(true,false)));
                    object.addMeasurement(new Measurement(Measurements.MEDIAN_Z_CAL,object.getZMedian(false,false)));
                }
            }
        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(CENTROID_METHOD, Parameter.CHOICE_ARRAY,Methods.MEAN,Methods.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        objectMeasurementReferences.setAllCalculated(false);

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        String choice = parameters.getValue(CENTROID_METHOD);
        boolean useMean = choice.equals(Methods.MEAN) | choice.equals(Methods.BOTH);
        boolean useMedian = choice.equals(Methods.MEDIAN) | choice.equals(Methods.BOTH);

        if (useMean) {
            MeasurementReference meanXPx = objectMeasurementReferences.getOrPut(Measurements.MEAN_X_PX);
            MeasurementReference meanYPx = objectMeasurementReferences.getOrPut(Measurements.MEAN_Y_PX);
            MeasurementReference meanZSlice = objectMeasurementReferences.getOrPut(Measurements.MEAN_Z_SLICE);
            MeasurementReference meanXCal = objectMeasurementReferences.getOrPut(Units.replace(Measurements.MEAN_X_CAL));
            MeasurementReference meanYCal = objectMeasurementReferences.getOrPut(Units.replace(Measurements.MEAN_Y_CAL));
            MeasurementReference meanZCal = objectMeasurementReferences.getOrPut(Units.replace(Measurements.MEAN_Z_CAL));

            meanXPx.setImageObjName(inputObjectsName);
            meanYPx.setImageObjName(inputObjectsName);
            meanZSlice.setImageObjName(inputObjectsName);
            meanXCal.setImageObjName(inputObjectsName);
            meanYCal.setImageObjName(inputObjectsName);
            meanZCal.setImageObjName(inputObjectsName);

            meanXPx.setCalculated(true);
            meanYPx.setCalculated(true);
            meanZSlice.setCalculated(true);
            meanXCal.setCalculated(true);
            meanYCal.setCalculated(true);
            meanZCal.setCalculated(true);
        }

        if (useMedian) {
            MeasurementReference medianXPx = objectMeasurementReferences.getOrPut(Measurements.MEDIAN_X_PX);
            MeasurementReference medianYPx = objectMeasurementReferences.getOrPut(Measurements.MEDIAN_Y_PX);
            MeasurementReference medianZSlice = objectMeasurementReferences.getOrPut(Measurements.MEDIAN_Z_SLICE);
            MeasurementReference medianXCal = objectMeasurementReferences.getOrPut(Units.replace(Measurements.MEDIAN_X_CAL));
            MeasurementReference medianYCal = objectMeasurementReferences.getOrPut(Units.replace(Measurements.MEDIAN_Y_CAL));
            MeasurementReference medianZCal = objectMeasurementReferences.getOrPut(Units.replace(Measurements.MEDIAN_Z_CAL));

            medianXPx.setImageObjName(inputObjectsName);
            medianYPx.setImageObjName(inputObjectsName);
            medianZSlice.setImageObjName(inputObjectsName);
            medianXCal.setImageObjName(inputObjectsName);
            medianYCal.setImageObjName(inputObjectsName);
            medianZCal.setImageObjName(inputObjectsName);

            medianXPx.setCalculated(true);
            medianYPx.setCalculated(true);
            medianZSlice.setCalculated(true);
            medianXCal.setCalculated(true);
            medianYCal.setCalculated(true);
            medianZCal.setCalculated(true);
        }

        return objectMeasurementReferences;

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
