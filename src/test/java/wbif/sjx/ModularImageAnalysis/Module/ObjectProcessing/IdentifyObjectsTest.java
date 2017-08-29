package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import ij.IJ;
import ij.ImagePlus;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.Module.ExpectedObjects;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.Obj;
import wbif.sjx.ModularImageAnalysis.Object.ObjSet;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;
import wbif.sjx.common.Object.Point;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Created by Stephen Cross on 29/08/2017.
 */
public class IdentifyObjectsTest {
    private double tolerance = 1E-2;

    @Test
    public void testRunBlackBackground8Bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects3D_8bit_blackBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects();
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);

        // Running IdentifyObjects
        identifyObjects.run(workspace,true);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());
        // Getting the object set
        ObjSet objects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",objects.getName());

        // Checking the number of detected objects
        assertEquals(8,objects.size());

        // Checking the spatial calibration and coordinate limits of each object
        HashMap<Integer,double[]> expectedValues = ExpectedObjects.get3D();

        for (Obj object:objects.values()) {
            // Getting the number of voxels in this object (this is used as the key for the expected values map)
            int nVoxels = object.getNVoxels();

            // Getting the relevant measures
            double[] expected = expectedValues.get(nVoxels);
            assertNotNull("Null means no expected object with the specified number of voxels",expected);

            // Testing coordinate ranges
            int[][] coordinateRange = object.getCoordinateRange();
            assertEquals("X-min",expected[ExpectedObjects.X_MIN],coordinateRange[0][0],tolerance);
            assertEquals("X-max",expected[ExpectedObjects.X_MAX],coordinateRange[0][1],tolerance);
            assertEquals("Y-min",expected[ExpectedObjects.Y_MIN],coordinateRange[1][0],tolerance);
            assertEquals("Y-max",expected[ExpectedObjects.Y_MAX],coordinateRange[1][1],tolerance);
            assertEquals("X-min",expected[ExpectedObjects.Z_MIN],coordinateRange[2][0],tolerance);
            assertEquals("Y-max",expected[ExpectedObjects.Z_MAX],coordinateRange[2][1],tolerance);
            assertEquals("F",expected[ExpectedObjects.F],object.getT(),tolerance);

            // Checking the objects have the correct spatial calibration
            double dppXY = object.getDistPerPxXY();
            double dppZ = object.getDistPerPxZ();
            assertEquals("Spatial calibration in XY",0.02,dppXY,tolerance);
            assertEquals("Spatial calibration in Z",0.1,dppZ,tolerance);

        }
    }

    @Test
    public void testRunWhiteBackground8Bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects3D_8bit_whiteBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects();
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,true);

        // Running IdentifyObjects
        identifyObjects.run(workspace,true);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());
        // Getting the object set
        ObjSet objects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",objects.getName());

        // Checking the number of detected objects
        assertEquals(8,objects.size());

        // Checking the spatial calibration and coordinate limits of each object
        HashMap<Integer,double[]> expectedValues = ExpectedObjects.get3D();

        for (Obj object:objects.values()) {
            // Getting the number of voxels in this object (this is used as the key for the expected values map)
            int nVoxels = object.getNVoxels();

            // Getting the relevant measures
            double[] expected = expectedValues.get(nVoxels);
            assertNotNull("Null means no expected object with the specified number of voxels",expected);

            // Testing coordinate ranges
            int[][] coordinateRange = object.getCoordinateRange();
            assertEquals("X-min",expected[ExpectedObjects.X_MIN],coordinateRange[0][0],tolerance);
            assertEquals("X-max",expected[ExpectedObjects.X_MAX],coordinateRange[0][1],tolerance);
            assertEquals("Y-min",expected[ExpectedObjects.Y_MIN],coordinateRange[1][0],tolerance);
            assertEquals("Y-max",expected[ExpectedObjects.Y_MAX],coordinateRange[1][1],tolerance);
            assertEquals("X-min",expected[ExpectedObjects.Z_MIN],coordinateRange[2][0],tolerance);
            assertEquals("Y-max",expected[ExpectedObjects.Z_MAX],coordinateRange[2][1],tolerance);
            assertEquals("F",expected[ExpectedObjects.F],object.getT(),tolerance);

            // Checking the objects have the correct spatial calibration
            double dppXY = object.getDistPerPxXY();
            double dppZ = object.getDistPerPxZ();
            assertEquals("Spatial calibration in XY",0.02,dppXY,tolerance);
            assertEquals("Spatial calibration in Z",0.1,dppZ,tolerance);

        }
    }
}