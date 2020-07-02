package wbif.sjx.MIA.Module.ImageProcessing.Stack;

import java.util.LinkedHashMap;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.ParameterGroup;
import wbif.sjx.MIA.Object.Parameters.RemovedImageP;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.PartnerRefCollection;

/**
 * Created by sc13967 on 30/06/2017.
 */
public class RemoveImage extends Module {
    public static final String REMOVAL_SEPARATOR = "Images to remove";
    public static final String INPUT_IMAGE = "Input image";
    public static final String RETAIN_MEASUREMENTS = "Retain measurements";
    public static final String REMOVE_ANOTHER_IMAGE = "Remove another image";

    public RemoveImage(ModuleCollection modules) {
        super("Remove image",modules);
    }


    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_STACK;
    }

    @Override
    public String getDescription() {
        return "Removes the specified image(s) from the workspace.  Doing this helps keep memory usage down.  Measurements associated with an image can be retained for further use.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        ParameterGroup parameterGroup = parameters.getParameter(REMOVE_ANOTHER_IMAGE);
        LinkedHashMap<Integer,ParameterCollection> collections = parameterGroup.getCollections(false);

        for (ParameterCollection collection:collections.values()) {
            String inputImageName = collection.getValue(INPUT_IMAGE);
            boolean retainMeasurements = collection.getValue(RETAIN_MEASUREMENTS);

            // Removing the relevant image from the workspace
            writeMessage("Removing image ("+inputImageName+") from workspace");
            workspace.removeImage(inputImageName,retainMeasurements);

        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(REMOVAL_SEPARATOR,this));

        ParameterCollection collection = new ParameterCollection();
        collection.add(new RemovedImageP(INPUT_IMAGE,this,"","Name of the image to be removed from the workspace."));
        collection.add(new BooleanP(RETAIN_MEASUREMENTS,this,false,"Retain measurements for this image, or remove everything.  When selected, the image intensity information will be removed, as this is typically where most memory us used, however any measurements associated with it will be retained."));
        parameters.add(new ParameterGroup(REMOVE_ANOTHER_IMAGE,this,collection,1,"Mark another image from the workspace for removal."));

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
