package com.authapp.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permisos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PermisoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nombre;

    @Column
    private String descripcion;
}
