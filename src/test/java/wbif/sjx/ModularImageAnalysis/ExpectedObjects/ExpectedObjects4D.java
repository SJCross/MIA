package wbif.sjx.ModularImageAnalysis.ExpectedObjects;

import java.util.HashMap;
import java.util.List;

/**
 * Created by sc13967 on 21/02/2018.
 */
public class ExpectedObjects4D extends ExpectedObjects {
    @Override
    public List<Integer[]> getCoordinates5D() {
        return getCoordinates5D("/coordinates/ExpectedObjects4D.csv");
    }

    @Override
    public boolean is2D() {
        return false;
    }

    @Override
    public HashMap<Integer, HashMap<String, Double>> getMeasurements() {
        return null;
    }
}
