// TODO: See how TrackMate behaves with different channels.  Currently always assigns channel to "0".

package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification;

import fiji.plugin.trackmate.*;
import fiji.plugin.trackmate.detection.DetectorKeys;
import fiji.plugin.trackmate.detection.LogDetectorFactory;
import fiji.plugin.trackmate.features.spot.SpotRadiusEstimatorFactory;
import fiji.plugin.trackmate.tracking.LAPUtils;
import fiji.plugin.trackmate.tracking.TrackerKeys;
import fiji.plugin.trackmate.tracking.sparselap.SparseLAPTrackerFactory;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.NormaliseIntensity;
import wbif.sjx.ModularImageAnalysis.Module.Visualisation.AddObjectsOverlay;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.common.Process.IntensityMinMax;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by sc13967 on 15/05/2017.
 */
public class RunTrackMate extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_SPOT_OBJECTS = "Output spot objects";
    public static final String CALIBRATED_UNITS = "Calibrated units";
    public static final String DO_SUBPIXEL_LOCALIZATION = "Do sub-pixel localisation";
    public static final String DO_MEDIAN_FILTERING = "Median filtering";
    public static final String RADIUS = "Radius";
    public static final String THRESHOLD = "Threshold";
    public static final String NORMALISE_INTENSITY = "Normalise intensity";
    public static final String DO_TRACKING = "Run tracking";
    public static final String OUTPUT_TRACK_OBJECTS = "Output track objects";
    public static final String LINKING_MAX_DISTANCE = "Max linking distance";
    public static final String GAP_CLOSING_MAX_DISTANCE = "Gap closing max distance";
    public static final String MAX_FRAME_GAP = "Max frame gap";
    public static final String ESTIMATE_SIZE = "Estimate spot size";
    public static final String SHOW_OBJECTS = "Show objects";
    public static final String SHOW_ID = "Show ID";
    public static final String ID_MODE = "ID source";


    public interface IDModes {
        String USE_SPOT_ID = "Use spot ID";
        String USE_TRACK_ID = "Use track ID";

        String[] ALL = new String[]{USE_SPOT_ID, USE_TRACK_ID};

    }

    public interface Measurements {
        String RADIUS_PX = "SPOT_DETECT_TRACK // RADIUS_(PX)";
        String RADIUS_CAL = "SPOT_DETECT_TRACK // RADIUS_(${CAL})";
        String ESTIMATED_DIAMETER_PX = "SPOT_DETECT_TRACK // EST_DIAMETER_(PX)";
        String ESTIMATED_DIAMETER_CAL = "SPOT_DETECT_TRACK // EST_DIAMETER_(${CAL})";

    }


    public Settings initialiseSettings(ImagePlus ipl, Calibration calibration) {
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS);
        boolean subpixelLocalisation = parameters.getValue(DO_SUBPIXEL_LOCALIZATION);
        boolean medianFiltering = parameters.getValue(DO_MEDIAN_FILTERING);
        double radius = parameters.getValue(RADIUS);
        double threshold = parameters.getValue(THRESHOLD);
        double maxLinkDist = parameters.getValue(LINKING_MAX_DISTANCE);
        double maxGapDist = parameters.getValue(GAP_CLOSING_MAX_DISTANCE);
        int maxFrameGap = parameters.getValue(MAX_FRAME_GAP);

        // Applying conversion to parameters
        if (calibratedUnits) {
            double dppXY = calibration.getX(1);
            double dppZ = calibration.getZ(1);
            String calibrationUnits = calibration.getUnits();

            radius = calibration.getRawX(radius);
            maxLinkDist = calibration.getRawX(maxLinkDist);
            maxGapDist = calibration.getRawX(maxGapDist);

        }

        // Initialising settings for TrackMate
        Settings settings = new Settings();

        settings.setFrom(ipl);

        settings.detectorFactory = new LogDetectorFactory();
        settings.detectorSettings.put(DetectorKeys.KEY_DO_SUBPIXEL_LOCALIZATION, subpixelLocalisation);
        settings.detectorSettings.put(DetectorKeys.KEY_DO_MEDIAN_FILTERING, medianFiltering);
        settings.detectorSettings.put(DetectorKeys.KEY_RADIUS, radius);
        settings.detectorSettings.put(DetectorKeys.KEY_THRESHOLD, threshold);
        settings.detectorSettings.put(DetectorKeys.KEY_TARGET_CHANNEL, 1);

        settings.addSpotAnalyzerFactory(new SpotRadiusEstimatorFactory<>());

        settings.trackerFactory  = new SparseLAPTrackerFactory();
        settings.trackerSettings = LAPUtils.getDefaultLAPSettingsMap();
        settings.trackerSettings.put(TrackerKeys.KEY_ALLOW_TRACK_SPLITTING, false);
        settings.trackerSettings.put(TrackerKeys.KEY_ALLOW_TRACK_MERGING, false);
        settings.trackerSettings.put(TrackerKeys.KEY_LINKING_MAX_DISTANCE, maxLinkDist);
        settings.trackerSettings.put(TrackerKeys.KEY_GAP_CLOSING_MAX_DISTANCE, maxGapDist);
        settings.trackerSettings.put(TrackerKeys.KEY_GAP_CLOSING_MAX_FRAME_GAP,maxFrameGap);

        return settings;

    }

    public ObjCollection getSpots(Model model, Calibration calibration, boolean is2D) {
        String spotObjectsName = parameters.getValue(OUTPUT_SPOT_OBJECTS);

        // Getting trackObjects and adding them to the output trackObjects
        writeMessage("Processing detected objects");

        // Getting calibration
        double dppXY = calibration.getX(1);
        double dppZ = calibration.getZ(1);
        String calibrationUnits = calibration.getUnits();

        ObjCollection spotObjects = new ObjCollection(spotObjectsName);
        SpotCollection spots = model.getSpots();
        for (Spot spot:spots.iterable(false)) {
            Obj spotObject = new Obj(spotObjectsName,spot.ID(),dppXY,dppZ,calibrationUnits,is2D);
            spotObject.addCoord((int) spot.getDoublePosition(0),(int) spot.getDoublePosition(1),(int) spot.getDoublePosition(2));
            spotObject.setT((int) Math.round(spot.getFeature(Spot.FRAME)));

            spotObject.addMeasurement(new Measurement(Measurements.RADIUS_PX,spot.getFeature(Spot.RADIUS),this));
            spotObject.addMeasurement(new Measurement(Units.replace(Measurements.RADIUS_CAL),spot.getFeature(Spot.RADIUS)*dppXY,this));
            spotObject.addMeasurement(new Measurement(Measurements.ESTIMATED_DIAMETER_PX,spot.getFeature(SpotRadiusEstimatorFactory.ESTIMATED_DIAMETER),this));
            spotObject.addMeasurement(new Measurement(Units.replace(Measurements.ESTIMATED_DIAMETER_CAL),spot.getFeature(SpotRadiusEstimatorFactory.ESTIMATED_DIAMETER)*dppXY,this));

            spotObjects.add(spotObject);

        }

        // Adding spotObjects to the workspace
        writeMessage(spots.getNSpots(false)+" trackObjects detected");

        return spotObjects;

    }

    public ObjCollection[] getSpotsAndTracks(Model model, Calibration calibration, boolean is2D) {
        String spotObjectsName = parameters.getValue(OUTPUT_SPOT_OBJECTS);
        String trackObjectsName = parameters.getValue(OUTPUT_TRACK_OBJECTS);

        // Getting calibration
        double dppXY = calibration.getX(1);
        double dppZ = calibration.getZ(1);
        String calibrationUnits = calibration.getUnits();

        ObjCollection spotObjects = new ObjCollection(spotObjectsName);
        ObjCollection trackObjects = new ObjCollection(trackObjectsName);

        // Converting tracks to local track model
        writeMessage("Converting tracks to local track model");
        TrackModel trackModel = model.getTrackModel();
        Set<Integer> trackIDs = trackModel.trackIDs(false);

        for (Integer trackID : trackIDs) {
            // If necessary, creating a new summary object for the track
            Obj trackObject = new Obj(trackObjectsName, trackID, dppXY, dppZ, calibrationUnits,is2D);
            ArrayList<Spot> spots = new ArrayList<>(trackModel.trackSpots(trackID));

            // Sorting spots based on frame number
            spots.sort((o1, o2) -> {
                double t1 = o1.getFeature(Spot.FRAME);
                double t2 = o2.getFeature(Spot.FRAME);
                return t1 > t2 ? 1 : t1 == t2 ? 0 : -1;
            });

            // Getting x,y,f and 2-channel spot intensities from TrackMate results
            for (Spot spot : spots) {
                // Initialising a new HCObject to store this track and assigning a unique ID and group (track) ID.
                Obj spotObject = new Obj(spotObjectsName, spotObjects.getNextID(), dppXY, dppZ, calibrationUnits,is2D);

                spotObject.addMeasurement(new Measurement(Measurements.RADIUS_PX,spot.getFeature(Spot.RADIUS),this));
                spotObject.addMeasurement(new Measurement(Units.replace(Measurements.RADIUS_CAL),spot.getFeature(Spot.RADIUS)*dppXY,this));
                spotObject.addMeasurement(new Measurement(Measurements.ESTIMATED_DIAMETER_PX,spot.getFeature(SpotRadiusEstimatorFactory.ESTIMATED_DIAMETER),this));
                spotObject.addMeasurement(new Measurement(Units.replace(Measurements.ESTIMATED_DIAMETER_CAL),spot.getFeature(SpotRadiusEstimatorFactory.ESTIMATED_DIAMETER)*dppXY,this));

                // Getting coordinates
                int x = (int) spot.getDoublePosition(0);
                int y = (int) spot.getDoublePosition(1);
                int z = (int) (spot.getDoublePosition(2) * dppZ / dppXY);
                int t = (int) Math.round(spot.getFeature(Spot.FRAME));

                // Adding coordinates to the instance objects
                spotObject.addCoord(x, y, z);
                spotObject.setT(t);

                // If necessary, adding coordinates to the summary objects
                trackObject.addCoord(x, y, z);
                trackObject.setT(0);

                // Adding the connection between instance and summary objects
                spotObject.addParent(trackObject);
                trackObject.addChild(spotObject);

                // Adding the instance object to the relevant collection
                spotObjects.add(spotObject);
                trackObjects.add(trackObject);

            }
        }

        // Displaying the number of objects detected
        writeMessage(spotObjects.size() + " spots detected");
        writeMessage(trackObjects.size() + " tracks detected");

        return new ObjCollection[]{spotObjects,trackObjects};

    }

    public void estimateSpotSize(ObjCollection spotObjects, ImagePlus ipl) {
        GetLocalObjectRegion getLocalObjectRegion = (GetLocalObjectRegion) new GetLocalObjectRegion()
                .updateParameterValue(GetLocalObjectRegion.OUTPUT_OBJECTS,"SpotVolume")
                .updateParameterValue(GetLocalObjectRegion.CALIBRATED_RADIUS,false)
                .updateParameterValue(GetLocalObjectRegion.USE_MEASUREMENT,true)
                .updateParameterValue(GetLocalObjectRegion.MEASUREMENT_NAME,Measurements.RADIUS_PX);

        ObjCollection volumeObjects = getLocalObjectRegion.getLocalRegions(spotObjects, ipl);

        // Replacing spot volumes with explicit volume
        for (Obj spotObject:spotObjects.values()) {
            Obj spotVolumeObject = spotObject.getChildren("SpotVolume").values().iterator().next();

            spotObject.setPoints(spotVolumeObject.getPoints());

        }
    }

    public void showObjects(ImagePlus ipl, ObjCollection spotObjects) {
        String trackObjectsName = parameters.getValue(OUTPUT_TRACK_OBJECTS);
        boolean doTracking = parameters.getValue(DO_TRACKING);
        boolean showID = parameters.getValue(SHOW_ID);

        HashMap<Integer, Color> colours;
        HashMap<Integer, String> labels;

        // Colours will depend on the detection/tracking mode
        if (doTracking) {
            String colourMode = ObjCollection.ColourModes.PARENT_ID;
            String labelMode = ObjCollection.LabelModes.PARENT_ID;

            colours = spotObjects.getColours(colourMode, trackObjectsName, true);
            labels = showID ? spotObjects.getIDs(labelMode, trackObjectsName, 0, false) : null;

        } else {
            String colourMode = ObjCollection.ColourModes.SINGLE_COLOUR;
            String colourName = ObjCollection.SingleColours.ORANGE;
            String labelMode = ObjCollection.LabelModes.ID;

            colours = spotObjects.getColours(colourMode,colourName,true);
            labels = showID ? spotObjects.getIDs(labelMode,"",0,false) : null;

        }

        // Creating a duplicate of the input image
        ipl = new Duplicator().run(ipl);
        IntensityMinMax.run(ipl,true);

        // Adding the overlay
        ((AddObjectsOverlay) new AddObjectsOverlay()
                .updateParameterValue(AddObjectsOverlay.POSITION_MODE,AddObjectsOverlay.PositionModes.CENTROID)
                .updateParameterValue(AddObjectsOverlay.LABEL_SIZE,8))
                .createOverlay(ipl,spotObjects,colours,labels);

        // Displaying the overlay
        ipl.show();

    }


    @Override
    public String getTitle() {
        return "Run TrackMate";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void run(Workspace workspace) {
        // Loading input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        // Storing, then removing calibration.  This will be reapplied after the detection.
        Calibration calibration = ipl.getCalibration();
        ipl.setCalibration(null);

        // Getting parameters
        String spotObjectsName = parameters.getValue(OUTPUT_SPOT_OBJECTS);
        boolean normaliseIntensity = parameters.getValue(NORMALISE_INTENSITY);
        boolean doTracking = parameters.getValue(DO_TRACKING);
        boolean estimateSize = parameters.getValue(ESTIMATE_SIZE);

        // If image should be normalised
        if (normaliseIntensity) {
            ipl = new Duplicator().run(ipl);
            NormaliseIntensity.normaliseIntensity(ipl);
        }

        // Initialising TrackMate model to store data
        Model model = new Model();
        model.setLogger(Logger.VOID_LOGGER);
        Settings settings = initialiseSettings(ipl,calibration);
        TrackMate trackmate = new TrackMate(model, settings);

        // Resetting ipl to the input image
        ipl = inputImage.getImagePlus();

        ObjCollection spotObjects;
        if (doTracking) {
            writeMessage("Running detection and tracking");
            if (!trackmate.process()) System.err.println(trackmate.getErrorMessage());

            ObjCollection[] spotsAndTracks = getSpotsAndTracks(model,calibration,ipl.getNSlices()==1);
            spotObjects = spotsAndTracks[0];
            ObjCollection trackObjects = spotsAndTracks[1];

            if (estimateSize) estimateSpotSize(spotObjects,ipl);

            // Adding objects to the workspace
            workspace.addObjects(spotObjects);
            workspace.addObjects(trackObjects);

        } else {
            writeMessage("Running detection only");
            if (!trackmate.checkInput()) System.err.println(trackmate.getErrorMessage());
            if (!trackmate.execDetection()) System.err.println(trackmate.getErrorMessage());
            if (!trackmate.computeSpotFeatures(false)) System.err.println(trackmate.getErrorMessage());

            spotObjects = getSpots(model,calibration,ipl.getNSlices()==1);

            if (estimateSize) estimateSpotSize(spotObjects,ipl);

            workspace.addObjects(spotObjects);

        }

        // Displaying objects (if selected)
        if (parameters.getValue(SHOW_OBJECTS)) showObjects(ipl,spotObjects);

        // Reapplying calibration to input image
        inputImage.getImagePlus().setCalibration(calibration);

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(OUTPUT_SPOT_OBJECTS, Parameter.OUTPUT_OBJECTS,""));

        parameters.add(new Parameter(CALIBRATED_UNITS, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(DO_SUBPIXEL_LOCALIZATION, Parameter.BOOLEAN,true));
        parameters.add(new Parameter(DO_MEDIAN_FILTERING, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(RADIUS, Parameter.DOUBLE,2.0));
        parameters.add(new Parameter(THRESHOLD, Parameter.DOUBLE,5000.0));
        parameters.add(new Parameter(NORMALISE_INTENSITY, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(ESTIMATE_SIZE, Parameter.BOOLEAN,false));

        parameters.add(new Parameter(DO_TRACKING, Parameter.BOOLEAN,true));
        parameters.add(new Parameter(LINKING_MAX_DISTANCE, Parameter.DOUBLE,2.0));
        parameters.add(new Parameter(GAP_CLOSING_MAX_DISTANCE, Parameter.DOUBLE,2.0));
        parameters.add(new Parameter(MAX_FRAME_GAP, Parameter.INTEGER,3));

        parameters.add(new Parameter(OUTPUT_TRACK_OBJECTS, Parameter.OUTPUT_OBJECTS,""));

        parameters.add(new Parameter(SHOW_OBJECTS, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(SHOW_ID, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(ID_MODE, Parameter.CHOICE_ARRAY,IDModes.USE_SPOT_ID,IDModes.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_SPOT_OBJECTS));

        returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));
        returnedParameters.add(parameters.getParameter(DO_SUBPIXEL_LOCALIZATION));
        returnedParameters.add(parameters.getParameter(DO_MEDIAN_FILTERING));
        returnedParameters.add(parameters.getParameter(RADIUS));
        returnedParameters.add(parameters.getParameter(THRESHOLD));
        returnedParameters.add(parameters.getParameter(NORMALISE_INTENSITY));
        returnedParameters.add(parameters.getParameter(ESTIMATE_SIZE));

        returnedParameters.add(parameters.getParameter(DO_TRACKING));
        if (parameters.getValue(DO_TRACKING)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_TRACK_OBJECTS));
            returnedParameters.add(parameters.getParameter(LINKING_MAX_DISTANCE));
            returnedParameters.add(parameters.getParameter(GAP_CLOSING_MAX_DISTANCE));
            returnedParameters.add(parameters.getParameter(MAX_FRAME_GAP));
        }

        returnedParameters.add(parameters.getParameter(SHOW_OBJECTS));
        if (parameters.getValue(SHOW_OBJECTS)) {
            returnedParameters.add(parameters.getParameter(SHOW_ID));

            if (parameters.getValue(DO_TRACKING)) {
                if (parameters.getValue(SHOW_ID)) {
                    returnedParameters.add(parameters.getParameter(ID_MODE));

                }
            }
        }

        return returnedParameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        objectMeasurementReferences.setAllCalculated(false);

        String outputSpotObjectsName = parameters.getValue(OUTPUT_SPOT_OBJECTS);

        MeasurementReference reference = objectMeasurementReferences.getOrPut(Measurements.RADIUS_PX);
        reference.setImageObjName(outputSpotObjectsName);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Units.replace(Measurements.RADIUS_CAL));
        reference.setImageObjName(outputSpotObjectsName);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Measurements.ESTIMATED_DIAMETER_PX);
        reference.setImageObjName(outputSpotObjectsName);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Units.replace(Measurements.ESTIMATED_DIAMETER_CAL));
        reference.setImageObjName(outputSpotObjectsName);
        reference.setCalculated(true);

        return objectMeasurementReferences;

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {
        if (parameters.getValue(DO_TRACKING)) {
            relationships.addRelationship(parameters.getValue(OUTPUT_TRACK_OBJECTS), parameters.getValue(OUTPUT_SPOT_OBJECTS));

        }
    }
}

