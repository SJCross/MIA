package wbif.sjx.MIA.Module.ObjectMeasurements.Spatial;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class FitEllipseTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new FitEllipse().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new FitEllipse().getHelp());
    }
}