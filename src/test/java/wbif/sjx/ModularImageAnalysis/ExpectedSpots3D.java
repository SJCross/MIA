package wbif.sjx.ModularImageAnalysis;

import wbif.sjx.ModularImageAnalysis.Object.Obj;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;

import java.util.HashMap;

/**
 * Created by Stephen Cross on 10/09/2017.
 */
public class ExpectedSpots3D extends ExpectedObjects {
    public int[][] getCoordinates3D() {
        return new int[][]{{1,17,10,0,8,0},
                {2,35,41,0,3,0},
                {3,24,42,0,1,0},
                {4,24,44,0,1,0},
                {5,46,44,0,6,0},
                {6,52,4,0,0,0},
                {7,19,13,0,0,0},
                {8,0,31,0,2,0},
                {9,0,75,0,5,0},
                {10,45,71,0,7,0},
                {11,63,64,0,11,0},
                {12,56,40,0,10,0},
                {13,39,27,0,3,0},
                {14,32,18,0,1,0},
                {15,21,5,0,6,0},
                {16,21,70,0,8,0},
                {17,51,65,0,10,0},
                {18,51,36,0,10,0},
                {19,15,36,0,5,0},
                {20,15,57,0,2,0},
                {21,8,17,0,2,0},
                {22,35,45,0,5,0},
                {23,36,21,0,7,0},
                {24,44,14,0,2,0},
                {25,44,14,0,7,0}};

    }

    @Override
    public HashMap<Integer, HashMap<String, Double>> getMeasurements() {
        return null;
    }
}
