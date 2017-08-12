package mini.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Simple dialog for diplaying error messages
 */
public class ErrorDialog extends JDialog {
    public static String DEFAULT_TITLE = "Error in application";
    public static int PADDING = 8;

    /**
     * Create a new Dialog with a title and a message.
     * @param message
     * @param title
     */
    public ErrorDialog(String message, String title) {
        setTitle(title);
        setSize(new Dimension(600, 400));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        Container container = getContentPane();
        container.setLayout(new BorderLayout());

        JTextArea textArea = new JTextArea();
        textArea.setText(message);
        textArea.setEditable(false);
        textArea.setMargin(new Insets(PADDING, PADDING, PADDING, PADDING));
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        final JDialog dialog = this;
        JButton button = new JButton(new AbstractAction("OK"){
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        add(button, BorderLayout.SOUTH);
    }

    public ErrorDialog(String message){
        this(message, DEFAULT_TITLE);
    }

    /**
     * Show a dialog with the proved message.
     * @param message
     */
    public static void showDialog(String message){
        ErrorDialog dialog = new ErrorDialog(message);
        dialog.setVisible(true);
    }
}
