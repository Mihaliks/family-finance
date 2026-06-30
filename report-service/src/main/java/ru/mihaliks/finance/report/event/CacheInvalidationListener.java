package ru.mihaliks.finance.report.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.mihaliks.finance.common.event.FinanceDataChangedEvent;
import ru.mihaliks.finance.report.service.ReportService;

@Component
public class CacheInvalidationListener {
    private final ReportService reportService;

    public CacheInvalidationListener(ReportService reportService) {
        this.reportService = reportService;
    }

    @KafkaListener(topics = "finance-data-changed", groupId = "report-cache")
    public void onChanged(FinanceDataChangedEvent event) {
        reportService.clearCache();
    }
}
