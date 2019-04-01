package wbif.sjx.MIA.Module.ImageProcessing.Pixel;

import ij.IJ;
import ij.ImagePlus;
import org.junit.BeforeClass;
import org.junit.Test;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleTest;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Workspace;

import java.net.URLDecoder;

import static org.junit.Assert.*;

/**
 * Created by sc13967 on 26/03/2018.
 */
public class InvertIntensityTest extends ModuleTest {
    @BeforeClass
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Override
    public void testGetTitle() {
        assertNotNull(new InvertIntensity().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new InvertIntensity().getHelp());
    }

    @Test
    public void testRun3D8bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageMath/NoisyGradient3D_Invert_8bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising InvertIntensity
        InvertIntensity invertIntensity = new InvertIntensity();
        invertIntensity.initialiseParameters();
        invertIntensity.updateParameterValue(ImageMath.INPUT_IMAGE,"Test_image");
        invertIntensity.updateParameterValue(ImageMath.OUTPUT_IMAGE,"Test_output");
        invertIntensity.updateParameterValue(ImageMath.APPLY_TO_INPUT,false);

        // Running Module
        invertIntensity.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRun3DApplyToInput8bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageMath/NoisyGradient3D_Invert_8bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising InvertIntensity
        InvertIntensity invertIntensity = new InvertIntensity();
        invertIntensity.initialiseParameters();
        invertIntensity.updateParameterValue(ImageMath.INPUT_IMAGE,"Test_image");
        invertIntensity.updateParameterValue(ImageMath.OUTPUT_IMAGE,"Test_output");
        invertIntensity.updateParameterValue(ImageMath.APPLY_TO_INPUT,true);

        // Running Module
        invertIntensity.execute(workspace);

        // Checking the images in the workspace
        assertEquals(1,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_image");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRun3D16bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_16bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageMath/NoisyGradient3D_Invert_16bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising InvertIntensity
        InvertIntensity invertIntensity = new InvertIntensity();
        invertIntensity.initialiseParameters();
        invertIntensity.updateParameterValue(ImageMath.INPUT_IMAGE,"Test_image");
        invertIntensity.updateParameterValue(ImageMath.OUTPUT_IMAGE,"Test_output");
        invertIntensity.updateParameterValue(ImageMath.APPLY_TO_INPUT,false);

        // Running Module
        invertIntensity.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRun3D32bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_32bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageMath/NoisyGradient3D_Invert_32bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising InvertIntensity
        InvertIntensity invertIntensity = new InvertIntensity();
        invertIntensity.initialiseParameters();
        invertIntensity.updateParameterValue(ImageMath.INPUT_IMAGE,"Test_image");
        invertIntensity.updateParameterValue(ImageMath.OUTPUT_IMAGE,"Test_output");
        invertIntensity.updateParameterValue(ImageMath.APPLY_TO_INPUT,false);

        // Running Module
        invertIntensity.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }
}