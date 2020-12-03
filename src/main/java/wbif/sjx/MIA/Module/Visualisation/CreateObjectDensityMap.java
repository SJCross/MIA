package wbif.sjx.MIA.Module.Visualisation;

import com.drew.lang.annotations.Nullable;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.FilterImage;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.common.MathFunc.CumStat;
import wbif.sjx.common.MathFunc.Indexer;
import wbif.sjx.common.Object.Point;
import wbif.sjx.common.Object.Volume.SpatCal;


public class CreateObjectDensityMap extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String RANGE = "Range";
    public static final String AVERAGE_SLICES = "Average slices";
    public static final String AVERAGE_TIME = "Average time";

    public CreateObjectDensityMap(ModuleCollection modules) {
        super("Create object density map",modules);
    }


    public static void process(CumStat[] cumStats, Indexer indexer, ObjCollection objects, @Nullable String message) {
        // Adding objects
        int count = 0;
        int nTotal = objects.size();
        for (Obj object:objects.values()) {
            if (message != null) writeStatus("Processing object "+(++count)+" of "+nTotal,message);

            // Getting all object points
            for (Point<Integer> point:object.getCoordinateSet()) {
                // Getting index for this point
                int z = indexer.getDim()[2] == 1 ? 0 : point.getZ();
                int t = indexer.getDim()[3] == 1 ? 0 : object.getT();
                int idx = indexer.getIndex(new int[]{point.getX(),point.getY(),z,t});

                // Adding measurement
                cumStats[idx].addMeasure(1);

            }
        }
    }

    public static Image convertToImage(CumStat[] cumStats, Indexer indexer, String outputImageName, Calibration calibration) {
        int[] dim = indexer.getDim();
        int width = dim[0];
        int height = dim[1];
        int nSlices = dim[2];
        int nFrames = dim[3];

        // Creating ImagePlus
        ImagePlus outputIpl = IJ.createHyperStack(outputImageName,width,height,1,nSlices,nFrames,32);
        outputIpl.setCalibration(calibration);

        // Iterating over all points in the image
        for (int z=0;z<nSlices;z++) {
            for (int t=0;t<nFrames;t++) {
                outputIpl.setPosition(1,z+1,t+1);
                ImageProcessor ipr = outputIpl.getProcessor();

                for (int x=0;x<width;x++) {
                    for (int y=0;y<height;y++) {
                        // Getting relevant index
                        int idx = indexer.getIndex(new int[]{x,y,z,t});
                        CumStat cumStat = cumStats[idx];
                        ipr.setf(x,y,(float) cumStat.getN());
                    }
                }
            }
        }

        return new Image(outputImageName,outputIpl);

    }


    @Override
    public String getPackageName() {
        return PackageNames.VISUALISATION;
    }

    @Override
    public Category getCategory() {
        return Categories.VISUALISATION;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        int range = parameters.getValue(RANGE);
        boolean averageZ = parameters.getValue(AVERAGE_SLICES);
        boolean averageT = parameters.getValue(AVERAGE_TIME);

        // Initialising stores
        SpatCal calibration = inputObjects.getSpatialCalibration();
        int nFrames = inputObjects.getNFrames();
        CumStat[] cumStats = CreateMeasurementMap.initialiseCumStats(calibration,nFrames,averageZ,averageT);
        Indexer indexer = CreateMeasurementMap.initialiseIndexer(calibration,nFrames,averageZ,averageT);

        // Compressing relevant measures
        process(cumStats,indexer,inputObjects,getName());

        // Converting statistic array to Image
        writeStatus("Creating output image");
        Calibration imageCalibration = calibration.createImageCalibration();
        Image outputImage = convertToImage(cumStats,indexer,outputImageName,imageCalibration);

        // Applying blur
        FilterImage.runGaussian2DFilter(outputImage.getImagePlus(),range);

        workspace.addImage(outputImage);
        if (showOutput) outputImage.showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE,this));
        parameters.add(new IntegerP(RANGE,this,3));
        parameters.add(new BooleanP(AVERAGE_SLICES,this,true));
        parameters.add(new BooleanP(AVERAGE_TIME,this,true));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
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
}
