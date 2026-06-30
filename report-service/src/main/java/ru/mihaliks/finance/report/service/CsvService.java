package ru.mihaliks.finance.report.service;

import org.springframework.stereotype.Service;
import ru.mihaliks.finance.report.api.ReportDtos.OperationView;
import ru.mihaliks.finance.report.api.ReportDtos.ReportResponse;

@Service
public class CsvService {
    public String create(ReportResponse report) {
        StringBuilder csv = new StringBuilder("date,type,amount,userId,familyId,categoryId,description\n");
        for (OperationView operation : report.operations()) {
            csv.append(operation.operationDate()).append(',')
                    .append(operation.type()).append(',')
                    .append(operation.amount()).append(',')
                    .append(operation.userId()).append(',')
                    .append(operation.familyId() == null ? "" : operation.familyId()).append(',')
                    .append(operation.categoryId()).append(',')
                    .append('"').append(escape(operation.description())).append('"').append('\n');
        }
        return csv.toString();
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\"", "\"\"");
    }
}
