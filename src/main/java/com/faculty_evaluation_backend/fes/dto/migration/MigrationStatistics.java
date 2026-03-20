package com.faculty_evaluation_backend.fes.dto.migration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MigrationStatistics {
    private int totalRecords;
    private int processed;
    private int success;
    private int failed;
    private int skipped;

    private double successRate;

    private int batchSize;
    private int totalBatches;
}
