package ru.mihaliks.finance.report.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.mihaliks.finance.common.security.CurrentUser;
import ru.mihaliks.finance.report.api.ReportDtos.GroupBy;
import ru.mihaliks.finance.report.api.ReportDtos.ReportResponse;
import ru.mihaliks.finance.report.api.ReportDtos.UiConfig;
import ru.mihaliks.finance.report.service.CsvService;
import ru.mihaliks.finance.report.service.ReportService;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@RestController
public class ReportController {
    private final ReportService reportService;
    private final CsvService csvService;
    private final String authServiceUrl;

    public ReportController(ReportService reportService, CsvService csvService,
                            @Value("${app.auth-service-public-url}") String authServiceUrl) {
        this.reportService = reportService;
        this.csvService = csvService;
        this.authServiceUrl = authServiceUrl;
    }

    @GetMapping("/api/reports")
    public Mono<ReportResponse> report(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID familyId,
            @RequestParam(required = false) Set<UUID> memberIds,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "NONE") GroupBy groupBy,
            @AuthenticationPrincipal Jwt jwt
    ) {
        CurrentUser user = CurrentUser.from(jwt);
        return reportService.build(user.id(), user.token(), from, to, familyId, memberIds, type, groupBy);
    }

    @GetMapping(value = "/api/reports/export.csv", produces = "text/csv")
    public Mono<ResponseEntity<String>> csv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID familyId,
            @RequestParam(required = false) Set<UUID> memberIds,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "NONE") GroupBy groupBy,
            @AuthenticationPrincipal Jwt jwt
    ) {
        CurrentUser user = CurrentUser.from(jwt);
        return reportService.build(user.id(), user.token(), from, to, familyId, memberIds, type, groupBy)
                .map(report -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.csv")
                        .contentType(MediaType.parseMediaType("text/csv"))
                        .body(csvService.create(report)));
    }

    @GetMapping("/api/ui-config")
    public UiConfig uiConfig() {
        return new UiConfig(authServiceUrl);
    }
}
