import com.google.gson.Gson;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CustomerViewController {
    private JTextField customerIDTF;
    private JTextField customerFNTF;
    private JTextField customerLNTF;
    private JTextField customerPhoneTF;
    private JButton loadCustomerButton;
    private JButton saveCustomerButton;
    private JPanel mainPanel;

    private Client client;

    public CustomerViewController(Client client) {
        this.client = client;

        loadCustomerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String customerID = customerIDTF.getText();
                Message message = new Message(Message.LOAD_CUSTOMER, customerID);
                client.sendMessage(message);
            }
        });

        saveCustomerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Customer customer = new Customer();

                customer.setCustomerID(customerIDTF.getText());
                customer.setFirstName(customerFNTF.getText());
                customer.setLastName(customerLNTF.getText());
                customer.setPhoneNumber(customerPhoneTF.getText());
                Gson gson = new Gson();
                String customerString = gson.toJson(customer);

                Message message = new Message(Message.SAVE_CUSTOMER, customerString);
                client.sendMessage(message);
            }
        });
    }

    public JPanel getMainPanel() {
        if (client.account.getId() != 0){
            lockdownAndLoadID(client.account.getId());
        }
        return mainPanel;
    }

    public void lockdownAndLoadID(int id){
        customerIDTF.setText(Integer.toString(id));
        customerIDTF.setEditable(false);
        String customerID = customerIDTF.getText();
        Message message = new Message(Message.LOAD_CUSTOMER, customerID);
        client.sendMessage(message);
    }

    public void updateCustomerInfo(Customer customer) {
        customerIDTF.setText(customer.getCustomerID());
        customerFNTF.setText(customer.getFirstName());
        customerLNTF.setText(customer.getLastName());
        customerPhoneTF.setText(customer.getPhoneNumber());
    }
}
