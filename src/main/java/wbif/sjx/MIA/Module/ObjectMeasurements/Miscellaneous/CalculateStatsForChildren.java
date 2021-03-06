package wbif.sjx.MIA.Module.ObjectMeasurements.Miscellaneous;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChildObjectsP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ObjectMeasurementP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.References.ObjMeasurementRef;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.common.MathFunc.CumStat;

public class CalculateStatsForChildren extends Module {
    public static final String INPUT_SEPARATOR = "Objects input";
    public static final String PARENT_OBJECTS = "Parent objects";
    public static final String CHILD_OBJECTS = "Child objects";

    public static final String STATISTIC_SEPARATOR = "Statistics";
    public static final String MEASUREMENT = "Measurement";
    public static final String CALCULATE_MEAN = "Calculate mean";
    public static final String CALCULATE_STD = "Calculate standard deviation";
    public static final String CALCULATE_MIN = "Calculate minimum";
    public static final String CALCULATE_MAX = "Calculate maximum";
    public static final String CALCULATE_SUM = "Calculate sum";

    public CalculateStatsForChildren(ModuleCollection modules) {
        super("Calculate statistics for children",modules);
    }

    public interface Measurements {
        String MEAN = "MEAN";
        String STD = "STD";
        String MIN = "MIN";
        String MAX = "MAX";
        String SUM = "SUM";

    }

    public static String getFullName(String childObjectName, String measurement, String measurementType) {
        return "CHILD_STATS // "+measurementType+"_"+childObjectName+"_\""+measurement+"\"";
    }

    public static void processObject(Obj parentObject, String childObjectsName, String measurement, boolean[] statsToCalculate) {
        ObjCollection childObjects = parentObject.getChildren(childObjectsName);

        // Calculating statistics for measurement
        CumStat cs = new CumStat();
        if (childObjects != null) {
            for (Obj childObject : childObjects.values()) {
                // Check the measurement exists
                if (childObject.getMeasurement(measurement) == null) continue;

                if (childObject.getMeasurement(measurement).getValue() != Double.NaN) {
                    cs.addMeasure(childObject.getMeasurement(measurement).getValue());
                }
            }
        }

        if (statsToCalculate[0]) {
            String name = getFullName(childObjectsName,measurement,Measurements.MEAN);
            parentObject.addMeasurement(new Measurement(name, cs.getMean()));
        }

        if (statsToCalculate[1]) {
            String name = getFullName(childObjectsName,measurement,Measurements.STD);
            parentObject.addMeasurement(new Measurement(name, cs.getStd()));
        }

        if (statsToCalculate[2]) {
            String name = getFullName(childObjectsName,measurement,Measurements.MIN);
            parentObject.addMeasurement(new Measurement(name, cs.getMin()));
        }

        if (statsToCalculate[3]) {
            String name = getFullName(childObjectsName,measurement,Measurements.MAX);
            parentObject.addMeasurement(new Measurement(name, cs.getMax()));
        }

        if (statsToCalculate[4]) {
            String name = getFullName(childObjectsName,measurement,Measurements.SUM);
            parentObject.addMeasurement(new Measurement(name, cs.getSum()));
        }
    }



    @Override
    public Category getCategory() {
        return Categories.OBJECT_MEASUREMENTS_MISCELLANEOUS;
    }

    @Override
    public String getDescription() {
        return "Calculates statistics for a measurement associated with all child objects of parent object.  The calculated statistics are stored as new measurements, associated with the relevant parent object.  For example, calculating the summed volume of all child objects (from a specified collection) of each parent object.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input objects
        String parentObjectsName = parameters.getValue(PARENT_OBJECTS);
        ObjCollection parentObjects = workspace.getObjects().get(parentObjectsName);

        // Getting other parameters
        String childObjectsName = parameters.getValue(CHILD_OBJECTS);
        String measurement = parameters.getValue(MEASUREMENT);
        boolean[] statsToCalculate = new boolean[5];
        statsToCalculate[0] = parameters.getValue(CALCULATE_MEAN);
        statsToCalculate[1] = parameters.getValue(CALCULATE_STD);
        statsToCalculate[2] = parameters.getValue(CALCULATE_MIN);
        statsToCalculate[3] = parameters.getValue(CALCULATE_MAX);
        statsToCalculate[4] = parameters.getValue(CALCULATE_SUM);

        for (Obj parentObject:parentObjects.values()) {
            processObject(parentObject,childObjectsName,measurement,statsToCalculate);
        }

        if (showOutput) parentObjects.showMeasurements(this,modules);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputObjectsP(PARENT_OBJECTS,this));
        parameters.add(new ChildObjectsP(CHILD_OBJECTS,this));

