package wbif.sjx.ModularImageAnalysis.Module.Deprecated;

import ij.ImagePlus;
import ij.plugin.ChannelSplitter;
import ij.plugin.Duplicator;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.InputImageP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.IntegerP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.OutputImageP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.ParameterCollection;

/**
 * Created by Stephen on 08/05/2017.
 */
public class ChannelExtractor extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String CHANNEL_TO_EXTRACT = "Channel to extract (>= 1)";

    @Override
    public String getTitle() {
        return "Channel extractor";
    }

    @Override
    public String getPackageName() {
        return PackageNames.DEPRECATED;
    }

    @Override
    public String getHelp() {
        return "NOTE: This Module has been superseeded by the ExtractSubstack Module.  It will " +
                "be removed in a future release.\r\n" +
                "Extracts a single channel from a stack.";
    }

    @Override
    public boolean run(Workspace workspace) {
        // Loading input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        writeMessage("Loading image ("+inputImageName+") into workspace");
        ImagePlus ipl = workspace.getImages().get(inputImageName).getImagePlus();

        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        int channel = parameters.getValue(CHANNEL_TO_EXTRACT);

        // Getting selected channel
        writeMessage("Extracting channel "+channel);
        ipl = new Duplicator().run(ipl);
        ImagePlus outputChannelImagePlus = ChannelSplitter.split(ipl)[channel-1];

        // Adding image to workspace
        writeMessage("Adding image ("+outputImageName+") to workspace");
        Image outputImage = new Image(outputImageName,outputChannelImagePlus);
        workspace.addImage(outputImage);

        if (showOutput) outputImage.showImage();

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE,this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE,this));
        parameters.add(new IntegerP(CHANNEL_TO_EXTRACT,this,1));

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
