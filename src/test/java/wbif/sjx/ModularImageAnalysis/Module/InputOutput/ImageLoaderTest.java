package wbif.sjx.ModularImageAnalysis.Module.InputOutput;

import ij.IJ;
import ij.ImagePlus;
import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack.CropImage;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;

import static org.junit.Assert.*;

/**
 * Created by steph on 29/08/2017.
 */
public class ImageLoaderTest {
    @Test
    public void testGetTitle() throws Exception {
        assertNotNull(new ImageLoader().getTitle());

    }

    @Test
    public void testRunWithSpecificTiffFile() throws Exception {
        // Initialising a blank workspace
        Workspace workspace = new Workspace(0,null,1);

        // Initialising ImageFileLoader
        ImageLoader imageLoader = new ImageLoader();
        imageLoader.initialiseParameters();

        // Setting parameters
        imageLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.SPECIFIC_FILE);
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BlankHyperstack5D_8bit.tif").getPath(),"UTF-8");
        imageLoader.updateParameterValue(ImageLoader.FILE_PATH,pathToImage);
        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageLoader.updateParameterValue(ImageLoader.SHOW_IMAGE,false);

        // Running module
        imageLoader.run(workspace);

        // Checking there is one image in the workspace
        assertEquals(1,workspace.getImages().size());

        // Getting the loaded image
        Image image = workspace.getImage("Test_Output_Image");

        // Checking the image has the expected name
        assertEquals("Test_Output_Image",image.getName());

        // Checking there are no measurements associated with this image
        assertEquals(0,image.getMeasurements().size());

        // Checking the dimensions of the image
        assertEquals(64,image.getImagePlus().getWidth());
        assertEquals(76,image.getImagePlus().getHeight());
        assertEquals(2,image.getImagePlus().getNChannels());
        assertEquals(12,image.getImagePlus().getNSlices());
        assertEquals(6,image.getImagePlus().getNFrames());

