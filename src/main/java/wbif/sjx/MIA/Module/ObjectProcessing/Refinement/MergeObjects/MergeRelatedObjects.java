package wbif.sjx.MIA.Module.ObjectProcessing.Refinement.MergeObjects;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.ChildObjectsP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.Objects.OutputObjectsP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

public class MergeRelatedObjects extends Module {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String PARENT_OBJECTS = "Parent objects";
    public static final String CHILD_OBJECTS = "Child objects";

    public static final String OUTPUT_SEPARATOR = "Object output";
    public static final String OUTPUT_MODE = "Output mode";
    public static final String OUTPUT_MERGED_OBJECTS = "Output overlapping objects";
    public static final String MERGE_MODE = "Merge mode";

    public interface OutputModes {
        String CREATE_NEW_OBJECT = "Create new object";
        String UPDATE_PARENT = "Merge children into parent";

        String[] ALL = new String[] { CREATE_NEW_OBJECT, UPDATE_PARENT };

    }

    public interface MergeModes {
        String MERGE_CHILDREN_ONLY = "Merge children only";
        String MERGE_PARENTS_AND_CHILDREN = "Merge parents and children";

        String[] ALL = new String[] { MERGE_CHILDREN_ONLY, MERGE_PARENTS_AND_CHILDREN };

    }

    public MergeRelatedObjects(ModuleCollection modules) {
        super("Merge related objects", modules);
    }

    public static ObjCollection mergeRelatedObjectsCreateNew(ObjCollection parentObjects, String childObjectsName,
            String relatedObjectsName, String mergeMode) {
        ObjCollection relatedObjects = new ObjCollection(relatedObjectsName, parentObjects);

        for (Obj parentObj : parentObjects.values()) {
            // Collecting all children for this parent. If none are present, skip to the
            // next parent
            ObjCollection currChildObjects = parentObj.getChildren(childObjectsName);
            if (currChildObjects.size() == 0)
                continue;

            // Creating a new Obj and assigning pixels from the parent and all children
            Obj relatedObject = relatedObjects.createAndAddNewObject(parentObj.getVolumeType());
            relatedObject.setT(parentObj.getT());
            relatedObjects.add(relatedObject);
            parentObj.addChild(relatedObject);
            relatedObject.addParent(parentObj);

            // Transferring points from the child object to the new object
            for (Obj childObject : currChildObjects.values())
                relatedObject.getCoordinateSet().addAll(childObject.getCoordinateSet());

            switch (mergeMode) {
                case MergeModes.MERGE_PARENTS_AND_CHILDREN:
                    relatedObject.getCoordinateSet().addAll(parentObj.getCoordinateSet());
                    break;
            }

        }

        return relatedObjects;

    }

    public static void mergeRelatedObjectsUpdateParent(ObjCollection parentObjects, String childObjectsName,
            String mergeMode) {
        for (Obj parentObj : parentObjects.values()) {
            // Collecting all children for this parent. If none are present, skip to the
            // next parent
            ObjCollection currChildObjects = parentObj.getChildren(childObjectsName);
            if (currChildObjects.size() == 0)
                continue;

            // Transferring points from the child object to the parent object
            for (Obj childObject : currChildObjects.values())
                parentObj.getCoordinateSet().addAll(childObject.getCoordinateSet());

            // Removing any surfaces/centroids that have been previously calculated
            parentObj.clearCentroid();
            parentObj.clearProjected();
            parentObj.clearSurface();
            parentObj.clearROIs();

        }
    }


    @Override
    public Category getCategory() {
        return Categories.OBJECT_PROCESSING_REFINEMENT_MERGE_OBJECTS;
    }

