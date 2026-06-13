package com.server.app.services;

import com.server.app.dto.finanzas.CuentaCreateDto;
import com.server.app.dto.finanzas.TransferenciaDto;
import com.server.app.dto.response.Pagination;
import com.server.app.dto.response.PaginationMeta;
import com.server.app.entities.Categoria;
import com.server.app.entities.Cuenta;
import com.server.app.entities.Movimiento;
import com.server.app.entities.User;
import com.server.app.exceptions.BadRequestException;
import com.server.app.exceptions.ForbiddenException;
import com.server.app.exceptions.NotFoundException;
import com.server.app.repositories.CategoriaRepository;
import com.server.app.repositories.CuentaRepository;
import com.server.app.repositories.MovimientoRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class FinanzasService {

    private final CuentaRepository cuentaRepository;
    private final CategoriaRepository categoriaRepository;
    private final MovimientoRepository movimientoRepository;

    public Pagination<Cuenta> getCuentas(User user, int page, int size) {
        Page<Cuenta> result = cuentaRepository.findByUsuarioId(user.getId(), PageRequest.of(page - 1, size));
        return buildPagination(result);
    }

    @Transactional
    public Cuenta createCuenta(User user, CuentaCreateDto dto) {
        Cuenta cuenta = Cuenta.builder()
                .alias(dto.getAlias())
                .moneda(dto.getMoneda().toUpperCase())
                .saldoBase(dto.getSaldoBase())
                .tipo(dto.getTipo())
                .usuario(user)
                .build();
        return cuentaRepository.save(cuenta);
    }

    public Pagination<Movimiento> getMovimientos(User user, int page, int size,
                                                  LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        Page<Movimiento> result = movimientoRepository.findByUsuarioIdAndFechas(
                user.getId(), fechaDesde, fechaHasta, PageRequest.of(page - 1, size));
        return buildPagination(result);
    }

    @Transactional
    public Movimiento transferir(User user, TransferenciaDto dto) {
        Cuenta origen = cuentaRepository.findById(dto.getCuentaOrigenId())
                .orElseThrow(() -> new NotFoundException("Cuenta de origen no encontrada"));

        if (origen.getUsuario().getId() != user.getId()) {
            throw new ForbiddenException("No tienes permiso sobre la cuenta de origen");
        }

        Cuenta destino = cuentaRepository.findById(dto.getCuentaDestinoId())
                .orElseThrow(() -> new NotFoundException("Cuenta de destino no encontrada"));

        if (origen.getId().equals(destino.getId())) {
            throw new BadRequestException("La cuenta de origen y destino no pueden ser la misma");
        }

        if (origen.getSaldoBase().compareTo(dto.getMonto()) < 0) {
            throw new BadRequestException("Saldo insuficiente en la cuenta de origen");
        }

        BigDecimal tasa = dto.getTasaCambio() != null ? dto.getTasaCambio() : BigDecimal.ONE;
        BigDecimal montoConvertido = dto.getMonto().multiply(tasa);

        origen.setSaldoBase(origen.getSaldoBase().subtract(dto.getMonto()));
        destino.setSaldoBase(destino.getSaldoBase().add(montoConvertido));

        cuentaRepository.save(origen);
        cuentaRepository.save(destino);

        Movimiento movimiento = Movimiento.builder()
                .monto(dto.getMonto())
                .monedaOriginal(origen.getMoneda())
                .tasaCambio(tasa)
                .fecha(LocalDateTime.now())
                .descripcion(dto.getDescripcion())
                .cuenta(origen)
                .categoria(getOrCreateTransferCategoria())
                .build();

        return movimientoRepository.save(movimiento);
    }

    public Pagination<Categoria> getCategorias(int page, int size) {
        Page<Categoria> result = categoriaRepository.findAll(PageRequest.of(page - 1, size));
        return buildPagination(result);
    }

    private Categoria getOrCreateTransferCategoria() {
        return categoriaRepository.findAll().stream()
                .filter(c -> "Transferencia".equalsIgnoreCase(c.getNombre()))
                .findFirst()
                .orElseGet(() -> {
                    Categoria cat = Categoria.builder()
                            .nombre("Transferencia")
                            .tipo(Categoria.TipoCategoria.Egreso)
                            .build();
                    return categoriaRepository.save(cat);
                });
    }

    private <T> Pagination<T> buildPagination(Page<T> page) {
        PaginationMeta meta = new PaginationMeta(
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements()
        );
        return new Pagination<>(page.getContent(), meta);
    }
}
