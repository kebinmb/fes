package com.faculty_evaluation_backend.fes.migration.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

@Slf4j
@Component
public class ParallelMigrationExecutor {
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;
    public ParallelMigrationExecutor(){
        threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(8);
        threadPoolTaskExecutor.setMaxPoolSize(16);
        threadPoolTaskExecutor.setQueueCapacity(1000);
        threadPoolTaskExecutor.setThreadNamePrefix("migration-");
        threadPoolTaskExecutor.initialize();
    }

    public <T> void processInParallel(List<T> data, int batchSize, BatchProcessor<T> processor) {
        int total = data.size();

        List<CompletableFuture<Void>> futures =
                IntStream.range(0, (total + batchSize - 1) / batchSize)
                        .mapToObj(i -> {
                            int start = i * batchSize;
                            int end = Math.min(start + batchSize, total);
                            List<T> batch = data.subList(start, end);

                            return CompletableFuture.runAsync(() -> {
                                // ❌ DO NOT catch here → let it fail
                                processor.process(batch);
                            }, threadPoolTaskExecutor);
                        })
                        .toList();

        try {
            // 🔥 This will THROW if any batch fails
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        } catch (Exception ex) {
            log.error("❌ FATAL: Parallel migration failed. Stopping all batches.", ex);

            // 🔥 Force shutdown of threads
            threadPoolTaskExecutor.shutdown();

            // 🔥 Terminal output
            System.err.println("\n========= PARALLEL MIGRATION FAILED =========");
            System.err.println("Error: " + ex.getMessage());
            ex.printStackTrace();
            System.err.println("=============================================\n");

            throw new RuntimeException("Parallel migration failed", ex);
        }

        log.info("✅ Parallel processing completed successfully");
    }

    @FunctionalInterface
    public interface BatchProcessor<T>{
        void process(List<T> batch);
    }
}
