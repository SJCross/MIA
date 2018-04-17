package wbif.sjx.ModularImageAnalysis.Module.ImageMeasurements;

import ij.IJ;
import ij.ImagePlus;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects3D;
import wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial.MeasureObjectCentroid;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.Measurement;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;

import static org.junit.Assert.*;

public class MeasureImageIntensityTest {
    private double tolerance = 1E-2;

    @Test
    public void testGetTitle() {
        assertNotNull(new MeasureImageIntensity().getTitle());
    }

    @Test
    public void testRun2DImage8bit() throws UnsupportedEncodingException {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising MeasureImageIntensity
        MeasureImageIntensity measureImageIntensity = new MeasureImageIntensity();
        measureImageIntensity.initialiseParameters();
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.INPUT_IMAGE,"Test_image");
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_MEAN,true);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_MIN,true);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_MAX,true);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_STDEV,true);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_SUM,true);

        // Running MeasureImageIntensity
        measureImageIntensity.run(workspace);

        // Verifying results
        assertEquals(5,image.getMeasurements().size());
        assertEquals(126.1, image.getMeasurement(MeasureImageIntensity.Measurements.MEAN).getValue(),tolerance);
        assertEquals(0, image.getMeasurement(MeasureImageIntensity.Measurements.MIN).getValue(),tolerance);
        assertEquals(255, image.getMeasurement(MeasureImageIntensity.Measurements.MAX).getValue(),tolerance);
        assertEquals(73.76, image.getMeasurement(MeasureImageIntensity.Measurements.STDEV).getValue(),tolerance);
        assertEquals(613349d, image.getMeasurement(MeasureImageIntensity.Measurements.SUM).getValue(),tolerance);

    }

    @Test
    public void testRun2DImage16bit() throws UnsupportedEncodingException {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient2D_16bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising MeasureImageIntensity
        MeasureImageIntensity measureImageIntensity = new MeasureImageIntensity();
        measureImageIntensity.initialiseParameters();
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.INPUT_IMAGE,"Test_image");
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_MEAN,true);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_MIN,true);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_MAX,true);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_STDEV,true);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_SUM,true);

        // Running MeasureImageIntensity
        measureImageIntensity.run(workspace);

        // Verifying results
        assertEquals(5,image.getMeasurements().size());
        assertEquals(25209.9, image.getMeasurement(MeasureImageIntensity.Measurements.MEAN).getValue(),tolerance);
        assertEquals(20, image.getMeasurement(MeasureImageIntensity.Measurements.MIN).getValue(),tolerance);
        assertEquals(52287, image.getMeasurement(MeasureImageIntensity.Measurements.MAX).getValue(),tolerance);
        assertEquals(14743.32, image.getMeasurement(MeasureImageIntensity.Measurements.STDEV).getValue(),tolerance);
        assertEquals(122620952d, image.getMeasurement(MeasureImageIntensity.Measurements.SUM).getValue(),tolerance);

    }

    @Test
    public void testRun3DImage8bit() throws UnsupportedEncodingException {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising MeasureImageIntensity
        MeasureImageIntensity measureImageIntensity = new MeasureImageIntensity();
        measureImageIntensity.initialiseParameters();
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.INPUT_IMAGE,"Test_image");
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_MEAN,true);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_MIN,true);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_MAX,true);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_STDEV,true);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_SUM,true);

        // Running MeasureImageIntensity
        measureImageIntensity.run(workspace);

        // Verifying results
        assertEquals(5,image.getMeasurements().size());
        assertEquals(126.03, image.getMeasurement(MeasureImageIntensity.Measurements.MEAN).getValue(),tolerance);
        assertEquals(0, image.getMeasurement(MeasureImageIntensity.Measurements.MIN).getValue(),tolerance);
        assertEquals(255, image.getMeasurement(MeasureImageIntensity.Measurements.MAX).getValue(),tolerance);
        assertEquals(73.69, image.getMeasurement(MeasureImageIntensity.Measurements.STDEV).getValue(),tolerance);
        assertEquals(7355854d, image.getMeasurement(MeasureImageIntensity.Measurements.SUM).getValue(),tolerance);

    }

    @Test
    public void testRun4DImage8bit() throws UnsupportedEncodingException {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient5D_8bit_C1.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising MeasureImageIntensity
        MeasureImageIntensity measureImageIntensity = new MeasureImageIntensity();
        measureImageIntensity.initialiseParameters();
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.INPUT_IMAGE,"Test_image");
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_MEAN,true);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_MIN,true);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_MAX,true);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_STDEV,true);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_SUM,true);

        // Running MeasureImageIntensity
        measureImageIntensity.run(workspace);

        // Verifying results
        assertEquals(5,image.getMeasurements().size());
        assertEquals(126.10, image.getMeasurement(MeasureImageIntensity.Measurements.MEAN).getValue(),tolerance);
        assertEquals(0, image.getMeasurement(MeasureImageIntensity.Measurements.MIN).getValue(),tolerance);
        assertEquals(255, image.getMeasurement(MeasureImageIntensity.Measurements.MAX).getValue(),tolerance);
        assertEquals(73.50, image.getMeasurement(MeasureImageIntensity.Measurements.STDEV).getValue(),tolerance);
        assertEquals(29440126d, image.getMeasurement(MeasureImageIntensity.Measurements.SUM).getValue(),tolerance);

    }

    @Test
    public void testRun5DImage8bit() throws UnsupportedEncodingException {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient5D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising MeasureImageIntensity
        MeasureImageIntensity measureImageIntensity = new MeasureImageIntensity();
        measureImageIntensity.initialiseParameters();
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.INPUT_IMAGE,"Test_image");
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_MEAN,true);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_MIN,true);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_MAX,true);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_STDEV,true);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_SUM,true);

        // Running MeasureImageIntensity
        measureImageIntensity.run(workspace);

        // Verifying results
        assertEquals(5,image.getMeasurements().size());
        assertEquals(126.10, image.getMeasurement(MeasureImageIntensity.Measurements.MEAN).getValue(),tolerance);
        assertEquals(0, image.getMeasurement(MeasureImageIntensity.Measurements.MIN).getValue(),tolerance);
        assertEquals(255, image.getMeasurement(MeasureImageIntensity.Measurements.MAX).getValue(),tolerance);

        // Note: the IntensityCalculator will only measure intensity for the currently-selected channel
        assertEquals(73.50, image.getMeasurement(MeasureImageIntensity.Measurements.STDEV).getValue(),tolerance);
        assertEquals(29440126d, image.getMeasurement(MeasureImageIntensity.Measurements.SUM).getValue(),tolerance);

    }
}