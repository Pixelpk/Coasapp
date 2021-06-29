package com.coasapp.coas.shopping;

/**
 * Created by AK INFOPARK on 05-05-2018.
 */

public class ProductCategories {

    String id;
    String name;
    String image="";

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    boolean checked;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

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

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    String parent;

    public ProductCategories(String id, String name, String parent, String image, boolean checked) {
        this.id = id;
        this.name = name;
        this.parent = parent;
        this.image = image;
        this.checked = checked;
    }
}
