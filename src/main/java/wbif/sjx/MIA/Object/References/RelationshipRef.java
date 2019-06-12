package wbif.sjx.MIA.Object.References;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import wbif.sjx.MIA.Object.References.Abstract.SummaryRef;

public class RelationshipRef extends SummaryRef {
    private final String parentName;
    private final String childName;
    private String description = "";


    public RelationshipRef(Node node) {
        super(node);

        NamedNodeMap map = node.getAttributes();
        this.childName = map.getNamedItem("CHILD_NAME").getNodeValue();
        this.parentName = map.getNamedItem("PARENT_NAME").getNodeValue();

        setAttributesFromXML(node);

    }

    public RelationshipRef(String parentName, String childName) {
        super(createName(parentName,childName));
        this.parentName = parentName;
        this.childName = childName;

    }

    @Override
    public void appendXMLAttributes(Element element)  {
        super.appendXMLAttributes(element);

        element.setAttribute("CHILD_NAME",childName);
        element.setAttribute("PARENT_NAME",parentName);

    }

    @Override
    public void setAttributesFromXML(Node node) {
        super.setAttributesFromXML(node);

    }

    public String getParentName() {
        return parentName;
    }

    public String getChildName() {
        return childName;
    }

    public static String createName(String parent, String child) {
        return parent+" // "+child;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}