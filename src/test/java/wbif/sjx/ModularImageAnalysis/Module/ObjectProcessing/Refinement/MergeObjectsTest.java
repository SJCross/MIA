package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Refinement;

import org.junit.BeforeClass;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.MergedObjects3D;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.ExpectedObjects;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.Objects3D;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.Spots3D;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Obj;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import static org.junit.Assert.*;

public class MergeObjectsTest {
    @BeforeClass
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Test
    public void testGetTitle() {
        assertNotNull(new MergeObjects().getTitle());
    }

    @Test
    public void testRunWithObjectDeletion() throws GenericMIAException {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection inputObj1 = new Objects3D().getObjects("Input_obj_1",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(inputObj1);
        ObjCollection inputObj2 = new Spots3D().getObjects("Input_obj_2",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(inputObj2);

        // Initialising FilterObjects module
        MergeObjects mergeObjects = new MergeObjects();
        mergeObjects.updateParameterValue(MergeObjects.INPUT_OBJECTS_1,"Input_obj_1");
        mergeObjects.updateParameterValue(MergeObjects.INPUT_OBJECTS_2,"Input_obj_2");
        mergeObjects.updateParameterValue(MergeObjects.OUTPUT_OBJECTS,"Output_obj");
        mergeObjects.updateParameterValue(MergeObjects.DELETE_INPUTS,true);

        // Running the module
        mergeObjects.run(workspace);

        // Getting expected output objects
        ObjCollection expectedOutputObj= new MergedObjects3D().getObjects("Output_obj",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(expectedOutputObj);

        // Getting actual output objects
        ObjCollection actualOutputObj = workspace.getObjectSet("Output_obj");

        // Checking the number of detected objects
        assertEquals(33,actualOutputObj.size());
        assertNull(workspace.getObjectSet("Input_obj_1"));
        assertNull(workspace.getObjectSet("Input_obj_2"));

        for (Obj object:actualOutputObj.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedOutputObj.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @Test
    public void testRunWithoutObjectDeletion() throws GenericMIAException {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection inputObj1 = new Objects3D().getObjects("Input_obj_1", ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(inputObj1);
        ObjCollection inputObj2 = new Spots3D().getObjects("Input_obj_2",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(inputObj2);

        // Initialising FilterObjects module
        MergeObjects mergeObjects = new MergeObjects();
        mergeObjects.updateParameterValue(MergeObjects.INPUT_OBJECTS_1,"Input_obj_1");
        mergeObjects.updateParameterValue(MergeObjects.INPUT_OBJECTS_2,"Input_obj_2");
        mergeObjects.updateParameterValue(MergeObjects.OUTPUT_OBJECTS,"Output_obj");
        mergeObjects.updateParameterValue(MergeObjects.DELETE_INPUTS,false);

        // Running the module
        mergeObjects.run(workspace);

        // Getting expected output objects
        ObjCollection expectedOutputObj= new MergedObjects3D().getObjects("Output_obj",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(expectedOutputObj);

        // Getting actual output objects
        ObjCollection actualOutputObj = workspace.getObjectSet("Output_obj");

        // Checking the number of detected objects
        assertEquals(33,actualOutputObj.size());
        assertEquals(8,workspace.getObjectSet("Input_obj_1").size());
        assertEquals(25,workspace.getObjectSet("Input_obj_2").size());

        for (Obj object:actualOutputObj.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedOutputObj.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }
}