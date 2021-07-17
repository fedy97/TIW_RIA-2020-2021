
package it.polimi.tiw.bean;

public class ShippingPolicyBean {

    private String minItem;
    private String maxItem;
    private String shipCost;

    public String getShipCost() {

        return shipCost;
    }

    public void setShipCost(String shipCost) {

        this.shipCost = shipCost;
    }

    public String getMinItem() {

        return minItem;
    }

    public void setMinItem(String minItem) {

        this.minItem = minItem;
    }

    public String getMaxItem() {

        return maxItem;
    }

    public void setMaxItem(String maxItem) {

        this.maxItem = maxItem;
    }

    @Override
    public String toString() {

        return "ShippingPolicyBean{" + "minItem='" + minItem + '\'' + ", maxItem='" + maxItem + '\'' + ", shipCost='"
                + shipCost + '\'' + '}';
    }
}
