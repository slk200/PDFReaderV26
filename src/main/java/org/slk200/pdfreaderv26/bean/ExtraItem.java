package org.slk200.pdfreaderv26.bean;

/**
 * 附加选项
 */
public class ExtraItem {
    private String name;
    private String price;
    private int num;

    public ExtraItem() {
    }

    public ExtraItem(String name, String price, int num) {
        this.name = name;
        this.price = price;
        this.num = num;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return getName() + " : " + getPrice() + " : " + getNum();
    }
}
