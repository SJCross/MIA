package wbif.sjx.MIA.Object.Parameters;

import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.FilterImage;
import wbif.sjx.MIA.Object.ModuleCollection;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static org.junit.Assert.*;

public class DoublePTest {
    private static double tolerance = 1E-10;

    @Test
    public void getRawStringValueProvidedInteger() {
        ModuleCollection modules = new ModuleCollection();
        FilterImage filterImage = new FilterImage(modules);

        DoubleP doubleP = new DoubleP("Test val",filterImage,2);

        assertEquals("2.0",doubleP.getRawStringValue());

    }

    @Test
    public void getRawStringValueProvided4DP() {
        ModuleCollection modules = new ModuleCollection();
        FilterImage filterImage = new FilterImage(modules);

        DoubleP doubleP = new DoubleP("Test val",filterImage,3.142);

        assertEquals("3.142",doubleP.getRawStringValue());

    }

    @Test
    public void getRawStringValueProvided10DP() {
        ModuleCollection modules = new ModuleCollection();
        FilterImage filterImage = new FilterImage(modules);

        DoubleP doubleP = new DoubleP("Test val",filterImage,1.2345678901);

        assertEquals("1.2345678901",doubleP.getRawStringValue());

    }

    @Test
    public void getRawStringValueProvidedScientific() {
        ModuleCollection modules = new ModuleCollection();
        FilterImage filterImage = new FilterImage(modules);

        DoubleP doubleP = new DoubleP("Test val",filterImage,6.46E-42);

        assertEquals("6.46E-42",doubleP.getRawStringValue());

    }

    @Test
    public void getRawStringValueProvidedBlank() {
        ModuleCollection modules = new ModuleCollection();
        FilterImage filterImage = new FilterImage(modules);

        DoubleP doubleP = new DoubleP("Test val",filterImage,"");

        assertEquals("",doubleP.getRawStringValue());

    }

    @Test
    public void getValueProvidedInteger() {
        ModuleCollection modules = new ModuleCollection();
        FilterImage filterImage = new FilterImage(modules);

        DoubleP doubleP = new DoubleP("Test val",filterImage,2);

        assertEquals(2,doubleP.getValue(),tolerance);

    }

    @Test
    public void getValueProvided4dp() {
        ModuleCollection modules = new ModuleCollection();
        FilterImage filterImage = new FilterImage(modules);

        DoubleP doubleP = new DoubleP("Test val",filterImage,3.142);

        assertEquals(3.142,doubleP.getValue(),tolerance);

    }

    @Test
    public void getValueProvided10DP() {
        ModuleCollection modules = new ModuleCollection();
        FilterImage filterImage = new FilterImage(modules);

        DoubleP doubleP = new DoubleP("Test val",filterImage,1.2345678901);

        assertEquals(1.2345678901,doubleP.getValue(),tolerance);

    }

    @Test
    public void getValueProvidedScientific() {
        ModuleCollection modules = new ModuleCollection();
        FilterImage filterImage = new FilterImage(modules);

        DoubleP doubleP = new DoubleP("Test val",filterImage,6.46E-42);

        assertEquals(6.46E-42,doubleP.getValue(),tolerance);

    }

    @Test (expected = NumberFormatException.class)
    public void getValueProvidedBlank() {
        ModuleCollection modules = new ModuleCollection();
        FilterImage filterImage = new FilterImage(modules);

        DoubleP doubleP = new DoubleP("Test val",filterImage,"");

        // The following should throw the NumberFormatException
        doubleP.getValue();

    }

    @Test
    public void duplicate() {
        ModuleCollection modules = new ModuleCollection();
        FilterImage filterImage = new FilterImage(modules);

        DoubleP doubleP = new DoubleP("Test val",filterImage,42.54);
        DoubleP duplicated = doubleP.duplicate();

        assertEquals("Test val",duplicated.getName());
        assertEquals(filterImage,duplicated.getModule());
        assertFalse(duplicated.isVisible());
        assertEquals(42.54,duplicated.getValue(),tolerance);

    }

