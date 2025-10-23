package com.example.Order_Service.Domains.model;

import jakarta.persistence.*;


@Entity
@Table(name = "order_items")
public class OrderItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;
    private String name; // snapshot of product name
    private int quantity;
    private double unitPrice;


    public void setProductId(Long productId){
        this.productId=productId;
    }
    public Long getProductId(){
        return productId;
    }
    public void setName(String name){
        this.name=name;
    }
    public String getName(){
        return name;
    }
    public void setQuantity(int quantity){
        this.quantity=quantity;
    }
    public int getQuantity(){
        return quantity;
    }
    public void setUnitPrice(double unitPrice){
        this.unitPrice=unitPrice;
    }

    public double getUnitPrice() {
        return unitPrice;
    }
}
