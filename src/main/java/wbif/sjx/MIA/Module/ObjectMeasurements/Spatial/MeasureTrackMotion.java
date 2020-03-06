package wbif.sjx.MIA.Module.ObjectMeasurements.Spatial;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.common.MathFunc.CumStat;
import wbif.sjx.common.Object.Point;
import wbif.sjx.common.Object.Timepoint;
import wbif.sjx.common.Object.Track;

import java.util.TreeMap;

/**
 * Created by steph on 24/05/2017.
 */
public class MeasureTrackMotion extends Module {
    public static final String INPUT_SEPARATOR = "Input objects";
    public static final String INPUT_TRACK_OBJECTS = "Input track objects";
    public static final String INPUT_SPOT_OBJECTS = "Input spot objects";

    public static final String MEASUREMENT_SEPARATOR = "Measurement controls";
    public static final String SUBTRACT_AVERAGE_MOTION = "Subtract average motion";

    public MeasureTrackMotion(ModuleCollection modules) {
        super("Measure track motion",modules);
    }


    public interface Measurements {
        String DURATION = "DURATION_(FRAMES)";
        String FIRST_FRAME = "FIRST_FRAME";
        String X_VELOCITY_PX = "X_VELOCITY_(PX/FRAME_RANGE)";
        String X_VELOCITY_CAL = "X_VELOCITY_(${CAL}/FRAME_RANGE)";
        String Y_VELOCITY_PX = "Y_VELOCITY_(PX/FRAME_RANGE)";
        String Y_VELOCITY_CAL = "Y_VELOCITY_(${CAL}/FRAME_RANGE)";
        String Z_VELOCITY_SLICES = "Z_VELOCITY_(SLICES/FRAME_RANGE)";
        String Z_VELOCITY_CAL = "Z_VELOCITY_(${CAL}/FRAME_RANGE)";
        String MEAN_X_VELOCITY_PX = "MEAN_X_VELOCITY_(PX/FRAME_RANGE)";
        String MEAN_X_VELOCITY_CAL = "MEAN_X_VELOCITY_(${CAL}/FRAME_RANGE)";
        String MEAN_Y_VELOCITY_PX = "MEAN_Y_VELOCITY_(PX/FRAME_RANGE)";
        String MEAN_Y_VELOCITY_CAL = "MEAN_Y_VELOCITY_(${CAL}/FRAME_RANGE)";
        String MEAN_Z_VELOCITY_SLICES = "MEAN_Z_VELOCITY_(SLICES/FRAME_RANGE)";
        String MEAN_Z_VELOCITY_CAL = "MEAN_Z_VELOCITY_(${CAL}/FRAME_RANGE)";
        String MEAN_INSTANTANEOUS_SPEED_PX = "MEAN_INSTANTANEOUS_SPEED_(PX/FRAME_RANGE)";
        String MEAN_INSTANTANEOUS_SPEED_CAL = "MEAN_INSTANTANEOUS_SPEED_(${CAL}/FRAME_RANGE)";
        String TOTAL_PATH_LENGTH_PX = "TOTAL_PATH_LENGTH_(PX)";
        String TOTAL_PATH_LENGTH_CAL = "TOTAL_PATH_LENGTH_(${CAL})";
        String EUCLIDEAN_DISTANCE_PX = "EUCLIDEAN_DISTANCE_(PX)";
        String EUCLIDEAN_DISTANCE_CAL = "EUCLIDEAN_DISTANCE_(${CAL})";
        String DIRECTIONALITY_RATIO = "DIRECTIONALITY_RATIO";
        String STDEV_ANGULAR_PERSISTENCE = "STDEV_ANGULAR_PERSISTENCE";
        String INSTANTANEOUS_SPEED_PX = "INSTANTANEOUS_SPEED_(PX/FRAME_RANGE)";
        String INSTANTANEOUS_SPEED_CAL = "INSTANTANEOUS_SPEED_(${CAL}/FRAME_RANGE)";
        String CUMULATIVE_PATH_LENGTH_PX = "CUMULATIVE_PATH_LENGTH_(PX)";
        String CUMULATIVE_PATH_LENGTH_CAL = "CUMULATIVE_PATH_LENGTH_(${CAL})";
        String ROLLING_EUCLIDEAN_DISTANCE_PX = "ROLLING_EUCLIDEAN_DISTANCE_(PX)";
        String ROLLING_EUCLIDEAN_DISTANCE_CAL = "ROLLING_EUCLIDEAN_DISTANCE_(${CAL})";
        String ROLLING_DIRECTIONALITY_RATIO = "ROLLING_DIRECTIONALITY_RATIO";
        String ANGULAR_PERSISTENCE = "ANGULAR_PERSISTENCE";
        String DETECTION_FRACTION = "DETECTION_FRACTION";
        String RELATIVE_FRAME = "RELATIVE_FRAME";

    }


