
package it.polimi.tiw.model;

public class PriceModel {

    private Float price;

    public PriceModel(Float price) {

        this.price = price;
    }

    public Float getPrice() {

        return price;
    }

    public void setPrice(Float price) {

        this.price = price;
    }

    @Override
    public String toString() {

        return "PriceModel{" + "price=" + price + '}';
    }
}
