package com.savefrut.mscatalogo.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tipo_medicion")
public class TipoMedicion {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "medicion_seq")
    @SequenceGenerator(name = "medicion_seq", sequenceName = "tipo_medicion_medicion_id_seq", allocationSize = 1)
    @Column(name = "medicion_id")
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;
}
