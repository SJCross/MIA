package wbif.sjx.MIA.Module.Hidden;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import wbif.sjx.MIA.Module.ModuleTest;
import wbif.sjx.MIA.Module.Core.InputControl;

public class InputControlTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new InputControl(null).getDescription());
    }
}