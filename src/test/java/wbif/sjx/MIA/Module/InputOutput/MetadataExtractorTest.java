package wbif.sjx.MIA.Module.InputOutput;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ModuleTest;
import wbif.sjx.MIA.Object.Workspace;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class MetadataExtractorTest extends ModuleTest {
    @BeforeAll
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new MetadataExtractor(null).getDescription());
    }

    @Test
    public void testRunKeywordMatching() throws IOException {
        // Creating the fake file to processAutomatic
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        File testFile = temporaryFolder.newFile("Test filename with k2 keyword.tif");

        // Creating a new workspace
        Workspace workspace = new Workspace(0,testFile,1);

        // Creating list of keywords
        String keywords = "Keyword1, k2, with gaps";

        // Setting up the module
        MetadataExtractor extractor = new MetadataExtractor(new ModuleCollection());
        extractor.initialiseParameters();
        extractor.updateParameterValue(MetadataExtractor.EXTRACTOR_MODE,MetadataExtractor.ExtractorModes.KEYWORD_MODE);
        extractor.updateParameterValue(MetadataExtractor.KEYWORD_LIST,keywords);

        // Running the module
        extractor.execute(workspace);

        // Testing the returned value
        assertEquals("k2",workspace.getMetadata().getKeyword());

    }

    @Test
    public void testRunKeywordMatchingWithGaps() throws IOException {
        // Creating the fake file to processAutomatic
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        File testFile = temporaryFolder.newFile("Test filename with gaps keyword.tif");

        // Creating a new workspace
        Workspace workspace = new Workspace(0,testFile,1);

        // Creating list of keywords
        String keywords = "Keyword1, k2, with gaps";

        // Setting up the module
        MetadataExtractor extractor = new MetadataExtractor(new ModuleCollection());
        extractor.initialiseParameters();
        extractor.updateParameterValue(MetadataExtractor.EXTRACTOR_MODE,MetadataExtractor.ExtractorModes.KEYWORD_MODE);
        extractor.updateParameterValue(MetadataExtractor.KEYWORD_LIST,keywords);

        // Running the module
        extractor.execute(workspace);

        // Testing the returned value
        assertEquals("with gaps",workspace.getMetadata().getKeyword());

    }

    @Test
    public void testRunKeywordMatchingWithSymbols() throws IOException {
        // Creating the fake file to processAutomatic
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        File testFile = temporaryFolder.newFile("Test filename with %$ keyword.tif");

        // Creating a new workspace
        Workspace workspace = new Workspace(0,testFile,1);

        // Creating list of keywords
        String keywords = "Keyword1, k2, %$, with gaps";

        // Setting up the module
        MetadataExtractor extractor = new MetadataExtractor(new ModuleCollection());
        extractor.initialiseParameters();
        extractor.updateParameterValue(MetadataExtractor.EXTRACTOR_MODE,MetadataExtractor.ExtractorModes.KEYWORD_MODE);
        extractor.updateParameterValue(MetadataExtractor.KEYWORD_LIST,keywords);

        // Running the module
        extractor.execute(workspace);

        // Testing the returned value
        assertEquals("%$",workspace.getMetadata().getKeyword());

    }

    @Test
    public void testRunKeywordMissing() throws IOException {
        // Creating the fake file to processAutomatic
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        File testFile = temporaryFolder.newFile("Test filename without keyword.tif");

        // Creating a new workspace
        Workspace workspace = new Workspace(0,testFile,1);

        // Creating list of keywords
        String keywords = "Keyword1, k2, with gaps";

        // Setting up the module
        MetadataExtractor extractor = new MetadataExtractor(new ModuleCollection());
        extractor.initialiseParameters();
        extractor.updateParameterValue(MetadataExtractor.EXTRACTOR_MODE,MetadataExtractor.ExtractorModes.KEYWORD_MODE);
        extractor.updateParameterValue(MetadataExtractor.KEYWORD_LIST,keywords);

        // Running the module
        extractor.execute(workspace);

        // Testing the returned value
        assertEquals("",workspace.getMetadata().getKeyword());

    }
}