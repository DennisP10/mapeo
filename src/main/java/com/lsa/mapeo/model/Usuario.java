package com.lsa.mapeo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "usuarios")
@Data
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String correo;

    @ManyToOne
    @JoinColumn(name = "dominio_id")
    private Dominio dominio;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;
}