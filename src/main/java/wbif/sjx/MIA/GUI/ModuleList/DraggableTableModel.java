package wbif.sjx.MIA.GUI.ModuleList;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Object.ModuleCollection;

import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class DraggableTableModel extends DefaultTableModel implements Reorderable {
    private ModuleCollection modules;

    public DraggableTableModel(Object[][] data, Object[] columnNames, ModuleCollection modules) {
        super(data, columnNames);
        this.modules = modules;

    }

    @Override
    public void reorder(int[] fromIndices, int toIndex) {
        modules.reorder(fromIndices,toIndex);

        GUI.updateModules();
        GUI.updateParameters();

    }
}
