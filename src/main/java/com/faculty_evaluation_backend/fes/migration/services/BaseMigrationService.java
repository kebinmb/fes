package com.faculty_evaluation_backend.fes.migration.services;

import com.faculty_evaluation_backend.fes.config.database.LegacyDataSourceContext;
import com.faculty_evaluation_backend.fes.config.database.LegacyDatabase;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseMigrationService {

    protected void executePerCampus(MigrationAction action) {

        for (LegacyDatabase database : LegacyDatabase.values()) {

            try {

                log.info("=================================================");
                log.info("USING LEGACY DATABASE : {}", database.name());

                LegacyDataSourceContext.set(database);

                action.execute(database);

                log.info(
                        "Migration completed successfully for {}",
                        database.name()
                );

            } catch (Exception ex) {

                log.error(
                        "Migration failed for campus : {}",
                        database.name(),
                        ex
                );

            } finally {

                LegacyDataSourceContext.clear();

                log.info(
                        "CLEARED DATASOURCE CONTEXT : {}",
                        database.name()
                );

                log.info("=================================================");
            }
        }
    }

    @FunctionalInterface
    public interface MigrationAction {
        void execute(LegacyDatabase database);
    }
}