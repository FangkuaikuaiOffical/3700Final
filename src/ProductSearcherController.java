import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProductSearcherController {

    String[] resultColumnNames = {"ID", "Name","Price","Quantity"};
    String[][] resultDefaultData = {{"ID", "Name","Price","Quantity"}};
    private DefaultTableModel defaultTableModel = new DefaultTableModel(resultDefaultData,resultColumnNames);

    private JTextField keywordTF;
    private JButton searchButton;
    private JTable resultTable;
    private JPanel mainPanel;
    private Client client;




    public ProductSearcherController(Client client){

        this.client = client;

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                defaultTableModel.setRowCount(0);
                String keyword = keywordTF.getText();
                Message message = new Message(Message.SEARCH_PRODUCT,keyword);
                client.sendMessage(message);
            }
        });
    }

    public void createTable(){
        resultTable.setModel(defaultTableModel);
    }



    public void displayResult(ProductList productList) {
        for(int i = 0; i < productList.list.size();i++){
            defaultTableModel.addRow(
                    new Object[]{
                            productList.list.get(i).getProductID(),
                            productList.list.get(i).getName(),
                            productList.list.get(i).getPrice(),
                            productList.list.get(i).getQuantity()
                    }
            );

        }
    }

    public JPanel getMainPanel() {
        createTable();
        return mainPanel;
    }


}
