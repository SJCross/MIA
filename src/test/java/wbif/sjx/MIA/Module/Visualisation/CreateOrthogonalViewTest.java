package wbif.sjx.MIA.Module.Visualisation;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class CreateOrthogonalViewTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new CreateOrthogonalView<>(null).getDescription());
    }
}