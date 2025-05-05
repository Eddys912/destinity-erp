package com.destinity.erp.inventory;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

/**
 * Modelo que representa un producto dentro del sistema.
 * Este modelo esta diseñado para ser almacenado en MongoDb como documento.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductModel {

    private ObjectId id;
    private String name;
    private Double price;
    private Integer stock;
    private String category;
    private String description;
    private String image;
    private String provider;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
