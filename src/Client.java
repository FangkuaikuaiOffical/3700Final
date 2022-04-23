import com.google.gson.Gson;

import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Base64;

public class Client {

    public Account account;
    private JTextArea messageTextArea;
    private JButton manageCheckoutButton;
    private JButton manageProductButton;
    private JButton manageCustomerButton;
    private JButton loginButton;
    private JPanel mainPanel;
    private JButton logoutButton;
    private JButton searchProductButton;
    private JButton changePasswordButton;

    private SecretKey secretKey;
    private byte[] initializationVector;

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    private Gson gson;

    private Worker worker;

    private ProductViewController productViewController;
    private CustomerViewController customerViewController;
    private OrderViewController orderViewController;
    private LoginPanelController loginPanelController;
    private ProductSearcherController productSearcherController;
    private ChangePasswordPanelController changePasswordPanelController;





    public void LogoutAndHideButtons(){
        loginButton.setVisible(true);
        logoutButton.setVisible(false);
        manageCheckoutButton.setVisible(false);
        manageCustomerButton.setVisible(false);
        manageProductButton.setVisible(false);
    }
    public void ShowManagerButtons(){
        manageProductButton.setVisible(true);
        manageCustomerButton.setVisible(true);
        loginButton.setVisible(false);
        logoutButton.setVisible(true);
    }
    public void ShowUserButtons(){
        loginButton.setVisible(false);
        manageCheckoutButton.setVisible(true);
        manageCustomerButton.setVisible(true);
        logoutButton.setVisible(true);
    }



    public Client() {
        account = new Account("0","0");
        account.setId(0);
        try {
            socket = new Socket(InetAddress.getByName("127.0.0.1"), 12002);
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            secretKey = KeyService.createAESKey();

            String keyString = KeyService.convertSecretKeyToString(secretKey);

            dataOutputStream.writeUTF(keyString);

            // send the initialization vector

            initializationVector = KeyService.createInitializationVector();

            String vectorString = Base64.getEncoder().encodeToString(initializationVector);

            dataOutputStream.writeUTF(vectorString);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        gson = new Gson();

        worker = new Worker();
        Thread workerThread = new Thread(worker);
        workerThread.start();

        this.productViewController = new ProductViewController(this);
        this.customerViewController = new CustomerViewController(this);
        this.orderViewController = new OrderViewController(this);
        this.loginPanelController = new LoginPanelController(this);
        this.productSearcherController = new ProductSearcherController(this);
        this.changePasswordPanelController = new ChangePasswordPanelController(this);


        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LogoutAndHideButtons();
            }
        });

        changePasswordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame("Change Password");
                frame.setContentPane(changePasswordPanelController.getMainPanel());
                frame.setMinimumSize(new Dimension(800, 400));
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
            }
        });

        manageProductButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame("Manage Product");
                frame.setContentPane(productViewController.getMainPanel());
                frame.setMinimumSize(new Dimension(800, 400));
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
            }
        });

        manageCustomerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame("Manage Customer");
                frame.setContentPane(customerViewController.getMainPanel());
                frame.setMinimumSize(new Dimension(800,400));
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
            }
        });

        manageCheckoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame("Manage Order");
                frame.setContentPane(orderViewController.getMainPanel());
                frame.setMinimumSize(new Dimension(800,400));
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
            }
        });

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame("Login");
                frame.setContentPane(loginPanelController.getMainPanel());
                frame.setMinimumSize(new Dimension(800,400));
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
            }
        });

        searchProductButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame("Search");
                frame.setContentPane(productSearcherController.getMainPanel());
                frame.setMinimumSize(new Dimension(800,400));
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
            }
        });
    }

    public void sendMessage(Message message) {

        String str = gson.toJson(message);
        try {
            // Encrypting the message
            // using the symmetric key
            byte[] cipherText
                    = new byte[0];
            try {
                cipherText = KeyService.do_AESEncryption(
                str,
                secretKey,
                initializationVector);
            } catch (Exception e) {
                e.printStackTrace();
            }

            String cipherTextString = Base64.getEncoder().encodeToString(cipherText);

            dataOutputStream.writeUTF(cipherTextString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private class Worker implements Runnable {

        @Override
        public void run() {
            while (true) {
                String replyString = null;
                try {
                    replyString = dataInputStream.readUTF();

                    byte[] decode = Base64.getDecoder().decode(replyString);

                    try {
                        replyString
                                = KeyService.do_AESDecryption(
                                decode,
                                secretKey,
                                initializationVector);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Message message = gson.fromJson(replyString, Message.class);
                processMessage(message);

            }
        }
    }

    private void processMessage(Message message) {
        messageTextArea.append(gson.toJson(message) + "\n");
        switch (message.getId()) {
            case Message.LOAD_PRODUCT_REPLY: {
                Product product = gson.fromJson(message.getContent(), Product.class);
                productViewController.updateProductInfo(product);
                break;
            }
            case Message.LOAD_CUSTOMER_REPLY:{
                Customer customer = gson.fromJson(message.getContent(),Customer.class);
                customerViewController.updateCustomerInfo(customer);
                break;
            }
            case Message.LOAD_ORDER_REPLY:{
                Order order = gson.fromJson(message.getContent(),Order.class);
                orderViewController.updateOrderInfo(order);
                break;
            }
            case Message.LOGIN_ADMIN_SUCCESS:{
                Account justLogin = gson.fromJson(message.getContent(),Account.class);
                account.setId(justLogin.getId());
                JOptionPane.showMessageDialog(null,"Admin login successful");
                ShowManagerButtons();
                break;
            }
            case Message.LOGIN_USER_SUCCESS:{
                Account justLogin = gson.fromJson(message.getContent(),Account.class);
                account.setId(justLogin.getId());
                JOptionPane.showMessageDialog(null,"User login successful");
                ShowUserButtons();
                break;
            }
            case Message.LOGIN_FAIL:{
                JOptionPane.showMessageDialog(null,"Login failed");
                break;
            }
            case Message.NEW_USER_SUCCESS:{
                JOptionPane.showMessageDialog(null,"New user registered");
            }

            case Message.SEARCH_PRODUCT_RESULT:{
                ProductList productList = gson.fromJson(message.getContent(),ProductList.class);
                productSearcherController.displayResult(productList);


            }

            default:
        }

    }

    public static void main(String[] args) {

        JFrame frame = new JFrame("Client");
        frame.setContentPane(new Client().mainPanel);
        frame.setMinimumSize(new Dimension(800, 400));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);


    }
}
