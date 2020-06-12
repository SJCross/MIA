package wbif.sjx.MIA.Macro.ObjectProcessing;

import wbif.sjx.MIA.Macro.MacroOperationTest;

import static org.junit.jupiter.api.Assertions.*;

public class MIA_ListObjectsInWorkspaceTest extends MacroOperationTest {

    @Override
    public void testGetName() {
        assertNotNull(new MIA_ListObjectsInWorkspace(null).getName());
    }

    @Override
    public void testGetArgumentsDescription() {
        assertNotNull(new MIA_ListObjectsInWorkspace(null).getArgumentsDescription());
    }

    @Override
    public void testGetDescription() {
        assertNotNull(new MIA_ListObjectsInWorkspace(null).getDescription());
    }
}