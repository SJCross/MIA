// TODO: See how TrackMate behaves with different channels.  Currently always assigns channel to "0".

package wbif.sjx.MIA.Module.ObjectProcessing.Identification;

import fiji.plugin.trackmate.*;
import fiji.plugin.trackmate.detection.DetectorKeys;
import fiji.plugin.trackmate.detection.LogDetectorFactory;
import fiji.plugin.trackmate.features.spot.SpotRadiusEstimatorFactory;
import fiji.plugin.trackmate.tracking.LAPUtils;
import fiji.plugin.trackmate.tracking.TrackerKeys;
import fiji.plugin.trackmate.tracking.kalman.KalmanTrackerFactory;
import fiji.plugin.trackmate.tracking.sparselap.SparseLAPTrackerFactory;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.Visualisation.Overlays.AddObjectCentroid;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.MIA.Process.ColourFactory;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.Object.Volume.PointOutOfRangeException;
import wbif.sjx.common.Object.Volume.VolumeType;
import wbif.sjx.common.Process.IntensityMinMax;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by sc13967 on 15/05/2017.
 */
public class RunTrackMate extends Module {
    public static final String INPUT_SEPARATOR = "Image input, object output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_SPOT_OBJECTS = "Output spot objects";

    public static final String SPOT_SEPARATOR = "Spot detection";
    public static final String CALIBRATED_UNITS = "Calibrated units";
    public static final String DO_SUBPIXEL_LOCALIZATION = "Do sub-pixel localisation";
    public static final String DO_MEDIAN_FILTERING = "Median filtering";
    public static final String RADIUS = "Radius";
    public static final String THRESHOLD = "Threshold";
    public static final String ESTIMATE_SIZE = "Estimate spot size";

    public static final String TRACK_SEPARATOR = "Spot tracking";
    public static final String DO_TRACKING = "Run tracking";
    public static final String TRACKING_METHOD = "Tracking method";
    public static final String OUTPUT_TRACK_OBJECTS = "Output track objects";
    public static final String LINKING_MAX_DISTANCE = "Max linking distance";
    public static final String INITIAL_SEARCH_RADIUS = "Initial search radius";
    public static final String SEARCH_RADIUS = "Search radius";
    public static final String GAP_CLOSING_MAX_DISTANCE = "Gap closing max distance";
    public static final String MAX_FRAME_GAP = "Max frame gap";


    public interface TrackingMethods {
        String KALMAN = "Linear motion (Kalman)";
        String SIMPLE = "Simple";

        String[] ALL = new String[]{KALMAN,SIMPLE};

    }

    public interface Measurements {
        String RADIUS_PX = "SPOT_DETECT_TRACK // RADIUS_(PX)";
        String RADIUS_CAL = "SPOT_DETECT_TRACK // RADIUS_(${CAL})";
        String ESTIMATED_DIAMETER_PX = "SPOT_DETECT_TRACK // EST_DIAMETER_(PX)";
        String ESTIMATED_DIAMETER_CAL = "SPOT_DETECT_TRACK // EST_DIAMETER_(${CAL})";

    }


    public RunTrackMate(ModuleCollection modules) {
        super("Run TrackMate",modules);
    }

    public Settings initialiseSettings(ImagePlus ipl, Calibration calibration) {
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS);
        boolean subpixelLocalisation = parameters.getValue(DO_SUBPIXEL_LOCALIZATION);
        boolean medianFiltering = parameters.getValue(DO_MEDIAN_FILTERING);
        double radius = parameters.getValue(RADIUS);
        double threshold = parameters.getValue(THRESHOLD);
        String trackingMethod = parameters.getValue(TRACKING_METHOD);
        double maxLinkDist = parameters.getValue(LINKING_MAX_DISTANCE);
        double maxGapDist = parameters.getValue(GAP_CLOSING_MAX_DISTANCE);
        double initialSearchRadius = parameters.getValue(INITIAL_SEARCH_RADIUS);
        double searchRadius = parameters.getValue(SEARCH_RADIUS);
        int maxFrameGap = parameters.getValue(MAX_FRAME_GAP);

