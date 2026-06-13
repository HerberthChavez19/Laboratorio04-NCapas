package com.server.app.dto.finanzas;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferenciaDto {

    @NotNull(message = "La cuenta de origen es obligatoria")
    private Long cuentaOrigenId;

    @NotNull(message = "La cuenta de destino es obligatoria")
    private Long cuentaDestinoId;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser positivo")
    private BigDecimal monto;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    private BigDecimal tasaCambio;
}
