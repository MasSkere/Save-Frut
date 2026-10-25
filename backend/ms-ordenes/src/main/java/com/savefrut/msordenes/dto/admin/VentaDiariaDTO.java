package com.savefrut.msordenes.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VentaDiariaDTO(LocalDate fecha, BigDecimal total, Long cantidad) {}
