package wbif.sjx.MIA.Module.Miscellaneous;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class ConditionalAnalysisTerminationTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new ConditionalAnalysisTermination(null).getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new ConditionalAnalysisTermination(null).getHelp());
    }
}