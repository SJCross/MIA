package wbif.sjx.ModularImageAnalysis.Module.ImageMeasurements;

import ij.ImagePlus;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.BooleanP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.InputImageP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.ParameterCollection;
import wbif.sjx.common.Analysis.IntensityCalculator;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MathFunc.CumStat;

/**
 * Created by sc13967 on 12/05/2017.
 */
public class MeasureImageIntensity extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String MEASURE_MEAN = "Measure mean";
    public static final String MEASURE_STDEV = "Measure standard deviation";
    public static final String MEASURE_MIN = "Measure minimum";
    public static final String MEASURE_MAX = "Measure maximum";
    public static final String MEASURE_SUM = "Measure sum";


    public interface Measurements {
        String MEAN = "INTENSITY // MEAN";
        String MIN = "INTENSITY // MIN";
        String MAX = "INTENSITY // MAX";
        String SUM = "INTENSITY // SUM";
        String STDEV = "INTENSITY // STDEV";

    }


    @Override
    public String getTitle() {
        return "Measure image intensity";

    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_MEASUREMENTS;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public boolean run(Workspace workspace) {
       // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        writeMessage("Loading image ("+inputImageName+")");
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Running measurement
        CumStat cs = IntensityCalculator.calculate(inputImagePlus.getImageStack());

        // Adding measurements to image
        if (parameters.getValue(MEASURE_MEAN))
            inputImage.addMeasurement(new Measurement(Measurements.MEAN, cs.getMean()));
        if (parameters.getValue(MEASURE_MIN))
            inputImage.addMeasurement(new Measurement(Measurements.MIN, cs.getMin()));
        if (parameters.getValue(MEASURE_MAX))
            inputImage.addMeasurement(new Measurement(Measurements.MAX, cs.getMax()));
        if (parameters.getValue(MEASURE_STDEV))
            inputImage.addMeasurement(new Measurement(Measurements.STDEV, cs.getStd(CumStat.SAMPLE)));
        if (parameters.getValue(MEASURE_SUM))
            inputImage.addMeasurement(new Measurement(Measurements.SUM, cs.getSum()));

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(MEASURE_MEAN, this, true));
        parameters.add(new BooleanP(MEASURE_MIN, this, true));
        parameters.add(new BooleanP(MEASURE_MAX, this, true));
        parameters.add(new BooleanP(MEASURE_STDEV, this, true));
        parameters.add(new BooleanP(MEASURE_SUM, this, true));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        String inputImageName = parameters.getValue(INPUT_IMAGE);

        imageMeasurementRefs.setAllCalculated(false);

        MeasurementRef mean = imageMeasurementRefs.getOrPut(Measurements.MEAN);
        mean.setImageObjName(inputImageName);
        mean.setCalculated(parameters.getValue(MEASURE_MEAN));

        MeasurementRef min = imageMeasurementRefs.getOrPut(Measurements.MIN);
        min.setImageObjName(inputImageName);
        min.setCalculated(parameters.getValue(MEASURE_MIN));

        MeasurementRef max = imageMeasurementRefs.getOrPut(Measurements.MAX);
        max.setImageObjName(inputImageName);
        max.setCalculated(parameters.getValue(MEASURE_MAX));

        MeasurementRef stdev = imageMeasurementRefs.getOrPut(Measurements.STDEV);
        stdev.setImageObjName(inputImageName);
        stdev.setCalculated(parameters.getValue(MEASURE_STDEV));

        MeasurementRef sum = imageMeasurementRefs.getOrPut(Measurements.SUM);
        sum.setImageObjName(inputImageName);
        sum.setCalculated(parameters.getValue(MEASURE_SUM));

        return imageMeasurementRefs;

    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }

}
