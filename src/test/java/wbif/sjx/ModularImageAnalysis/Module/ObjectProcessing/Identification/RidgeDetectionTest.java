package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class RidgeDetectionTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new RidgeDetection().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new RidgeDetection().getHelp());
    }
}