import com.google.gson.Gson;

import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;

public class Server {

    private JPanel mainPanel;
    private JTextArea messageTextArea;

    private ServerSocket serverSocket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    private SecretKey secretKey;

    private byte[] initializationVector;

    private Worker worker;

    public Server() {
        try {
            serverSocket = new ServerSocket(12002);
            Socket socket = serverSocket.accept();

            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            String keyString = dataInputStream.readUTF();

            secretKey = KeyService.convertStringToSecretKeyto(keyString);

            // receive the initialization vector

            String vectorString = dataInputStream.readUTF();

            initializationVector = Base64.getDecoder().decode(vectorString);



        } catch (IOException ex) {
            ex.printStackTrace();
        }


        worker = new Worker();
        Thread workerThread = new Thread(worker);
        workerThread.start();
    }

    private class Worker implements Runnable {

        @Override
        public void run() {


            while (true) {
                String decryptedText = null;
                try {

                    String receive = dataInputStream.readUTF();

                    byte[] decode = Base64.getDecoder().decode(receive);

                    decryptedText
                            = KeyService.do_AESDecryption(
                            decode,
                            secretKey,
                            initializationVector);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                messageTextArea.append("Received: " + decryptedText + "\n");

                Message replyMessage = DatabaseManager.getInstance().process(decryptedText);

                Gson gson = new Gson();
                try {
                    String replyString = gson.toJson(replyMessage);


                    // Encrypting the message
                    // using the symmetric key
                    byte[] cipherText
                            = KeyService.do_AESEncryption(
                            replyString,
                            secretKey,
                            initializationVector);

                    String cipherTextString = Base64.getEncoder().encodeToString(cipherText);
                    dataOutputStream.writeUTF(cipherTextString);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Server");
        frame.setContentPane(new Server().mainPanel);
        frame.setMinimumSize(new Dimension(800, 400));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