    public static String getFullName(String measurement, boolean subtractAverageMotion) {
        if (subtractAverageMotion) return "TRACK_ANALYSIS // (AV_SUB) " + measurement;
        return "TRACK_ANALYSIS // " + measurement;
    }


    public static Track createTrack(Obj trackObject, String spotObjectsName) {
        // Getting the corresponding spots for this track
        Track track = new Track("px");
        for (Obj spotObject : trackObject.getChildren(spotObjectsName).values()) {
            double x = spotObject.getXMean(true);
            double y = spotObject.getYMean(true);
            double z = spotObject.getZMean(true,true);

            int t = spotObject.getT();
            track.addTimepoint(x,y,z,t);
        }

        // Create track object
        return track;

    }

    public static Track createAverageTrack(ObjCollection tracks, String spotObjectsName) {
        TreeMap<Integer,CumStat> x = new TreeMap<>();
        TreeMap<Integer,CumStat> y = new TreeMap<>();
        TreeMap<Integer,CumStat> z = new TreeMap<>();

        for (Obj track:tracks.values()) {
            for (Obj spot:track.getChildren(spotObjectsName).values()) {
                int t = spot.getT();

                // Adding new CumStats to store coordinates at this timepoint if there isn't one already
                x.putIfAbsent(t,new CumStat());
                y.putIfAbsent(t,new CumStat());
                z.putIfAbsent(t,new CumStat());

                // Adding current coordinates
                x.get(t).addMeasure(spot.getXMean(true));
                y.get(t).addMeasure(spot.getYMean(true));
                z.get(t).addMeasure(spot.getZMean(true,true));

            }
        }

        // Creating the average track
        Track averageTrack = new Track("px");
        for (int t:x.keySet()) averageTrack.addTimepoint(x.get(t).getMean(),y.get(t).getMean(),z.get(t).getMean(),t);

        return averageTrack;

    }

    public static void subtractAverageMotion(Track track, Track averageTrack) {
        // Iterating over each frame, subtracting the average motion
        for (int f:track.getF()) {
            Point<Double> point = track.getPointAtFrame(f);
            double x = point.getX();
            double y = point.getY();
            double z = point.getZ();

            double xAv = averageTrack.getX(f);
            double yAv = averageTrack.getY(f);
            double zAv = averageTrack.getZ(f);

            point.setX(x-xAv);
            point.setY(y-yAv);
            point.setZ(z-zAv);

        }
    }

