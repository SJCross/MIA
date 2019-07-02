package wbif.sjx.MIA.Module.Visualisation;

import javax.annotation.Nullable;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.FilterImage;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.common.MathFunc.CumStat;
import wbif.sjx.common.MathFunc.Indexer;
import wbif.sjx.common.Object.Point;


public class CreateObjectDensityMap extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String TEMPLATE_IMAGE = "Template image";
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
            if (message != null) writeMessage("Processing object "+(++count)+" of "+nTotal,message);

            // Getting all object points
            for (Point<Integer> point:object.getPoints()) {
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
    public String getDescription() {
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting template image
        String templateImageName = parameters.getValue(TEMPLATE_IMAGE);
        Image templateImage = workspace.getImage(templateImageName);

        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        int range = parameters.getValue(RANGE);
        boolean averageZ = parameters.getValue(AVERAGE_SLICES);
        boolean averageT = parameters.getValue(AVERAGE_TIME);

        // Initialising stores
        CumStat[] cumStats = CreateMeasurementMap.initialiseCumStats(templateImage,averageZ,averageT);
        Indexer indexer = CreateMeasurementMap.initialiseIndexer(templateImage,averageZ,averageT);

        // Compressing relevant measures
        process(cumStats,indexer,inputObjects,getName());

        // Converting statistic array to Image
        writeMessage("Creating output image");
        Calibration calibration = templateImage.getImagePlus().getCalibration();
        Image outputImage = convertToImage(cumStats,indexer,outputImageName,calibration);

        // Applying blur
        FilterImage.runGaussian2DFilter(outputImage.getImagePlus(),range);

        workspace.addImage(outputImage);
        if (showOutput) outputImage.showImage();

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this));
        parameters.add(new InputImageP(TEMPLATE_IMAGE,this));
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
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }

}
