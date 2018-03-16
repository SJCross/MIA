package wbif.sjx.ModularImageAnalysis.Module.InputOutput;

import ij.IJ;
import ij.ImagePlus;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects3D;
import wbif.sjx.ModularImageAnalysis.ExpectedSpots3D;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Refinement.MergeObjects;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import java.io.File;
import java.net.URLDecoder;

import static org.junit.Assert.*;

/**
 * Created by sc13967 on 13/11/2017.
 */
public class ImageSaverTest {
    @Test
    public void testGetTitle() throws Exception {
        assertNotNull(new ImageSaver().getTitle());
    }

    @Test
    public void testRunSaveWithInputFile() throws Exception {
        // Create a temporary folder and tell the workspace that's where the input was (even though it wasn't)
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        File testFile = temporaryFolder.newFile("TestFile.tif");

        // Creating a new workspace
        Workspace workspace = new Workspace(0,testFile);

        // Load the test image and put in the workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects2D_8bit_whiteBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising FilterObjects module
        ImageSaver imageSaver = new ImageSaver();
        imageSaver.initialiseParameters();
        imageSaver.updateParameterValue(ImageSaver.INPUT_IMAGE,"Test_image");
        imageSaver.updateParameterValue(ImageSaver.SAVE_LOCATION,ImageSaver.SaveLocations.SAVE_WITH_INPUT);
        imageSaver.updateParameterValue(ImageSaver.MIRROR_DIRECTORY_ROOT,"");
        imageSaver.updateParameterValue(ImageSaver.SAVE_FILE_PATH,"");
        imageSaver.updateParameterValue(ImageSaver.SAVE_SUFFIX,"_test");
        imageSaver.updateParameterValue(ImageSaver.FLATTEN_OVERLAY,false);

        // Running the module
        imageSaver.run(workspace,false);

        // Checking the new file exists in the temporary folder
        String[] tempFileContents = temporaryFolder.getRoot().list();
        boolean contains = false;
        for (String name:tempFileContents) {
            if (name.equals("TestFile_test.tif")) {
                contains = true;
            }
        }
        assertTrue(contains);

    }

    @Test
    public void testRunSaveAtSpecificLocation() throws Exception {
        // Create a temporary folder and tell the workspace that's where the input was (even though it wasn't)
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();

        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Load the test image and put in the workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects2D_8bit_whiteBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising FilterObjects module
        ImageSaver imageSaver = new ImageSaver();
        imageSaver.initialiseParameters();
        imageSaver.updateParameterValue(ImageSaver.INPUT_IMAGE,"Test_image");
        imageSaver.updateParameterValue(ImageSaver.SAVE_LOCATION,ImageSaver.SaveLocations.SPECIFIC_LOCATION);
        imageSaver.updateParameterValue(ImageSaver.MIRROR_DIRECTORY_ROOT,"");
        imageSaver.updateParameterValue(ImageSaver.SAVE_FILE_PATH,temporaryFolder.getRoot().getAbsolutePath()+"\\TestFile.tif");
        imageSaver.updateParameterValue(ImageSaver.SAVE_SUFFIX,"_test2");
        imageSaver.updateParameterValue(ImageSaver.FLATTEN_OVERLAY,false);

        // Running the module
        imageSaver.run(workspace,false);

        // Checking the new file exists in the temporary folder
        String[] tempFileContents = temporaryFolder.getRoot().list();
        boolean contains = false;
        for (String name:tempFileContents) {
            if (name.equals("TestFile_test2.tif")) {
                contains = true;
            }
        }
        assertTrue(contains);
    }

    @Test @Ignore
    public void testRunSaveInMirroredDirectory() throws Exception {

    }

    @Test @Ignore
    public void testRunSaveWithFlattenedOverlay() throws Exception {
    }
}