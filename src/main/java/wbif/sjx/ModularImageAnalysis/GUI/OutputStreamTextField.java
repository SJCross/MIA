package wbif.sjx.ModularImageAnalysis.GUI;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by sc13967 on 24/05/2017.
 */
public class OutputStreamTextField extends OutputStream {
    private final JTextField textField;
    private String currText = "";

    public OutputStreamTextField(JTextField textField) {
        this.textField = textField;

    }

    @Override
    public void write(int b) throws IOException {
        // If the current character is a return line don't add anything to the
        if (b == '\n') {
            currText = "";

        } else {
            textField.setText(currText+(char) b);
            currText = textField.getText();

        }
    }
}
