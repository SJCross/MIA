package wbif.sjx.MIA.Module.Visualisation;

import java.awt.Color;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Text.MessageP;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.PartnerRefCollection;

/**
 * Created by Stephen Cross on 14/10/2019.
 */
public class ShowMetadata extends Module {
    public static final String MESSAGE = "Message";

    public ShowMetadata(ModuleCollection modules) {
        super("Show metadata", modules);

        // This module likely wants to have this enabled (otherwise it does nothing)
        showOutput = true;

    }

    @Override
    public String getDescription() {
        return "Displays all measurements associated with an image.";
    }

    @Override
    public String getPackageName() {
        return PackageNames.VISUALISATION;
    }

    @Override
    protected Status process(Workspace workspace) {
        if (showOutput) workspace.showMetadata();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new MessageP(MESSAGE,this,"There are no parameters for this module", Color.BLACK));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}