        // Checking the image has the expected calibration
        assertEquals(0.02,image.getImagePlus().getCalibration().getX(1),1E-10);
        assertEquals(0.02,image.getImagePlus().getCalibration().getY(1),1E-10);
        assertEquals(0.1,image.getImagePlus().getCalibration().getZ(1),1E-10);

    }

    @Test
    public void testRunWithCurrentTiffFile() throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BlankHyperstack5D_8bit.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        Workspace workspace = new Workspace(0,new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader();
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.SHOW_IMAGE,false);

        // Running module
        imageFileLoader.run(workspace);

        // Checking there is one image in the workspace
        assertEquals(1,workspace.getImages().size());

        // Getting the loaded image
        Image image = workspace.getImage("Test_Output_Image");

        // Checking the image has the expected name
        assertEquals("Test_Output_Image",image.getName());

        // Checking there are no measurements associated with this image
        assertEquals(0,image.getMeasurements().size());

        // Checking the dimensions of the image
        assertEquals(64,image.getImagePlus().getWidth());
        assertEquals(76,image.getImagePlus().getHeight());
        assertEquals(2,image.getImagePlus().getNChannels());
        assertEquals(12,image.getImagePlus().getNSlices());
        assertEquals(6,image.getImagePlus().getNFrames());

        // Checking the image has the expected calibration
        assertEquals(0.02,image.getImagePlus().getCalibration().getX(1),1E-10);
        assertEquals(0.02,image.getImagePlus().getCalibration().getY(1),1E-10);
        assertEquals(0.1,image.getImagePlus().getCalibration().getZ(1),1E-10);

    }

    @Test
    public void testRunWithCurrentTiffFileBioformats() throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BlankHyperstack5D_8bit.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        Workspace workspace = new Workspace(0,new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader();
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.SHOW_IMAGE,false);

        // Running module
        imageFileLoader.run(workspace);

        // Checking there is one image in the workspace
        assertEquals(1,workspace.getImages().size());

        // Getting the loaded image
        Image image = workspace.getImage("Test_Output_Image");

        // Checking the image has the expected name
        assertEquals("Test_Output_Image",image.getName());

        // Checking there are no measurements associated with this image
        assertEquals(0,image.getMeasurements().size());

        // Checking the dimensions of the image
        assertEquals(64,image.getImagePlus().getWidth());
        assertEquals(76,image.getImagePlus().getHeight());
        assertEquals(2,image.getImagePlus().getNChannels());
        assertEquals(12,image.getImagePlus().getNSlices());
        assertEquals(6,image.getImagePlus().getNFrames());

        // Checking the image has the expected calibration
        assertEquals(0.02,image.getImagePlus().getCalibration().getX(1),1E-10);
        assertEquals(0.02,image.getImagePlus().getCalibration().getY(1),1E-10);
        assertEquals(0.1,image.getImagePlus().getCalibration().getZ(1),1E-10);

    }

    @Test
    public void testRunWithCurrentLifFile() throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BlankLif5D_8bit.lif").getPath(),"UTF-8");

        // Initialising a blank workspace
        Workspace workspace = new Workspace(0,new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader();
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.SHOW_IMAGE,false);

        // Running module
        imageFileLoader.run(workspace);

        // Checking there is one image in the workspace
        assertEquals(1,workspace.getImages().size());

        // Getting the loaded image
        Image image = workspace.getImage("Test_Output_Image");

        // Checking the image has the expected name
        assertEquals("Test_Output_Image",image.getName());

        // Checking there are no measurements associated with this image
        assertEquals(0,image.getMeasurements().size());

        // Checking the dimensions of the image
        assertEquals(64,image.getImagePlus().getWidth());
        assertEquals(64,image.getImagePlus().getHeight());
        assertEquals(2,image.getImagePlus().getNChannels());
        assertEquals(12,image.getImagePlus().getNSlices());
        assertEquals(6,image.getImagePlus().getNFrames());

        // Checking the image has the expected calibration
        assertEquals(5.55,image.getImagePlus().getCalibration().getX(1),1E-2);
        assertEquals(5.55,image.getImagePlus().getCalibration().getY(1),1E-2);
        assertEquals(2.00,image.getImagePlus().getCalibration().getZ(1),1E-2);

    }

    @Test
    public void testRunWithCropping() throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient5D_8bit.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        Workspace workspace = new Workspace(0,new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader();
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.CROP_IMAGE,true);
        imageFileLoader.updateParameterValue(CropImage.LEFT,3);
        imageFileLoader.updateParameterValue(CropImage.TOP,12);
        imageFileLoader.updateParameterValue(CropImage.WIDTH,49);
        imageFileLoader.updateParameterValue(CropImage.HEIGHT,37);
        imageFileLoader.updateParameterValue(ImageLoader.SHOW_IMAGE,false);

        // Running module
        imageFileLoader.run(workspace);

        // Getting expected image
        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/CropImage/NoisyGradient5D_8bit_3-12-52-49.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Checking there is one image in the workspace
        assertEquals(1,workspace.getImages().size());

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Test_Output_Image").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(8,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(49,outputImage.getWidth());
        assertEquals(37,outputImage.getHeight());
        assertEquals(2,outputImage.getNChannels());
        assertEquals(12,outputImage.getNSlices());
        assertEquals(4,outputImage.getNFrames());

        // Checking the individual image pixel values
        for (int c=0;c<outputImage.getNChannels();c++) {
            for (int z = 0; z < outputImage.getNSlices(); z++) {
                for (int t = 0; t < outputImage.getNFrames(); t++) {
                    expectedImage.setPosition(c+1, z + 1, t + 1);
                    outputImage.setPosition(c+1, z + 1, t + 1);

                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
                    float[][] actualValues = outputImage.getProcessor().getFloatArray();

                    assertArrayEquals(expectedValues, actualValues);

                }
            }
        }
    }

    @Test
    public void testRunWithSpecifiedCalibration() throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient5D_8bit.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        Workspace workspace = new Workspace(0,new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader();
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.CROP_IMAGE,false);
        imageFileLoader.updateParameterValue(ImageLoader.SET_CAL,true);
        imageFileLoader.updateParameterValue(ImageLoader.XY_CAL,0.5);
        imageFileLoader.updateParameterValue(ImageLoader.Z_CAL,1.2);
        imageFileLoader.updateParameterValue(ImageLoader.UNITS,"newUnits");
        imageFileLoader.updateParameterValue(ImageLoader.SHOW_IMAGE,false);

        // Running module
        imageFileLoader.run(workspace);

        // Checking there is one image in the workspace
        assertEquals(1,workspace.getImages().size());

        // Getting the loaded image
        Image image = workspace.getImage("Test_Output_Image");

        // Checking the dimensions of the image
        assertEquals(64,image.getImagePlus().getWidth());
        assertEquals(76,image.getImagePlus().getHeight());
        assertEquals(2,image.getImagePlus().getNChannels());
        assertEquals(12,image.getImagePlus().getNSlices());
        assertEquals(4,image.getImagePlus().getNFrames());

        // Checking the image has the expected calibration
        assertEquals(0.5,image.getImagePlus().getCalibration().getX(1),1E-2);
        assertEquals(0.5,image.getImagePlus().getCalibration().getY(1),1E-2);
        assertEquals(1.2,image.getImagePlus().getCalibration().getZ(1),1E-2);

    }
}