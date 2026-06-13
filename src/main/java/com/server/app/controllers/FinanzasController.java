package com.server.app.controllers;

import com.server.app.dto.finanzas.CuentaCreateDto;
import com.server.app.dto.finanzas.TransferenciaDto;
import com.server.app.dto.response.Pagination;
import com.server.app.entities.Categoria;
import com.server.app.entities.Cuenta;
import com.server.app.entities.Movimiento;
import com.server.app.entities.User;
import com.server.app.services.FinanzasService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/finanzas")
@AllArgsConstructor
public class FinanzasController {

    private final FinanzasService finanzasService;

    @GetMapping("/cuentas")
    public ResponseEntity<Pagination<Cuenta>> getCuentas(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(finanzasService.getCuentas(user, page, size));
    }

    @PostMapping("/cuentas")
    public ResponseEntity<Cuenta> createCuenta(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CuentaCreateDto dto) {
        return ResponseEntity.ok(finanzasService.createCuenta(user, dto));
    }

    @GetMapping("/movimientos")
    public ResponseEntity<Pagination<Movimiento>> getMovimientos(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        return ResponseEntity.ok(finanzasService.getMovimientos(user, page, size, fechaDesde, fechaHasta));
    }

    @PostMapping("/transferencias")
    public ResponseEntity<Movimiento> transferir(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody TransferenciaDto dto) {
        return ResponseEntity.ok(finanzasService.transferir(user, dto));
    }

    @GetMapping("/categorias")
    public ResponseEntity<Pagination<Categoria>> getCategorias(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(finanzasService.getCategorias(page, size));
    }
}
