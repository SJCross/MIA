package wbif.sjx.MIA.Module.Visualisation.Overlays;

import java.util.HashMap;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.ChildObjectsP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.ObjectMeasurementP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.ParentObjectsP;
import wbif.sjx.MIA.Object.Parameters.PartnerObjectsP;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ParentChildRefCollection;
import wbif.sjx.MIA.Process.ColourFactory;

public abstract class Overlay extends Module {
    public static final String COLOUR_MODE = "Colour mode";
    public static final String SINGLE_COLOUR = "Single colour";
    public static final String CHILD_OBJECTS_FOR_COLOUR = "Child objects for colour";
    public static final String MEASUREMENT_FOR_COLOUR = "Measurement for colour";
    public static final String PARENT_OBJECT_FOR_COLOUR = "Parent object for colour";
    public static final String PARTNER_OBJECTS_FOR_COLOUR = "Partner objects for colour";
    public static final String OPACITY = "Opacity (%)";

    public Overlay(String name, ModuleCollection modules) {
        super(name, modules);
    }

    @Override
    public String getDescription() {
        return null;
    }

    public interface ColourModes {
        String CHILD_COUNT = "Child count";
        String ID = "ID";
        String MEASUREMENT_VALUE = "Measurement value";
        String PARENT_ID = "Parent ID";
        String PARENT_MEASUREMENT_VALUE = "Parent measurement value";
        String PARTNER_COUNT = "Partner count";
        String RANDOM_COLOUR = "Random colour";
        String SINGLE_COLOUR = "Single colour";

        String[] ALL = new String[] { CHILD_COUNT, ID, MEASUREMENT_VALUE, PARENT_ID, PARENT_MEASUREMENT_VALUE,
                PARTNER_COUNT, RANDOM_COLOUR, SINGLE_COLOUR };

    }

    public interface SingleColours extends ColourFactory.SingleColours {
    }

