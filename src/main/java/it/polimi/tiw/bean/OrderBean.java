
package it.polimi.tiw.bean;

import java.io.Serializable;
import java.util.List;

public class OrderBean implements Serializable {

    private String            id;
    private String            sellerId;
    private String            priceArticles;
    private String            priceShipment;
    private String            shipmentDate;
    private String            userId;
    private String            sellerName;
    private String            sellerRating;
    private String            name;
    private String            surname;
    private String            email;
    private String            shipmentAddr;
    private String            orderDate;
    private String priceTotal;
    private List<ArticleBean> articleBeans;

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getSellerId() {

        return sellerId;
    }

    public void setSellerId(String sellerId) {

        this.sellerId = sellerId;
    }

    public String getPriceArticles() {

        return priceArticles;
    }

    public void setPriceArticles(String priceArticles) {

        this.priceArticles = priceArticles;
    }

    public String getPriceShipment() {

        return priceShipment;
    }

    public void setPriceShipment(String priceShipment) {

        this.priceShipment = priceShipment;
    }

    public String getShipmentDate() {

        return shipmentDate;
    }

    public void setShipmentDate(String shipmentDate) {

        this.shipmentDate = shipmentDate;
    }

    public String getUserId() {

        return userId;
    }

    public void setUserId(String userId) {

        this.userId = userId;
    }

    public String getSellerName() {

        return sellerName;
    }

    public void setSellerName(String sellerName) {

        this.sellerName = sellerName;
    }

    public String getSellerRating() {

        return sellerRating;
    }

    public void setSellerRating(String sellerRating) {

        this.sellerRating = sellerRating;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getSurname() {

        return surname;
    }

    public void setSurname(String surname) {

        this.surname = surname;
    }

    public String getEmail() {

        return email;
    }

    public void setEmail(String email) {

        this.email = email;
    }

    public String getShipmentAddr() {

        return shipmentAddr;
    }

    public void setShipmentAddr(String shipmentAddr) {

        this.shipmentAddr = shipmentAddr;
    }

    public String getOrderDate() {

        return orderDate;
    }

    public void setOrderDate(String orderDate) {

        this.orderDate = orderDate;
    }

    public List<ArticleBean> getArticleBeans() {

        return articleBeans;
    }

    public void setArticleBeans(List<ArticleBean> articleBeans) {

        this.articleBeans = articleBeans;
    }

    public String getPriceTotal() {
        return priceTotal;
    }

    public void setPriceTotal(String priceTotal) {
        this.priceTotal = priceTotal;
    }

    @Override
    public String toString() {
        return "OrderBean{" +
                "id='" + id + '\'' +
                ", sellerId='" + sellerId + '\'' +
                ", priceArticles='" + priceArticles + '\'' +
                ", priceShipment='" + priceShipment + '\'' +
                ", shipmentDate='" + shipmentDate + '\'' +
                ", userId='" + userId + '\'' +
                ", sellerName='" + sellerName + '\'' +
                ", sellerRating='" + sellerRating + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", email='" + email + '\'' +
                ", shipmentAddr='" + shipmentAddr + '\'' +
                ", orderDate='" + orderDate + '\'' +
                ", priceTotal='" + priceTotal + '\'' +
                ", articleBeans=" + articleBeans +
                '}';
    }
}
