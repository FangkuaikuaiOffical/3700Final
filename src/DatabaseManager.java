import com.google.gson.Gson;

import javax.annotation.processing.Processor;
import javax.xml.crypto.Data;
import java.sql.*;
import java.util.ArrayList;


public class DatabaseManager {

    private static DatabaseManager databaseManager;
    public static DatabaseManager getInstance() {
        if (databaseManager == null) databaseManager = new DatabaseManager();
        return databaseManager;
    }

    private Connection connection;

    private DatabaseManager() {

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:data/store.db");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

    public Message process(String requestString) {

        Gson gson = new Gson();
        Message message = gson.fromJson(requestString, Message.class);

        switch (message.getId()) {
            case Message.SEARCH_PRODUCT:{
                ProductList list = searchProduct(message.getContent());
                return new Message(Message.SEARCH_PRODUCT_RESULT,gson.toJson(list));
            }

            case Message.LOGIN_TRY:{
                Account account = gson.fromJson(message.getContent(), Account.class);
                int result = tryToLogin(account);
                if (result == 1) {
                    return new Message(Message.LOGIN_ADMIN_SUCCESS,gson.toJson(account));
                }
                else if (result == 0) {
                    return new Message(Message.LOGIN_USER_SUCCESS,gson.toJson(account));
                }
                else if (result == 3){
                    return new Message(Message.LOGIN_FAIL, "Login failed");
                }
                return new Message(Message.FAIL,"login error");
            }

            case Message.NEW_USER:{
                Account account = gson.fromJson(message.getContent(),Account.class);
                boolean result = newUser(account);
                if (result) return new Message(Message.SUCCESS, "New user registered");
                else return new Message(Message.FAIL, "Cannot registered");
            }

            case Message.CHANGE_PASSWORD:{
                Account account = gson.fromJson(message.getContent(),Account.class);
                boolean result = changePassword(account);
                if (result) return new Message(Message.SUCCESS,"Password changed");
                else return new Message(Message.FAIL,"Password change failed");
            }

            case Message.LOAD_PRODUCT: {
                Product product = loadProduct(Integer.parseInt(message.getContent()));
                return new Message(Message.LOAD_PRODUCT_REPLY, gson.toJson(product));
            }

            case Message.SAVE_PRODUCT: {
                Product product = gson.fromJson(message.getContent(), Product.class);
                boolean result = saveProduct(product);
                if (result) return new Message(Message.SUCCESS, "Product saved");
                else return new Message(Message.FAIL, "Cannot save the product");
            }

            case Message.LOAD_CUSTOMER: {
                Customer customer = loadCustomer(Integer.parseInt(message.getContent()));
                return new Message(Message.LOAD_CUSTOMER_REPLY,gson.toJson(customer));
            }

            case Message.SAVE_CUSTOMER:{
                Customer customer = gson.fromJson(message.getContent(),Customer.class);
                boolean result = saveCustomer(customer);
                if (result) return new Message(Message.SUCCESS, "Customer saved" );
                else return new Message(Message.FAIL, "Cannot Save the Customer");
            }

            case Message.LOAD_ORDER:{
                Order order = loadOrder(Integer.parseInt(message.getContent()));
                return new Message(Message.LOAD_ORDER_REPLY,gson.toJson(order));
            }

            case Message.CHECK_HISTORY:{
                ArrayList<Integer> history = checkHistory(Integer.parseInt(message.getContent()));
                return new Message(Message.CHECK_HISTORY_REPLY, gson.toJson(history));
            }

            case Message.SAVE_ORDER:{
                Order order = gson.fromJson(message.getContent(),Order.class);
                boolean result = saveOrder(order);
                if (result) return new Message(Message.NEW_USER_SUCCESS, "Order saved");
                else return new Message(Message.FAIL, "Cannot save the order");
            }

            case Message.SAVE_ORDER_LINE:{
                OrderLine orderLine = gson.fromJson(message.getContent(),OrderLine.class);
                Boolean result = saveOrderLineAndGetCost(orderLine);
                if (result) return new Message(Message.SUCCESS, "Order Line Saved");
                else return new Message(Message.FAIL,"Cannot Save Order Line");
            }

            case Message.SEARCH_ORDER:{
                String [][] orderTable = searchOrderTable(Integer.parseInt(message.getContent()));
                return new Message(Message.SEARCH_ORDER_REPLY, gson.toJson(orderTable));
            }

            case Message.DELETE_ORDER_LINE: {
                int id = Integer.parseInt(message.getContent());
                deleteOrderLine(id);
                return new Message(Message.SUCCESS,"delete success");
            }

            default:
                return new Message(Message.FAIL, "Cannot process the message");
        }
    }


    public void deleteOrderLine (int id){
        try {
            PreparedStatement preparedStatement = connection.prepareStatement
                    ("DELETE FROM OrderLine WHERE OrderID = ?");
            preparedStatement.setInt(1,id);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Integer> checkHistory (int id) {
        ArrayList<Integer> list = new ArrayList<Integer>();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement
                    ("SELECT  * FROM  Orderline WHERE CustomerID = ?");
            preparedStatement.setInt(1,id);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                list.add(resultSet.getInt(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public String[][] searchOrderTable(int id) {
        String[][] table = {{"0","0"}};
        Gson gson = new Gson();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement
                    ("SELECT * FROM Orderline WHERE OrderID = ?");
            preparedStatement.setInt(1,id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()){
                table = gson.fromJson(resultSet.getString(3), String[][].class);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  table;
    }

    public boolean saveOrderLineAndGetCost (OrderLine orderLine){
        Gson gson = new Gson();
        String productAndQuantity = gson.toJson(orderLine.getProductIDAndQuantity());

        try {
            PreparedStatement preparedStatement = connection.prepareStatement
                    ("SELECT * FROM OrderLine WHERE OrderID = ?");
            preparedStatement.setInt(1,orderLine.getOrderID());

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()){
                preparedStatement = connection.prepareStatement
                        ("UPDATE OrderLine SET CustomerID = ?, OrderLineTXT = ?, Cost = ? WHERE OrderID = ?");
                preparedStatement.setInt(1, orderLine.getCustomerID());
                preparedStatement.setString(2,productAndQuantity);
                preparedStatement.setDouble(3,orderLine.getCost());
                preparedStatement.setInt(4,orderLine.getOrderID());
            }
            else {
                preparedStatement = connection.prepareStatement
                        ("INSERT INTO OrderLine VALUES (?, ?, ?, ?)");
                preparedStatement.setInt(1,orderLine.getOrderID());
                preparedStatement.setInt(2, orderLine.getCustomerID());
                preparedStatement.setString(3,productAndQuantity);
                preparedStatement.setDouble(4,orderLine.getCost());
            }
            preparedStatement.execute();
            preparedStatement.close();
            resultSet.close();
            return true;


        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }



    public ProductList searchProduct (String keyword){
        ProductList list = new ProductList();

        try {
            Statement statement = connection.createStatement();
            String sql = "SELECT * FROM Products WHERE Name LIKE \'%" +  keyword + "%\'";
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()){
                Product product = new Product();
                product.setProductID(resultSet.getInt(1));
                product.setName(resultSet.getString(2));
                product.setPrice(resultSet.getDouble(3));
                product.setQuantity(resultSet.getDouble(4));
                list.list.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return list;
    }

    public Product loadProduct(int id) {
        try {
            String query = "SELECT * FROM Products WHERE ProductID = " + id;

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                Product product = new Product();
                product.setProductID(resultSet.getInt(1));
                product.setName(resultSet.getString(2));
                product.setPrice(resultSet.getDouble(3));
                product.setQuantity(resultSet.getDouble(4));
                resultSet.close();
                statement.close();
                return product;
            }

        } catch (SQLException e) {
            System.out.println("Database access error!");
            e.printStackTrace();
        }
        return null;
    }

    public Customer loadCustomer(int id) {
        try {
            String query = "SELECT * FROM Customers WHERE CustomerID = " + id;

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()){
                Customer customer = new Customer();
                customer.setCustomerID(resultSet.getString(1));
                customer.setFirstName(resultSet.getString(2));
                customer.setLastName(resultSet.getString(3));
                customer.setPhoneNumber(resultSet.getString(4));
                resultSet.close();
                statement.close();

                return customer;
            }

        }
        catch (SQLException e) {
            System.out.println("Database access error!");
            e.printStackTrace();
        }
        return null;
    }

    public Order loadOrder(int id) {
        try{
            String query = "SELECT * FROM Orders WHERE OrderID = " + id;
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            if(resultSet.next()){
                Order order = new Order();
                order.setOrderID(resultSet.getInt(1));
                order.setDate(resultSet.getDate(2));
                order.setCustomerID(resultSet.getInt(3));
                order.setTotalCost(resultSet.getDouble(4));
                order.setTotalTax(resultSet.getDouble(5));
                resultSet.close();
                statement.close();
                return order;
            }

        }catch (SQLException e){
            System.out.println("Database access error!");
            e.printStackTrace();
        }
        return null;
    }

    public boolean saveProduct(Product product) {
        try {
            PreparedStatement statement = connection.prepareStatement
                    ("SELECT * FROM Products WHERE ProductID = ?");
            statement.setInt(1, product.getProductID());

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) { // this product exists, update its fields
                statement = connection.prepareStatement
                        ("UPDATE Products SET Name = ?, Price = ?, Quantity = ? WHERE ProductID = ?");
                statement.setString(1, product.getName());
                statement.setDouble(2, product.getPrice());
                statement.setDouble(3, product.getQuantity());
                statement.setInt(4, product.getProductID());
            }
            else { // this product does not exist, use insert into
                statement = connection.prepareStatement("INSERT INTO Products VALUES (?, ?, ?, ?)");
                statement.setString(2, product.getName());
                statement.setDouble(3, product.getPrice());
                statement.setDouble(4, product.getQuantity());
                statement.setInt(1, product.getProductID());
            }
            statement.execute();
            resultSet.close();
            statement.close();
            return true;        // save successfully

        } catch (SQLException e) {
            System.out.println("Database access error!");
            e.printStackTrace();
            return false; // cannot save!
        }
    }

    public boolean saveCustomer(Customer customer) {
        try{
            PreparedStatement statement = connection.prepareStatement
                    ("SELECT * FROM Customers WHERE CustomerID = ?");
            statement.setString(1,customer.getCustomerID());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()){
                statement = connection.prepareStatement
                        ("UPDATE Customers SET FirstName = ?, LastName = ?, Phone = ? WHERE CustomerID = ?");
                statement.setString(1,customer.getFirstName());
                statement.setString(2,customer.getLastName());
                statement.setString(3,customer.getPhoneNumber());
                statement.setString(4,customer.getCustomerID());
            }
            else {
                statement = connection.prepareStatement("INSERT INTO Customers VALUES (?, ?, ?, ?)");
                statement.setString(2,customer.getFirstName());
                statement.setString(3,customer.getLastName());
                statement.setString(4,customer.getPhoneNumber());
                statement.setString(1,customer.getCustomerID());
            }
            statement.execute();
            resultSet.close();
            statement.close();
            return true;
        } catch (SQLException e){
            System.out.println("Database access error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveOrder(Order order){
        try{
            PreparedStatement statement = connection.prepareStatement
                    ("SELECT * FROM Orders WHERE OrderID = ?");
            statement.setInt(1, order.getOrderID());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()){
                statement = connection.prepareStatement
                        ("UPDATE Orders SET OrderDate = ?, CustomerID = ?, " +
                                "TotalCost = ?, TotalTax = ? WHERE OrderID = ?");
                statement.setDate(1,order.getDate());
                statement.setInt(2,order.getCustomerID());
                statement.setDouble(3,order.getTotalCost());
                statement.setDouble(4,order.getTotalTax());
                statement.setInt(5,order.getOrderID());
            }else {
                statement = connection.prepareStatement
                        ("INSERT INTO Orders VALUES (?, ?, ?, ?, ?)");
                statement.setDate(2,order.getDate());
                statement.setInt(3,order.getCustomerID());
                statement.setDouble(4,order.getTotalCost());
                statement.setDouble(5,order.getTotalTax());
                statement.setInt(1,order.getOrderID());
            }
            statement.execute();
            resultSet.close();
            statement.close();
            return true;

        }catch(SQLException e){
            System.out.println("Database access error!");
            e.printStackTrace();
            return false;
        }
    }

    public int tryToLogin (Account account) {
        //Manager: 1
        //User: 0
        //Error: 3
        try {
            Statement statement = connection.createStatement();
            String sql = "SELECT * FROM Users WHERE Username = \'" + account.getUsername() + "\' AND Password = \'"
                    + account.getPassword() + "\'";
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()){
                account.setId(resultSet.getInt(1));
                int i = resultSet.getInt(5);
                return i;
            }
            else return 3;
        }catch (SQLException e){
            System.out.println("Database access error!");
            e.printStackTrace();
            return 3;
        }
    }

    public boolean newUser (Account account){
        try {
            PreparedStatement checkDuplicate = connection.prepareStatement
                    ("SELECT * FROM Users WHERE Username = ?");
            checkDuplicate.setString(1,account.getUsername());
            ResultSet resultSet = checkDuplicate.executeQuery();
            if (resultSet.next()) {
                resultSet.close();
                return false;
            }
            resultSet.close();
            PreparedStatement statement = connection.prepareStatement
                ("INSERT INTO Users VALUES (NULL, ? , ?, NULL ,0)");
            statement.setString(1, account.getUsername());
            statement.setString(2, account.getPassword());
            statement.execute();
            statement.close();
            return true;
        }catch (SQLException e){
            System.out.println("Database access error!");
            e.printStackTrace();
            return false;
        }
    }

    public Boolean changePassword(Account account){
        try {
            PreparedStatement statement = connection.prepareStatement
                    ("UPDATE Users SET Password = ? WHERE UserID = ?");
            statement.setString(1,account.getPassword());
            statement.setInt(2,account.getId());
            statement.execute();
            statement.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
