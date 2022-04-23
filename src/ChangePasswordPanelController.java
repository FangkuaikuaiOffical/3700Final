import com.google.gson.Gson;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChangePasswordPanelController {
    private JPanel mainPanel;
    private JPasswordField passwordField;
    private JButton updateNewPasswordButton;

    private Client client;

    public ChangePasswordPanelController(Client client){
        this.client = client;

        updateNewPasswordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Account account = new Account(null,null);
                account.setId(client.account.getId());
                account.setPassword(new String(passwordField.getPassword()));
                Gson gson = new Gson();
                String accountString = gson.toJson(account);
                Message message = new Message(Message.CHANGE_PASSWORD,accountString);
                client.sendMessage(message);

            }
        });
    }



    public JPanel getMainPanel() {
        return mainPanel;
    }
}
