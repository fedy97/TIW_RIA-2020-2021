
package it.polimi.tiw.utils;

public class SellerArticleEntity {

    private String sellerId;
    private String articleId;
    private String price;

    public String getSellerId() {

        return sellerId;
    }

    public void setSellerId(String sellerId) {

        this.sellerId = sellerId;
    }

    public String getArticleId() {

        return articleId;
    }

    public void setArticleId(String articleId) {

        this.articleId = articleId;
    }

    public String getPrice() {

        return price;
    }

    public void setPrice(String price) {

        this.price = price;
    }

    @Override
    public String toString() {

        return "SellerArticleEntry{" + "sellerId='" + sellerId + '\'' + ", articleId='" + articleId + '\'' + ", price='"
                + price + '\'' + '}';
    }
}