    public static void calculateTemporalMeasurements(Obj trackObject, Track track, boolean averageSubtracted) {
        if (track.size() == 0) {
            String name = getFullName(Measurements.DURATION,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.FIRST_FRAME,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.DETECTION_FRACTION,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));

        } else {
            Timepoint<Double> firstPoint = track.values().iterator().next();

            int duration = track.getDuration();
            String name = getFullName(Measurements.DURATION,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, duration));
            name = getFullName(Measurements.FIRST_FRAME,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, firstPoint.getF()));

            int nSpots = track.values().size();
            double detectionFraction = (double) nSpots / ((double) duration+1);
            name = getFullName(Measurements.DETECTION_FRACTION, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, detectionFraction));
        }
    }

    public static void calculateVelocity(Obj trackObject, Track track, boolean averageSubtracted) {
        if (track.size() <= 1) {
            String name = getFullName(Measurements.MEAN_X_VELOCITY_PX,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.MEAN_X_VELOCITY_CAL,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.MEAN_Y_VELOCITY_PX,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.MEAN_Y_VELOCITY_CAL,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.MEAN_Z_VELOCITY_SLICES,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.MEAN_Z_VELOCITY_CAL,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.MEAN_INSTANTANEOUS_SPEED_PX,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.MEAN_INSTANTANEOUS_SPEED_CAL,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));

        } else {
            // Calculating track motion
            double distPerPxXY = trackObject.getDppXY();
            double distPerPxZ = trackObject.getDppZ();

            TreeMap<Integer, Double> xVelocity = track.getInstantaneousXVelocity();
            TreeMap<Integer, Double> yVelocity = track.getInstantaneousYVelocity();
            TreeMap<Integer, Double> zVelocity = track.getInstantaneousZVelocity();
            TreeMap<Integer, Double> speed = track.getInstantaneousSpeed();

            CumStat cumStatX = new CumStat();
            CumStat cumStatY = new CumStat();
            CumStat cumStatZ = new CumStat();
            CumStat cumStatSpeed = new CumStat();

            for (int frame:xVelocity.keySet()) {
                // The first value is set to zero
                if (frame == 0) continue;
                cumStatX.addMeasure(xVelocity.get(frame));
                cumStatY.addMeasure(yVelocity.get(frame));
                cumStatZ.addMeasure(zVelocity.get(frame));
                cumStatSpeed.addMeasure(speed.get(frame));
            }

            String name = getFullName(Measurements.MEAN_X_VELOCITY_PX,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, cumStatX.getMean()));
            name = getFullName(Measurements.MEAN_X_VELOCITY_CAL,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, cumStatX.getMean() * distPerPxXY));
            name = getFullName(Measurements.MEAN_Y_VELOCITY_PX,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, cumStatY.getMean()));
            name = getFullName(Measurements.MEAN_Y_VELOCITY_CAL,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, cumStatY.getMean() * distPerPxXY));
            name = getFullName(Measurements.MEAN_Z_VELOCITY_SLICES,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, cumStatZ.getMean() * distPerPxXY/distPerPxZ));
            name = getFullName(Measurements.MEAN_Z_VELOCITY_CAL,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, cumStatZ.getMean() * distPerPxXY));
            name = getFullName(Measurements.MEAN_Z_VELOCITY_SLICES,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, cumStatZ.getMean() * distPerPxXY/distPerPxZ));
            name = getFullName(Measurements.MEAN_Z_VELOCITY_CAL,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, cumStatZ.getMean() * distPerPxXY));
            name = getFullName(Measurements.MEAN_INSTANTANEOUS_SPEED_PX,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, cumStatSpeed.getMean()));
            name = getFullName(Measurements.MEAN_INSTANTANEOUS_SPEED_CAL,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, cumStatSpeed.getMean() * distPerPxXY));

        }
    }

    public static void calculateRelativeTimepoint(Obj trackObject, Track track, String inputSpotObjectsName, boolean averageSubtracted) {
        if (track.size() == 0) return;

        int firstTimepoint = track.values().iterator().next().getF();

        for (Obj spotObject : trackObject.getChildren(inputSpotObjectsName).values()) {
            int currentTimepoint = spotObject.getT();
            String name = getFullName(Measurements.RELATIVE_FRAME, averageSubtracted);
            spotObject.addMeasurement(new Measurement(name, currentTimepoint-firstTimepoint));

        }
    }

    public static void calculateSpatialMeasurements(Obj trackObject, Track track, boolean averageSubtracted) {
        if (track.size() == 0) {
            // Adding measurements to track objects
            String name = getFullName(Measurements.EUCLIDEAN_DISTANCE_PX,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.EUCLIDEAN_DISTANCE_CAL,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.TOTAL_PATH_LENGTH_PX,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.TOTAL_PATH_LENGTH_CAL,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.DIRECTIONALITY_RATIO,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));

        } else {
            // If the track has a single time-point there's no velocity to measure
            double distPerPxXY = trackObject.getDppXY();
            double euclideanDistance = track.getEuclideanDistance();
            double totalPathLength = track.getTotalPathLength();

            String name = getFullName(Measurements.EUCLIDEAN_DISTANCE_PX,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, euclideanDistance));
            name = getFullName(Measurements.EUCLIDEAN_DISTANCE_CAL,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, euclideanDistance*distPerPxXY));
            name = getFullName(Measurements.TOTAL_PATH_LENGTH_PX,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, totalPathLength));
            name = getFullName(Measurements.TOTAL_PATH_LENGTH_CAL,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, totalPathLength*distPerPxXY));
            name = getFullName(Measurements.DIRECTIONALITY_RATIO,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, track.getDirectionalityRatio()));

        }
    }

    public static void calculateInstantaneousVelocity(Obj trackObject, Track track, String inputSpotObjectsName, boolean averageSubtracted) {
        double distPerPxXY = trackObject.getDppXY();
        double distPerPxZ = trackObject.getDppZ();

        TreeMap<Integer, Double> xVelocity = track.getInstantaneousXVelocity();
        TreeMap<Integer, Double> yVelocity = track.getInstantaneousYVelocity();
        TreeMap<Integer, Double> zVelocity = track.getInstantaneousZVelocity();
        TreeMap<Integer, Double> speed = track.getInstantaneousSpeed();

        // Getting the first timepoint
        int minT = Integer.MAX_VALUE;
        for (Obj spotObject : trackObject.getChildren(inputSpotObjectsName).values()) {
            minT = Math.min(minT, spotObject.getT());
        }

        for (Obj spotObject : trackObject.getChildren(inputSpotObjectsName).values()) {
            int t = spotObject.getT();

            // For the first time-point set certain velocity measurements to Double.NaN (rather than zero)
            if (t == minT) {
                String name = getFullName(Measurements.X_VELOCITY_PX,averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, Double.NaN));
                name = getFullName(Measurements.X_VELOCITY_CAL,averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, Double.NaN));
                name = getFullName(Measurements.Y_VELOCITY_PX,averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, Double.NaN));
                name = getFullName(Measurements.Y_VELOCITY_CAL,averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, Double.NaN));
                name = getFullName(Measurements.Z_VELOCITY_SLICES,averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, Double.NaN));
                name = getFullName(Measurements.Z_VELOCITY_CAL,averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, Double.NaN));
                name = getFullName(Measurements.INSTANTANEOUS_SPEED_PX,averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, Double.NaN));
                name = getFullName(Measurements.INSTANTANEOUS_SPEED_CAL,averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, Double.NaN));

            } else {
                String name = getFullName(Measurements.X_VELOCITY_PX,averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, xVelocity.get(t)));
                name = getFullName(Measurements.X_VELOCITY_CAL,averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, xVelocity.get(t) * distPerPxXY));
                name = getFullName(Measurements.Y_VELOCITY_PX,averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, yVelocity.get(t)));
                name = getFullName(Measurements.Y_VELOCITY_CAL,averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, yVelocity.get(t) * distPerPxXY));
                name = getFullName(Measurements.Z_VELOCITY_SLICES,averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, zVelocity.get(t) * distPerPxXY/distPerPxZ));
                name = getFullName(Measurements.Z_VELOCITY_CAL,averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, zVelocity.get(t) * distPerPxXY));
                name = getFullName(Measurements.INSTANTANEOUS_SPEED_PX,averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, speed.get(t)));
                name = getFullName(Measurements.INSTANTANEOUS_SPEED_CAL,averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, speed.get(t) * distPerPxXY));

            }
        }
    }

    public static void calculateInstantaneousSpatialMeasurements(Obj trackObject, Track track, String inputSpotObjectsName, boolean averageSubtracted) {
        double distPerPxXY = trackObject.getDppXY();

        // Calculating rolling values
        TreeMap<Integer, Double> pathLength = track.getRollingTotalPathLength();
        TreeMap<Integer, Double> euclidean = track.getRollingEuclideanDistance();
        TreeMap<Integer, Double> dirRatio = track.getRollingDirectionalityRatio();
        TreeMap<Integer, Double> angularPersistence = track.getAngularPersistence();

        // Applying the relevant measurement to each spot
        for (Obj spotObject : trackObject.getChildren(inputSpotObjectsName).values()) {
            int t = spotObject.getT();

            // The remaining measurements are unaffected by whether it's the first time-point
            String name = getFullName(Measurements.CUMULATIVE_PATH_LENGTH_PX,averageSubtracted);
            spotObject.addMeasurement(new Measurement(name, pathLength.get(t)));
            name = getFullName(Measurements.CUMULATIVE_PATH_LENGTH_CAL,averageSubtracted);
            spotObject.addMeasurement(new Measurement(name, pathLength.get(t)*distPerPxXY));
            name = getFullName(Measurements.ROLLING_EUCLIDEAN_DISTANCE_PX,averageSubtracted);
            spotObject.addMeasurement(new Measurement(name, euclidean.get(t)));
            name = getFullName(Measurements.ROLLING_EUCLIDEAN_DISTANCE_CAL,averageSubtracted);
            spotObject.addMeasurement(new Measurement(name, euclidean.get(t)*distPerPxXY));
            name = getFullName(Measurements.ROLLING_DIRECTIONALITY_RATIO,averageSubtracted);
            spotObject.addMeasurement(new Measurement(name, dirRatio.get(t)));
            name = getFullName(Measurements.ANGULAR_PERSISTENCE,averageSubtracted);
            spotObject.addMeasurement(new Measurement(name, angularPersistence.get(t)));

        }
    }


    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_SPATIAL;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting input track objects
        String inputTrackObjectsName = parameters.getValue(INPUT_TRACK_OBJECTS);
        ObjCollection trackObjects = workspace.getObjects().get(inputTrackObjectsName);

        // Getting input spot objects
        String inputSpotObjectsName = parameters.getValue(INPUT_SPOT_OBJECTS);
        boolean subtractAverage = parameters.getValue(SUBTRACT_AVERAGE_MOTION);

        // If necessary, creating the average track
        Track averageTrack = null;
        if (subtractAverage) averageTrack = createAverageTrack(trackObjects,inputSpotObjectsName);

        // Converting objects to Track class object
        for (Obj trackObject:trackObjects.values()) {
            Track track = createTrack(trackObject,inputSpotObjectsName);

            // If necessary, applying the motion correction to the object
            if (subtractAverage) subtractAverageMotion(track,averageTrack);

            // Calculating the measurements
            calculateTemporalMeasurements(trackObject,track,subtractAverage);
            calculateVelocity(trackObject,track,subtractAverage);
            calculateSpatialMeasurements(trackObject,track,subtractAverage);
            calculateInstantaneousVelocity(trackObject,track,inputSpotObjectsName,subtractAverage);
            calculateInstantaneousSpatialMeasurements(trackObject,track,inputSpotObjectsName,subtractAverage);
            calculateRelativeTimepoint(trackObject,track,inputSpotObjectsName,subtractAverage);

        }

        if (showOutput) workspace.getObjectSet(inputSpotObjectsName).showMeasurements(this,modules);
        if (showOutput) trackObjects.showMeasurements(this,modules);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputTrackObjectsP(INPUT_TRACK_OBJECTS,this));
        parameters.add(new ChildObjectsP(INPUT_SPOT_OBJECTS,this));

        parameters.add(new ParamSeparatorP(MEASUREMENT_SEPARATOR,this));
        parameters.add(new BooleanP(SUBTRACT_AVERAGE_MOTION,this,false));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_TRACK_OBJECTS));
        returnedParameters.add(parameters.getParameter(INPUT_SPOT_OBJECTS));

        String objectName = parameters.getValue(INPUT_TRACK_OBJECTS);
        ((ChildObjectsP) parameters.getParameter(INPUT_SPOT_OBJECTS)).setParentObjectsName(objectName);

        returnedParameters.add(parameters.getParameter(MEASUREMENT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SUBTRACT_AVERAGE_MOTION));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefCollection returnedRefs = new ObjMeasurementRefCollection();
        String inputTrackObjects = parameters.getValue(INPUT_TRACK_OBJECTS);
        String inputSpotObjects  = parameters.getValue(INPUT_SPOT_OBJECTS);
        boolean subtractAverage = parameters.getValue(SUBTRACT_AVERAGE_MOTION);

        String name = getFullName(Measurements.DIRECTIONALITY_RATIO,subtractAverage);
        ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputTrackObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.EUCLIDEAN_DISTANCE_PX,subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputTrackObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.EUCLIDEAN_DISTANCE_CAL,subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputTrackObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.TOTAL_PATH_LENGTH_PX,subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputTrackObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.TOTAL_PATH_LENGTH_CAL,subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputTrackObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.DURATION,subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputTrackObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.FIRST_FRAME,subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputTrackObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.MEAN_X_VELOCITY_PX,subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputTrackObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.MEAN_X_VELOCITY_CAL,subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputTrackObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.MEAN_Y_VELOCITY_PX,subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputTrackObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.MEAN_Y_VELOCITY_CAL,subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputTrackObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.MEAN_Z_VELOCITY_SLICES,subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputTrackObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.MEAN_Z_VELOCITY_CAL,subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputTrackObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.MEAN_INSTANTANEOUS_SPEED_PX,subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputTrackObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.MEAN_INSTANTANEOUS_SPEED_CAL,subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputTrackObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.DETECTION_FRACTION,subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputTrackObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.X_VELOCITY_PX,subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputSpotObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.X_VELOCITY_CAL,subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputSpotObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.Y_VELOCITY_PX,subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputSpotObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.Y_VELOCITY_CAL,subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputSpotObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.Z_VELOCITY_SLICES,subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputSpotObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.Z_VELOCITY_CAL,subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputSpotObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.INSTANTANEOUS_SPEED_PX,subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputSpotObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.INSTANTANEOUS_SPEED_CAL,subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputSpotObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.CUMULATIVE_PATH_LENGTH_PX,subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputSpotObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.CUMULATIVE_PATH_LENGTH_CAL,subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputSpotObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.ROLLING_EUCLIDEAN_DISTANCE_PX,subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputSpotObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.ROLLING_EUCLIDEAN_DISTANCE_CAL,subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputSpotObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.ROLLING_DIRECTIONALITY_RATIO,subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputSpotObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.ANGULAR_PERSISTENCE,subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputSpotObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.RELATIVE_FRAME,subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputSpotObjects);
        returnedRefs.add(reference);

        return returnedRefs;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}
