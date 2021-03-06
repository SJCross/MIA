package wbif.sjx.MIA.Module.InputOutput;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URLDecoder;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ij.IJ;
import ij.ImagePlus;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ModuleTest;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.WorkspaceCollection;

/**
 * Created by sc13967 on 13/11/2017.
 */
public class ImageSaverTest extends ModuleTest {
    @BeforeAll
    public static void setVerbose() {
        Module.setVerbose(false);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new ImageSaver(null).getDescription());
    }

    @Test
    public void testRunSaveWithInputFileWithSeriesNumber(@TempDir Path tempPath) throws Exception {
        File temporaryFolder = tempPath.toFile();
        File testFile = new File(tempPath+File.separator+"TestFile.tif");
        testFile.createNewFile();

        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(testFile,1);

        // Load the test image and put in the workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects2D_8bit_whiteBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising FilterObjects module
        ImageSaver imageSaver = new ImageSaver(new ModuleCollection());
        imageSaver.initialiseParameters();
        imageSaver.updateParameterValue(ImageSaver.INPUT_IMAGE,"Test_image");
        imageSaver.updateParameterValue(ImageSaver.SAVE_LOCATION,ImageSaver.SaveLocations.SAVE_WITH_INPUT);
        imageSaver.updateParameterValue(ImageSaver.MIRROR_DIRECTORY_ROOT,"");
        imageSaver.updateParameterValue(ImageSaver.SAVE_FILE_PATH,"");
        imageSaver.updateParameterValue(ImageSaver.APPEND_SERIES_MODE,ImageSaver.AppendSeriesModes.SERIES_NUMBER);
        imageSaver.updateParameterValue(ImageSaver.APPEND_DATETIME_MODE,ImageSaver.AppendDateTimeModes.NEVER);
        imageSaver.updateParameterValue(ImageSaver.SAVE_SUFFIX,"_test");
        imageSaver.updateParameterValue(ImageSaver.FLATTEN_OVERLAY,false);

        // Running the module
        imageSaver.execute(workspace);

        // Checking the new file exists in the temporary folder
        String[] tempFileContents = temporaryFolder.list();
        boolean contains = false;
        for (String name:tempFileContents) {
            if (name.equals("TestFile_S1_test.tif")) {
                contains = true;
            }
        }
        assertTrue(contains);

    }

    @Test
    public void testRunSaveAtSpecificLocationWithSeriesNumber(@TempDir Path tempPath) throws Exception {
        File temporaryFolder = tempPath.toFile();

        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Load the test image and put in the workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects2D_8bit_whiteBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising FilterObjects module
        ImageSaver imageSaver = new ImageSaver(new ModuleCollection());
        imageSaver.initialiseParameters();
        imageSaver.updateParameterValue(ImageSaver.INPUT_IMAGE,"Test_image");
        imageSaver.updateParameterValue(ImageSaver.SAVE_LOCATION,ImageSaver.SaveLocations.SPECIFIC_LOCATION);
        imageSaver.updateParameterValue(ImageSaver.MIRROR_DIRECTORY_ROOT,"");
        imageSaver.updateParameterValue(ImageSaver.SAVE_NAME_MODE,ImageSaver.SaveNameModes.SPECIFIC_NAME);
        imageSaver.updateParameterValue(ImageSaver.SAVE_FILE_NAME,"TestFile.tif");
        imageSaver.updateParameterValue(ImageSaver.SAVE_FILE_PATH,temporaryFolder.getAbsolutePath());
        imageSaver.updateParameterValue(ImageSaver.APPEND_SERIES_MODE,ImageSaver.AppendSeriesModes.SERIES_NUMBER);
        imageSaver.updateParameterValue(ImageSaver.APPEND_DATETIME_MODE,ImageSaver.AppendDateTimeModes.NEVER);
        imageSaver.updateParameterValue(ImageSaver.SAVE_SUFFIX,"_test2");
        imageSaver.updateParameterValue(ImageSaver.FLATTEN_OVERLAY,false);

        // Running the module
        imageSaver.execute(workspace);

        // Checking the new file exists in the temporary folder
        String[] tempFileContents = temporaryFolder.list();
        boolean contains = false;
        for (String name:tempFileContents) {
            if (name.equals("TestFile_S1_test2.tif")) {
                contains = true;
            }
        }
        assertTrue(contains);
    }

    @Test @Disabled
    public void testRunSaveInMirroredDirectory() throws Exception {

    }

    @Test @Disabled
    public void testRunSaveWithFlattenedOverlay() throws Exception {
    }
}