package ru.mihaliks.finance.report.event;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.mihaliks.finance.common.event.FinanceDataChangedEvent;
import ru.mihaliks.finance.report.service.ReportService;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;

class CacheInvalidationListenerTest {
    @Test
    void clearsCacheForFinanceEvent() {
        ReportService reportService = Mockito.mock(ReportService.class);
        CacheInvalidationListener listener = new CacheInvalidationListener(reportService);

        listener.onChanged(new FinanceDataChangedEvent(UUID.randomUUID(), Instant.now(), "OPERATION",
                UUID.randomUUID(), UUID.randomUUID(), null, "CREATED"));

        verify(reportService).clearCache();
    }
}
