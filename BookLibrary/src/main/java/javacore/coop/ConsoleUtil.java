package javacore.coop;

import javax.swing.*;
import java.util.Scanner;

class ConsoleUtil {
    public static char[] readPassword(String message, Scanner scanner) {
        if (message == null) message = "";

        if (System.console() != null)
            return System.console().readPassword(message);
        else {
            // Try with Swing component
            char[] password = null;

            JFrame frmOpt = new JFrame();
            frmOpt.setLocationRelativeTo(null); // Center of screen

            JPanel panel = new JPanel();
            JLabel label = new JLabel(message);
            final int PASSWORD_WIDTH = 20;
            JPasswordField pass = new JPasswordField(PASSWORD_WIDTH);
            panel.add(label);
            panel.add(pass);
            String[] options = new String[]{"OK", "Cancel"};

            frmOpt.setVisible(true);
            frmOpt.setAlwaysOnTop(true);
            int option = JOptionPane.showOptionDialog(frmOpt, panel, "Password",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, options, options[1]);
            if (option == 0) { // pressing OK button
                password = pass.getPassword();
            }
            frmOpt.dispose();

            return password;
        }
    }
}
