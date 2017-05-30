//TODO: Throw an error (in the IDE) if a CHOICE_ARRAY or MEASUREMENT isn't constructed with a specified valueSource

package wbif.sjx.HighContent.Object;

import wbif.sjx.HighContent.Module.HCModule;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by sc13967 on 03/05/2017.
 */
public class HCParameter implements Serializable {
    /**
     * Name of Image class objects input to the module.  Used to connect images to be analysed between classes.  Input
     * images have been created by previous modules.
     */
    public final static int INPUT_IMAGE = 0;

    /**
     * Name of Image class objects output by the module.  Used to connect images to be analysed between classes.  Output
     * images are created by this module.
     */
    public final static int OUTPUT_IMAGE = 1;

    /**
     * Name of HCObject class objects input to the module.  Used to connect HCObjects to be analysed between classes.
     * Input HCObjects have been created by previous modules.
     */
    public final static int INPUT_OBJECTS = 2;

    /**
     * Name of HCObject class objects output by the module.  Used to connect HCObjects to be analysed between classes.
     * Output HCObjects are created by this module.
     */
    public final static int OUTPUT_OBJECTS = 3;

    /**
     * Single integer variable.  These can be set in ParameterWindow by numeric fields
     */
    public final static int INTEGER = 4;

    /**
     * Single double variable.  These can be set in ParameterWindow by numeric fields
     */
    public final static int DOUBLE = 5;

    /**
     * Single string variable.  These can be set in ParameterWindow by string fields
     */
    public final static int STRING = 6;

    /**
     * String array containing choices (e.g. names of thresholding methods).  These are displayed as drop-down choice
     * menus in ParameterWindow
     */
    public final static int CHOICE_ARRAY = 7;

    /**
     * HashMap containing numeric values to be set in ParameterWindow.  ParameterWindow iterates through each of these
     * and displays it in its own numeric field
     */
    public final static int CHOICE_MAP = 8;

    /**
     * Boolean class parameter.  These are displayed by ParameterWindow as checkboxes.
     */
    public final static int BOOLEAN = 9;

    /**
     * System file parameter.  These are displayed as buttons for loading file open dialog.  This is stored as an
     * absolute path String.
     */
    public final static int FILE_PATH = 10;

    /**
     * HCMeasurement input to the module.  This could be used as a parameter for plotting, or as a value for another
     * parameter
     */
    public final static int MEASUREMENT = 11;

    /**
     * Child object of the given parent object.
     */
    public final static int CHILD_OBJECTS = 12;

    /**
     * Miscellaneous object class parameter.  These can be anything not fitting the other categories.  These can't be
     * set using ParameterWindow.
     */
    public final static int OBJECT = 13;

    private final String name;
    private final int type;
    private Object valueSource; // Where the possible values come from (used for CHOICE_ARRAY and MEASUREMENT)
    private Object value;
    private boolean visible = true;


    // CONSTRUCTORS

    public HCParameter(String name, int type, Object value, Object valueSource) {
        this.type = type;
        this.name = name;
        this.value = value;
        this.valueSource = valueSource;

    }

    public HCParameter(String name, int type, Object value) {
        this.type = type;
        this.name = name;
        this.value = value;

    }


    // GETTERS AND SETTERS

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public Object getValueSource() {
        return valueSource;
    }

    public void setValueSource(Object valueSource) {
        this.valueSource = valueSource;
    }

    public <T> T getValue() {
        return (T) value;

    }

//    public Object getValue() {
//        return value;
//    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public String toString() {
        if (type == INPUT_IMAGE | type == OUTPUT_IMAGE | type == INPUT_OBJECTS | type == OUTPUT_OBJECTS) {
            return value.toString();

        } else if (type == INTEGER) {
            return String.valueOf(value);

        } else if (type == DOUBLE) {
            return String.valueOf(value);

        } else if (type == STRING) {
            return (String) value;

        } else if (type == CHOICE_ARRAY) {
            return (String) value;

        } else if (type == CHOICE_MAP) {
            HashMap<String,Double> vals = (HashMap<String, Double>) value;

            StringBuilder stringBuilder = new StringBuilder();
            for (String key:vals.keySet()) {
                stringBuilder.append("(");
                stringBuilder.append(key);
                stringBuilder.append("/");
                stringBuilder.append(vals.get(key));
                stringBuilder.append(")");

            }

            return stringBuilder.toString();

        } else if (type == BOOLEAN) {
            return String.valueOf(value);

        } else if (type == OBJECT) {
            return value.getClass().getName();

        }

        return "";

    }
}