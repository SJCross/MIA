package wbif.sjx.MIA.Module.ImageProcessing.Pixel;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class ColourDeconvolutionTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new ColourDeconvolution(null).getDescription());
    }
}