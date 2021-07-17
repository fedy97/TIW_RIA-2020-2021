
package it.polimi.tiw.bean;

import java.io.Serializable;

public class SellerBean implements Serializable {

    private String id;
    private String sellerName;
    private String sellerRating;
    private String priceThreshold;

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
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

    public String getPriceThreshold() {

        return priceThreshold;
    }

    public void setPriceThreshold(String priceThreshold) {

        this.priceThreshold = priceThreshold;
    }
}
