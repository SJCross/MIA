//TODO: Add ability to expand objects by a couple of pixels, to give a background signal estimate.  Probably only makes
//      sense for normalised distances

package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Intensity;

import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import ij.process.StackStatistics;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Miscellaneous.CreateDistanceMap;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MathFunc.CumStat;
import wbif.sjx.common.Object.Point;

import java.util.LinkedHashMap;

public class MeasureRadialIntensityProfile extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String INPUT_IMAGE = "Input image";
    public static final String REFERENCE_MODE = "Reference mode";
    public static final String DISTANCE_MAP_IMAGE = "Distance map image";
    public static final String NUMBER_OF_RADIAL_SAMPLES = "Number of radial samples";
    public static final String RANGE_MODE = "Range mode";
    public static final String MIN_DISTANCE = "Minimum distance";
    public static final String MAX_DISTANCE = "Maximum distance";
//    public static final String NORMALISE_DISTANCES = "Normalise distances to object size";
    //public static final String CALIBRATED_UNITS = "Calibrated units"; // To be added


    public interface ReferenceModes extends CreateDistanceMap.ReferenceModes {
        String CUSTOM_DISTANCE_MAP = "Custom distance map";

        String[] ALL = new String[]{CreateDistanceMap.ReferenceModes.DISTANCE_FROM_CENTROID,
                CreateDistanceMap.ReferenceModes.DISTANCE_FROM_EDGE,CUSTOM_DISTANCE_MAP};

    }

    public interface RangeModes {
        String AUTOMATIC_RANGE = "Automatic range";
        String MANUAL_RANGE = "Manual range";

        String[] ALL = new String[]{AUTOMATIC_RANGE,MANUAL_RANGE};

    }


    static Image getDistanceMap(ObjCollection inputObjects, Image inputImage, String referenceMode) {
        switch (referenceMode) {
            case ReferenceModes.DISTANCE_FROM_CENTROID:
                return CreateDistanceMap.getCentroidDistanceMap(inputImage,inputObjects,"Distance map");

            case ReferenceModes.DISTANCE_FROM_EDGE:
                return CreateDistanceMap.getEdgeDistanceMap(inputImage,inputObjects,"Distance map",false);
        }

        return null;

    }

    /**
     * Calculates the bin centroids for the distance measurements from the maximum range in the distance map.
     * @param distanceMap
     * @param nRadialSamples
     * @return
     */
    static double[] getDistanceBins(Image distanceMap, int nRadialSamples) {
        // Getting the maximum distance measurement
        StackStatistics stackStatistics = new StackStatistics(distanceMap.getImagePlus());
        double minDistance = stackStatistics.min;
        double maxDistance = stackStatistics.max;

        double[] distanceBins = new double[nRadialSamples];
        double binWidth = (maxDistance-minDistance)/(nRadialSamples-1);
        for (int i=0;i<nRadialSamples;i++) {
            distanceBins[i] = (i*binWidth)+minDistance;
            System.err.println("Bin "+i+"_"+(i*binWidth));
        }

        return distanceBins;

    }

    /**
     * Calculates the bin centroids for the distance measurements from provided range values.
     * @param nRadialSamples
     * @param minDistance
     * @param maxDistance
     * @return
     */
    static double[] getDistanceBins(int nRadialSamples, double minDistance, double maxDistance) {
        double[] distanceBins = new double[nRadialSamples];
        double binWidth = (maxDistance-minDistance)/(nRadialSamples-1);
        for (int i=0;i<nRadialSamples;i++) {
            distanceBins[i] = (i*binWidth)+minDistance;
            System.err.println("Bin "+i+"_"+(i*binWidth));
        }

        return distanceBins;

    }

    static CumStat[] processObject(Obj inputObject, Image inputImage, Image distanceMap, double[] distanceBins) {
        // Setting up CumStats to hold results
        CumStat[] cumStats = new CumStat[distanceBins.length];
        for (int i=0;i<cumStats.length;i++) cumStats[i] = new CumStat();

        ImagePlus inputIpl = inputImage.getImagePlus();
        ImagePlus distanceMapIpl = distanceMap.getImagePlus();

        int t = inputObject.getT();

        double minDist = distanceBins[0];
        double maxDist = distanceBins[distanceBins.length-1];
        double binWidth = distanceBins[1]-distanceBins[0];

        for (Point<Integer> point:inputObject.getPoints()) {
            int x = point.getX();
            int y = point.getY();
            int z = point.getZ();

            inputIpl.setPosition(1, z + 1, t + 1);
            distanceMapIpl.setPosition(1, z + 1, t + 1);

            double distance = distanceMapIpl.getProcessor().getPixelValue(x, y);
            double intensity = inputIpl.getProcessor().getPixelValue(x, y);

            double bin = Math.round((distance - minDist) / binWidth) * binWidth + minDist;

            // Ensuring the bin is within the specified range
            bin = Math.min(bin, maxDist);
            bin = Math.max(bin, minDist);

            // Adding the measurement to the relevant bin
            for (int i=0;i<distanceBins.length;i++) {
                if (bin == distanceBins[i]) cumStats[i].addMeasure(intensity);
            }
        }

        return cumStats;

    }

    @Override
    public String getTitle() {
        return "Measure radial intensity profile";
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
    protected void run(Workspace workspace) throws GenericMIAException {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);

        // Getting other parameters
        String referenceMode = parameters.getValue(REFERENCE_MODE);
        String distanceMapImageName = parameters.getValue(DISTANCE_MAP_IMAGE);
        int nRadialSample = parameters.getValue(NUMBER_OF_RADIAL_SAMPLES);
        String rangeMode = parameters.getValue(RANGE_MODE);
        double minDistance = parameters.getValue(MIN_DISTANCE);
        double maxDistance = parameters.getValue(MAX_DISTANCE);

        // Getting the distance map for all objects
        Image distanceMap = null;
        switch (referenceMode) {
            case ReferenceModes.CUSTOM_DISTANCE_MAP:
                distanceMap = workspace.getImage(distanceMapImageName);
                break;

            case ReferenceModes.DISTANCE_FROM_EDGE:
            case ReferenceModes.DISTANCE_FROM_CENTROID:
                distanceMap = getDistanceMap(inputObjects,inputImage,referenceMode);
                break;
        }

        // Getting the distance bin centroids
        double[] distanceBins = null;
        switch (rangeMode) {
            case RangeModes.AUTOMATIC_RANGE:
                distanceBins = getDistanceBins(distanceMap,nRadialSample);
                break;

            case RangeModes.MANUAL_RANGE:
                distanceBins = getDistanceBins(nRadialSample,minDistance,maxDistance);
                break;
        }

        // Creating a new ResultsTable
        ResultsTable resultsTable = new ResultsTable();

        // Adding distance bin values to ResultsTable6g
        for (int i=0;i<distanceBins.length;i++) {
            resultsTable.setValue("Distance",i,distanceBins[i]);
        }

        // Processing each object
        int count = 0;
        int total = inputObjects.size();
        for (Obj inputObject:inputObjects.values()) {
            writeMessage("Processing object "+(++count)+" of "+total);
            CumStat[] cumStats = processObject(inputObject,inputImage,distanceMap,distanceBins);

            for (int i=0;i<distanceBins.length;i++) {
                double meanVal = cumStats[i].getMean();
                resultsTable.setValue(("Object "+count),i,meanVal);
            }
        }

        resultsTable.show("Radial intensity profile");

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS,Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(INPUT_IMAGE,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(REFERENCE_MODE,Parameter.CHOICE_ARRAY,ReferenceModes.DISTANCE_FROM_CENTROID,ReferenceModes.ALL));
        parameters.add(new Parameter(DISTANCE_MAP_IMAGE,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(NUMBER_OF_RADIAL_SAMPLES,Parameter.INTEGER,10));
        parameters.add(new Parameter(RANGE_MODE,Parameter.CHOICE_ARRAY,RangeModes.AUTOMATIC_RANGE,RangeModes.ALL));
        parameters.add(new Parameter(MIN_DISTANCE,Parameter.DOUBLE,0d));
        parameters.add(new Parameter(MAX_DISTANCE,Parameter.DOUBLE,1d));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(REFERENCE_MODE));
        switch ((String) parameters.getValue(REFERENCE_MODE)) {
            case ReferenceModes.CUSTOM_DISTANCE_MAP:
                returnedParameters.add(parameters.getParameter(DISTANCE_MAP_IMAGE));
                break;
        }

        returnedParameters.add(parameters.getParameter(NUMBER_OF_RADIAL_SAMPLES));

        returnedParameters.add(parameters.getParameter(RANGE_MODE));
        switch ((String) parameters.getValue(RANGE_MODE)) {
            case RangeModes.MANUAL_RANGE:
                returnedParameters.add(parameters.getParameter(MIN_DISTANCE));
                returnedParameters.add(parameters.getParameter(MAX_DISTANCE));
                break;
        }

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
