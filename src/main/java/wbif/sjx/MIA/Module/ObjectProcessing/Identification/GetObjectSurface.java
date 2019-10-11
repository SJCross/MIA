package wbif.sjx.MIA.Module.ObjectProcessing.Identification;

import ij.ImagePlus;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Process.ColourFactory;
import wbif.sjx.common.Object.LUTs;
import wbif.sjx.common.Object.Volume.Volume;
import wbif.sjx.common.Object.Volume.VolumeType;

import java.util.HashMap;

public class GetObjectSurface extends Module {
    public static final String INPUT_SEPARATOR = "Image input, object output";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_OBJECTS = "Output objects";


    public GetObjectSurface(ModuleCollection modules) {
        super("Get object surface", modules);
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_IDENTIFICATION;
    }

    @Override
    protected boolean process(Workspace workspace) {
        // Getting parameters
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);
        ObjCollection outputObjects = new ObjCollection(outputObjectsName);

        for (Obj inputObject:inputObjects.values()) {
            Volume outputVolume = inputObject.getSurface();
            Obj outputObject = new Obj(VolumeType.POINTLIST,outputObjectsName,outputObjects.getAndIncrementID(),inputObject);
            outputObject.setCoordinateSet(outputVolume.getCoordinateSet());
            outputObjects.add(outputObject);
        }

        workspace.addObjects(outputObjects);

        // Showing objects
        if (showOutput) {
            HashMap<Integer,Float> hues = ColourFactory.getRandomHues(outputObjects);
            ImagePlus dispIpl = outputObjects.convertToImage(outputObjectsName,null,hues,8,false).getImagePlus();
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
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS,this));

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
        RelationshipRefCollection returnedRelationships = new RelationshipRefCollection();

        returnedRelationships.add(relationshipRefs.getOrPut(parameters.getValue(INPUT_OBJECTS),parameters.getValue(OUTPUT_OBJECTS)));

        return returnedRelationships;

    }

    @Override
    public boolean verify() {
        return true;
    }

    @Override
    public String getDescription() {
        return "Create surface objects for each input object.  Surfaces are stored as children of the input object.";
    }
}
