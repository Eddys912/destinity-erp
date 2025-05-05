package com.destinity.erp.sales;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO expone información resumida de una venta a traves de la API.
 * Se emplea para mostrar los datos necesarios en el frontend o sistemas
 * externos, sin incluir la estructura completa del modelo.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleDTO {

    private String id;
    private String name;
    private String payment;
    private Double total;
    private String status;
    private LocalDateTime sale;

    public SaleDTO(SaleModel sale) {
        this.id = (sale.getId() != null)
                ? sale.getId().toHexString()
                : null;
        this.name = (sale.getCustomerInfo() != null)
                ? sale.getCustomerInfo().getName()
                : null;
        this.payment = sale.getPaymentMethod();
        this.total = sale.getTotalAmount();
        this.status = sale.getStatus();
        this.sale = sale.getSaleDate();
    }
}
