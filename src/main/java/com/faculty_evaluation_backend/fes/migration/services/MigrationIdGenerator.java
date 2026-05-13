package com.faculty_evaluation_backend.fes.migration.services;

import com.faculty_evaluation_backend.fes.config.database.LegacyDatabase;

public class MigrationIdGenerator {

    public static Integer generateNumericId(
            LegacyDatabase database,
            Integer originalId
    ) {

        if (originalId == null) {
            return null;
        }

        int prefix = switch (database) {

            case LEGACY_TALISAY -> 1;
            case LEGACY_ALIJIS -> 2;
            case LEGACY_FT -> 3;
            case LEGACY_BINALBAGAN -> 4;
        };

        return Integer.parseInt(prefix + String.format("%06d", originalId));
    }

    public static String generateStringId(
            LegacyDatabase database,
            String originalId
    ) {

        if (originalId == null) {
            return null;
        }

        String prefix = switch (database) {

            case LEGACY_TALISAY -> "TAL";
            case LEGACY_ALIJIS -> "ALI";
            case LEGACY_FT -> "FT";
            case LEGACY_BINALBAGAN -> "BIN";
        };

        return prefix + "-" + originalId;
    }
}