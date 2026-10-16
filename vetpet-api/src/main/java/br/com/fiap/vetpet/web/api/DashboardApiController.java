package br.com.fiap.vetpet.web.api;

import br.com.fiap.vetpet.dto.response.DashboardResponse;
import br.com.fiap.vetpet.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Indicadores da clínica")
public class DashboardApiController {

    private final DashboardService dashboardService;

    @GetMapping
    @Operation(summary = "Resumo de indicadores, próximos atendimentos e perguntas pendentes")
    public DashboardResponse resumo() {
        return dashboardService.resumo();
    }
}
