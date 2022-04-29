import java.util.ArrayList;

public class OrderLine {

    private int customerID;
    private int orderID;
    private String[][] productIDAndQuantity;
    private double cost;


    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public int getOrderID() {
        return orderID;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }



    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public String[][] getProductIDAndQuantity() {
        return productIDAndQuantity;
    }

    public void setProductIDAndQuantity(String[][] productIDAndQuantity) {
        this.productIDAndQuantity = productIDAndQuantity;
    }
}
