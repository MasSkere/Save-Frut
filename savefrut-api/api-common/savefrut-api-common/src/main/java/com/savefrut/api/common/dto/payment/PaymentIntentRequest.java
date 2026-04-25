package com.savefrut.api.dto.payment;

import lombok.Data;
import java.util.List;

// Esto es lo que el cliente nos envía:
// solo la lista de items y el costo de envío.
@Data
public class PaymentIntentRequest {
    private List<Item> items;
    private Double costoEnvio;
    
    @Data
    public static class Item {
        private Long id; // ID del producto
        private int cantidad;
    }
}
