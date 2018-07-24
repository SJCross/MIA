//TODO: Implement Ricci, et al. (Cell, 2015) clustering method using density map maxima for initial centroid guesses.

// Calculates clusters using the DBSCAN algorithm.  This has the advantage over K-means clustering that it doesn't
// require the number of clusters to be known in advance.  Ricci, et al. (Cell, 2015) calculate a density map for the
// image, then use the local maxima as starting points for the cluster centroids.

package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Refinement;

import org.apache.commons.math3.ml.clustering.*;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MathFunc.CumStat;
import wbif.sjx.common.MathFunc.Indexer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by sc13967 on 21/06/2017.
 */
public class ObjectClusterer extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String CLUSTER_OBJECTS = "Cluster (parent) objects";
    public static final String CLUSTERING_ALGORITHM = "Clustering algorithm";
    public static final String K_CLUSTERS = "Number of clusters";
    public static final String MAX_ITERATIONS = "Maximum number of iterations";
    public static final String EPS = "Neighbourhood for clustering (epsilon)";
    public static final String MIN_POINTS = "Minimum number of points per cluster";

    public interface ClusteringAlgorithms {
        String KMEANSPLUSPLUS = "KMeans++";
        String DBSCAN = "DBSCAN";

        String[] ALL = new String[]{KMEANSPLUSPLUS, DBSCAN};

    }

    public interface Measurements {
        String N_POINTS_IN_CLUSTER = "CLUSTER // N_POINTS_IN_CLUSTER";
        String CLUSTER_AREA_XY_PX = "CLUSTER // CLUSTER_AREA_2D_(PX^2)";
        String CLUSTER_AREA_XY_CAL = "CLUSTER // CLUSTER_AREA_2D_(${CAL}^2)";

    }


    private ObjCollection runKMeansPlusPlus(ObjCollection outputObjects, List<LocationWrapper> locations, double dppXY, double dppZ, String calibratedUnits, boolean is2D) {
        String outputObjectsName = parameters.getValue(CLUSTER_OBJECTS);
        int kClusters = parameters.getValue(K_CLUSTERS);
        int maxIterations = parameters.getValue(MAX_ITERATIONS);

        KMeansPlusPlusClusterer<LocationWrapper> clusterer = new KMeansPlusPlusClusterer<>(kClusters,maxIterations);
        List<CentroidCluster<LocationWrapper>> clusters = clusterer.cluster(locations);

        // Assigning relationships between points and clusters
        for (CentroidCluster<LocationWrapper> cluster:clusters) {
            Obj outputObject = new Obj(outputObjectsName,outputObjects.getNextID(),dppXY,dppZ,calibratedUnits,is2D);

            double[] centroid = cluster.getCenter().getPoint();
            outputObject.addCoord((int) Math.round(centroid[0]),(int) Math.round(centroid[1]),(int) Math.round(centroid[2]));

            for (LocationWrapper point:cluster.getPoints()) {
                Obj pointObject = point.getObject();
                pointObject.addParent(outputObject);
                outputObject.addChild(pointObject);
            }

            outputObjects.add(outputObject);

        }

        return outputObjects;

    }

    private ObjCollection runDBSCAN(ObjCollection outputObjects, List<LocationWrapper> locations, double dppXY, double dppZ, String calibratedUnits, boolean is2D) {
        String outputObjectsName = parameters.getValue(CLUSTER_OBJECTS);
        double eps = parameters.getValue(EPS);
        int minPoints = parameters.getValue(MIN_POINTS);

        DBSCANClusterer<LocationWrapper> clusterer = new DBSCANClusterer<>(eps, minPoints);
        List<Cluster<LocationWrapper>> clusters = clusterer.cluster(locations);

        // Assigning relationships between points and clusters
        for (Cluster<LocationWrapper> cluster:clusters) {
            Obj outputObject = new Obj(outputObjectsName,outputObjects.getNextID(),dppXY,dppZ,calibratedUnits,is2D);

            // Calculating the centroid (DBSCAN doesn't give one)
            CumStat[] cs = new CumStat[]{new CumStat(), new CumStat(), new CumStat()};

            for (LocationWrapper point:cluster.getPoints()) {
                Obj pointObject = point.getObject();
                pointObject.addParent(outputObject);
                outputObject.addChild(pointObject);

                // Getting the centroid of the current object
                cs[0].addMeasure(pointObject.getXMean(true));
                cs[1].addMeasure(pointObject.getYMean(true));
                cs[2].addMeasure(pointObject.getZMean(true,false)); // We now want to go back to original Z-coordinates

            }

            outputObject.addCoord((int) Math.round(cs[0].getMean()),(int) Math.round(cs[1].getMean()),(int) Math.round(cs[2].getMean()));

            outputObjects.add(outputObject);

        }

        return outputObjects;

    }

    @Override
    public String getTitle() {
        return "Object clustering";

    }

    @Override
    public String getHelp() {
        return "Clusters objects using K-Means and/or DB-SCAN algorithms." +
                "\nUses Apache Commons Math library for clustering.";

    }

    @Override
    public void run(Workspace workspace) {
        // Getting objects to measure
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting output objects name
        String outputObjectsName = parameters.getValue(CLUSTER_OBJECTS);
        ObjCollection outputObjects = new ObjCollection(outputObjectsName);

        // Getting parameters
        String clusteringAlgorithm = parameters.getValue(CLUSTERING_ALGORITHM);
        int kClusters = parameters.getValue(K_CLUSTERS);
        int maxIterations = parameters.getValue(MAX_ITERATIONS);
        double eps = parameters.getValue(EPS);
        int minPoints = parameters.getValue(MIN_POINTS);

        // If there are no input objects skipping this module
        Obj firstObject = inputObjects.getFirst();
        if (firstObject == null) {
            workspace.addObjects(outputObjects);
            return;
        }

        // Getting object parameters
        double dppXY = firstObject.getDistPerPxXY();
        double dppZ = firstObject.getDistPerPxZ();
        String calibratedUnits = firstObject.getCalibratedUnits();
        boolean twoD = firstObject.is2D();

        // Adding points to collection
        writeMessage("Adding points to clustering algorithm");
        List<LocationWrapper> locations = new ArrayList<>(inputObjects.size());
        for (Obj inputObject:inputObjects.values()) {
            locations.add(new LocationWrapper(inputObject));
        }

        // Running clustering system
        writeMessage("Running clustering algorithm");

        switch (clusteringAlgorithm) {
            case ClusteringAlgorithms.KMEANSPLUSPLUS:
                runKMeansPlusPlus(outputObjects, locations, dppXY, dppZ, calibratedUnits, twoD);
                break;

            case ClusteringAlgorithms.DBSCAN:
                runDBSCAN(outputObjects, locations, dppXY, dppZ, calibratedUnits,twoD);
                break;

        }

        // Adding measurement to each cluster and adding coordinates to clusters
        for (Obj outputObject:outputObjects.values()) {
            ObjCollection children = outputObject.getChildren(inputObjectsName);

            // The number of children per cluster
            Measurement measurement = new Measurement(Measurements.N_POINTS_IN_CLUSTER,children.size(),this);
            outputObject.addMeasurement(measurement);

            // Coordinates are stored as integers, so converting eps into an integer too
            int epsInt = (int) Math.floor(eps);

            // Getting limits of the current cluster
            int[][] limits = children.getSpatialLimits();
            int xMax = limits[0][1]+epsInt;
            int yMax = limits[1][1]+epsInt;

            Indexer indexer = new Indexer(xMax,yMax);
            HashSet<Integer> points = new HashSet<>();

            for (Obj child:children.values()) {
                int xCent = (int) Math.round(child.getXMean(true));
                int yCent = (int) Math.round(child.getYMean(true));

                for (int xx=xCent-epsInt;xx<xCent+epsInt;xx++) {
                    for (int yy=yCent-epsInt;yy<yCent+epsInt;yy++) {
                        if (Math.sqrt((xx-xCent)*(xx-xCent)+(yy-yCent)*(yy-yCent)) < eps) {
                            int idx = indexer.getIndex(new int[]{xx,yy});

                            // Coordinates outside the indexed region are returned as -1
                            if (idx != -1) points.add(idx);

                        }
                    }
                }
            }

            int area = points.size();
            outputObject.addMeasurement(new Measurement(Measurements.CLUSTER_AREA_XY_PX, area, this));
            outputObject.addMeasurement(new Measurement(Units.replace(Measurements.CLUSTER_AREA_XY_CAL), area*dppXY*dppXY, this));

            // Adding coordinates
            for (int idx : points) {
                int[] coords = indexer.getCoord(idx);

                outputObject.addCoord(coords[0],coords[1],0);
                outputObject.setT(0);

            }
        }

        writeMessage("Adding objects ("+outputObjectsName+") to workspace");
        workspace.addObjects(outputObjects);

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(CLUSTER_OBJECTS, Parameter.OUTPUT_OBJECTS,null));
        parameters.add(new Parameter(CLUSTERING_ALGORITHM, Parameter.CHOICE_ARRAY,ClusteringAlgorithms.DBSCAN,ClusteringAlgorithms.ALL));
        parameters.add(new Parameter(K_CLUSTERS, Parameter.INTEGER,100));
        parameters.add(new Parameter(MAX_ITERATIONS, Parameter.INTEGER,10000));
        parameters.add(new Parameter(EPS, Parameter.DOUBLE,10.0));
        parameters.add(new Parameter(MIN_POINTS, Parameter.INTEGER,5));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(CLUSTER_OBJECTS));
        returnedParameters.add(parameters.getParameter(CLUSTERING_ALGORITHM));

        if (parameters.getValue(CLUSTERING_ALGORITHM).equals(ClusteringAlgorithms.KMEANSPLUSPLUS)) {
            // Running KMeans++ clustering
            returnedParameters.add(parameters.getParameter(K_CLUSTERS));
            returnedParameters.add(parameters.getParameter(MAX_ITERATIONS));

        } else if (parameters.getValue(CLUSTERING_ALGORITHM).equals(ClusteringAlgorithms.DBSCAN)) {
            // Running DBSCAN clustering
            returnedParameters.add(parameters.getParameter(EPS));
            returnedParameters.add(parameters.getParameter(MIN_POINTS));

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

        String outputObjectsName = parameters.getValue(CLUSTER_OBJECTS);

        MeasurementReference reference = objectMeasurementReferences.getOrPut(Measurements.CLUSTER_AREA_XY_PX);
        reference.setImageObjName(outputObjectsName);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Units.replace(Measurements.CLUSTER_AREA_XY_CAL));
        reference.setImageObjName(outputObjectsName);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Measurements.N_POINTS_IN_CLUSTER);
        reference.setImageObjName(outputObjectsName);
        reference.setCalculated(true);

        return objectMeasurementReferences;

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {
        String clusterObjectsName = parameters.getValue(CLUSTER_OBJECTS);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        relationships.addRelationship(clusterObjectsName,inputObjectsName);

    }
}