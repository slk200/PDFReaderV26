package org.slk200.pdfreaderv26.bean;

/**
 * 附加项
 */
public class Extra {
    private String name;
    private String price;

    public Extra() {
    }

    public Extra(String name, String price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return getName() + " : " + getPrice();
    }
}
