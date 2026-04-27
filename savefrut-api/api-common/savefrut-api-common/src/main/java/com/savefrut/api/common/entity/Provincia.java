package com.savefrut.api.common.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "provincias")
public class Provincia {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "provincia_seq")
    @SequenceGenerator(name = "provincia_seq", sequenceName = "provincias_provincia_id_seq", allocationSize = 1)
    @Column(name = "provincia_id")
    private Long provinciaId;

    @Column(name = "nombre")
    private String nombre;
}