package com.destinity.erp.sales;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

/**
 * Modelo que representa una venta dentro del sistema.
 * Este modelo esta diseñado para ser almacenado en MongoDb como documento.
 * Contiene informacion del cliente, del producto vendido y metadatos de transaccion.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleModel {

    private ObjectId id;
    private CustomerInfo customerInfo;
    private ProductSold productSold;
    private String paymentMethod;
    private Double totalAmount;
    private String status;
    private LocalDateTime saleDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Subdocumento que almacena los datos basicos del cliente relacionado a la venta.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerInfo {

        private String id;
        private String name;
        private String email;
    }

    /**
     * Subdocumento que contiene los detalles del producto vendido.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSold {

        private String id;
        private String name;
        private Double price;
        private Integer quantity;
        private Double subTotal;
    }

}
