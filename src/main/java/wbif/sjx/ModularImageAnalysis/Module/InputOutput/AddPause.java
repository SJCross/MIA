package wbif.sjx.ModularImageAnalysis.Module.InputOutput;

import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;

import javax.swing.*;

/**
 * Created by sc13967 on 09/02/2018.
 */
public class AddPause extends Module {
    public static final String ENABLE_PAUSE = "Enable pause";

    private static final String RESUME = "Resume";
    private static final String TERMINATE = "Terminate";

    @Override
    public String getTitle() {
        return "Add pause";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected void run(Workspace workspace, boolean verbose) throws GenericMIAException {
        if (! (boolean) parameters.getValue(ENABLE_PAUSE)) return;

        String[] options = {RESUME,TERMINATE};
        JOptionPane optionPane = new JOptionPane("Execution paused.  What would you like to do?",JOptionPane.QUESTION_MESSAGE,JOptionPane.OK_CANCEL_OPTION,null,options);
        JDialog dialog = optionPane.createDialog(null, "Execution paused");
        dialog.setModal(false);
        dialog.setVisible(true);

        while (optionPane.getValue() == JOptionPane.UNINITIALIZED_VALUE) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        switch ((String) optionPane.getValue()) {
            case RESUME:
                break;

            case TERMINATE:
                System.out.println("Complete!");
                Thread.currentThread().interrupt();
                break;

        }
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(ENABLE_PAUSE,Parameter.BOOLEAN,true));
    }

    @Override
    protected void initialiseMeasurementReferences() {

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
