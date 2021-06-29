package com.coasapp.coas.shopping;

/**
 * Created by AK INFOPARK on 09-05-2018.
 */

public class Products {

    String productId;
    String product;
    String sku;
    String mrp;
    String price;
    String desc;

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    boolean checked = false;

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    String count;


    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    String images;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getMrp() {
        return mrp;
    }

    public void setMrp(String mrp) {
        this.mrp = mrp;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    String status;

}
