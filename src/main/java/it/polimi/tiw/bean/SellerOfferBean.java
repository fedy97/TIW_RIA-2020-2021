
package it.polimi.tiw.bean;

import java.util.List;

import it.polimi.tiw.utils.Ignore;
import it.polimi.tiw.utils.Pair;

public class SellerOfferBean {

    private String                   sellerId;
    private String                   sellerName;
    private String                   price;
    private String                   sellerRating;
    private String                   priceThreshold;

    @Ignore
    private List<ShippingPolicyBean> shippingPolicies;

    @Ignore
    private Pair<Integer, Float>     existingArticles;

    public String getSellerId() {

        return sellerId;
    }

    public void setSellerId(String sellerId) {

        this.sellerId = sellerId;
    }

    public String getSellerName() {

        return sellerName;
    }

    public void setSellerName(String sellerName) {

        this.sellerName = sellerName;
    }

    public String getPrice() {

        return price;
    }

    public void setPrice(String price) {

        this.price = price;
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

    public List<ShippingPolicyBean> getShippingPolicies() {

        return shippingPolicies;
    }

    public void setShippingPolicies(List<ShippingPolicyBean> shippingPolicies) {

        this.shippingPolicies = shippingPolicies;
    }

    public Pair<Integer, Float> getExistingArticles() {

        return existingArticles;
    }

    public void setExistingArticles(Pair<Integer, Float> existingArticles) {

        this.existingArticles = existingArticles;
    }

    @Override
    public String toString() {

        return "SellerOfferBean{" + "sellerId='" + sellerId + '\'' + ", sellerName='" + sellerName + '\'' + ", price='"
                + price + '\'' + ", sellerRating='" + sellerRating + '\'' + ", priceThreshold='" + priceThreshold + '\''
                + ", shippingPolicies=" + shippingPolicies + ", existingArticles=" + existingArticles + '}';
    }
}
