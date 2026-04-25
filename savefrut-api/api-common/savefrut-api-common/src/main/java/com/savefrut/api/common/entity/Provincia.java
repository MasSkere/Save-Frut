package com.savefrut.api.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "provincias")
public class Provincia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "provincia_id")
    private Long provinciaId;

    @Column(name = "nombre")
    private String nombre;
}