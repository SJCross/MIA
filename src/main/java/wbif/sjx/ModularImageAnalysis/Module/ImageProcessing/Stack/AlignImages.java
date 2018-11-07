package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;

public class AlignImages extends Module {
    public static final String INPUT_STATIC_IMAGE = "Input static image";
    public static final String INPUT_MOVING_IMAGE = "Input moving image";
    public static final String ALIGNMENT_MODE = "Alignment mode";


    public interface AlignmentModes {
        String MANUAL_AFFINE = "Manual affine";

        String[] ALL = new String[]{MANUAL_AFFINE};

    }


    @Override
    public String getTitle() {
        return "Align images";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_STACK;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected void run(Workspace workspace) {
        // Getting input images
        String inputStaticImageName = parameters.getValue(INPUT_STATIC_IMAGE);
        Image inputStaticImage = workspace.getImage(inputStaticImageName);
        String inputMovingImageName = parameters.getValue(INPUT_MOVING_IMAGE);
        Image inputMovingImage = workspace.getImage(inputMovingImageName);

        // Getting other parameters
        String alignmentMode = parameters.getValue(ALIGNMENT_MODE);



    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_STATIC_IMAGE,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(INPUT_MOVING_IMAGE,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(ALIGNMENT_MODE,Parameter.CHOICE_ARRAY,AlignmentModes.MANUAL_AFFINE,AlignmentModes.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
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
    public MetadataReferenceCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
