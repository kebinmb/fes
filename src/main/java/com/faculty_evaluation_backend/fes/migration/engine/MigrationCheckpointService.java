package com.faculty_evaluation_backend.fes.migration.engine;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class MigrationCheckpointService {
    private final ConcurrentHashMap<String, Integer> checkpoints = new ConcurrentHashMap<>();

    public void saveCheckpoint(String key, int index){
        checkpoints.put(key, index);
    }
    public int getCheckpoint(String key){
        return checkpoints.getOrDefault(key, 0);
    }
    public void clear(String key){
        checkpoints.remove(key);
    }
}
