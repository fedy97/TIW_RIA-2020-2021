
package it.polimi.tiw.bean;

import java.util.List;

import it.polimi.tiw.utils.Ignore;

public class ArticleBean {

    private String                id;
    private String                name;
    private String                description;
    private String                category;
    private String                photo;
    private String                quantity;
    private String                price;

    @Ignore
    private List<SellerOfferBean> sellers;

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public String getCategory() {

        return category;
    }

    public void setCategory(String category) {

        this.category = category;
    }

    public String getPhoto() {

        return photo;
    }

    public void setPhoto(String photo) {

        this.photo = photo;
    }

    public String getQuantity() {

        return quantity;
    }

    public void setQuantity(String quantity) {

        this.quantity = quantity;
    }

    public String getPrice() {

        return price;
    }

    public void setPrice(String price) {

        this.price = price;
    }

    public List<SellerOfferBean> getSellers() {

        return sellers;
    }

    public void setSellers(List<SellerOfferBean> sellers) {

        this.sellers = sellers;
    }

    @Override
    public String toString() {

        return "ArticleBean{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", description='" + description + '\''
                + ", category='" + category + '\'' + ", photo='" + photo + '\'' + ", quantity='" + quantity + '\''
                + ", price='" + price + '\'' + ", sellers=" + sellers + '}';
    }
}
