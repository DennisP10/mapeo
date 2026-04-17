package com.lsa.mapeo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "dominios")
@Data
public class Dominio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "url_dominio")
    private String urlDominio;
}