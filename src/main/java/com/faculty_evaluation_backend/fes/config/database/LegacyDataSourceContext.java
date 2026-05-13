package com.faculty_evaluation_backend.fes.config.database;

public class LegacyDataSourceContext {
    private static final ThreadLocal<LegacyDatabase> CONTEXT = new ThreadLocal<>();

    public static void set(LegacyDatabase db){
        CONTEXT.set(db);
    }
    public static LegacyDatabase get(){
        return CONTEXT.get();
    }
    public static void clear(){
        CONTEXT.remove();
    }
}
