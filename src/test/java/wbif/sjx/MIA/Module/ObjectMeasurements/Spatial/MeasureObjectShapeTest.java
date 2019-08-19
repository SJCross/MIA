package wbif.sjx.MIA.Module.ObjectMeasurements.Spatial;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import wbif.sjx.MIA.ExpectedObjects.ExpectedObjects;
import wbif.sjx.MIA.ExpectedObjects.Objects3D;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleTest;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Units;
import wbif.sjx.MIA.Object.Workspace;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Stephen Cross on 03/09/2017.
 */
public class MeasureObjectShapeTest extends ModuleTest {
    private double tolerance = 1E-2;

    @BeforeAll
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new MeasureObjectShape(null).getDescription());

    }

    @Test @Disabled
    public void testRun() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String inputObjectsName = "Test objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection testObjects = new Objects3D().getObjects(inputObjectsName, ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Initialising MeasureObjectShape
        MeasureObjectShape measureObjectShape = new MeasureObjectShape(null);
        measureObjectShape.initialiseParameters();
        measureObjectShape.updateParameterValue(MeasureObjectShape.INPUT_OBJECTS,inputObjectsName);


        // Running IdentifyObjects
        measureObjectShape.execute(workspace);

        // Checking the workspace contains a single object set
        assertEquals(1,workspace.getObjects().size());

        // Checking the number of objects in the set
        assertNotNull(workspace.getObjectSet(inputObjectsName));
        assertEquals(8,workspace.getObjectSet(inputObjectsName).size());

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj testObject:testObjects.values()) {
            double expectedNVoxels = testObject.getMeasurement(Objects3D.Measures.EXP_N_VOXELS.name()).getValue();
            double actualNVoxels = testObject.getMeasurement(MeasureObjectShape.Measurements.N_VOXELS).getValue();
            assertEquals(expectedNVoxels, actualNVoxels,tolerance);

            double expectedProjDiaPX = testObject.getMeasurement(Objects3D.Measures.EXP_PROJ_DIA_PX.name()).getValue();
            double actualProjDiaPX = testObject.getMeasurement(MeasureObjectShape.Measurements.PROJ_DIA_PX).getValue();
            assertEquals(expectedProjDiaPX, actualProjDiaPX, tolerance);

            double expectedProjDiaCal = testObject.getMeasurement(Objects3D.Measures.EXP_PROJ_DIA_CAL.name()).getValue();
            double actualProjDiaCal = testObject.getMeasurement(Units.replace(MeasureObjectShape.Measurements.PROJ_DIA_CAL)).getValue();
            assertEquals(expectedProjDiaCal, actualProjDiaCal, tolerance);

        }
    }

}