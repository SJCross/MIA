package wbif.sjx.MIA.Module.Visualisation.Overlays;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

/**
 * Created by Stephen Cross on 29/03/2019.
 */
public class AddTracksTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new AddTracks(null).getHelp());
    }
}