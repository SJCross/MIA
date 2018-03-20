package wbif.sjx.ModularImageAnalysis.Module.ImageMeasurements;

import ij.ImagePlus;
import ij.plugin.Duplicator;
import inra.ijpb.binary.ChamferWeights3D;
import inra.ijpb.plugins.GeodesicDistanceMap3D;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.Visualisation.ShowObjects;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.common.MathFunc.CumStat;

import java.util.HashMap;

/**
 * Created by Stephen on 17/11/2017.
 */
public class MeasureIntensityDistribution extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String MEASUREMENT_TYPE = "Measurement type";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String PROXIMAL_DISTANCE = "Proximal distance";
    public static final String SPATIAL_UNITS = "Spatial units";
    public static final String IGNORE_ON_OBJECTS = "Ignore values on objects";

    public interface MeasurementTypes {
        String FRACTION_PROXIMAL_TO_OBJECTS = "Fraction proximal to objects";
        String INTENSITY_WEIGHTED_PROXIMITY = "Intensity-weighted proximity";

        String[] ALL = new String[]{FRACTION_PROXIMAL_TO_OBJECTS,INTENSITY_WEIGHTED_PROXIMITY};

    }

    public interface SpatialUnits {
        String CALIBRATED = "Calibrated";
        String PIXELS = "Pixel";

        String[] ALL = new String[]{CALIBRATED,PIXELS};

    }

    private interface Measurements {
        String N_PX_INRANGE = "N_PX_INRANGE";
        String N_PX_OUTRANGE = "N_PX_OUTRANGE";
        String SUM_INT_INRANGE = "SUM_INT_INRANGE";
        String SUM_INT_OUTRANGE = "SUM_INT_OUTRANGE";
        String MEAN_INT_INRANGE = "MEAN_INT_INRANGE";
        String MEAN_INT_OUTRANGE = "MEAN_INT_OUTRANGE";
        String MEAN_PROXIMITY = "MEAN_PROXIMITY";
        String STDEV_PROXIMITY = "STDEV_PROXIMITY";

    }


    private String getFullName(String objectsName, String measurement) {
        return "INT_DISTR//"+objectsName+"_"+measurement;
    }

    public CumStat[] measureFractionProximal(ObjCollection inputObjects, Image inputImage, double proximalDistance, boolean ignoreOnObjects) {
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Get binary image showing the objects
        HashMap<Integer,Float> hues = inputObjects.getHue(ObjCollection.ColourModes.SINGLE_COLOUR,"",false);
        Image objectsImage = inputObjects.convertObjectsToImage("Objects", inputImagePlus, ShowObjects.ColourModes.SINGLE_COLOUR, hues, true);

        // Calculating a 3D distance map for the binary image
        ImagePlus maskIpl = new Duplicator().run(objectsImage.getImagePlus());

        // Inverting the mask intensity
        for (int z = 1; z <= maskIpl.getNSlices(); z++) {
            for (int c = 1; c <= maskIpl.getNChannels(); c++) {
                for (int t = 1; t <= maskIpl.getNFrames(); t++) {
                    maskIpl.setPosition(c, z, t);
                    maskIpl.getProcessor().invert();
                }
            }
        }
        maskIpl.setPosition(1,1,1);

        float[] weights = ChamferWeights3D.WEIGHTS_3_4_5_7.getFloatWeights();
        ImagePlus distIpl = new GeodesicDistanceMap3D().process(objectsImage.getImagePlus(),maskIpl,"Dist",weights,true);

        // Iterating over all pixels in the input image, adding intensity measurements to CumStat objects (one
        // for pixels in the proximity range, one for pixels outside it).
        CumStat[] cs = new CumStat[2];
        cs[0] = new CumStat();
        cs[1] = new CumStat();

        for (int z = 0; z < distIpl.getNSlices(); z++) {
            for (int c = 0; c < distIpl.getNChannels(); c++) {
                for (int t = 0; t < distIpl.getNFrames(); t++) {
                    distIpl.setPosition(c+1, z+1, t+1);
                    inputImagePlus.setPosition(c+1, z+1, t+1);

                    float[][] distVals = distIpl.getProcessor().getFloatArray();
                    float[][] inputVals = inputImagePlus.getProcessor().getFloatArray();

                    for (int x=0;x<distVals.length;x++) {
                        for (int y=0;y<distVals[0].length;y++) {
                            float dist = distVals[x][y];
                            float val = inputVals[x][y];

                            if (ignoreOnObjects && dist == 0) continue;

                            if (dist <= proximalDistance) {
                                cs[0].addMeasure(val);
                            } else {
                                cs[1].addMeasure(val);
                            }
                        }
                    }
                }
            }
        }

        distIpl.setPosition(1, 1, 1);
        inputImagePlus.setPosition(1, 1, 1);

        return cs;

    }

    public CumStat measureIntensityWeightedProximity(ObjCollection inputObjects, Image inputImage, boolean ignoreOnObjects) {
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Get binary image showing the objects
        HashMap<Integer,Float> hues = inputObjects.getHue(ObjCollection.ColourModes.SINGLE_COLOUR,"",false);
        Image objectsImage = inputObjects.convertObjectsToImage("Objects", inputImagePlus, ShowObjects.ColourModes.SINGLE_COLOUR, hues, true);

        // Calculating a 3D distance map for the binary image
        ImagePlus maskIpl = new Duplicator().run(objectsImage.getImagePlus());

        // Inverting the mask intensity
        for (int z = 1; z <= maskIpl.getNSlices(); z++) {
            for (int c = 1; c <= maskIpl.getNChannels(); c++) {
                for (int t = 1; t <= maskIpl.getNFrames(); t++) {
                    maskIpl.setPosition(c, z, t);
                    maskIpl.getProcessor().invert();
                }
            }
        }
        maskIpl.setPosition(1,1,1);

        float[] weights = ChamferWeights3D.WEIGHTS_3_4_5_7.getFloatWeights();
        ImagePlus distIpl = new GeodesicDistanceMap3D().process(objectsImage.getImagePlus(),maskIpl,"Dist",weights,true);

        // Iterating over all pixels in the input image, adding intensity measurements to CumStat objects (one
        // for pixels in the proximity range, one for pixels outside it).
        CumStat cs = new CumStat();

        for (int z = 0; z < distIpl.getNSlices(); z++) {
            for (int c = 0; c < distIpl.getNChannels(); c++) {
                for (int t = 0; t < distIpl.getNFrames(); t++) {
                    distIpl.setPosition(c+1, z+1, t+1);
                    inputImagePlus.setPosition(c+1, z+1, t+1);

                    float[][] distVals = distIpl.getProcessor().getFloatArray();
                    float[][] inputVals = inputImagePlus.getProcessor().getFloatArray();

                    for (int x=0;x<distVals.length;x++) {
                        for (int y=0;y<distVals[0].length;y++) {
                            float dist = distVals[x][y];
                            float val = inputVals[x][y];

                            if (ignoreOnObjects && dist == 0) continue;

                            cs.addMeasure(dist,val);

                        }
                    }
                }
            }
        }

        distIpl.setPosition(1, 1, 1);
        inputImagePlus.setPosition(1, 1, 1);

        return cs;
    }

    @Override
    public String getTitle() {
        return "Measure intensity distribution";
    }

    @Override
    public String getHelp() {
        return "CURRENTLY ONLY WORKS IN 3D";
    }

    @Override
    protected void run(Workspace workspace, boolean verbose) throws GenericMIAException {
        // Getting parameters
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        String measurementType = parameters.getValue(MEASUREMENT_TYPE);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        double proximalDistance = parameters.getValue(PROXIMAL_DISTANCE);
        String spatialUnits = parameters.getValue(SPATIAL_UNITS);
        boolean ignoreOnObjects = parameters.getValue(IGNORE_ON_OBJECTS);

        Image inputImage = workspace.getImages().get(inputImageName);

        if (spatialUnits.equals(SpatialUnits.CALIBRATED)) {
            proximalDistance = inputImage.getImagePlus().getCalibration().getRawX(proximalDistance);
        }

        switch (measurementType) {
            case MeasurementTypes.FRACTION_PROXIMAL_TO_OBJECTS:
                ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

                // Checking if there are any objects to measure
                if (inputObjects.size() == 0) {
                    inputImage.addMeasurement(
                            new Measurement(getFullName(inputObjectsName, Measurements.N_PX_INRANGE), Double.NaN));
                    inputImage.addMeasurement(
                            new Measurement(getFullName(inputObjectsName, Measurements.N_PX_OUTRANGE), Double.NaN));
                    inputImage.addMeasurement(
                            new Measurement(getFullName(inputObjectsName, Measurements.MEAN_INT_INRANGE), Double.NaN));
                    inputImage.addMeasurement(
                            new Measurement(getFullName(inputObjectsName, Measurements.MEAN_INT_OUTRANGE), Double.NaN));
                    inputImage.addMeasurement(
                            new Measurement(getFullName(inputObjectsName, Measurements.MEAN_PROXIMITY), Double.NaN));
                    inputImage.addMeasurement(
                            new Measurement(getFullName(inputObjectsName, Measurements.STDEV_PROXIMITY), Double.NaN));
                    return;
                }

                CumStat[] css = measureFractionProximal(inputObjects, inputImage, proximalDistance, ignoreOnObjects);

                inputImage.addMeasurement(
                        new Measurement(getFullName(inputObjectsName, Measurements.N_PX_INRANGE), css[0].getN()));
                inputImage.addMeasurement(
                        new Measurement(getFullName(inputObjectsName, Measurements.N_PX_OUTRANGE), css[1].getN()));
                inputImage.addMeasurement(
                        new Measurement(getFullName(inputObjectsName, Measurements.MEAN_INT_INRANGE), css[0].getMean()));
                inputImage.addMeasurement(
                        new Measurement(getFullName(inputObjectsName, Measurements.MEAN_INT_OUTRANGE), css[1].getMean()));
                inputImage.addMeasurement(
                        new Measurement(getFullName(inputObjectsName, Measurements.MEAN_PROXIMITY), css[0].getSum()));
                inputImage.addMeasurement(
                        new Measurement(getFullName(inputObjectsName, Measurements.STDEV_PROXIMITY), css[1].getSum()));

                if (verbose) System.out.println("[" + moduleName + "] Number of pixels inside range = " + css[0].getN());
                if (verbose) System.out.println("[" + moduleName + "] Number of pixels outside range = " + css[1].getN());
                if (verbose) System.out.println("[" + moduleName + "] Total intensity in range = " + css[0].getSum());
                if (verbose) System.out.println("[" + moduleName + "] Total intensity outside range = " + css[1].getSum());
                if (verbose) System.out.println("[" + moduleName + "] Mean intensity in range = " + css[0].getMean());
                if (verbose) System.out.println("[" + moduleName + "] Mean intensity outside range = " + css[1].getMean());

                break;

            case MeasurementTypes.INTENSITY_WEIGHTED_PROXIMITY:
                inputObjects = workspace.getObjects().get(inputObjectsName);

                // Checking if there are any objects to measure
                if (inputObjects.size() == 0) {
                    inputImage.addMeasurement(
                            new Measurement(getFullName(inputObjectsName, Measurements.MEAN_PROXIMITY), Double.NaN));
                    inputImage.addMeasurement(
                            new Measurement(getFullName(inputObjectsName, Measurements.STDEV_PROXIMITY), Double.NaN));
                    return;
                }

                CumStat cs = measureIntensityWeightedProximity(inputObjects, inputImage, ignoreOnObjects);

                inputImage.addMeasurement(
                        new Measurement(getFullName(inputObjectsName, Measurements.MEAN_PROXIMITY), cs.getMean()));
                inputImage.addMeasurement(
                        new Measurement(getFullName(inputObjectsName, Measurements.STDEV_PROXIMITY), cs.getStd()));

                if (verbose)
                    System.out.println("[" + moduleName + "] Mean intensity proximity = " + cs.getMean() + " +/- "+cs.getStd());

                break;

        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(MEASUREMENT_TYPE, Parameter.CHOICE_ARRAY,
                MeasurementTypes.FRACTION_PROXIMAL_TO_OBJECTS, MeasurementTypes.ALL));
        parameters.add(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(PROXIMAL_DISTANCE, Parameter.DOUBLE,2d));
        parameters.add(new Parameter(SPATIAL_UNITS, Parameter.CHOICE_ARRAY, SpatialUnits.PIXELS, SpatialUnits.ALL));
        parameters.add(new Parameter(IGNORE_ON_OBJECTS, Parameter.BOOLEAN,true));

    }

    @Override
    protected void initialiseMeasurementReferences() {
        imageMeasurementReferences.add(new MeasurementReference(Measurements.N_PX_INRANGE));
        imageMeasurementReferences.add(new MeasurementReference(Measurements.N_PX_OUTRANGE));
        imageMeasurementReferences.add(new MeasurementReference(Measurements.MEAN_INT_INRANGE));
        imageMeasurementReferences.add(new MeasurementReference(Measurements.MEAN_INT_OUTRANGE));
        imageMeasurementReferences.add(new MeasurementReference(Measurements.SUM_INT_INRANGE));
        imageMeasurementReferences.add(new MeasurementReference(Measurements.SUM_INT_OUTRANGE));
        imageMeasurementReferences.add(new MeasurementReference(Measurements.MEAN_PROXIMITY));
        imageMeasurementReferences.add(new MeasurementReference(Measurements.N_PX_INRANGE));
        imageMeasurementReferences.add(new MeasurementReference(Measurements.N_PX_INRANGE));
        imageMeasurementReferences.add(new MeasurementReference(Measurements.STDEV_PROXIMITY));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(MEASUREMENT_TYPE));

        switch ((String) parameters.getValue(MEASUREMENT_TYPE)) {
            case MeasurementTypes.FRACTION_PROXIMAL_TO_OBJECTS:
                returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
                returnedParameters.add(parameters.getParameter(PROXIMAL_DISTANCE));
                returnedParameters.add(parameters.getParameter(SPATIAL_UNITS));

                break;

            case MeasurementTypes.INTENSITY_WEIGHTED_PROXIMITY:
                returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

                break;

        }

        returnedParameters.add(parameters.getParameter(IGNORE_ON_OBJECTS));

        return returnedParameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        String imageName = parameters.getValue(INPUT_IMAGE);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        MeasurementReference nPxInrange = imageMeasurementReferences.get(Measurements.N_PX_INRANGE);
        MeasurementReference nPxOutrange = imageMeasurementReferences.get(Measurements.N_PX_OUTRANGE);
        MeasurementReference meanIntInrange = imageMeasurementReferences.get(Measurements.MEAN_INT_INRANGE);
        MeasurementReference meanIntOutrange = imageMeasurementReferences.get(Measurements.MEAN_INT_OUTRANGE);
        MeasurementReference sumIntInrange = imageMeasurementReferences.get(Measurements.SUM_INT_INRANGE);
        MeasurementReference sumIntOutrange = imageMeasurementReferences.get(Measurements.SUM_INT_OUTRANGE);
        MeasurementReference meanProximity = imageMeasurementReferences.get(Measurements.MEAN_PROXIMITY);
        MeasurementReference stdevProximity = imageMeasurementReferences.get(Measurements.STDEV_PROXIMITY);

        switch ((String) parameters.getValue(MEASUREMENT_TYPE)) {
            case MeasurementTypes.FRACTION_PROXIMAL_TO_OBJECTS:
                nPxInrange.setCalculated(true);
                nPxOutrange.setCalculated(true);
                meanIntInrange.setCalculated(true);
                meanIntOutrange.setCalculated(true);
                sumIntInrange.setCalculated(true);
                sumIntOutrange.setCalculated(true);
                meanProximity.setCalculated(false);
                stdevProximity.setCalculated(false);

                nPxInrange.setNickName(getFullName(inputObjectsName, Measurements.N_PX_INRANGE));
                nPxOutrange.setNickName(getFullName(inputObjectsName, Measurements.N_PX_OUTRANGE));
                meanIntInrange.setNickName(getFullName(inputObjectsName, Measurements.MEAN_INT_INRANGE));
                meanIntOutrange.setNickName( getFullName(inputObjectsName, Measurements.MEAN_INT_OUTRANGE));
                sumIntInrange.setNickName(getFullName(inputObjectsName, Measurements.SUM_INT_INRANGE));
                sumIntOutrange.setNickName(getFullName(inputObjectsName, Measurements.SUM_INT_OUTRANGE));

                break;

            case MeasurementTypes.INTENSITY_WEIGHTED_PROXIMITY:
                nPxInrange.setCalculated(false);
                nPxOutrange.setCalculated(false);
                meanIntInrange.setCalculated(false);
                meanIntOutrange.setCalculated(false);
                sumIntInrange.setCalculated(false);
                sumIntOutrange.setCalculated(false);
                meanProximity.setCalculated(true);
                stdevProximity.setCalculated(true);

                meanProximity.setNickName(getFullName(inputObjectsName, Measurements.MEAN_PROXIMITY));
                stdevProximity.setNickName(getFullName(inputObjectsName, Measurements.STDEV_PROXIMITY));

                break;
        }

        return imageMeasurementReferences;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