    public HashMap<Integer, Float> getHues(ObjCollection inputObjects) {
        // Getting colour settings
        String colourMode = parameters.getValue(COLOUR_MODE);
        String singleColour = parameters.getValue(SINGLE_COLOUR);
        String childObjectsForColourName = parameters.getValue(CHILD_OBJECTS_FOR_COLOUR);
        String parentObjectsForColourName = parameters.getValue(PARENT_OBJECT_FOR_COLOUR);
        String partnerObjectsForColourName = parameters.getValue(PARTNER_OBJECTS_FOR_COLOUR);
        String measurementForColour = parameters.getValue(MEASUREMENT_FOR_COLOUR);

        // Generating colours for each object
        switch (colourMode) {
            case ColourModes.SINGLE_COLOUR:
            default:
                return ColourFactory.getSingleColourHues(inputObjects, singleColour);
            case ColourModes.CHILD_COUNT:
                return ColourFactory.getChildCountHues(inputObjects, childObjectsForColourName, true);
            case ColourModes.ID:
                return ColourFactory.getIDHues(inputObjects, true);
            case ColourModes.RANDOM_COLOUR:
                return ColourFactory.getRandomHues(inputObjects);
            case ColourModes.MEASUREMENT_VALUE:
                return ColourFactory.getMeasurementValueHues(inputObjects, measurementForColour, true);
            case ColourModes.PARENT_ID:
                return ColourFactory.getParentIDHues(inputObjects, parentObjectsForColourName, true);
            case ColourModes.PARENT_MEASUREMENT_VALUE:
                return ColourFactory.getParentMeasurementValueHues(inputObjects, parentObjectsForColourName,
                        measurementForColour, true);
            case ColourModes.PARTNER_COUNT:
                return ColourFactory.getPartnerCountHues(inputObjects, partnerObjectsForColourName, true);
        }
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ChoiceP(COLOUR_MODE, this, ColourModes.SINGLE_COLOUR, ColourModes.ALL,
                "Method for determining colour of each object's corresponding overlay:<br>"
                        + "<br>- \"" + ColourModes.CHILD_COUNT + "\" Overlay colour is determined by the number of children each object has.  "
                        + "Colour range runs across the first half of the visible spectrum (i.e. red to cyan) and is maximised, so the object "
                        + "with the fewest children is shown in red and the object with the most, in cyan.  Objects without any children are always shown in red.  "
                        + "Child objects used for counting are selected with the \""+CHILD_OBJECTS_FOR_COLOUR+"\" parameter.<br>"

                        + "<br>- \"" + ColourModes.ID + "\" Overlay colour is quasi-randomly selected based on the ID number of the object.  The colour used for a specific "
                        + "ID number will always be the same and is calculated using the equation <i>hue = (ID * 1048576 % 255) / 255</i>.<br>"

                        + "<br>- \"" + ColourModes.MEASUREMENT_VALUE + "\" Overlay colour is determined by a measurement value.  "
                        + "Colour range runs across the first half of the visible spectrum (i.e. red to cyan) and is maximised, so the object "
                        + "with the smallest measurement is shown in red and the object with the largest, in cyan.  Objects missing the relevant measurement "
                        + " are always shown in red.  The measurement value is selected with the \""+MEASUREMENT_FOR_COLOUR+"\" parameter.<br>"
                        
                        + "<br>- \"" + ColourModes.PARENT_ID + "\" Overlay colour is quasi-randomly selected based on the ID number of a parent of this object.  "
                        + "The colour used for a specific ID number will always be the same and is calculated using the equation <i>hue = (ID * 1048576 % 255) / 255</i>.  "
                        + "The parent object is selected with the \""+PARENT_OBJECT_FOR_COLOUR+"\" parameter.<br>"

                        + "<br>- \"" + ColourModes.PARENT_MEASUREMENT_VALUE + "\" Overlay colour is determined by a measurement value of a parent of this object.  "
                        + "Colour range runs across the first half of the visible spectrum (i.e. red to cyan) and is maximised, so the object "
                        + "with the smallest measurement is shown in red and the object with the largest, in cyan.  Objects either missing the relevant measurement or without "
                        + "the relevant parent are always shown in red.  The parent object is selected with the \""+PARENT_OBJECT_FOR_COLOUR+"\" parameter and the measurement "
                        + "value is selected with the \""+MEASUREMENT_FOR_COLOUR+"\" parameter.<br>"

                        + "<br>- \"" + ColourModes.PARTNER_COUNT + "\"  Overlay colour is determined by the number of partners each object has.  "
                        + "Colour range runs across the first half of the visible spectrum (i.e. red to cyan) and is maximised, so the object "
                        + "with the fewest partners is shown in red and the object with the most, in cyan.  Objects without any partners are always shown in red.  "
                        + "Partner objects used for counting are selected with the \""+PARTNER_OBJECTS_FOR_COLOUR+"\" parameter.<br>"

                        + "<br>- \"" + ColourModes.RANDOM_COLOUR + "\" Overlay colour is randomly selected for each object.  "
                        + "Unlike the \""+ColourModes.ID+"\" option, the colours generated here will be different for each evaluation of the module.<br>"

                        + "<br>- \"" + ColourModes.SINGLE_COLOUR + "\" (default option) Overlay colour is fixed to one of a predetermined list of colours.  All objects "
                        + " will be assigned the same overlay colour.  The colour is chosen using the \""+SINGLE_COLOUR+"\" parameter.<br>"));

        parameters.add(new ChoiceP(SINGLE_COLOUR, this, SingleColours.WHITE, SingleColours.ALL,
                "Colour for all object overlays to be rendered using.  This parameter is used if \"" + COLOUR_MODE
                        + "\" is set to \""+ColourModes.SINGLE_COLOUR+"\".  Options are: "+String.join(",", SingleColours.ALL)+"."));

