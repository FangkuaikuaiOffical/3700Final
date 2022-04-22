import com.google.gson.Gson;
import java.sql.Date;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OrderViewController {
    private JButton loadOrderButton;
    private JButton saveOrderButton;
    private JTextField orderIDTF;
    private JTextField orderDateTF;
    private JTextField orderCIDTF;
    private JTextField orderCostTF;
    private JTextField orderTaxTF;
    private JPanel mainPanel;
    private Client client;

    public OrderViewController (Client client){
        this.client = client;

        loadOrderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String orderID = orderIDTF.getText();
                Message message = new Message(Message.LOAD_ORDER, orderID);
                client.sendMessage(message);
            }
        });

        saveOrderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Order order = new Order();
                order.setOrderID(Integer.parseInt(orderIDTF.getText()));
                order.setDate(Date.valueOf(orderDateTF.getText()));
                order.setCustomerID(Integer.parseInt(orderCIDTF.getText()));
                order.setTotalCost(Double.parseDouble(orderCostTF.getText()));
                order.setTotalTax(Double.parseDouble(orderTaxTF.getText()));
                Gson gson = new Gson();
                String orderString = gson.toJson(order);
                Message message = new Message(Message.SAVE_ORDER, orderString);
                client.sendMessage(message);
            }
        });
    }


    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void updateOrderInfo(Order order) {
        orderIDTF.setText(String.valueOf(order.getOrderID()));
        orderDateTF.setText(order.getDate().toString());
        orderCIDTF.setText(String.valueOf(order.getCustomerID()));
        orderCostTF.setText(String.valueOf(order.getTotalCost()));
        orderTaxTF.setText(String.valueOf(order.getTotalTax()));

    }
}
