package com.coasapp.coas.shopping;

public class ProductImages {

    String id;
    String image;

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    String urlImage;

    public int getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(int placeholder) {
        this.placeholder = placeholder;
    }

    int placeholder;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    String status;
    boolean selected;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    String source="file";
    String color;

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
