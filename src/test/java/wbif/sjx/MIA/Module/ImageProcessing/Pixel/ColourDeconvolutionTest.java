package wbif.sjx.MIA.Module.ImageProcessing.Pixel;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import wbif.sjx.MIA.Module.ModuleTest;

public class ColourDeconvolutionTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new ColourDeconvolution(null).getDescription());
    }
}