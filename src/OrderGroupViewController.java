import com.google.gson.Gson;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OrderGroupViewController {

    String[] orderColumnNames = {"ID","Quantity"};
    String[][] orderData = {{"0","0"}};
    private DefaultTableModel defaultTableModel = new DefaultTableModel(orderData,orderColumnNames);
    private JTable orderTable;
    private JButton confirmOrderButton;
    private JPanel mainPanel;
    private JTextField orderIDTF;
    private JButton deleteOrderButton;
    private JButton addALineButton;
    private JButton deleteLastLineButton;
    private JButton loadOrderButton;
    public JTextField historyTF;
    private JButton historyButton;
    private Client client;


    private void createTable() {
        orderTable.setModel(defaultTableModel);
    }

    private String[][] writeOrderListFromTable() {
        String[][] order = new String[defaultTableModel.getRowCount()][defaultTableModel.getColumnCount()];
        for (int i = 0; i < defaultTableModel.getRowCount(); i++){
            for (int j = 0; j < defaultTableModel.getColumnCount(); j++ ){
                order[i][j] = (defaultTableModel.getValueAt(i,j).toString());
            }
        }
        return order;
    }

    public void writeTableFromOrderList(String[][] order) {
        defaultTableModel.setRowCount(0);
        for(int i = 0; i < order.length; i++){
            defaultTableModel.addRow(order[i]);
        }
    }


    public OrderGroupViewController(Client client) {

        this.client = client;

        addALineButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                defaultTableModel.addRow(new Integer[]{0,0});
            }
        });

        deleteLastLineButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                defaultTableModel.removeRow(defaultTableModel.getRowCount() - 1);
            }
        });

        historyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int id = client.account.getId();
                Message message = new Message(Message.CHECK_HISTORY, String.valueOf(id));
                client.sendMessage(message);
            }
        });

        confirmOrderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OrderLine orderLine = new OrderLine();
                orderLine.setCustomerID(client.account.getId());
                orderLine.setOrderID(Integer.parseInt(orderIDTF.getText()));
                orderLine.setProductIDAndQuantity(writeOrderListFromTable());
                orderLine.setCost(0); //Cost is too complex to get. I give up.

                Gson gson = new Gson();
                String orderLineString = gson.toJson(orderLine);
                Message message = new Message(Message.SAVE_ORDER_LINE, orderLineString);
                client.sendMessage(message);
            }
        });

        loadOrderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String id = orderIDTF.getText();
                Message message = new Message(Message.SEARCH_ORDER, id);
                client.sendMessage(message);


            }
        });

        deleteOrderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int id = Integer.parseInt(orderIDTF.getText());
//                Gson gson = new Gson();
//                String idString = gson.toJson(id);
                Message message = new Message(Message.DELETE_ORDER_LINE, String.valueOf(id));
                client.sendMessage(message);
            }
        });

    }

//    public double getTotal (String[][] table){
//        int cost = 0;
//        for (int i = 0; i < table.length; i++ ){
//            Message message = new Message(Message.CHECK_COST, table[i][0]);
//            client.sendMessage(message);
//        }
//        return cost;
//    }

//    public void showTotalCostDialog (int total) {
//        JOptionPane.showMessageDialog(null,"Your order total is: " + total);
//    }

    public JPanel getMainPanel() {
        createTable();
        return mainPanel;
    }
}
