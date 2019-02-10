package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Intensity;

import ij.ImagePlus;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification.GetLocalObjectRegion;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.MathFunc.CumStat;

import java.util.ArrayList;

/**
 * Similar to MeasureObjectIntensity, but performed on circular (or spherical) regions of interest around each point in
 * 3D.  Allows the user to specify the region around each point to be measured.  Intensity traces are stored as
 * HCMultiMeasurements
 */
public class MeasureSpotIntensity extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String INPUT_OBJECTS = "Input spot objects";
    public static final String RADIUS_SOURCE = "Radius value source";
    public static final String FIXED_VALUE= "Fixed value";
    public static final String RADIUS_MEASUREMENT = "Radius measurement";
    public static final String CALIBRATED_UNITS = "Calibrated units";
    public static final String MEASURE_MEAN = "Measure mean";
    public static final String MEASURE_STDEV = "Measure standard deviation";
    public static final String MEASURE_MIN = "Measure minimum";
    public static final String MEASURE_MAX = "Measure maximum";
    public static final String MEASURE_SUM = "Measure sum";

    public interface RadiusSources {
        String FIXED_VALUE = "Fixed value";
        String MEASUREMENT = "Measurement";

        String[] ALL = new String[]{FIXED_VALUE,MEASUREMENT};

    }

    public interface Measurements {
        String MEAN = "MEAN";
        String MIN = "MIN";
        String MAX = "MAX";
        String STDEV = "STDEV";
        String SUM = "SUM";

    }


    private String getFullName(String imageName, String measurement) {
        return "SPOT_INTENSITY // "+imageName+"_"+measurement;
    }

    @Override
    public String getTitle() {
        return "Measure spot intensity";

    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_INTENSITY;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public boolean run(Workspace workspace) {
        // Getting image to measure spot intensity for
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        // Getting objects to measure
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        double radius = parameters.getValue(FIXED_VALUE);
        boolean calibrated = parameters.getValue(CALIBRATED_UNITS);
        String radiusSource = parameters.getValue(RADIUS_SOURCE);
        double fixedValue = parameters.getValue(FIXED_VALUE);
        String radiusMeasurement = parameters.getValue(RADIUS_MEASUREMENT);

        // Checking if there are any objects to measure
        if (inputObjects.size() == 0) {
            for (Obj inputObject:inputObjects.values()) {
                if (parameters.getValue(MEASURE_MEAN))
                    inputObject.getParent(inputObjectsName).addMeasurement(new Measurement(getFullName(inputImageName,Measurements.MEAN), Double.NaN));
                if (parameters.getValue(MEASURE_MIN))
                    inputObject.getParent(inputObjectsName).addMeasurement(new Measurement(getFullName(inputImageName,Measurements.MIN), Double.NaN));
                if (parameters.getValue(MEASURE_MAX))
                    inputObject.getParent(inputObjectsName).addMeasurement(new Measurement(getFullName(inputImageName,Measurements.MAX), Double.NaN));
                if (parameters.getValue(MEASURE_STDEV))
                    inputObject.getParent(inputObjectsName).addMeasurement(new Measurement(getFullName(inputImageName,Measurements.STDEV), Double.NaN));
                if (parameters.getValue(MEASURE_SUM))
                    inputObject.getParent(inputObjectsName).addMeasurement(new Measurement(getFullName(inputImageName,Measurements.SUM), Double.NaN));

            }

            return true;

        }

        boolean useMeasurement = radiusSource.equals(RadiusSources.MEASUREMENT);

        // Getting local object region
        ObjCollection spotObjects = null;
        try {
            spotObjects = new GetLocalObjectRegion().getLocalRegions(inputObjects,inputObjectsName,ipl,useMeasurement,radiusMeasurement,radius,calibrated);
        } catch (IntegerOverflowException e) {
            return false;
        }

        // Running through each object's timepoints, getting intensity measurements
        for (Obj spotObject:spotObjects.values()) {
            // Getting pixel coordinates
            ArrayList<Integer> x = spotObject.getXCoords();
            ArrayList<Integer> y = spotObject.getYCoords();
            ArrayList<Integer> z = spotObject.getZCoords();
            Integer t = spotObject.getT();

            // Initialising the cumulative statistics object to store pixel intensities.  Unlike MeasureObjectIntensity,
            // this uses a multi-element MultiCumStat where each element corresponds to a different frame
            CumStat cs = new CumStat();

            // Running through all pixels in this object and adding the intensity to the MultiCumStat object
            for (int i=0;i<x.size();i++) {
                ipl.setPosition(1,z.get(i)+1,t+1);
                cs.addMeasure(ipl.getProcessor().getPixelValue(x.get(i),y.get(i)));

            }

            // Calculating mean, std, min and max intensity and adding to the parent (we will discard the expanded
            // objects after this module has run)
            if (parameters.getValue(MEASURE_MEAN))
                spotObject.getParent(inputObjectsName).addMeasurement(new Measurement(getFullName(inputImageName,Measurements.MEAN), cs.getMean()));
            if (parameters.getValue(MEASURE_MIN))
                spotObject.getParent(inputObjectsName).addMeasurement(new Measurement(getFullName(inputImageName,Measurements.MIN), cs.getMin()));
            if (parameters.getValue(MEASURE_MAX))
                spotObject.getParent(inputObjectsName).addMeasurement(new Measurement(getFullName(inputImageName,Measurements.MAX), cs.getMax()));
            if (parameters.getValue(MEASURE_STDEV))
                spotObject.getParent(inputObjectsName).addMeasurement(new Measurement(getFullName(inputImageName,Measurements.STDEV), cs.getStd(CumStat.SAMPLE)));
            if (parameters.getValue(MEASURE_SUM))
                spotObject.getParent(inputObjectsName).addMeasurement(new Measurement(getFullName(inputImageName,Measurements.SUM), cs.getSum()));

        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new BooleanP(CALIBRATED_UNITS, this,false));
        parameters.add(new ChoiceP(RADIUS_SOURCE,this,RadiusSources.FIXED_VALUE,RadiusSources.ALL));
        parameters.add(new DoubleP(FIXED_VALUE, this,2.0));
        parameters.add(new ObjectMeasurementP(RADIUS_MEASUREMENT, this));
        parameters.add(new BooleanP(MEASURE_MEAN, this, true));
        parameters.add(new BooleanP(MEASURE_MIN, this, true));
        parameters.add(new BooleanP(MEASURE_MAX, this, true));
        parameters.add(new BooleanP(MEASURE_STDEV, this, true));
        parameters.add(new BooleanP(MEASURE_SUM, this, true));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));
        returnedParameters.add(parameters.getParameter(RADIUS_SOURCE));

        switch ((String) parameters.getValue(RADIUS_SOURCE)) {
            case RadiusSources.FIXED_VALUE:
                returnedParameters.add(parameters.getParameter(FIXED_VALUE));
                break;

            case RadiusSources.MEASUREMENT:
                returnedParameters.add(parameters.getParameter(RADIUS_MEASUREMENT));
                ((ObjectMeasurementP) parameters.getParameter(RADIUS_MEASUREMENT)).setObjectName(inputObjectsName);
                break;
        }

        returnedParameters.add(parameters.getParameter(MEASURE_MEAN));
        returnedParameters.add(parameters.getParameter(MEASURE_MIN));
        returnedParameters.add(parameters.getParameter(MEASURE_MAX));
        returnedParameters.add(parameters.getParameter(MEASURE_STDEV));
        returnedParameters.add(parameters.getParameter(MEASURE_SUM));

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        objectMeasurementRefs.setAllCalculated(false);

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String inputImageName = parameters.getValue(INPUT_IMAGE);

        if (parameters.getValue(MEASURE_MEAN)) {
            String name = getFullName(inputImageName, Measurements.MEAN);
            MeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setImageObjName(inputObjectsName);
            reference.setCalculated(true);
        }

        if (parameters.getValue(MEASURE_MIN)) {
            String name = getFullName(inputImageName, Measurements.MIN);
            MeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setImageObjName(inputObjectsName);
            reference.setCalculated(true);
        }

        if (parameters.getValue(MEASURE_MAX)) {
            String name = getFullName(inputImageName, Measurements.MAX);
            MeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setImageObjName(inputObjectsName);
            reference.setCalculated(true);
        }

        if (parameters.getValue(MEASURE_STDEV)) {
            String name = getFullName(inputImageName, Measurements.STDEV);
            MeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setImageObjName(inputObjectsName);
            reference.setCalculated(true);
        }

        if (parameters.getValue(MEASURE_SUM)) {
            String name = getFullName(inputImageName, Measurements.SUM);
            MeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setImageObjName(inputObjectsName);
            reference.setCalculated(true);
        }

        return objectMeasurementRefs;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }

}
