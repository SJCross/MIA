package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel;

import ij.CompositeImage;
import ij.process.LUT;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.*;
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
        String RANDOM = "Random";

        String[] ALL = new String[]{GREY,RED,GREEN,BLUE,CYAN,MAGNETA,YELLOW,FIRE,RANDOM};

    }

    public interface ChannelModes {
        String ALL_CHANNELS = "All channels";
        String SPECIFIC_CHANNELS = "Specific channels";

        String[] ALL = new String[]{ALL_CHANNELS,SPECIFIC_CHANNELS};

    }


    public static LUT getLUT(String lookupTableName) {
        switch (lookupTableName) {
            case LookupTables.GREY:
            default:
                return LUT.createLutFromColor(Color.WHITE);
            case LookupTables.RED:
                return LUT.createLutFromColor(Color.RED);
            case LookupTables.GREEN:
                return LUT.createLutFromColor(Color.GREEN);
            case LookupTables.BLUE:
                return LUT.createLutFromColor(Color.BLUE);
            case LookupTables.CYAN:
                return LUT.createLutFromColor(Color.CYAN);
            case LookupTables.MAGNETA:
                return LUT.createLutFromColor(Color.MAGENTA);
            case LookupTables.YELLOW:
                return LUT.createLutFromColor(Color.YELLOW);
            case LookupTables.FIRE:
                return LUTs.BlackFire();
            case LookupTables.RANDOM:
                return LUTs.Random(true);
        }
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
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);

        // Getting parameters
        String lookupTableName = parameters.getValue(LOOKUP_TABLE);
        String channelMode = parameters.getValue(CHANNEL_MODE);
        int channel = parameters.getValue(CHANNEL);
        LUT lut = getLUT(lookupTableName);

        switch (channelMode) {
            case ChannelModes.ALL_CHANNELS:
                inputImage.getImagePlus().setLut(lut);
                break;

            case ChannelModes.SPECIFIC_CHANNELS:
                ((CompositeImage) inputImage.getImagePlus()).setChannelLut(lut,channel);
                break;
        }

        if (showOutput) inputImage.showImage();

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
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(LOOKUP_TABLE));
        returnedParameters.add(parameters.getParameter(CHANNEL_MODE));

        switch ((String) parameters.getValue(CHANNEL_MODE)) {
            case ChannelModes.SPECIFIC_CHANNELS:
                returnedParameters.add(parameters.getParameter(CHANNEL));
                break;
        }

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipCollection updateAndGetRelationships() {
        return null;
    }

}
