package wbif.sjx.MIA.Module.ObjectProcessing.Identification;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class ActiveContourObjectDetectionTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new ActiveContourObjectDetection().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new ActiveContourObjectDetection().getHelp());
    }
}