package com.server.app.dto.finanzas;

import com.server.app.entities.Cuenta.TipoCuenta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CuentaCreateDto {

    @NotBlank(message = "El alias es obligatorio")
    @Size(max = 100, message = "El alias no puede superar 100 caracteres")
    private String alias;

    @NotBlank(message = "La moneda es obligatoria")
    @Size(min = 3, max = 10, message = "La moneda debe tener entre 3 y 10 caracteres")
    private String moneda;

    @NotNull(message = "El saldo base es obligatorio")
    @Positive(message = "El saldo base debe ser positivo")
    private BigDecimal saldoBase;

    @NotNull(message = "El tipo de cuenta es obligatorio")
    private TipoCuenta tipo;
}