        parameters.add(new ChildObjectsP(CHILD_OBJECTS_FOR_COLOUR, this, "Object collection used to determine the colour of the overlay based on number of children per object when \"" + COLOUR_MODE
                + "\" is set to \"" + ColourModes.CHILD_COUNT
                + "\".  These objects will be children of the input objects."));
        
        parameters.add(new ObjectMeasurementP(MEASUREMENT_FOR_COLOUR, this, "Measurement used to determine the colour of the overlay when \"" + COLOUR_MODE
        + "\" is set to either \"" + ColourModes.MEASUREMENT_VALUE+ "\" or \""+ColourModes.PARENT_MEASUREMENT_VALUE+"\"."));

        parameters.add(new ParentObjectsP(PARENT_OBJECT_FOR_COLOUR, this, "Object collection used to determine the colour of the overlay based on either the ID or measurement value "
        + " of a parent object when \"" + COLOUR_MODE + "\" is set to either  \"" + ColourModes.PARENT_ID + "\" or \""+ColourModes.PARENT_MEASUREMENT_VALUE+"\".  These objects will be parents of the input objects."));
        
        parameters.add(new PartnerObjectsP(PARTNER_OBJECTS_FOR_COLOUR, this, "Object collection used to determine the colour of the overlay based on number of partners per object when \"" + COLOUR_MODE
        + "\" is set to \"" + ColourModes.PARTNER_COUNT
        + "\".  These objects will be partners of the input objects."));

        parameters.add(new DoubleP(OPACITY, this, 100, "Opacity of the overlay to be rendered.  This is a value between 0 (totally transparent) and 100 (totally opaque)."));

    }

    @Override
    public String getPackageName() {
        return null;
    }

    @Override
    protected Status process(Workspace workspace) {
        return Status.FAIL;
    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return null;
    }

    public ParameterCollection updateAndGetParameters(String inputObjectsName) {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(COLOUR_MODE));
        switch ((String) parameters.getValue(COLOUR_MODE)) {
            case ColourModes.CHILD_COUNT:
                returnedParameters.add(parameters.getParameter(CHILD_OBJECTS_FOR_COLOUR));
                ((ChildObjectsP) parameters.getParameter(CHILD_OBJECTS_FOR_COLOUR))
                        .setParentObjectsName(inputObjectsName);
                break;

            case ColourModes.SINGLE_COLOUR:
                returnedParameters.add(parameters.getParameter(SINGLE_COLOUR));
                break;

            case ColourModes.MEASUREMENT_VALUE:
                returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_COLOUR));
                if (inputObjectsName != null) {
                    ObjectMeasurementP colourMeasurement = parameters.getParameter(MEASUREMENT_FOR_COLOUR);
                    colourMeasurement.setObjectName(inputObjectsName);
                }
                break;

            case ColourModes.PARENT_ID:
                returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_COLOUR));
                ((ParentObjectsP) parameters.getParameter(PARENT_OBJECT_FOR_COLOUR))
                        .setChildObjectsName(inputObjectsName);
                break;

            case ColourModes.PARENT_MEASUREMENT_VALUE:
                returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_COLOUR));
                ((ParentObjectsP) parameters.getParameter(PARENT_OBJECT_FOR_COLOUR))
                        .setChildObjectsName(inputObjectsName);

                returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_COLOUR));
                ObjectMeasurementP colourMeasurement = parameters.getParameter(MEASUREMENT_FOR_COLOUR);
                colourMeasurement.setObjectName(parameters.getValue(PARENT_OBJECT_FOR_COLOUR));

                break;

            case ColourModes.PARTNER_COUNT:
                returnedParameters.add(parameters.getParameter(PARTNER_OBJECTS_FOR_COLOUR));
                ((PartnerObjectsP) parameters.getParameter(PARTNER_OBJECTS_FOR_COLOUR))
                        .setPartnerObjectsName(inputObjectsName);
                break;
        }

        returnedParameters.add(parameters.getParameter(OPACITY));

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
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

}
