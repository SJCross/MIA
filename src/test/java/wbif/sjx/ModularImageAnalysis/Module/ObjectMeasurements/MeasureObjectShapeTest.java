package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements;

import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.Module.ExpectedObjects3D;
import wbif.sjx.ModularImageAnalysis.Object.Obj;
import wbif.sjx.ModularImageAnalysis.Object.ObjSet;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Created by Stephen Cross on 03/09/2017.
 */
public class MeasureObjectShapeTest {
    @Test
    public void testRun() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Setting object parameters
        String inputObjectsName = "Test objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating objects and adding to workspace
        ObjSet testObjects = ExpectedObjects3D.getObjects(inputObjectsName,true,dppXY,dppZ,calibratedUnits);
        workspace.addObjects(testObjects);

        // Initialising MeasureObjectShape
        MeasureObjectShape measureObjectShape = new MeasureObjectShape();
        measureObjectShape.initialiseParameters();
        measureObjectShape.updateParameterValue(MeasureObjectShape.INPUT_OBJECTS,inputObjectsName);

        // Running IdentifyObjects
        measureObjectShape.run(workspace,false);

        // Checking the workspace contains a single object set
        assertEquals("Number of ObjSets in Workspace",1,workspace.getObjects().size());

        // Checking the number of objects in the set
        assertNotNull(workspace.getObjectSet(inputObjectsName));
        assertEquals(8,workspace.getObjectSet(inputObjectsName).size());

        // Getting expected values
        HashMap<Integer,double[]> expectedValues = ExpectedObjects3D.getExpectedValues3D();

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj testObject:testObjects.values()) {
            double[] currExpectedValues = expectedValues.get(testObject.getPoints().size());

            assertEquals("Number of measurements",1,testObject.getMeasurements().size());
            assertEquals("Measurement name","N_VOXELS",testObject.getMeasurements().keySet().iterator().next());

            int expectedNVoxels = (int) currExpectedValues[ExpectedObjects3D.N_VOXELS];
            int actualNVoxels = (int) testObject.getMeasurement("N_VOXELS").getValue();
            assertEquals("Measurement value", expectedNVoxels, actualNVoxels);

        }
    }
}