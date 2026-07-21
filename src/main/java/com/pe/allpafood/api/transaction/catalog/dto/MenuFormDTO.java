package com.pe.allpafood.api.transaction.catalog.dto;

import com.pe.allpafood.api.transaction.plan.entities.benefits.DetailEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class MenuFormDTO{
    private Integer id;
    @NotNull(message = "El nombre del plato es obligatorio.")
    @NotBlank(message = "El nombre del plato es obligatorio.")
    private String name;

    @NotNull(message = "La descripción del plato es obligatoria.")
    @NotBlank(message = "La descripción del plato es obligatoria.")
    private String description;

    private List<String> types;

    @NotNull(message = "El precio real del plato es obligatoria.")
    @NotBlank(message = "El precio real del plato es obligatoria.")
    private float price;

    @NotNull(message = "El precio promocional del plato es obligatoria.")
    @NotBlank(message = "El precio promocional del plato es obligatoria.")
    private float previousPrice;

    @NotNull(message = "Las propiedades del plato son obligatorias.")
    private List<DetailEntity<String>> properties;

    private MultipartFile image;


}
