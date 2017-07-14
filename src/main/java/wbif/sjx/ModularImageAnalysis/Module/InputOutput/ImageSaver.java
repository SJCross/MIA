package wbif.sjx.ModularImageAnalysis.Module.InputOutput;

import ij.IJ;
import ij.ImagePlus;
import org.apache.commons.io.FilenameUtils;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Process.IntensityMinMax;

import java.io.File;

/**
 * Created by sc13967 on 26/06/2017.
 */
public class ImageSaver extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String SAVE_LOCATION = "Save location";
    public static final String SAVE_FILE_PATH = "File path";
    public static final String SAVE_SUFFIX = "Add filename suffix";
    public static final String FLATTEN_OVERLAY = "Flatten overlay";

    private static final String SAVE_WITH_INPUT = "Save with input file";
    private static final String SPECIFIC_LOCATION = "Specific location";
    private static final String[] SAVE_LOCATIONS = new String[]{SAVE_WITH_INPUT,SPECIFIC_LOCATION};

    @Override
    public String getTitle() {
        return "Save image";
    }

    @Override
    public String getHelp() {
        return "+++INCOMPLETE+++";
    }

    @Override
    public void execute(Workspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        String saveLocation = parameters.getValue(SAVE_LOCATION);
        String filePath = parameters.getValue(SAVE_FILE_PATH);
        String suffix = parameters.getValue(SAVE_SUFFIX);
        boolean flattenOverlay = parameters.getValue(FLATTEN_OVERLAY);

        if (flattenOverlay) {
            // Flattening overlay onto image for saving
            if (inputImagePlus.getNSlices() > 1) {
                IntensityMinMax.run(inputImagePlus,true);
                if (inputImagePlus.getOverlay() != null) inputImagePlus.flattenStack();

            } else {
                IntensityMinMax.run(inputImagePlus,false);
                if (inputImagePlus.getOverlay() != null) inputImagePlus.flatten();
            }
        }

        if (saveLocation.equals(SAVE_WITH_INPUT)) {
            File rootFile = workspace.getMetadata().getFile();
            String path = rootFile.getParent()+ "\\"+FilenameUtils.removeExtension(rootFile.getName());
            path = path + suffix + ".tif";
            IJ.save(inputImagePlus,path);

        } else if (saveLocation.equals(SPECIFIC_LOCATION)) {
            String path = FilenameUtils.removeExtension(filePath);
            path = path + suffix + ".tif";
            IJ.save(inputImagePlus,path);

        }

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.addParameter(new Parameter(SAVE_LOCATION, Parameter.CHOICE_ARRAY,SAVE_LOCATIONS[0],SAVE_LOCATIONS));
        parameters.addParameter(new Parameter(SAVE_FILE_PATH, Parameter.FILE_PATH,null));
        parameters.addParameter(new Parameter(SAVE_SUFFIX, Parameter.STRING,""));
        parameters.addParameter(new Parameter(FLATTEN_OVERLAY, Parameter.BOOLEAN,true));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        ParameterCollection returnedParamters = new ParameterCollection();

        returnedParamters.addParameter(parameters.getParameter(INPUT_IMAGE));
        returnedParamters.addParameter(parameters.getParameter(SAVE_LOCATION));

        if (parameters.getValue(SAVE_LOCATION).equals(SPECIFIC_LOCATION)) {
            returnedParamters.addParameter(parameters.getParameter(SAVE_FILE_PATH));
        }

        returnedParamters.addParameter(parameters.getParameter(SAVE_SUFFIX));
        returnedParamters.addParameter(parameters.getParameter(FLATTEN_OVERLAY));

        return returnedParamters;

    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
