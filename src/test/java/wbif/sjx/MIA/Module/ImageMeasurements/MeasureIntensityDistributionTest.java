package wbif.sjx.MIA.Module.ImageMeasurements;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

/**
 * Created by Stephen on 17/11/2017.
 */
public class MeasureIntensityDistributionTest extends ModuleTest {

    @BeforeClass
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new MeasureIntensityDistribution(null).getDescription());
    }

    @Test @Ignore
    public void testMeasureIntensityWeightedProximity() throws Exception {

    }

    @Test @Ignore
    public void run() throws Exception {

    }
}