        // Applying conversion to parameters
        if (calibratedUnits) {
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

        switch (trackingMethod) {
            case TrackingMethods.KALMAN:
                settings.trackerFactory = new KalmanTrackerFactory();
                settings.trackerSettings = LAPUtils.getDefaultLAPSettingsMap();
                settings.trackerSettings.put(TrackerKeys.KEY_LINKING_MAX_DISTANCE, initialSearchRadius);
                settings.trackerSettings.put(TrackerKeys.KEY_GAP_CLOSING_MAX_FRAME_GAP,maxFrameGap);
                settings.trackerSettings.put(KalmanTrackerFactory.KEY_KALMAN_SEARCH_RADIUS,searchRadius);
                break;
            case TrackingMethods.SIMPLE:
                settings.trackerFactory  = new SparseLAPTrackerFactory();
                settings.trackerSettings = LAPUtils.getDefaultLAPSettingsMap();
                settings.trackerSettings.put(TrackerKeys.KEY_ALLOW_TRACK_SPLITTING, false);
                settings.trackerSettings.put(TrackerKeys.KEY_ALLOW_TRACK_MERGING, false);
                settings.trackerSettings.put(TrackerKeys.KEY_LINKING_MAX_DISTANCE, maxLinkDist);
                settings.trackerSettings.put(TrackerKeys.KEY_GAP_CLOSING_MAX_DISTANCE, maxGapDist);
                settings.trackerSettings.put(TrackerKeys.KEY_GAP_CLOSING_MAX_FRAME_GAP,maxFrameGap);
                break;
        }

        return settings;

    }

    public ObjCollection getSpots(Model model, Calibration calibration, int width, int height, int nSlices) throws IntegerOverflowException {
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
            Obj spotObject = new Obj(VolumeType.POINTLIST,spotObjectsName,spot.ID(),width,height,nSlices,dppXY,dppZ,calibrationUnits);
            try {
                spotObject.add((int) spot.getDoublePosition(0),(int) spot.getDoublePosition(1),(int) spot.getDoublePosition(2));
            } catch (PointOutOfRangeException e) {
                e.printStackTrace();
            }
            spotObject.setT((int) Math.round(spot.getFeature(Spot.FRAME)));

            spotObject.addMeasurement(new Measurement(Measurements.RADIUS_PX,spot.getFeature(Spot.RADIUS)));
            spotObject.addMeasurement(new Measurement(Measurements.RADIUS_CAL,spot.getFeature(Spot.RADIUS)*dppXY));
            spotObject.addMeasurement(new Measurement(Measurements.ESTIMATED_DIAMETER_PX,spot.getFeature(SpotRadiusEstimatorFactory.ESTIMATED_DIAMETER)));
            spotObject.addMeasurement(new Measurement(Measurements.ESTIMATED_DIAMETER_CAL,spot.getFeature(SpotRadiusEstimatorFactory.ESTIMATED_DIAMETER)*dppXY));

            spotObjects.add(spotObject);

        }

        // Adding spotObjects to the workspace
        writeMessage(spots.getNSpots(false)+" trackObjects detected");

