package wbif.sjx.MIA.Module.ObjectProcessing.Identification;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class RidgeDetectionTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new RidgeDetection(null).getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new RidgeDetection(null).getHelp());
    }
}