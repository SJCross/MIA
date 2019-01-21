package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel;

import ij.CompositeImage;
import ij.process.LUT;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.ChoiceP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.InputImageP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.IntegerP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.ParameterCollection;
import wbif.sjx.common.Object.LUTs;

import java.awt.*;

public class SetLookupTable extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String LOOKUP_TABLE = "Lookup table";
    public static final String CHANNEL_MODE = "Channel mode";
    public static final String CHANNEL = "Channel";

    public interface LookupTables {
        String GREY = "Grey";
        String RED = "Red";
        String GREEN = "Green";
        String BLUE = "Blue";
        String CYAN = "Cyan";
        String MAGNETA = "Magenta";
        String YELLOW = "Yellow";
        String FIRE = "Fire";

        String[] ALL = new String[]{GREY,RED,GREEN,BLUE,CYAN,MAGNETA,YELLOW,FIRE};

    }

    public interface ChannelModes {
        String ALL_CHANNELS = "All channels";
        String SPECIFIC_CHANNELS = "Specific channels";

        String[] ALL = new String[]{ALL_CHANNELS,SPECIFIC_CHANNELS};

    }

    @Override
    public String getTitle() {
        return "Set lookup table";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_PIXEL;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected boolean run(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);

        // Getting parameters
        String lookupTableName = parameters.getValue(LOOKUP_TABLE);
        String channelMode = parameters.getValue(CHANNEL_MODE);
        int channel = parameters.getValue(CHANNEL);

        LUT lut;
        switch (lookupTableName) {
            case LookupTables.GREY:
            default:
                lut = LUT.createLutFromColor(Color.WHITE);
                break;

            case LookupTables.RED:
                lut = LUT.createLutFromColor(Color.RED);
                break;

            case LookupTables.GREEN:
                lut = LUT.createLutFromColor(Color.GREEN);
                break;

            case LookupTables.BLUE:
                lut = LUT.createLutFromColor(Color.BLUE);
                break;

            case LookupTables.CYAN:
                lut = LUT.createLutFromColor(Color.CYAN);
                break;

            case LookupTables.MAGNETA:
                lut = LUT.createLutFromColor(Color.MAGENTA);
                break;

            case LookupTables.YELLOW:
                lut = LUT.createLutFromColor(Color.YELLOW);
                break;

            case LookupTables.FIRE:
                lut = LUTs.BlackFire();
                break;
        }

        switch (channelMode) {
            case ChannelModes.ALL_CHANNELS:
                inputImage.getImagePlus().setLut(lut);
                break;

            case ChannelModes.SPECIFIC_CHANNELS:
                ((CompositeImage) inputImage.getImagePlus()).setChannelLut(lut,channel);
                break;
        }

        if (showOutput) showImage(inputImage);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE,this));
        parameters.add(new ChoiceP(LOOKUP_TABLE,this,LookupTables.GREY,LookupTables.ALL));
        parameters.add(new ChoiceP(CHANNEL_MODE,this,ChannelModes.ALL_CHANNELS,ChannelModes.ALL));
        parameters.add(new IntegerP(CHANNEL,this,1));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParamters = new ParameterCollection();

        returnedParamters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParamters.add(parameters.getParameter(LOOKUP_TABLE));
        returnedParamters.add(parameters.getParameter(CHANNEL_MODE));

        switch ((String) parameters.getValue(CHANNEL_MODE)) {
            case ChannelModes.SPECIFIC_CHANNELS:
                returnedParamters.add(parameters.getParameter(CHANNEL));
                break;
        }

        return returnedParamters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementReferences() {
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
