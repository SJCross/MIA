package wbif.sjx.MIA.Module.ObjectMeasurements.Miscellaneous;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class CalculateStatsForChildrenTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new CalculateStatsForChildren(null).getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new CalculateStatsForChildren(null).getHelp());
    }
}