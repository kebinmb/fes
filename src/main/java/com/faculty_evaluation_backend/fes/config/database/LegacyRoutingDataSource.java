package com.faculty_evaluation_backend.fes.config.database;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class LegacyRoutingDataSource
        extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return LegacyDataSourceContext.get();
    }
}