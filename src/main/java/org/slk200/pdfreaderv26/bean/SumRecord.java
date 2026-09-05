package org.slk200.pdfreaderv26.bean;

/**
 * 累计记录
 */
public class SumRecord {
    private int page;
    private double price;
    private int num;
    private double spec;
    private String sum;

    public SumRecord() {
    }

    public SumRecord(int page, double price, int num, double spec, String sum) {
        this.page = page;
        this.price = price;
        this.num = num;
        this.spec = spec;
        this.sum = sum;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setSum(String sum) {
        this.sum = sum;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setSpec(double spec) {
        this.spec = spec;
    }

    public int getPage() {
        return page;
    }

    public double getPrice() {
        return price;
    }

    public int getNum() {
        return num;
    }

    public double getSpec() {
        return spec;
    }

    public String getSum() {
        return sum;
    }

    @Override
    public String toString() {
        return getPage() + " x " + getPrice() + " x " + getNum() + " + " + getSpec() + " = " + getSum();
    }
}
