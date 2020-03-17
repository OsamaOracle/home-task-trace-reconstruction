package org.icoder.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icoder.mapping.Trace;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class LogConsumer {

    private Logger logger = LogManager.getLogger(LogConsumer.class);

    private volatile TraceStackProducer traceStackProducer = new TraceStackProducer();
    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    private ObjectMapper objectMapper = new ObjectMapper();


    /**
     * Process input file
     *
     * @param fileInput  File
     * @param fileOutput File
     * @throws Exception
     */
    public List<Trace> process(File fileInput, File fileOutput, Boolean processOutput) throws Exception {

        int cores = Runtime.getRuntime().availableProcessors();
        logger.debug("Starting processing, num of system cores {}", cores);
        long start = System.currentTimeMillis();

        ExecutorService executorService = Executors.newFixedThreadPool(cores);

        executorService.execute(() -> fileInputReader(fileInput));
        executorService.execute(() -> traceStackProducer.processNullTreeStack());

        if (cores > 2) {
            int finCores = cores - 2; // null tree stack and input reader
            for (int i = 1; i <= finCores; ++i) {
                final int threadID = i;
                executorService.execute(() -> {
                    logger.debug("Start processing thread nr {}, threadId {}", threadID, Thread.currentThread().getId());
                    traceStackProducer.processTreeStack(threadID, finCores);
                });
            }
        } else {
            executorService.execute(() -> traceStackProducer.processTreeStack(1, 1));
        }


        // @TODO implement buffered writer with id hash maps of json location in file to remove memory consumption during trace processing
        // @TODO processing will be more cpu / io intensive but less memory expensive

        // tick status update
        statusUpdate();

        while (!traceStackProducer.isProcessDone()) {
            // block main thread
        }
        List<Trace> traces = traceStackProducer.getTraces();
        logger.debug("Shut down all threads now! traces size {}", traces.size());
        executorService.shutdownNow();
        scheduledExecutorService.shutdownNow();
        logger.debug("Terminate all threads terminated");

        // write
        processOutputWriter(traces, fileOutput, processOutput);

        long end = System.currentTimeMillis();
        logger.debug("time executed {}s {}ms", ((end - start) / 1000), end - start);
        return traces;
    }

    /**
     * Print status processing update
     */
    private void statusUpdate() {
        scheduledExecutorService.schedule(() -> {
            traceStackProducer.tickStatus();
            statusUpdate();
        }, 20, TimeUnit.SECONDS);
    }

    /**
     * File input reader separation
     *
     * @param fileInput File
     */
    private void fileInputReader(File fileInput) {
        try {
            logger.debug("Start processing file {}", fileInput.getCanonicalPath());
            final long count = Files.lines(fileInput.toPath(), Charset.defaultCharset()).count();
            logger.debug("Number of lines {}", count);
            BufferedReader bufferedReader = Files.newBufferedReader(fileInput.toPath());
            for (long i = count; i > 0; --i) {
                String line = bufferedReader.readLine();
                traceStackProducer.append(line);
            }
            logger.debug("All lines read from input file setting read as done");
            traceStackProducer.setReadDone();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Write data std out or a file
     *
     * @param traceList     List<Trace>
     * @param fileOutput    File
     * @param processOutput Boolean
     */
    private void processOutputWriter(List<Trace> traceList, File fileOutput, Boolean processOutput) {
        if (processOutput) {
            if (fileOutput != null) {
                try {
                    BufferedWriter bufferedWriter = Files.newBufferedWriter(fileOutput.toPath());
                    for (Trace trace : traceList) {
                        bufferedWriter.write(objectMapper.valueToTree(trace).toString());
                        bufferedWriter.write("\n");
                    }
                } catch (Exception e) {
                    logger.error("Error during writing data to file");
                }
            } else {
                System.out.print("\n");
                System.out.print("\n");
                traceList.forEach(item -> {
                    System.out.print(objectMapper.valueToTree(item).toString());
                    System.out.print("\n");
                });
                System.out.print("\n");
                System.out.print("\n");
            }
        }
    }
}
