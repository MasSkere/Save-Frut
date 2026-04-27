package com.savefrut.api.common.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "distritos")
public class Distrito {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "distrito_seq")
    @SequenceGenerator(name = "distrito_seq", sequenceName = "distritos_distrito_id_seq", allocationSize = 1)
    @Column(name = "distrito_id")
    private Long distritoId;

    @Column(name = "nombre")
    private String nombre;

    // Guardamos solo el ID, no necesitamos la relación completa
    @Column(name = "provincia_id")
    private Long provinciaId;
}