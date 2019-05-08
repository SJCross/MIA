package wbif.sjx.MIA.Object;

import org.junit.Test;
import wbif.sjx.MIA.Object.References.MeasurementRef;

import static org.junit.Assert.*;

public class MeasurementRefTest {
    @Test
    public void testConstructor() {
        MeasurementRef measurementReference = new MeasurementRef("Test name");

        assertEquals("Test name",measurementReference.getName());
        assertEquals("",measurementReference.getImageObjName());
        assertTrue(measurementReference.isAvailable());
        assertTrue(measurementReference.isExportIndividual());
    }

    @Test
    public void testToString() {
        MeasurementRef measurementReference = new MeasurementRef("Test name");

        String expected = "Measurement reference (Test name)";
        String actual = measurementReference.toString();

        assertEquals(expected,actual);
    }
}