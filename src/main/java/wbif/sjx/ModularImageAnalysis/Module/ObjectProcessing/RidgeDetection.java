// TODO: Check "frame" from RidgeDetection is 0-indexed
// TODO: Add junction linking (could be just for objects with a single shared junction)
// TODO: Add multitimepoint analysis (LineDetector only works on a single image in 2D)

package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import de.biomedical_imaging.ij.steger.*;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Module.Visualisation.AddObjectsOverlay;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Process.IntensityMinMax;

import java.util.*;

/**
 * Created by sc13967 on 30/05/2017.
 */
public class RidgeDetection extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String LOWER_THRESHOLD = "Lower threshold";
    public static final String UPPER_THRESHOLD = "Upper threshold";
    public static final String SIGMA = "Sigma";
    public static final String MIN_LENGTH = "Minimum length (px)";
    public static final String MAX_LENGTH = "Maximum length (px)";
    public static final String CONTOUR_CONTRAST = "Contour contrast";
    public static final String LINK_CONTOURS = "Link contours";
    public static final String SHOW_OBJECTS = "Show objects";

    private Reference outputObjects;

    private interface Measurements {
        String LENGTH_PX = "RIDGE_DETECT//LENGTH_(PX)";

    }

    private interface ContourContrast {
        String DARK_LINE = "Dark line";
        String LIGHT_LINE = "Light line";

        String[] ALL = new String[]{DARK_LINE,LIGHT_LINE};

    }


    @Override
    public String getTitle() {
        return "Ridge detection";
    }

    @Override
    public String getHelp() {
        return "Uses the RidgeDetection Fiji plugin by Thorsten Wagner, which implements Carsten " +
                "\nSteger's paper \"An Unbiased Detector of Curvilinear Structures\"" +
                "\nINCOMPLETE";

    }

    @Override
    protected void run(Workspace workspace, boolean verbose) throws GenericMIAException {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting output image name
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        ObjCollection outputObjects = new ObjCollection(outputObjectsName);

        // Getting parameters (RidgeDetection plugin wants to use pixel units only)
        double lowerThreshold = parameters.getValue(LOWER_THRESHOLD);
        double upperThreshold = parameters.getValue(UPPER_THRESHOLD);
        double sigma = parameters.getValue(SIGMA);
        String contourContrast = parameters.getValue(CONTOUR_CONTRAST);
        double minLength = parameters.getValue(MIN_LENGTH);
        double maxLength = parameters.getValue(MAX_LENGTH);
        boolean linkContours = parameters.getValue(LINK_CONTOURS);

        // Storing the image calibration
        Calibration calibration = inputImagePlus.getCalibration();
        double dppXY = calibration.getX(1);
        double dppZ = calibration.getZ(1);
        String calibrationUnits = calibration.getUnits();


        LineDetector lineDetector = new LineDetector();
        boolean darkLine = contourContrast.equals(ContourContrast.DARK_LINE);

        // Iterating over each image in the stack
        int count = 1;
        int total = inputImagePlus.getNChannels()*inputImagePlus.getNSlices()*inputImagePlus.getNFrames();

        for (int c=0;c<inputImagePlus.getNChannels();c++) {
            for (int z=0;z<inputImagePlus.getNSlices();z++) {
                for (int t = 0; t < inputImagePlus.getNFrames(); t++) {
                    if (verbose) System.out.println("[" + moduleName + "] Processing image "+(count++)+" of "+total);
                    inputImagePlus.setPosition(c+1,z+1,t+1);

                    // Running the ridge detection
                    Lines lines;
                    try {
                         lines = lineDetector.detectLines(inputImagePlus.getProcessor(), sigma, upperThreshold,
                                lowerThreshold, minLength, maxLength, darkLine, true, false, false);
                    } catch (NegativeArraySizeException e) {
                        String errorMessage = "Ridge detection failed for file "+workspace.getMetadata().getFile().getName()
                                +" at position (C="+c+", Z="+z+", T="+t+")";
                        System.err.println(errorMessage);

                        continue;

                    }

                    Junctions junctions = lineDetector.getJunctions();

                    // If linking contours, adding all to a HashSet.  This prevents the same contours being added to
                    // the same set
                    HashMap<Line, HashSet<Line>> groups = new HashMap<>(); // Stored as <Line,LineGroup>
                    for (Line line : lines) {
                        HashSet<Line> lineGroup = new HashSet<>();
                        lineGroup.add(line);
                        groups.put(line, lineGroup);
                    }

                    // Iterating over each object, adding it to the nascent ObjCollection
                    if (linkContours) {
                        if (verbose) System.out.println("[" + moduleName + "] Linking contours");

                        for (Junction junction : junctions) {
                            // Getting the LineGroup associated with Line1.  If there isn't one, creating a new one
                            Line line1 = junction.getLine1();
                            HashSet<Line> group1 = groups.get(line1);

                            // Getting the LineGroup associated with Line2.  If there isn't one, creating a new one
                            Line line2 = junction.getLine2();
                            HashSet<Line> group2 = groups.get(line2);

                            // Adding all entries from the second LineGroup into the first
                            group1.addAll(group2);

                            // Removing the second Line from the HashMap, then re-adding it with the first LineGroup
                            groups.remove(line2);
                            groups.put(line2, group1);

                        }
                    }

                    // Getting the unique LineGroups
                    Set<HashSet<Line>> uniqueLineGroup = new HashSet<>(groups.values());
                    for (HashSet<Line> lineGroup : uniqueLineGroup) {
                        Obj outputObject = new Obj(outputObjectsName, outputObjects.getNextID(), dppXY, dppZ,
                                calibrationUnits);

                        double estLength = 0;
                        for (Line line : lineGroup) {
                            // Adding coordinates for the current line
                            float[] x = line.getXCoordinates();
                            float[] y = line.getYCoordinates();
                            for (int i = 0; i < x.length; i++) {
                                outputObject.addCoord(Math.round(x[i]), Math.round(y[i]), 0);
                            }

                            // Adding the estimated length to the current length
                            estLength += line.estimateLength();

                        }

                        // Setting single values for the current contour
                        outputObject.setT(t);
                        outputObject.addMeasurement(new Measurement(Measurements.LENGTH_PX, estLength));
                        outputObjects.add(outputObject);
                    }
                }
            }
        }

        inputImagePlus.setPosition(1,1,1);

        workspace.addObjects(outputObjects);

        if (parameters.getValue(SHOW_OBJECTS)) {
            // Adding image to workspace
            if (verbose)
                System.out.println("[" + moduleName + "] Adding objects (" + outputObjectsName + ") to workspace");

            // Creating a duplicate of the input image
            inputImagePlus = new Duplicator().run(inputImagePlus);
            IntensityMinMax.run(inputImagePlus, true);

            // Creating the overlay
            AddObjectsOverlay.createOverlay(inputImagePlus, outputObjects, "",
                    AddObjectsOverlay.ColourModes.RANDOM_COLOUR,"", AddObjectsOverlay.PositionModes.ALL_POINTS, "",
                    "", "", false, false, 0, "");

            // Displaying the overlay
            inputImagePlus.show();

        }
    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE, null));
        parameters.addParameter(new Parameter(OUTPUT_OBJECTS, Parameter.OUTPUT_OBJECTS, null));
        parameters.addParameter(new Parameter(LOWER_THRESHOLD, Parameter.DOUBLE, 0.5));
        parameters.addParameter(new Parameter(UPPER_THRESHOLD, Parameter.DOUBLE, 0.85));
        parameters.addParameter(new Parameter(SIGMA, Parameter.DOUBLE, 3d));
        parameters.addParameter(
                new Parameter(CONTOUR_CONTRAST,Parameter.CHOICE_ARRAY,ContourContrast.DARK_LINE,ContourContrast.ALL));
        parameters.addParameter(new Parameter(MIN_LENGTH, Parameter.DOUBLE, 0d));
        parameters.addParameter(new Parameter(MAX_LENGTH, Parameter.DOUBLE, 0d));
        parameters.addParameter(new Parameter(LINK_CONTOURS, Parameter.BOOLEAN, false));
        parameters.addParameter(new Parameter(SHOW_OBJECTS, Parameter.BOOLEAN, false));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        return parameters;
    }

    @Override
    public void initialiseReferences() {
        outputObjects = new Reference();
        objectReferences.add(outputObjects);

        outputObjects.addMeasurementReference(new MeasurementReference(Measurements.LENGTH_PX));

    }

    @Override
    public ReferenceCollection updateAndGetImageReferences() {
        return null;
    }

    @Override
    public ReferenceCollection updateAndGetObjectReferences() {
        outputObjects.setName(parameters.getValue(OUTPUT_OBJECTS));

        return objectReferences;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}