        return spotObjects;

    }

    public ObjCollection[] getSpotsAndTracks(Model model, Calibration calibration, int width, int height, int nSlices) throws IntegerOverflowException {
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
            Obj trackObject = new Obj(VolumeType.POINTLIST,trackObjectsName, trackID, width,height,nSlices,dppXY, dppZ, calibrationUnits);
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
                Obj spotObject = new Obj(spotObjectsName, spotObjects.getAndIncrementID(),trackObject);

                spotObject.addMeasurement(new Measurement(Measurements.RADIUS_PX,spot.getFeature(Spot.RADIUS)));
                spotObject.addMeasurement(new Measurement(Measurements.RADIUS_CAL,spot.getFeature(Spot.RADIUS)*dppXY));
                spotObject.addMeasurement(new Measurement(Measurements.ESTIMATED_DIAMETER_PX,spot.getFeature(SpotRadiusEstimatorFactory.ESTIMATED_DIAMETER)));
                spotObject.addMeasurement(new Measurement(Measurements.ESTIMATED_DIAMETER_CAL,spot.getFeature(SpotRadiusEstimatorFactory.ESTIMATED_DIAMETER)*dppXY));

                // Getting coordinates
                int x = (int) Math.round(spot.getDoublePosition(0));
                int y = (int) Math.round(spot.getDoublePosition(1));
                int z = (int) Math.round(spot.getDoublePosition(2) * dppZ / dppXY);
                int t = (int) Math.round(spot.getFeature(Spot.FRAME));

                // Adding coordinates to the instance objects
                try {
                    spotObject.add(x, y, z);
                } catch (PointOutOfRangeException e) {
                    continue;
                }
                spotObject.setT(t);

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

    public void estimateSpotSize(ObjCollection spotObjects, ImagePlus ipl) throws IntegerOverflowException {
        // Replacing spot volumes with explicit volume
        for (Obj spotObject:spotObjects.values()) {
            double radius = spotObject.getMeasurement(Measurements.RADIUS_PX).getValue();
            Obj volumeObject = GetLocalObjectRegion.getLocalRegion(spotObject,"SpotVolume",radius,false,false);
            spotObject.setCoordinateSet(volumeObject.getCoordinateSet());
            spotObject.clearSurface();
            spotObject.clearCentroid();
            spotObject.clearProjected();
        }
    }

    public void showObjects(ImagePlus ipl, ObjCollection spotObjects) {
        String trackObjectsName = parameters.getValue(OUTPUT_TRACK_OBJECTS);
        boolean doTracking = parameters.getValue(DO_TRACKING);

        HashMap<Integer, Float> hues;
        // Colours will depend on the detection/tracking mode
        if (doTracking) {
            hues = ColourFactory.getParentIDHues(spotObjects,trackObjectsName,true);
        } else {
            hues = ColourFactory.getSingleColourHues(spotObjects,ColourFactory.SingleColours.ORANGE);
        }

        String pointSize = AddObjectCentroid.PointSizes.SMALL;
        String pointType = AddObjectCentroid.PointTypes.CIRCLE;

        // Creating a duplicate of the input image
        ipl = new Duplicator().run(ipl);
        IntensityMinMax.run(ipl,true);

        // Adding the overlay
        AddObjectCentroid.addOverlay(ipl,spotObjects,hues,100,pointSize,pointType,false,true);
        ipl.setPosition(1,1,1);
        ipl.updateChannelAndDraw();

        // Displaying the overlay
        ipl.show();

    }


    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_IDENTIFICATION;
    }

    @Override
    public String getDescription() {
        return "Uses the TrackMate plugin included with Fiji to detect and track spots in images." +
                "<br><br>For more on TrackMate, go to https://imagej.net/TrackMate";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Loading input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        // Storing, then removing calibration.  This will be reapplied after the detection.
        Calibration calibration = ipl.getCalibration();
        ipl.setCalibration(null);
        int width = ipl.getWidth();
        int height = ipl.getHeight();
        int nSlices = ipl.getNSlices();

        // Getting parameters
        String spotObjectsName = parameters.getValue(OUTPUT_SPOT_OBJECTS);
        boolean doTracking = parameters.getValue(DO_TRACKING);
        boolean estimateSize = parameters.getValue(ESTIMATE_SIZE);

        // Initialising TrackMate model to store data
        Model model = new Model();
        model.setLogger(Logger.VOID_LOGGER);
        Settings settings = initialiseSettings(ipl,calibration);
        TrackMate trackmate = new TrackMate(model, settings);

        // Resetting ipl to the input image
        ipl = inputImage.getImagePlus();

        ObjCollection spotObjects;
        try {
            if (doTracking) {
                writeMessage("Running detection and tracking");
                if (!trackmate.process()) MIA.log.writeError(trackmate.getErrorMessage());

                ObjCollection[] spotsAndTracks = getSpotsAndTracks(model, calibration,width,height,nSlices);
                spotObjects = spotsAndTracks[0];
                ObjCollection trackObjects = spotsAndTracks[1];

                if (estimateSize) estimateSpotSize(spotObjects, ipl);

                // Adding objects to the workspace
                workspace.addObjects(spotObjects);
                workspace.addObjects(trackObjects);

            } else {
                writeMessage("Running detection only");
                if (!trackmate.checkInput()) MIA.log.writeError(trackmate.getErrorMessage());
                if (!trackmate.execDetection()) MIA.log.writeError(trackmate.getErrorMessage());
                if (!trackmate.computeSpotFeatures(false)) MIA.log.writeError(trackmate.getErrorMessage());

                spotObjects = getSpots(model,calibration,width,height,nSlices);

                if (estimateSize) estimateSpotSize(spotObjects, ipl);

                workspace.addObjects(spotObjects);

            }
        } catch (IntegerOverflowException e) {
            return false;
        }

        // Displaying objects (if selected)
        if (showOutput) showObjects(ipl,spotObjects);

        // Reapplying calibration to input image
        inputImage.getImagePlus().setCalibration(calibration);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this, "", "Image in which to detect spots."));
        parameters.add(new OutputObjectsP(OUTPUT_SPOT_OBJECTS, this, "", "Spot objects that will be added to the workspace.  If tracking is enabled, each spot will have a parent track object."));

        parameters.add(new ParamSeparatorP(SPOT_SEPARATOR, this));
        parameters.add(new BooleanP(CALIBRATED_UNITS, this, false, "Enable if spatial parameters (e.g. \""+RADIUS+"\" or \""+LINKING_MAX_DISTANCE+"\") are being specified in calibrated units.  If disabled, parameters are assumed to be specified in pixel units."));
        parameters.add(new BooleanP(DO_SUBPIXEL_LOCALIZATION, this,true, "Enable TrackMate's \"Subpixel localisation\" functionality."));
        parameters.add(new BooleanP(DO_MEDIAN_FILTERING, this,false, "Enable TrackMate's \"Median filtering\" functionality."));
        parameters.add(new DoubleP(RADIUS, this,2.0, "Expected radius of spots in the input image.  Specified in pixel units, unless \""+CALIBRATED_UNITS+"\" is selected."));
        parameters.add(new DoubleP(THRESHOLD, this,10.0,"Threshold for spot detection.  Threshold is applied to filtered image (Laplacian of Gaussian), so will be affected by the specified \""+RADIUS+"\" value.  Increase this value to make detection more selective (i.e. detect fewer spots)."));
        parameters.add(new BooleanP(ESTIMATE_SIZE, this,false,"When enabled, output spot objects will have explicit size (rather than a single, centroid coordinate) determined by the TrackMate-calculated estimated diameter."));

        parameters.add(new ParamSeparatorP(TRACK_SEPARATOR, this));
        parameters.add(new BooleanP(DO_TRACKING, this,true, "Track spot objects over time.  Spots in each frame will become children of a parent track object.  The track object itself won't contain any coordinate information."));
        parameters.add(new ChoiceP(TRACKING_METHOD,this, TrackingMethods.SIMPLE, TrackingMethods.ALL, "Method with which spots are tracked between frames<br>" +
                "<br>- \""+ TrackingMethods.KALMAN+"\" uses the previous position of a spot and its current velocity to estimate where the spot will be in the next frame. These predicted spots are linked to the spots in the current frame.  When dealing with particles moving at roughly constant speeds, this method should be more accurate.<br>" +
                "<br>- \""+ TrackingMethods.SIMPLE+"\" (default) calculates links between spot positions in the previous and current frames.  This does not take motion into account.<br>"));
        parameters.add(new DoubleP(LINKING_MAX_DISTANCE, this,10.0, "Maximum distance a spot can travel between frames and still be linked to its starting spot.  Specified in pixel units, unless \""+CALIBRATED_UNITS+"\" is selected."));
        parameters.add(new DoubleP(GAP_CLOSING_MAX_DISTANCE, this,10.0, "Maximum distance a spot can travel between \""+MAX_FRAME_GAP+"\" frames and still be linked to its starting spot.  This accounts for the greater distance a spot can move between detections when it's allowed to go undetected in some timepoints.  Specified in pixel units, unless \""+CALIBRATED_UNITS+"\" is selected."));
        parameters.add(new DoubleP(INITIAL_SEARCH_RADIUS,this,10.0, "Minimum spot separation required for creation of a new track."));
        parameters.add(new DoubleP(SEARCH_RADIUS,this,10.0, "Maximum distance between predicted spot location and location of spot in current frame."));
        parameters.add(new IntegerP(MAX_FRAME_GAP, this,3, "Maximum number of frames a spot can go undetected before it will be classed as a new track upon reappearance."));
        parameters.add(new OutputTrackObjectP(OUTPUT_TRACK_OBJECTS, this, "", "Track objects that will be added to the workspace.  These are parent objects to the spots in that track.  Track objects are simply used for linking spots to a common track and storing track-specific measurements."));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_SPOT_OBJECTS));

        returnedParameters.add(parameters.getParameter(SPOT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));
        returnedParameters.add(parameters.getParameter(DO_SUBPIXEL_LOCALIZATION));
        returnedParameters.add(parameters.getParameter(DO_MEDIAN_FILTERING));
        returnedParameters.add(parameters.getParameter(RADIUS));
        returnedParameters.add(parameters.getParameter(THRESHOLD));
        returnedParameters.add(parameters.getParameter(ESTIMATE_SIZE));

        returnedParameters.add(parameters.getParameter(TRACK_SEPARATOR));
        returnedParameters.add(parameters.getParameter(DO_TRACKING));
        if (parameters.getValue(DO_TRACKING)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_TRACK_OBJECTS));
            returnedParameters.add(parameters.getParameter(TRACKING_METHOD));
            switch ((String) parameters.getValue(TRACKING_METHOD)) {
                case TrackingMethods.KALMAN:
                    returnedParameters.add(parameters.get(INITIAL_SEARCH_RADIUS));
                    returnedParameters.add(parameters.get(SEARCH_RADIUS));
                    break;
                case TrackingMethods.SIMPLE:
                    returnedParameters.add(parameters.get(LINKING_MAX_DISTANCE));
                    returnedParameters.add(parameters.get(GAP_CLOSING_MAX_DISTANCE));
                    break;
            }
            returnedParameters.add(parameters.getParameter(MAX_FRAME_GAP));
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefCollection returnedRefs = new ObjMeasurementRefCollection();
        String outputSpotObjectsName = parameters.getValue(OUTPUT_SPOT_OBJECTS);

        ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(Measurements.RADIUS_PX);
        reference.setObjectsName(outputSpotObjectsName);
        reference.setDescription("Radius used as size estimate for spot detection.  Measured in pixel units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.RADIUS_CAL);
        reference.setObjectsName(outputSpotObjectsName);
        reference.setDescription("Radius used as size estimate for spot detection.  Measured in calibrated " +
                "("+Units.getOMEUnits().getSymbol()+") units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.ESTIMATED_DIAMETER_PX);
        reference.setObjectsName(outputSpotObjectsName);
        reference.setDescription("Diameter of spot as estimated by TrackMate.  Measured in pixel units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.ESTIMATED_DIAMETER_CAL);
        reference.setObjectsName(outputSpotObjectsName);
        reference.setDescription("Diameter of spots as estimated by TrackMate.  Measured in calibrated " +
                "("+Units.getOMEUnits().getSymbol()+") units.");
        returnedRefs.add(reference);

        return returnedRefs;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipRefCollection updateAndGetRelationships() {
        RelationshipRefCollection returnedRelationships = new RelationshipRefCollection();

        if (parameters.getValue(DO_TRACKING)) {
            returnedRelationships.add(relationshipRefs.getOrPut(parameters.getValue(OUTPUT_TRACK_OBJECTS), parameters.getValue(OUTPUT_SPOT_OBJECTS)));

        }

        return returnedRelationships;

    }

    @Override
    public boolean verify() {
        return true;
    }
}

