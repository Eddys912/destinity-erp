package com.destinity.erp.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO expone información resumida de un producto a traves de la API.
 * Se emplea para mostrar los datos necesarios en el frontend o sistemas
 * externos, sin incluir la estructura completa del modelo.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private String id;
    private String name;
    private double price;
    private int stock;
    private String category;
    private String description;
    private String image;
    private String provider;
    private String status;

    public ProductDTO(ProductModel product) {
        this.id = (product.getId() != null)
                ? product.getId().toHexString()
                : null;
        this.name = product.getName();
        this.price = product.getPrice();
        this.stock = product.getStock();
        this.category = product.getCategory();
        this.description = product.getDescription();
        this.image = product.getImage();
        this.provider = product.getProvider();
        this.status = product.getStatus();
    }
}