        parameters.add(new SeparatorP(STATISTIC_SEPARATOR,this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT,this));
        parameters.add(new BooleanP(CALCULATE_MEAN,this,true));
        parameters.add(new BooleanP(CALCULATE_STD,this,true));
        parameters.add(new BooleanP(CALCULATE_MIN,this,true));
        parameters.add(new BooleanP(CALCULATE_MAX,this,true));
        parameters.add(new BooleanP(CALCULATE_SUM,this,true));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        String objectName = parameters.getValue(PARENT_OBJECTS);
        ((ChildObjectsP) parameters.getParameter(CHILD_OBJECTS)).setParentObjectsName(objectName);

        String childObjectsName = parameters.getValue(CHILD_OBJECTS);
        ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT)).setObjectName(childObjectsName);

        return parameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefCollection returnedRefs = new ObjMeasurementRefCollection();

        String parentObjectsName = parameters.getValue(PARENT_OBJECTS);
        String childObjectsName = parameters.getValue(CHILD_OBJECTS);
        String measurementName = parameters.getValue(MEASUREMENT);

        if ((boolean) parameters.getValue(CALCULATE_MEAN)) {
            String name = getFullName(childObjectsName,measurementName,Measurements.MEAN);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(parentObjectsName);
            reference.setDescription("Mean value of measurement, \"" +measurementName+"\", for child objects, \""+
                    childObjectsName+"\".");
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(CALCULATE_STD)) {
            String name = getFullName(childObjectsName,measurementName,Measurements.STD);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(parentObjectsName);
            reference.setDescription("Standard deviation of measurement, \"" +measurementName+"\", for child objects, \""+
                    childObjectsName+"\".");
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(CALCULATE_MIN)) {
            String name = getFullName(childObjectsName,measurementName,Measurements.MIN);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(parentObjectsName);
            reference.setDescription("Minimum value of measurement, \"" +measurementName+"\", for child objects, \""+
                    childObjectsName+"\".");
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(CALCULATE_MAX)) {
            String name = getFullName(childObjectsName,measurementName,Measurements.MAX);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(parentObjectsName);
            reference.setDescription("Maximum value of measurement, \"" +measurementName+"\", for child objects, \""+
                    childObjectsName+"\".");
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(CALCULATE_SUM)) {
            String name = getFullName(childObjectsName,measurementName,Measurements.SUM);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(parentObjectsName);
            reference.setDescription("Summed value of measurement, \"" +measurementName+"\", for child objects, \""+
                    childObjectsName+"\".");
            returnedRefs.add(reference);
        }

        return returnedRefs;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
      parameters.get(PARENT_OBJECTS).setDescription("Input object collection from the workspace for which statistics of child object measurements will be calculated.  This object collection is a parent to those selected by the \""+CHILD_OBJECTS+"\" parameter.  Statistics for one measurement associated with all children of each input parent object will be calculated and added to this object as a new measurement.");

      parameters.get(CHILD_OBJECTS).setDescription("Input object collection from the workspace, where these objects are children of the collection selected by the \""+PARENT_OBJECTS+"\" parameter.)");

      parameters.get(MEASUREMENT).setDescription("Measurement associated with the child objects for which statistics will be calculated.  Statistics will be calculated for all children of a parent object.");

      parameters.get(CALCULATE_MEAN).setDescription("When selected, the mean value of the measurements will be calculated and added to the relevant parent object.");

      parameters.get(CALCULATE_STD).setDescription("When selected, the standard deviation of the measurements will be calculated and added to the relevant parent object.");

      parameters.get(CALCULATE_MIN).setDescription("When selected, the minimum value of the measurements will be calculated and added to the relevant parent object.");

      parameters.get(CALCULATE_MAX).setDescription("When selected, the maximum value of the measurements will be calculated and added to the relevant parent object.");

      parameters.get(CALCULATE_SUM).setDescription("When selected, the sum of the measurements will be calculated and added to the relevant parent object.");

    }
}
