// TODO: Add object linking for 4D - linking should be done on spatial overlap (similar to how its done in 3D)

package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.plugin.SubHyperstackMaker;
import inra.ijpb.binary.conncomp.FloodFillComponentsLabeling3D;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Miscellaneous.ConvertObjectsToImage;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Image;

import java.util.HashMap;

/**
 * Created by sc13967 on 06/06/2017.
 */
public class IdentifyObjects extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String WHITE_BACKGROUND = "Black objects/white background";
    public static final String SINGLE_OBJECT = "Identify as single object";
    public static final String SHOW_OBJECTS = "Show objects";


    private ObjCollection importFromImage(Image inputImage, String outputObjectsName, boolean whiteBackground, boolean singleObject) {
        ImagePlus inputImagePlus = inputImage.getImagePlus();
        ObjCollection outputObjects = new ObjCollection(outputObjectsName);

        for (int t = 1; t <= inputImagePlus.getNFrames(); t++) {
            writeMessage("Processing image "+t+" of "+inputImagePlus.getNFrames());
            // Creating a copy of the input image
            ImagePlus currStack = SubHyperstackMaker.makeSubhyperstack(
                    inputImagePlus,1+"-"+inputImagePlus.getNChannels(),1+"-"+inputImagePlus.getNSlices(),t+"-"+t);
            currStack = new Duplicator().run(currStack);

            if (whiteBackground) IJ.run(currStack, "Invert", "stack");

            // Applying connected components labelling
            FloodFillComponentsLabeling3D ffcl3D = new FloodFillComponentsLabeling3D(26);
            currStack.setStack(ffcl3D.computeLabels(currStack.getImageStack()));

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

    @Override
    public String getTitle() {
        return "Identify objects";
    }

    @Override
    public String getHelp() {
        return "INCOMPLETE" +
                "\nTakes a binary image and uses connected components labelling to create objects" +
                "\nUses MorphoLibJ to perform connected components labelling in 3D";
    }

    @Override
    public void run(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);

        // Getting parameters
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        boolean whiteBackground = parameters.getValue(WHITE_BACKGROUND);
        boolean singleObject = parameters.getValue(SINGLE_OBJECT);
        boolean showObjects = parameters.getValue(SHOW_OBJECTS);

        ObjCollection outputObjects = importFromImage(inputImage, outputObjectsName, whiteBackground, singleObject);

        writeMessage(outputObjects.size()+" objects detected");

        // Adding objects to workspace
        writeMessage("Adding objects ("+outputObjectsName+") to workspace");
        workspace.addObjects(outputObjects);

        // Showing objects
        if (showObjects) {
            HashMap<Integer,Float> hues = outputObjects.getHues(ObjCollection.ColourModes.RANDOM_COLOUR,"",false);
            outputObjects.convertObjectsToImage("Objects", inputImage.getImagePlus(), ConvertObjectsToImage.ColourModes.RANDOM_COLOUR, hues).getImagePlus().show();
        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(OUTPUT_OBJECTS,Parameter.OUTPUT_OBJECTS,null));
        parameters.add(new Parameter(WHITE_BACKGROUND,Parameter.BOOLEAN,true));
        parameters.add(new Parameter(SINGLE_OBJECT,Parameter.BOOLEAN,false));
        parameters.add(new Parameter(SHOW_OBJECTS,Parameter.BOOLEAN,false));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));
         returnedParameters.add(parameters.getParameter(WHITE_BACKGROUND));
            returnedParameters.add(parameters.getParameter(SINGLE_OBJECT));

        returnedParameters.add(parameters.getParameter(SHOW_OBJECTS));

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
