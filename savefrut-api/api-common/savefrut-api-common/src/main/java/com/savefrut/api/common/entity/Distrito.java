package com.savefrut.api.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "distritos")
public class Distrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "distrito_id")
    private Long distritoId;

    @Column(name = "nombre")
    private String nombre;

    // Guardamos solo el ID, no necesitamos la relación completa
    @Column(name = "provincia_id")
    private Long provinciaId;
}