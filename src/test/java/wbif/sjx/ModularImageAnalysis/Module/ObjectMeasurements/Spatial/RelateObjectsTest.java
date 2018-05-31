// TODO: Add calibrated distances to proximity relations

package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial;

import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.*;
import wbif.sjx.ModularImageAnalysis.Object.Obj;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.junit.Assert.*;

/**
 * Created by Stephen Cross on 10/09/2017.
 */
public class RelateObjectsTest {
    private double tolerance = 1E-10;

    @Test
    public void testGetTitle() throws Exception {
        assertNotNull(new RelateObjects().getTitle());

    }

    @Test
    public void testLinkMatchingIDsOneChild() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String inputObjectsName = "Test_objects";
        String inputSpotsName = "Test_spots";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection testObjects = new ExpectedObjects3D().getObjects(inputObjectsName,true,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);
        ObjCollection testSpots = new ExpectedSpots3D().getObjects(inputSpotsName,true,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testSpots);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects();
        relateObjects.initialiseParameters();
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,inputObjectsName);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,inputSpotsName);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.MATCHING_IDS);

        // Running RelateObjects
        relateObjects.run(workspace);

        // Checking the workspace contains two object sets
        assertEquals("Number of ObjSets in Workspace",2,workspace.getObjects().size());

        // Checking the number of objects in the set
        assertNotNull(workspace.getObjectSet(inputObjectsName));
        assertEquals(8,workspace.getObjectSet(inputObjectsName).size());
        assertNotNull(workspace.getObjectSet(inputSpotsName));
        assertEquals(25,workspace.getObjectSet(inputSpotsName).size());

        // Running through each object, checking it has the expected number of children and the expected value
        for (Obj testObject:testObjects.values()) {
            ObjCollection childSpots = testObject.getChildren(inputSpotsName);

            // Testing the number of children
            assertNotNull("Object has spot children",childSpots);
            assertEquals("One child per parent",1,childSpots.size());

            // Testing spot for parent
            Obj childSpot = childSpots.values().iterator().next();
            assertNotNull("Child spot has parent",childSpot.getParent(inputObjectsName));

            // Testing spot is at expected location
            int nPoints = testObject.getPoints().size();
            double expected = testObject.getMeasurement(ExpectedObjects3D.Measures.EXP_SPOT_ID_X.name()).getValue();
            double actual = childSpot.getX(true)[0];
            assertEquals(expected,actual,tolerance);

            expected = testObject.getMeasurement(ExpectedObjects3D.Measures.EXP_SPOT_ID_Y.name()).getValue();
            actual = childSpot.getY(true)[0];
            assertEquals(expected,actual,tolerance);

            expected = testObject.getMeasurement(ExpectedObjects3D.Measures.EXP_SPOT_ID_Z.name()).getValue();
            actual = childSpot.getZ(true,false)[0];
            assertEquals(expected,actual,tolerance);

        }
    }

    @Test
    public void testProximityCentroidLink() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String inputObjectsName = "Test_objects";
        String inputSpotsName = "Test_spots";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection testObjects = new ExpectedObjects3D().getObjects(inputObjectsName,true,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);
        ObjCollection testSpots = new ExpectedSpots3D().getObjects(inputSpotsName,true,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testSpots);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects();
        relateObjects.initialiseParameters();
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,inputObjectsName);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,inputSpotsName);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.CENTROID);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,false);

        // Running RelateObjects
        relateObjects.run(workspace);

        // Getting expected values
        HashMap<Integer, HashMap<String, Object>> expectedValues = new ExpectedObjects3D().getOtherValues();

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj testObject:testObjects.values()) {
            // Getting expected values for this object
            HashMap<String, Object> currExpectedValues = expectedValues.get(testObject.getPoints().size());
            int[] expectedX = (int[]) currExpectedValues.get(ExpectedObjects3D.Measures.EXP_SPOT_PROX_CENT_X.name());
            int[] expectedY = (int[]) currExpectedValues.get(ExpectedObjects3D.Measures.EXP_SPOT_PROX_CENT_Y.name());
            int[] expectedZ = (int[]) currExpectedValues.get(ExpectedObjects3D.Measures.EXP_SPOT_PROX_CENT_Z.name());
            double[] expectedDist = (double[]) currExpectedValues.get(ExpectedObjects3D.Measures.EXP_SPOT_PROX_CENT_DIST.name());

            // Getting child objects (those linked here)
            ObjCollection childSpots = testObject.getChildren(inputSpotsName);

            // Each object won't necessarily have any children
            if (childSpots == null) continue;

            // Testing the number of children
            assertEquals("Number of children",expectedX.length,childSpots.size());

            // Putting actual values into arrays
            int[] actualX = new int[childSpots.size()];
            int[] actualY = new int[childSpots.size()];
            int[] actualZ = new int[childSpots.size()];
            double[] actualDist = new double[childSpots.size()];

            int iter = 0;
            for (Obj childSpot:childSpots.values()) {
                actualX[iter] = (int) childSpot.getX(true)[0];
                actualY[iter] = (int) childSpot.getY(true)[0];
                actualZ[iter] = (int) childSpot.getZ(true,false)[0];
                String name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_CENTROID_PX,inputObjectsName);
                actualDist[iter++] = childSpot.getMeasurement(name).getValue();

            }

            // Sorting arrays
            Arrays.sort(expectedX);
            Arrays.sort(expectedY);
            Arrays.sort(expectedZ);
            Arrays.sort(expectedDist);
            Arrays.sort(actualX);
            Arrays.sort(actualY);
            Arrays.sort(actualZ);
            Arrays.sort(actualDist);

            // Comparing arrays
            assertArrayEquals(expectedX,actualX);
            assertArrayEquals(expectedY,actualY);
            assertArrayEquals(expectedZ,actualZ);
            assertArrayEquals(expectedDist,actualDist,tolerance);

        }
    }

    @Test
    public void testProximityCentroidLink20px() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String inputObjectsName = "Test_objects";
        String inputSpotsName = "Test_spots";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection testObjects = new ExpectedObjects3D().getObjects(inputObjectsName,true,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);
        ObjCollection testSpots = new ExpectedSpots3D().getObjects(inputSpotsName,true,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testSpots);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects();
        relateObjects.initialiseParameters();
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,inputObjectsName);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,inputSpotsName);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.CENTROID);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,true);
        relateObjects.updateParameterValue(RelateObjects.LINKING_DISTANCE,20.0);

        // Running RelateObjects
        relateObjects.run(workspace);

        // Getting expected values
        HashMap<Integer, HashMap<String, Object>> expectedValues = new ExpectedObjects3D().getOtherValues();

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj testObject:testObjects.values()) {
            // Getting expected values for this object
            HashMap<String, Object> currExpectedValues = expectedValues.get(testObject.getPoints().size());
            int[] expectedX = (int[]) currExpectedValues.get(ExpectedObjects3D.Measures.EXP_SPOT_PROX_CENT_20PX_X.name());
            int[] expectedY = (int[]) currExpectedValues.get(ExpectedObjects3D.Measures.EXP_SPOT_PROX_CENT_20PX_Y.name());
            int[] expectedZ = (int[]) currExpectedValues.get(ExpectedObjects3D.Measures.EXP_SPOT_PROX_CENT_20PX_Z.name());
            double[] expectedDist = (double[]) currExpectedValues.get(ExpectedObjects3D.Measures.EXP_SPOT_PROX_CENT_20PX_DIST.name());

            // Getting child objects (those linked here)
            ObjCollection childSpots = testObject.getChildren(inputSpotsName);

            // Each object won't necessarily have any children
            if (childSpots == null) continue;

            // Testing the number of children
            assertEquals("Number of children",expectedX.length,childSpots.size());

            // Putting actual values into arrays
            int[] actualX = new int[childSpots.size()];
            int[] actualY = new int[childSpots.size()];
            int[] actualZ = new int[childSpots.size()];
            double[] actualDist = new double[childSpots.size()];

            int iter = 0;
            for (Obj childSpot:childSpots.values()) {
                actualX[iter] = (int) childSpot.getX(true)[0];
                actualY[iter] = (int) childSpot.getY(true)[0];
                actualZ[iter] = (int) childSpot.getZ(true,false)[0];
                String name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_CENTROID_PX,inputObjectsName);
                actualDist[iter++] = childSpot.getMeasurement(name).getValue();

            }

            // Sorting arrays
            Arrays.sort(expectedX);
            Arrays.sort(expectedY);
            Arrays.sort(expectedZ);
            Arrays.sort(expectedDist);
            Arrays.sort(actualX);
            Arrays.sort(actualY);
            Arrays.sort(actualZ);
            Arrays.sort(actualDist);

            // Comparing arrays
            assertArrayEquals(expectedX,actualX);
            assertArrayEquals(expectedY,actualY);
            assertArrayEquals(expectedZ,actualZ);
            assertArrayEquals(expectedDist,actualDist,tolerance);

        }
    }

    @Test
    public void testProximitySurfaceLink() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String proxObj1Name = "Prox_obj_1";
        String proxObj2Name = "Prox_obj_2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection proxObj1 = new ExpectedProxCubes1().getObjects(proxObj1Name,true,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj1);
        ObjCollection proxObj2 = new ExpectedProxCubes2().getObjects(proxObj2Name,true,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj2);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects();
        relateObjects.initialiseParameters();
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,proxObj2Name);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,proxObj1Name);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.SURFACE);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,false);

        // Running RelateObjects
        relateObjects.run(workspace);

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj proxObj1Obj:proxObj1.values()) {
            // Checking the object has no children
            LinkedHashMap<String, ObjCollection> children = proxObj1Obj.getChildren();
            assertEquals(0, children.size());

            // Checking the parent ID is the one expected
            Obj parentObj = proxObj1Obj.getParent(proxObj2Name);
            double expectedParentID = proxObj1Obj.getMeasurement(ExpectedProxCubes1.Measures.SURF_PROX_ID.name()).getValue();
            int actualParentID = parentObj.getID();
            assertEquals(expectedParentID, actualParentID, tolerance);

            // Checking the distance to the parent
            double expectedSurfDistPx = proxObj1Obj.getMeasurement(ExpectedProxCubes1.Measures.SURF_PROX_DIST_PX.name()).getValue();
            String name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_SURFACE_PX,proxObj2Name);
            double actualSurfDistPx = proxObj1Obj.getMeasurement(name).getValue();
            assertEquals(expectedSurfDistPx, actualSurfDistPx, tolerance);

            double expectedSurfDistCal = proxObj1Obj.getMeasurement(ExpectedProxCubes1.Measures.SURF_PROX_DIST_CAL.name()).getValue();
            name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_SURFACE_CAL,proxObj2Name);
            double actualSurfDistCal = proxObj1Obj.getMeasurement(name).getValue();
            assertEquals(expectedSurfDistCal, actualSurfDistCal, tolerance);

        }
    }

    @Test
    public void testProximitySurfaceLink5px() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String proxObj1Name = "Prox_obj_1";
        String proxObj2Name = "Prox_obj_2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection proxObj1 = new ExpectedProxCubes1().getObjects(proxObj1Name,true,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj1);
        ObjCollection proxObj2 = new ExpectedProxCubes2().getObjects(proxObj2Name,true,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj2);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects();
        relateObjects.initialiseParameters();
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,proxObj2Name);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,proxObj1Name);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.SURFACE);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,true);
        relateObjects.updateParameterValue(RelateObjects.LINKING_DISTANCE,5.0);

        // Running RelateObjects
        relateObjects.run(workspace);

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj proxObj1Obj:proxObj1.values()) {
            // Checking the object has no children
            LinkedHashMap<String, ObjCollection> children = proxObj1Obj.getChildren();
            assertEquals(0, children.size());

            // Checking the parent ID is the one expected
            Obj parentObj = proxObj1Obj.getParent(proxObj2Name);
            double expectedParentID = proxObj1Obj.getMeasurement(ExpectedProxCubes1.Measures.SURF_PROX_ID_5PX.name()).getValue();
            if (Double.isNaN(expectedParentID)) {
                assertNull(parentObj);
            } else {
                int actualParentID = parentObj.getID();
                assertEquals(expectedParentID, actualParentID, tolerance);
            }

            // Checking the distance to the parent
            double expectedSurfDistPx = proxObj1Obj.getMeasurement(ExpectedProxCubes1.Measures.SURF_PROX_DIST_PX_5PX.name()).getValue();
            String name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_SURFACE_PX,proxObj2Name);
            double actualSurfDistPx = proxObj1Obj.getMeasurement(name).getValue();
            assertEquals(expectedSurfDistPx, actualSurfDistPx, tolerance);

            double expectedSurfDistCal = proxObj1Obj.getMeasurement(ExpectedProxCubes1.Measures.SURF_PROX_DIST_CAL_5PX.name()).getValue();
            name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_SURFACE_CAL,proxObj2Name);
            double actualSurfDistCal = proxObj1Obj.getMeasurement(name).getValue();
            assertEquals(expectedSurfDistCal, actualSurfDistCal, tolerance);

        }
    }

    @Test
    public void testProximityCentroidSurfaceLink() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String proxObj1Name = "Prox_obj_1";
        String proxObj2Name = "Prox_obj_2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection proxObj1 = new ExpectedProxCubes1().getObjects(proxObj1Name,true,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj1);
        ObjCollection proxObj2 = new ExpectedProxCubes2().getObjects(proxObj2Name,true,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj2);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects();
        relateObjects.initialiseParameters();
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,proxObj2Name);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,proxObj1Name);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.CENTROID_TO_SURFACE);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,false);

        // Running RelateObjects
        relateObjects.run(workspace);

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj proxObj1Obj:proxObj1.values()) {
            // Checking the object has no children
            LinkedHashMap<String, ObjCollection> children = proxObj1Obj.getChildren();
            assertEquals(0, children.size());

            // Checking the parent ID is the one expected
            Obj parentObj = proxObj1Obj.getParent(proxObj2Name);
            double expectedParentID = proxObj1Obj.getMeasurement(ExpectedProxCubes1.Measures.CENT_SURF_PROX_ID.name()).getValue();
            int actualParentID = parentObj.getID();
            assertEquals(expectedParentID, actualParentID, tolerance);

            // Checking the distance to the parent
            double expectedDistPx = proxObj1Obj.getMeasurement(ExpectedProxCubes1.Measures.CENT_SURF_PROX_DIST_PX.name()).getValue();
            String name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_CENT_SURF_PX,proxObj2Name);
            double actualfDistPx = proxObj1Obj.getMeasurement(name).getValue();
            assertEquals(expectedDistPx, actualfDistPx, tolerance);

            double expectedDistCal = proxObj1Obj.getMeasurement(ExpectedProxCubes1.Measures.CENT_SURF_PROX_DIST_CAL.name()).getValue();
            name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_CENT_SURF_CAL,proxObj2Name);
            double actualDistCal = proxObj1Obj.getMeasurement(name).getValue();
            assertEquals(expectedDistCal, actualDistCal, tolerance);

        }
    }

    @Test
    public void testProximityCentroidSurfaceLink5px() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String proxObj1Name = "Prox_obj_1";
        String proxObj2Name = "Prox_obj_2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection proxObj1 = new ExpectedProxCubes1().getObjects(proxObj1Name,true,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj1);
        ObjCollection proxObj2 = new ExpectedProxCubes2().getObjects(proxObj2Name,true,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj2);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects();
        relateObjects.initialiseParameters();
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,proxObj2Name);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,proxObj1Name);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.CENTROID_TO_SURFACE);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,true);
        relateObjects.updateParameterValue(RelateObjects.LINKING_DISTANCE,5.0);

        // Running RelateObjects
        relateObjects.run(workspace);

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj proxObj1Obj:proxObj1.values()) {
            // Checking the object has no children
            LinkedHashMap<String, ObjCollection> children = proxObj1Obj.getChildren();
            assertEquals(0, children.size());

            // Checking the parent ID is the one expected
            Obj parentObj = proxObj1Obj.getParent(proxObj2Name);
            double expectedParentID = proxObj1Obj.getMeasurement(ExpectedProxCubes1.Measures.CENT_SURF_PROX_ID_5PX.name()).getValue();
            if (Double.isNaN(expectedParentID)) {
                assertNull(parentObj);
            } else {
                int actualParentID = parentObj.getID();
                assertEquals(expectedParentID, actualParentID, tolerance);
            }

            // Checking the distance to the parent
            double expectedDistPx = proxObj1Obj.getMeasurement(ExpectedProxCubes1.Measures.CENT_SURF_PROX_DIST_PX_5PX.name()).getValue();
            String name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_CENT_SURF_PX,proxObj2Name);
            double actualfDistPx = proxObj1Obj.getMeasurement(name).getValue();
            assertEquals(expectedDistPx, actualfDistPx, tolerance);

            double expectedDistCal = proxObj1Obj.getMeasurement(ExpectedProxCubes1.Measures.CENT_SURF_PROX_DIST_CAL_5PX.name()).getValue();
            name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_CENT_SURF_CAL,proxObj2Name);
            double actualDistCal = proxObj1Obj.getMeasurement(name).getValue();
            assertEquals(expectedDistCal, actualDistCal, tolerance);

        }
    }

    @Test
    public void testProximityCentroidSurfaceResponse2D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String proxObj1Name = "Prox_obj_1";
        String proxObj2Name = "Prox_obj_2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection proxObj1 = new ExpectedProxSquares1(true).getObjects(proxObj1Name,true,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj1);
        ObjCollection proxObj2 = new ExpectedProxSquares2(true).getObjects(proxObj2Name,true,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj2);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects();
        relateObjects.initialiseParameters();
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,proxObj1Name);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,proxObj2Name);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.CENTROID_TO_SURFACE);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,false);

        // Running RelateObjects
        relateObjects.run(workspace);

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj proxObj2Obj:proxObj2.values()) {
            System.out.println(proxObj2Obj.getID());
            // Checking the object has no children
            LinkedHashMap<String, ObjCollection> children = proxObj2Obj.getChildren();
            assertEquals(0, children.size());

            // Checking the distance to the parent
            double expectedSurfDistPx = proxObj2Obj.getMeasurement(ExpectedProxSquares2.Measures.CENT_SURF_PROX_DIST_PX.name()).getValue();
            String name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_CENT_SURF_PX,proxObj1Name);
            double actualSurfDistPx = proxObj2Obj.getMeasurement(name).getValue();
            assertEquals(expectedSurfDistPx, actualSurfDistPx, tolerance);

        }
    }

    @Test @Ignore
    public void testProximityToChildren() throws Exception {
    }

    @Test @Ignore
    public void testSpatialLinking() throws Exception {
    }

}