package wbif.sjx.MIA.Module.Visualisation;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class ShowImageTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new ShowImage(null).getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new ShowImage(null).getHelp());
    }
}