    @Test
    public void setValueFromStringProvidedInteger() {
        ModuleCollection modules = new ModuleCollection();
        FilterImage filterImage = new FilterImage(modules);

        DoubleP doubleP = new DoubleP("Test val",filterImage,"");
        doubleP.setValueFromString("2");

        assertEquals(2,doubleP.getValue(),tolerance);

    }

    @Test
    public void setValueFromStringProvided4dp() {
        ModuleCollection modules = new ModuleCollection();
        FilterImage filterImage = new FilterImage(modules);

        DoubleP doubleP = new DoubleP("Test val",filterImage,"");
        doubleP.setValueFromString("3.142");

        assertEquals(3.142,doubleP.getValue(),tolerance);

    }

    @Test
    public void setValueFromStringProvided10DP() {
        ModuleCollection modules = new ModuleCollection();
        FilterImage filterImage = new FilterImage(modules);

        DoubleP doubleP = new DoubleP("Test val",filterImage,"");
        doubleP.setValueFromString("1.2345678901");

        assertEquals(1.2345678901,doubleP.getValue(),tolerance);

    }

    @Test
    public void setValueFromStringProvidedScientific() {
        ModuleCollection modules = new ModuleCollection();
        FilterImage filterImage = new FilterImage(modules);

        DoubleP doubleP = new DoubleP("Test val",filterImage,"");
        doubleP.setValueFromString("6.46E-42");

        assertEquals(6.46E-42,doubleP.getValue(),tolerance);

    }

    @Test
    public void setValueFromStringProvidedBlank() {
        ModuleCollection modules = new ModuleCollection();
        FilterImage filterImage = new FilterImage(modules);

        DoubleP doubleP = new DoubleP("Test val",filterImage,12);
        doubleP.setValueFromString("");

        // The following should throw the NumberFormatException
        assertEquals(12,doubleP.getValue(),tolerance);

    }

    @Test
    public void verifyDouble() {
        // It shouldn't be possible to get a false value here.
        ModuleCollection modules = new ModuleCollection();
        FilterImage filterImage = new FilterImage(modules);

        DoubleP doubleP = new DoubleP("Test val",filterImage,-23.5423423);

        assertTrue(doubleP.verify());

    }

    @Test
    public void appendXMLAttributes() throws ParserConfigurationException {
        ModuleCollection modules = new ModuleCollection();
        FilterImage filterImage = new FilterImage(modules);

        DoubleP doubleP = new DoubleP("Test val",filterImage,-23.5423423);

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = doc.createElement("Test");
        doubleP.appendXMLAttributes(element);

        NamedNodeMap namedNodeMap = element.getAttributes();
        assertEquals(4,namedNodeMap.getLength());

        assertNotNull(namedNodeMap.getNamedItem("NAME"));
        assertNotNull(namedNodeMap.getNamedItem("NICKNAME"));
        assertNotNull(namedNodeMap.getNamedItem("VALUE"));
        assertNotNull(namedNodeMap.getNamedItem("VISIBLE"));

        assertEquals("Test val",namedNodeMap.getNamedItem("NAME").getNodeValue());
        assertEquals("Test val",namedNodeMap.getNamedItem("NICKNAME").getNodeValue());
        assertEquals("-23.5423423",namedNodeMap.getNamedItem("VALUE").getNodeValue());
        assertEquals("false",namedNodeMap.getNamedItem("VISIBLE").getNodeValue());

    }

    @Test
    public void setAttributesFromXML() throws ParserConfigurationException {
        ModuleCollection modules = new ModuleCollection();
        FilterImage filterImage = new FilterImage(modules);

        DoubleP doubleP = new DoubleP("Test val",filterImage,-23.5423423);

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = doc.createElement("Test");
        element.setAttribute("NAME","New name");
        element.setAttribute("NICKNAME","New nick");
        element.setAttribute("VALUE","-3.142E4");
        element.setAttribute("VISIBLE","false");

        doubleP.setAttributesFromXML(element);

        assertEquals("Test val",doubleP.getName());
        assertEquals("New nick",doubleP.getNickname());
        assertEquals(-3.142E4,doubleP.getValue(),tolerance);
        assertFalse(doubleP.isVisible());

    }
}