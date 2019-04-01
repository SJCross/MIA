package wbif.sjx.MIA.ExpectedObjects;

import java.util.HashMap;
import java.util.List;

public class HorizontalCylinderR22 extends ExpectedObjects {
    public enum Measures {ID_8BIT,LC_LENGTH_PX,LC_LENGTH_CAL,LC_X1_PX,LC_Y1_PX,LC_Z1_SLICE,LC_X2_PX,LC_Y2_PX,
        LC_Z2_SLICE,MEAN_DIST_PX,MEAN_DIST_CAL,MAX_DIST_PX,MAX_DIST_CAL};

    @Override
    public List<Integer[]> getCoordinates5D() {
        return getCoordinates5D("/coordinates/HorizontalBinaryCylinder3D_R22.csv");
    }

    @Override
    public boolean is2D() {
        return false;
    }

    @Override
    public HashMap<Integer, HashMap<String, Double>> getMeasurements() {
        HashMap<Integer, HashMap<String, Double>> expectedValues = new HashMap<>();

        HashMap<String, Double> obj = new HashMap<>();
        obj.put(Measures.ID_8BIT.name(), 1d);
        obj.put(Measures.LC_LENGTH_PX.name(), 67d);
        obj.put(Measures.LC_LENGTH_CAL.name(), 1.34d);
        obj.put(Measures.LC_X1_PX.name(), 29d);
        obj.put(Measures.LC_Y1_PX.name(), 4d);
        obj.put(Measures.LC_Z1_SLICE.name(), 5d);
        obj.put(Measures.LC_X2_PX.name(), 29d);
        obj.put(Measures.LC_Y2_PX.name(), 71d);
        obj.put(Measures.LC_Z2_SLICE.name(), 5d);
        obj.put(Measures.MEAN_DIST_PX.name(), 22d);
        obj.put(Measures.MEAN_DIST_CAL.name(), 0.44d);
        obj.put(Measures.MAX_DIST_PX.name(), 22d);
        obj.put(Measures.MAX_DIST_CAL.name(), 0.44d);

        expectedValues.put(1, obj);

        return expectedValues;
    }
}
