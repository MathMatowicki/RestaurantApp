package com.example.restaurantapp.Model;

public class PopularCategoryModel {
    private String menu_id, food_id, name, image;

    public PopularCategoryModel() {
    }

    public PopularCategoryModel(String menu_id, String food_id, String name, String image) {
        this.menu_id = menu_id;
        this.food_id = food_id;
        this.name = name;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public String getMenu_id() {
        return menu_id;
    }

    public void setMenu_id(String menu_id) {
        this.menu_id = menu_id;
    }

    public String getFood_id() {
        return food_id;
    }

    public void setFood_id(String food_id) {
        this.food_id = food_id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
