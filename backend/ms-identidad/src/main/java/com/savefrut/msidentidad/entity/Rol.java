package com.savefrut.msidentidad.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "roles")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rol_seq")
    @SequenceGenerator(name = "rol_seq", sequenceName = "roles_rol_id_seq", allocationSize = 1)
    @Column(name = "rol_id")
    private Long id;

    @Column(name = "nombre", length = 20, nullable = false, unique = true)
    private String nombre;
}
