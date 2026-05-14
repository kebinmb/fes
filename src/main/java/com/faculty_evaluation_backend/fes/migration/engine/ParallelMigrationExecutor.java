package com.faculty_evaluation_backend.fes.migration.engine;

import com.faculty_evaluation_backend.fes.config.database.LegacyDataSourceContext;
import com.faculty_evaluation_backend.fes.config.database.LegacyDatabase;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

@Slf4j
@Component
public class ParallelMigrationExecutor {

    private final ExecutorService executorService;

    public ParallelMigrationExecutor() {

        this.executorService =
                new ThreadPoolExecutor(
                        2,
                        4,
                        60L,
                        TimeUnit.SECONDS,
                        new LinkedBlockingQueue<>(25),
                        new ThreadFactory() {

                            private int counter = 1;

                            @Override
                            public Thread newThread(Runnable r) {

                                Thread thread =
                                        new Thread(r);

                                thread.setName(
                                        "migration-worker-"
                                                + counter++
                                );

                                thread.setDaemon(false);

                                return thread;
                            }
                        },
                        new ThreadPoolExecutor.CallerRunsPolicy()
                );
    }

    public <T> void processInParallel(
            List<T> data,
            int batchSize,
            BatchProcessor<T> processor
    ) {

        if (data == null || data.isEmpty()) {

            log.warn("No data found for migration.");

            return;
        }

        int total = data.size();

        log.info(
                "Starting parallel migration for {} records",
                total
        );

        List<CompletableFuture<Void>> futures =
                new ArrayList<>();

        IntStream.range(
                        0,
                        (total + batchSize - 1)
                                / batchSize
                )
                .forEach(i -> {

                    int start = i * batchSize;

                    int end = Math.min(
                            start + batchSize,
                            total
                    );

                    List<T> batch =
                            data.subList(start, end);

                    LegacyDatabase currentDb =
                            LegacyDataSourceContext.get();

                    CompletableFuture<Void> future =
                            CompletableFuture.runAsync(() -> {

                                LegacyDataSourceContext.set(
                                        currentDb
                                );

                                try {

                                    log.info(
                                            "Processing batch {} -> {}",
                                            start,
                                            end
                                    );

                                    processor.process(batch);

                                    log.info(
                                            "Completed batch {} -> {}",
                                            start,
                                            end
                                    );

                                } catch (Exception ex) {

                                    log.error(
                                            "Batch failed [{} -> {}]",
                                            start,
                                            end,
                                            ex
                                    );

                                    throw new RuntimeException(ex);

                                } finally {

                                    LegacyDataSourceContext.clear();
                                }

                            }, executorService);

                    futures.add(future);
                });

        try {

            CompletableFuture
                    .allOf(
                            futures.toArray(
                                    new CompletableFuture[0]
                            )
                    )
                    .get(60, TimeUnit.MINUTES);

        } catch (TimeoutException ex) {

            log.error(
                    "Migration timeout reached.",
                    ex
            );

            throw new RuntimeException(
                    "Migration timeout reached.",
                    ex
            );

        } catch (Exception ex) {

            log.error(
                    "Parallel migration failed.",
                    ex
            );

            throw new RuntimeException(
                    "Parallel migration failed",
                    ex
            );
        }

        log.info(
                "Parallel migration completed successfully."
        );
    }

    @PreDestroy
    public void shutdown() {

        log.info("Shutting down migration executor");

        executorService.shutdown();
    }

    @FunctionalInterface
    public interface BatchProcessor<T> {
        void process(List<T> batch);
    }
}