    @Override
    public String getDescription() {
        return "Combine coordinates from related objects into a single object.  This module can either add coordinates from all child objects into the associated parent or create entirely new merged objects.  New merged objects can either contain just coordinates from child objects, or from the parent and its children.  Any duplicate coordinates arising from overlapping child objects will only be stored once.<br><br>Note: If updating the parent objects, any previously-measured object properties may be invalid (i.e. they are not updated).  To update such measurements it's necessary to re-run the relevant measurement modules.";

    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting input objects
        String parentObjectName = parameters.getValue(PARENT_OBJECTS);
        ObjCollection parentObjects = workspace.getObjects().get(parentObjectName);

        String childObjectsName = parameters.getValue(CHILD_OBJECTS);
        String outputMode = parameters.getValue(OUTPUT_MODE);
        String relatedObjectsName = parameters.getValue(OUTPUT_MERGED_OBJECTS);
        String mergeMode = parameters.getValue(MERGE_MODE);

        switch (outputMode) {
            case OutputModes.CREATE_NEW_OBJECT:
                ObjCollection relatedObjects = mergeRelatedObjectsCreateNew(parentObjects, childObjectsName,
                        relatedObjectsName, mergeMode);
                if (relatedObjects == null)
                    return Status.PASS;

                workspace.addObjects(relatedObjects);
                if (showOutput)
                    relatedObjects.convertToImageRandomColours().showImage();

                break;

            case OutputModes.UPDATE_PARENT:
                mergeRelatedObjectsUpdateParent(parentObjects, childObjectsName, mergeMode);
                if (showOutput)
                    parentObjects.convertToImageRandomColours().showImage();
                break;
        }
        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(PARENT_OBJECTS, this));
        parameters.add(new ChildObjectsP(CHILD_OBJECTS, this));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new ChoiceP(OUTPUT_MODE, this, OutputModes.CREATE_NEW_OBJECT, OutputModes.ALL));
        parameters.add(new OutputObjectsP(OUTPUT_MERGED_OBJECTS, this));
        parameters.add(new ChoiceP(MERGE_MODE, this, MergeModes.MERGE_CHILDREN_ONLY, MergeModes.ALL));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.get(INPUT_SEPARATOR));
        returnedParameters.add(parameters.get(PARENT_OBJECTS));
        returnedParameters.add(parameters.get(CHILD_OBJECTS));
        ChildObjectsP childObjectsP = parameters.getParameter(CHILD_OBJECTS);
        childObjectsP.setParentObjectsName(parameters.getValue(PARENT_OBJECTS));

        returnedParameters.add(parameters.get(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.get(OUTPUT_MODE));
        switch ((String) parameters.getValue(OUTPUT_MODE)) {
            case OutputModes.CREATE_NEW_OBJECT:
                returnedParameters.add(parameters.get(OUTPUT_MERGED_OBJECTS));
                returnedParameters.add(parameters.get(MERGE_MODE));
                break;
        }

        return returnedParameters;

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
        ParentChildRefCollection returnedRelationships = new ParentChildRefCollection();

        switch ((String) parameters.getValue(OUTPUT_MODE)) {
            case OutputModes.CREATE_NEW_OBJECT:
                returnedRelationships.add(parentChildRefs.getOrPut(parameters.getValue(PARENT_OBJECTS),
                        parameters.getValue(OUTPUT_MERGED_OBJECTS)));
                break;
        }

        return returnedRelationships;

    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        parameters.get(PARENT_OBJECTS).setDescription("Input parent objects for merging.  If \"" + OUTPUT_MODE
                + "\" is set to \"" + OutputModes.UPDATE_PARENT
                + "\" all the coordinates from child objects will be added to this object.  However, if operating in \""
                + OutputModes.CREATE_NEW_OBJECT + "\" mode and \"" + MERGE_MODE + "\" is set to \""
                + MergeModes.MERGE_PARENTS_AND_CHILDREN
                + "\", coordinates from parent objects will be added to the new merged objects.");

        parameters.get(CHILD_OBJECTS).setDescription("Child objects of the input parent.  If \"" + OUTPUT_MODE
                + "\" is set to \"" + OutputModes.UPDATE_PARENT
                + "\" all the coordinates from these objects will be added to their respective parent.  However, if operating in \""
                + OutputModes.CREATE_NEW_OBJECT
                + "\" coordinates from these objects will be added to the new merged objects.");

        parameters.get(OUTPUT_MODE).setDescription("Controls where the merged object coordinates are output to:<br><ul>"

                + "<li>\"" + OutputModes.CREATE_NEW_OBJECT
                + "\" For each input parent, a new merged object will be created.  These merged objects are themselves children of the parent object.</li>"

                + "<li>\"" + OutputModes.UPDATE_PARENT
                + "\" Combined coordinates (original coordinates from parent and coordinates of children) are added to this parent object.  Note: In this mode the coordinates of the parent object are being updated, so any previously-measured object properties may be invalid (i.e. they are not updated).  To update such measurements it's necessary to re-run the relevant measurement modules.</li></ul>");

        parameters.get(OUTPUT_MERGED_OBJECTS).setDescription(
                "If outputting new merged objects (as opposed to updating the parent), objects will be stored with this reference name.");

        parameters.get(MERGE_MODE).setDescription("When in \"" + OutputModes.CREATE_NEW_OBJECT
                + "\" mode, this parameter controls what coordinates are added to the new merged objects:<ul>"

                + "<li>\"" + MergeModes.MERGE_CHILDREN_ONLY
                + "\" Only coordinates from child objects are added to the merged object.  In this mode, coordinates for the parent are ignored.</li>"

                + "<li>\"" + MergeModes.MERGE_PARENTS_AND_CHILDREN
                + "\" Coordinates from both the parent and child objects are added to the new merged object.</li></ul>");

    }
}
