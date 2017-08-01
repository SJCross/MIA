package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import inra.ijpb.binary.ChamferWeights3D;
import inra.ijpb.binary.distmap.DistanceTransform3DShort;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MathFunc.CumStat;
import wbif.sjx.common.Object.Track;
import wbif.sjx.common.Object.TrackCollection;

import java.util.ArrayList;

/**
 * Created by sc13967 on 05/05/2017.
 */
public class MeasureObjectIntensity extends HCModule {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String INPUT_IMAGE = "Input image";
    public static final String MEASURE_MEAN = "Measure mean";
    public static final String MEASURE_STDEV = "Measure standard deviation";
    public static final String MEASURE_MIN = "Measure minimum";
    public static final String MEASURE_MAX = "Measure maximum";


    private void measureIntensity(Obj object, ImagePlus ipl) {
        // Getting parameters
        String imageName = parameters.getValue(INPUT_IMAGE);

        // Initialising the cumulative statistics object to store pixel intensities
        CumStat cs = new CumStat();

        // Getting pixel coordinates
        ArrayList<Integer> x = object.getCoordinates(Obj.X);
        ArrayList<Integer> y = object.getCoordinates(Obj.Y);
        ArrayList<Integer> z = object.getCoordinates(Obj.Z);
        int cPos = object.getCoordinates(Obj.C);
        int tPos = object.getCoordinates(Obj.T);

        // Running through all pixels in this object and adding the intensity to the MultiCumStat object
        for (int i=0;i<x.size();i++) {
            int zPos = z==null ? 0 : z.get(i);

            ipl.setPosition(cPos+1,zPos+1,tPos+1);
            cs.addMeasure(ipl.getProcessor().getPixelValue(x.get(i),y.get(i)));

        }

        // Calculating mean, std, min and max intensity
        if (parameters.getValue(MEASURE_MEAN))
            object.addMeasurement(new MIAMeasurement(imageName+"_MEAN", cs.getMean()));
        if (parameters.getValue(MEASURE_MIN))
            object.addMeasurement(new MIAMeasurement(imageName+"_MIN", cs.getMin()));
        if (parameters.getValue(MEASURE_MAX))
            object.addMeasurement(new MIAMeasurement(imageName+"_MAX", cs.getMax()));
        if (parameters.getValue(MEASURE_STDEV))
            object.addMeasurement(new MIAMeasurement(imageName+"_STD", cs.getStd(CumStat.SAMPLE)));

    }

    private void measureEdgeIntensity(Obj object, ImagePlus ipl) {
        // Getting parameters
        String imageName = parameters.getValue(INPUT_IMAGE);

        // Initialising the cumulative statistics object to store pixel intensities
        CumStat csEdge = new CumStat();
        CumStat csInterior = new CumStat();

        // Getting parent coordinates
        ArrayList<Integer> parentX = object.getCoordinates(Obj.X);
        ArrayList<Integer> parentY = object.getCoordinates(Obj.Y);
        ArrayList<Integer> parentZ = object.getCoordinates(Obj.Z);

        // Creating a Hyperstack to hold the distance transform
        int[][] range = object.getCoordinateRange();
        ImagePlus iplObj = IJ.createHyperStack("Objects", range[Obj.X][1]-range[Obj.X][0] + 1,
                range[Obj.Y][1]-range[Obj.Y][0] + 1, 1, range[Obj.Z][1]-range[Obj.Z][0], 1, 8);

        // Setting pixels corresponding to the parent object to 1
        for (int i=0;i<parentX.size();i++) {
            iplObj.setPosition(1,parentZ.get(i)-range[Obj.Z][0]+1,1);
            iplObj.getProcessor().set(parentX.get(i)-range[Obj.X][0],parentY.get(i)-range[Obj.Y][0],255);

        }

        // Creating distance map using MorphoLibJ
        short[] weights = ChamferWeights3D.BORGEFORS.getShortWeights();
        DistanceTransform3DShort distTransform = new DistanceTransform3DShort(weights,true);
        iplObj.setStack(distTransform.distanceMap(iplObj.getStack()));

        // Adding pixel intensities to CumStat
        // Running through all pixels in this object and adding the intensity to the MultiCumStat object
        for (int i=0;i<parentX.size();i++) {
            ipl.setPosition(1,parentZ.get(i)+1,1);
            iplObj.setPosition(1,parentZ.get(i)-range[Obj.Z][0]+1,1);

            double pixelVal = iplObj.getProcessor().getPixelValue(parentX.get(i)-range[Obj.X][0],parentY.get(i)-range[Obj.Y][0]);
            if (pixelVal <= edgeDistance) {
                csEdge.addMeasure(ipl.getProcessor().getPixelValue(parentX.get(i),parentY.get(i)));
                iplObj.getProcessor().set(parentX.get(i)-range[Obj.X][0],parentY.get(i)-range[Obj.Y][0],0);

            } else if (pixelVal > edgeDistance && (boolean) parameters.getValue(MEASURE_INTERIOR)) {
                csInterior.addMeasure(ipl.getProcessor().getPixelValue(parentX.get(i),parentY.get(i)));
                iplObj.getProcessor().set(parentX.get(i)-range[Obj.X][0],parentY.get(i)-range[Obj.Y][0],0);

            }
        }
    }

    @Override
    public String getTitle() {
        return "Measure object intensity";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void execute(Workspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting input objects
        String objectName = parameters.getValue(INPUT_OBJECTS);
        ObjSet objects = workspace.getObjects().get(objectName);

        // Getting input image
        String imageName = parameters.getValue(INPUT_IMAGE);
        Image image = workspace.getImages().get(imageName);
        ImagePlus ipl = image.getImagePlus();

        // Measuring intensity for each object and adding the measurement to that object
        for (Obj object:objects.values()) {
            // Calculating object intensity
            measureIntensity(object,ipl);

        }

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.addParameter(new Parameter(MEASURE_MEAN, Parameter.BOOLEAN, true));
        parameters.addParameter(new Parameter(MEASURE_MIN, Parameter.BOOLEAN, true));
        parameters.addParameter(new Parameter(MEASURE_MAX, Parameter.BOOLEAN, true));
        parameters.addParameter(new Parameter(MEASURE_STDEV, Parameter.BOOLEAN, true));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.addParameter(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.addParameter(parameters.getParameter(MEASURE_MEAN));
        returnedParameters.addParameter(parameters.getParameter(MEASURE_MIN));
        returnedParameters.addParameter(parameters.getParameter(MEASURE_MAX));
        returnedParameters.addParameter(parameters.getParameter(MEASURE_STDEV));

        return returnedParameters;

    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {
        boolean calcMean = parameters.getValue(MEASURE_MEAN);
        boolean calcMin = parameters.getValue(MEASURE_MIN);
        boolean calcMax = parameters.getValue(MEASURE_MAX);
        boolean calcStdev = parameters.getValue(MEASURE_STDEV);

        String inputImageName = parameters.getValue(INPUT_IMAGE);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        if (calcMean) measurements.addMeasurement(inputObjectsName,inputImageName+"_MEAN");
        if (calcMin) measurements.addMeasurement(inputObjectsName,inputImageName+"_MIN");
        if (calcMax) measurements.addMeasurement(inputObjectsName,inputImageName+"_MAX");
        if (calcStdev) measurements.addMeasurement(inputObjectsName,inputImageName+"_STD");

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
