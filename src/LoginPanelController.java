import com.google.gson.Gson;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginPanelController {
    private JButton loginButton;
    private JButton newUserButton;
    private JTextField usernameTF;
    private JPasswordField passwordTF;
    private JPanel mainPanel;
    private JPanel panel1;
    private JPanel panel2;

    private Client client;

    public JPanel getMainPanel() { return mainPanel; }

    private void closeThisWindow() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(mainPanel);
        frame.dispose();
    }


    public LoginPanelController(Client client){
        this.client = client;
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameTF.getText();
                String password = new String(passwordTF.getPassword());
                Account account = new Account(username,password);
                Gson gson = new Gson();
                String accountString = gson.toJson(account);
                Message message = new Message(Message.LOGIN_TRY,accountString);
                client.sendMessage(message);
                closeThisWindow();


            }
        });

        newUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameTF.getText();
                String password = new String(passwordTF.getPassword());
                if (username.isEmpty() || password.isEmpty()){
                    JOptionPane.showMessageDialog(null,"Username or Password cannot be blank!");
                }else {
                    Account account = new Account(username, password);
                    Gson gson = new Gson();
                    String newUserString = gson.toJson(account);
                    Message message = new Message(Message.NEW_USER, newUserString);
                    client.sendMessage(message);

                }

            }
        });
    }






}
