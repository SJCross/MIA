// TODO: Add calibrated distances to proximity relations

package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects3D;
import wbif.sjx.ModularImageAnalysis.ExpectedProxCubes1;
import wbif.sjx.ModularImageAnalysis.ExpectedProxCubes2;
import wbif.sjx.ModularImageAnalysis.ExpectedSpots3D;
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
    private double tolerance = 1E-2;

    @Test
    public void testGetTitle() throws Exception {
        assertNotNull(new RelateObjects().getTitle());

    }

    @Test
    public void testLinkMatchingIDsOneChild() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Setting object parameters
        String inputObjectsName = "Test_objects";
        String inputSpotsName = "Test_spots";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating objects and adding to workspace
        ObjCollection testObjects = new ExpectedObjects3D().getObjects(inputObjectsName,true,dppXY,dppZ,calibratedUnits);
        workspace.addObjects(testObjects);
        ObjCollection testSpots = new ExpectedSpots3D().getObjects(inputSpotsName,true,dppXY,dppZ,calibratedUnits);
        workspace.addObjects(testSpots);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects();
        relateObjects.initialiseParameters();
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,inputObjectsName);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,inputSpotsName);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.MATCHING_IDS);

        // Running RelateObjects
        relateObjects.run(workspace,false);

        // Checking the workspace contains two object sets
        assertEquals("Number of ObjSets in Workspace",2,workspace.getObjects().size());

        // Checking the number of objects in the set
        assertNotNull(workspace.getObjectSet(inputObjectsName));
        assertEquals(8,workspace.getObjectSet(inputObjectsName).size());
        assertNotNull(workspace.getObjectSet(inputSpotsName));
        assertEquals(25,workspace.getObjectSet(inputSpotsName).size());

        // Getting expected values
        HashMap<Integer,HashMap<String,Object>> expectedValues = ExpectedObjects3D.getExpectedValues3D();

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
            int expected = (int) expectedValues.get(nPoints).get(ExpectedObjects3D.Measures.SPOT_ID_X);
            int actual = (int) childSpot.getX(true)[0];
            assertEquals(expected,actual);

            expected = (int) expectedValues.get(nPoints).get(ExpectedObjects3D.Measures.SPOT_ID_Y);
            actual = (int) childSpot.getY(true)[0];
            assertEquals(expected,actual);

            expected = (int) expectedValues.get(nPoints).get(ExpectedObjects3D.Measures.SPOT_ID_Z);
            actual = (int) childSpot.getZ(true,false)[0];
            assertEquals(expected,actual);

        }
    }

    @Test
    public void testProximityCentroidLink() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Setting object parameters
        String inputObjectsName = "Test_objects";
        String inputSpotsName = "Test_spots";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating objects and adding to workspace
        ObjCollection testObjects = new ExpectedObjects3D().getObjects(inputObjectsName,true,dppXY,dppZ,calibratedUnits);
        workspace.addObjects(testObjects);
        ObjCollection testSpots = new ExpectedSpots3D().getObjects(inputSpotsName,true,dppXY,dppZ,calibratedUnits);
        workspace.addObjects(testSpots);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects();
        relateObjects.initialiseParameters();
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,inputObjectsName);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,inputSpotsName);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.CENTROID);
        relateObjects.updateParameterValue(RelateObjects.LINKING_DISTANCE,Double.MAX_VALUE);

        // Running RelateObjects
        relateObjects.run(workspace,false);

        // Getting expected values
        HashMap<Integer, HashMap<String, Object>> expectedValues = ExpectedObjects3D.getExpectedValues3D();

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj testObject:testObjects.values()) {
            // Getting expected values for this object
            HashMap<String, Object> currExpectedValues = expectedValues.get(testObject.getPoints().size());
            int[] expectedX = (int[]) currExpectedValues.get(ExpectedObjects3D.Measures.SPOT_PROX_CENT_X);
            int[] expectedY = (int[]) currExpectedValues.get(ExpectedObjects3D.Measures.SPOT_PROX_CENT_Y);
            int[] expectedZ = (int[]) currExpectedValues.get(ExpectedObjects3D.Measures.SPOT_PROX_CENT_Z);
            double[] expectedDist = (double[]) currExpectedValues.get(ExpectedObjects3D.Measures.SPOT_PROX_CENT_DIST);

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
                actualDist[iter++] = childSpot.getMeasurement(RelateObjects.Measurements.DIST_CENTROID_PX).getValue();

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
        Workspace workspace = new Workspace(0,null);

        // Setting object parameters
        String inputObjectsName = "Test_objects";
        String inputSpotsName = "Test_spots";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating objects and adding to workspace
        ObjCollection testObjects = new ExpectedObjects3D().getObjects(inputObjectsName,true,dppXY,dppZ,calibratedUnits);
        workspace.addObjects(testObjects);
        ObjCollection testSpots = new ExpectedSpots3D().getObjects(inputSpotsName,true,dppXY,dppZ,calibratedUnits);
        workspace.addObjects(testSpots);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects();
        relateObjects.initialiseParameters();
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,inputObjectsName);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,inputSpotsName);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.CENTROID);
        relateObjects.updateParameterValue(RelateObjects.LINKING_DISTANCE,20.0);

        // Running RelateObjects
        relateObjects.run(workspace,false);

        // Getting expected values
        HashMap<Integer, HashMap<String, Object>> expectedValues = ExpectedObjects3D.getExpectedValues3D();

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj testObject:testObjects.values()) {
            // Getting expected values for this object
            HashMap<String, Object> currExpectedValues = expectedValues.get(testObject.getPoints().size());
            int[] expectedX = (int[]) currExpectedValues.get(ExpectedObjects3D.Measures.SPOT_PROX_CENT_20PX_X);
            int[] expectedY = (int[]) currExpectedValues.get(ExpectedObjects3D.Measures.SPOT_PROX_CENT_20PX_Y);
            int[] expectedZ = (int[]) currExpectedValues.get(ExpectedObjects3D.Measures.SPOT_PROX_CENT_20PX_Z);
            double[] expectedDist = (double[]) currExpectedValues.get(ExpectedObjects3D.Measures.SPOT_PROX_CENT_20PX_DIST);

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
                actualDist[iter++] = childSpot.getMeasurement(RelateObjects.Measurements.DIST_CENTROID_PX).getValue();

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
        Workspace workspace = new Workspace(0,null);

        // Setting object parameters
        String proxObj1Name = "Prox_obj_1";
        String proxObj2Name = "Prox_obj_2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating objects and adding to workspace
        ObjCollection proxObj1 = new ExpectedProxCubes1().getObjects(proxObj1Name,true,dppXY,dppZ,calibratedUnits);
        workspace.addObjects(proxObj1);
        ObjCollection proxObj2 = new ExpectedProxCubes2().getObjects(proxObj2Name,true,dppXY,dppZ,calibratedUnits);
        workspace.addObjects(proxObj2);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects();
        relateObjects.initialiseParameters();
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,proxObj2Name);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,proxObj1Name);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.SURFACE);
        relateObjects.updateParameterValue(RelateObjects.LINKING_DISTANCE,Double.MAX_VALUE);

        // Running RelateObjects
        relateObjects.run(workspace,false);

        // Getting expected values
        HashMap<Integer, HashMap<ExpectedProxCubes1.Measures, Object>> expectedValues = ExpectedProxCubes1.getExpectedValues3D();

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj proxObj1Obj:proxObj1.values()) {
            // Getting expected values for this object
            HashMap<ExpectedProxCubes1.Measures, Object> currExpectedValues = expectedValues.get(proxObj1Obj.getPoints().size());

            // Checking the object has no children
            LinkedHashMap<String, ObjCollection> children = proxObj1Obj.getChildren();
            assertEquals(0, children.size());

            // Checking the parent ID is the one expected
            Obj parentObj = proxObj1Obj.getParent(proxObj2Name);
            int expectedParentID = (int) currExpectedValues.get(ExpectedProxCubes1.Measures.SURF_PROX_ID);
            int actualParentID = parentObj.getID();
            assertEquals(expectedParentID, actualParentID);

            // Checking the distance to the parent
            double expectedSurfDistPx = (double) currExpectedValues.get(ExpectedProxCubes1.Measures.SURF_PROX_DIST_PX);
            double actualSurfDistPx = proxObj1Obj.getMeasurement(RelateObjects.Measurements.DIST_SURFACE_PX).getValue();
            assertEquals(expectedSurfDistPx, actualSurfDistPx, tolerance);

            double expectedSurfDistCal = (double) currExpectedValues.get(ExpectedProxCubes1.Measures.SURF_PROX_DIST_CAL);
            double actualSurfDistCal = proxObj1Obj.getMeasurement(RelateObjects.Measurements.DIST_SURFACE_CAL).getValue();
            assertEquals(expectedSurfDistCal, actualSurfDistCal, tolerance);

        }
    }

    @Test @Ignore
    public void testProximitySurfaceLink3px() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Setting object parameters
        String proxObj1Name = "Prox_obj_1";
        String proxObj2Name = "Prox_obj_2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating objects and adding to workspace
        ObjCollection proxObj1 = new ExpectedProxCubes1().getObjects(proxObj1Name,true,dppXY,dppZ,calibratedUnits);
        workspace.addObjects(proxObj1);
        ObjCollection proxObj2 = new ExpectedProxCubes2().getObjects(proxObj2Name,true,dppXY,dppZ,calibratedUnits);
        workspace.addObjects(proxObj2);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects();
        relateObjects.initialiseParameters();
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,proxObj2Name);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,proxObj1Name);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.SURFACE);
        relateObjects.updateParameterValue(RelateObjects.LINKING_DISTANCE,3.0);

        // Running RelateObjects
        relateObjects.run(workspace,false);

        // Getting expected values
        HashMap<Integer, HashMap<ExpectedProxCubes1.Measures, Object>> expectedValues = ExpectedProxCubes1.getExpectedValues3D();

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj proxObj1Obj:proxObj1.values()) {
            // Getting expected values for this object
            HashMap<ExpectedProxCubes1.Measures, Object> currExpectedValues = expectedValues.get(proxObj1Obj.getPoints().size());

            // Checking the object has no children
            LinkedHashMap<String, ObjCollection> children = proxObj1Obj.getChildren();
            assertEquals(0, children.size());

            // Checking the parent ID is the one expected
            Obj parentObj = proxObj1Obj.getParent(proxObj2Name);
            int expectedParentID = (int) currExpectedValues.get(ExpectedProxCubes1.Measures.SURF_PROX_ID);
            int actualParentID = parentObj.getID();
            assertEquals(expectedParentID, actualParentID);

            // Checking the distance to the parent
            double expectedSurfDistPx = (double) currExpectedValues.get(ExpectedProxCubes1.Measures.SURF_PROX_DIST_PX);
            double actualSurfDistPx = proxObj1Obj.getMeasurement(RelateObjects.Measurements.DIST_SURFACE_PX).getValue();
            assertEquals(expectedSurfDistPx, actualSurfDistPx, tolerance);

            double expectedSurfDistCal = (double) currExpectedValues.get(ExpectedProxCubes1.Measures.SURF_PROX_DIST_CAL);
            double actualSurfDistCal = proxObj1Obj.getMeasurement(RelateObjects.Measurements.DIST_SURFACE_CAL).getValue();
            assertEquals(expectedSurfDistCal, actualSurfDistCal, tolerance);

        }
    }

    @Test @Ignore
    public void testProximityToChildren() throws Exception {
    }

    @Test @Ignore
    public void testSpatialLinking() throws Exception {
    }

}