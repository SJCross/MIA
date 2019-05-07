// TODO: Add object linking for 4D - linking should be done on spatial overlap (similar to how its done in 3D)

package wbif.sjx.MIA.Module.ObjectProcessing.Identification;

import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.plugin.SubHyperstackMaker;
import inra.ijpb.binary.conncomp.FloodFillComponentsLabeling3D;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.MeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.MIA.Process.ColourFactory;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.Object.LUTs;

import java.util.HashMap;

/**
 * Created by sc13967 on 06/06/2017.
 */
public class IdentifyObjects extends Module {
    public static final String INPUT_SEPARATOR = "Image input, object output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_OBJECTS = "Output objects";

    public static final String IDENTIFICATION_SEPARATOR = "Object identification";
    public static final String WHITE_BACKGROUND = "Black objects/white background";
    public static final String SINGLE_OBJECT = "Identify as single object";
    public static final String CONNECTIVITY = "Connectivity";

    public interface Connectivity {
        String SIX = "6";
        String TWENTYSIX = "26";

        String[] ALL = new String[]{SIX,TWENTYSIX};

    }


    private ObjCollection importFromImage(Image inputImage, String outputObjectsName, boolean whiteBackground,
                                          boolean singleObject, int connectivity, int labelBitDepth)
            throws IntegerOverflowException, RuntimeException {

        ImagePlus inputImagePlus = inputImage.getImagePlus();
        inputImagePlus = inputImagePlus.duplicate();
        ObjCollection outputObjects = new ObjCollection(outputObjectsName);

        for (int t = 1; t <= inputImagePlus.getNFrames(); t++) {
            writeMessage("Processing image "+t+" of "+inputImagePlus.getNFrames());

            // Creating a copy of the input image
            ImagePlus currStack;
            if (inputImagePlus.getNFrames()==1) {
                currStack = new Duplicator().run(inputImagePlus);

            } else {
                currStack = SubHyperstackMaker.makeSubhyperstack(inputImagePlus, "1-" +
                        inputImagePlus.getNChannels(), "1-" + inputImagePlus.getNSlices(), t + "-" + t);
                currStack.setCalibration(inputImagePlus.getCalibration());
            }
            currStack.updateChannelAndDraw();

            if (whiteBackground) InvertIntensity.process(currStack);

            // Applying connected components labelling
            FloodFillComponentsLabeling3D ffcl3D = new FloodFillComponentsLabeling3D(connectivity, labelBitDepth);
            currStack.setStack(ffcl3D.computeLabels(currStack.getStack()));

            // Converting image to objects
            Image tempImage = new Image("Temp image", currStack);
            ObjCollection currOutputObjects = tempImage.convertImageToObjects(outputObjectsName,singleObject);

            // Updating the current objects (setting the real frame number and offsetting the ID)
            int maxID = 0;
            for (Obj object:outputObjects.values()) {
                maxID = Math.max(object.getID(),maxID);
            }

            for (Obj object:currOutputObjects.values()) {
                object.setID(object.getID() + maxID + 1);
                object.setT(t-1);
                outputObjects.put(object.getID(),object);
            }
        }

        return outputObjects;

    }

    private static int getConnectivity(String connectivityName) {
        switch (connectivityName) {
            case Connectivity.SIX:
            default:
                return 6;
            case Connectivity.TWENTYSIX:
                return 26;
        }
    }


    @Override
    public String getTitle() {
        return "Identify objects";
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_IDENTIFICATION;
    }

    @Override
    public String getHelp() {
        return  "Takes a binary image and uses connected components labelling to create objects" +
                "\nUses MorphoLibJ to perform connected components labelling in 3D. " +
                "\n\nLarger label bit depths will require more memory, but will enable more objects " +
                "\nto be detected (8-bit = 255 objects, 16-bit = 65535 objects, 32-bit = (near) unlimited.";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);

        // Getting parameters
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        boolean whiteBackground = parameters.getValue(WHITE_BACKGROUND);
        boolean singleObject = parameters.getValue(SINGLE_OBJECT);
        String connectivityName = parameters.getValue(CONNECTIVITY);

        // Getting options
        int connectivity = getConnectivity(connectivityName);

        ObjCollection outputObjects = null;
        try {
            try {
                outputObjects = importFromImage(inputImage, outputObjectsName, whiteBackground, singleObject, connectivity, 16);
            } catch (RuntimeException e2) {
                outputObjects = importFromImage(inputImage, outputObjectsName, whiteBackground, singleObject, connectivity, 32);
            }
        } catch (IntegerOverflowException e3) {
            return false;
        }

        writeMessage(outputObjects.size()+" objects detected");

        // Adding objects to workspace
        writeMessage("Adding objects ("+outputObjectsName+") to workspace");
        workspace.addObjects(outputObjects);

        // Showing objects
        if (showOutput) {
            HashMap<Integer,Float> hues = ColourFactory.getRandomHues(outputObjects);
            ImagePlus dispIpl = outputObjects.convertObjectsToImage("Objects",inputImage,hues,8,false).getImagePlus();
            dispIpl.setLut(LUTs.Random(true));
            dispIpl.setPosition(1,1,1);
            dispIpl.updateChannelAndDraw();
            dispIpl.show();
        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE,this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS,this));

        parameters.add(new ParamSeparatorP(IDENTIFICATION_SEPARATOR,this));
        parameters.add(new BooleanP(WHITE_BACKGROUND,this,true));
        parameters.add(new BooleanP(SINGLE_OBJECT,this,false));
        parameters.add(new ChoiceP(CONNECTIVITY, this, Connectivity.TWENTYSIX, Connectivity.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs(ModuleCollection modules) {
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
