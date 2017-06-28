package wbif.sjx.ModularImageAnalysis.Object;

import java.util.LinkedHashMap;

/**
 * Created by sc13967 on 12/05/2017.
 */
public class HCObjectSet extends LinkedHashMap<Integer,HCObject> {
    private String name;
    private int maxID = 0;

    public HCObjectSet(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void add(HCObject object) {
        put(object.getID(),object);

    }

    public int getNextID() {
        maxID++;
        return maxID;
    }
}
