package com.lsa.mapeo.dto;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class CargaMasivaDTO {
    private List<Map<String, Object>> clientes;
    private String urlDominio;
    private String empresa;
}