package wbif.sjx.MIA.Module.ImageProcessing.Pixel;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class ColourDeconvolutionTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new ColourDeconvolution(null).getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new ColourDeconvolution(null).getHelp());
    }
}