package com.savefrut.api.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tipo_medicion")
public class TipoMedicion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medicion_id")
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;
}