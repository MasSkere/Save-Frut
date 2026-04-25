package com.savefrut.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "vendedores")
public class Vendedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vendedor_id")
    private Long vendedorId;

    @Column(name = "ruc", nullable = false, unique = true, length = 11)
    private String ruc;

    @Column(name = "razon_social", nullable = false, length = 255)
    private String razonSocial;
    
    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @JsonIgnore // Para evitar el error de Lazy Loading
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;
        
    @Column(name = "direccion")
    private String direccion;

    @Column(name = "horario")
    private String horario;

    @Column(name = "latitud")
    private BigDecimal latitud;

    @Column(name = "longitud")
    private BigDecimal longitud;

    // Usamos EAGER para que traiga el distrito simple
    @ManyToOne(fetch = FetchType.EAGER) 
    @JoinColumn(name = "distrito_id")
    private Distrito distrito;
}