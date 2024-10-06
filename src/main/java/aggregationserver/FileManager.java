package aggregationserver;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.*;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Comparator;

public class FileManager {

    private static final Logger logger = Logger.getLogger(FileManager.class.getName());
    private static final long FILE_EXPIRATION_PERIOD = 30 * 1000; // 30 seconds
    private static final long CLEANUP_PERIOD = 1000; // 1 seconds - Activates Cleanup daemon
    private static final int MAX_ENTRIES = 20; // Max 20 entries at a time

    private final BlockingQueue<WriteRequest> writeQueue;
    private final ConcurrentHashMap<String, Long> lastUpdateTimestamps; // To track last update time
    private final PriorityQueue<WriteRequest> writePriorityQueue; // To manage oldest requests
    private final File storageDirectory;
    private volatile boolean isRunning;

    public FileManager(String storagePath) {
        this.writeQueue = new LinkedBlockingQueue<>();
        this.lastUpdateTimestamps = new ConcurrentHashMap<>();
        this.writePriorityQueue = new PriorityQueue<>(Comparator.comparing(WriteRequest::timestamp));
        this.storageDirectory = new File(storagePath);

        if (!storageDirectory.exists()) {
            if (!storageDirectory.mkdirs()) {
                logger.severe("Failed to create storage directory.");
            }
        }
    }

    public void start() {
        isRunning = true;
        Thread writeThread = new Thread(this::processWriteRequests);
        writeThread.start();

        Thread cleanupDaemon = new Thread(this::startCleanupDaemon);
        cleanupDaemon.setDaemon(true); // Background thread
        cleanupDaemon.start();

        logger.info("FileManager started with write queue and cleanup daemon.");
    }

    public void shutdown() {
        isRunning = false;
        logger.info("FileManager is shutting down...");
        processRemainingRequests();
    }

    public void addWriteRequest(WriteRequest request) {
        try {
            writeQueue.put(request);
            lastUpdateTimestamps.put(request.stationId(), System.currentTimeMillis());

            // Add to the priority queue for ordering by timestamp
            writePriorityQueue.add(request);

            logger.info("Added write request for station ID: " + request.stationId());

            // If more than 20 entries, remove the oldest
            if (writePriorityQueue.size() > MAX_ENTRIES) {
                WriteRequest oldestRequest = writePriorityQueue.poll();
                deleteOldestEntry(oldestRequest.stationId());
            }
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Failed to add write request to the queue.", e);
        }
    }

    private void processWriteRequests() {
        while (isRunning) {
            try {
                WriteRequest request = writeQueue.take(); // Blocks until a request is available
                processWriteRequest(request);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.log(Level.SEVERE, "Write request processing interrupted.", e);
            }
        }
    }

    private void processWriteRequest(WriteRequest request) {
        try {
            String filePath = storageDirectory + "/" + request.stationId() + ".json";
            File file = new File(filePath);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(request.data().toJson()); // Writing JSON data to the file
                logger.info("Successfully wrote data for station ID: " + request.stationId());
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to write data for station ID: " + request.stationId(), e);
        }
    }

    // Clean up files that haven't been updated in 30 seconds
    private void startCleanupDaemon() {
        while (isRunning) {
            try {
                Thread.sleep(CLEANUP_PERIOD);
                cleanExpiredData();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.log(Level.SEVERE, "Cleanup daemon interrupted.", e);
            }
        }
    }

    // Remove files that haven't been updated in the last 30 seconds
    private void cleanExpiredData() {
        long currentTime = System.currentTimeMillis();
        long expirationTime = FILE_EXPIRATION_PERIOD; // 30 seconds rule
        for (Map.Entry<String, Long> entry : lastUpdateTimestamps.entrySet()) {
            String stationId = entry.getKey();
            long lastUpdated = entry.getValue();
            if (currentTime - lastUpdated > expirationTime) {
                deleteExpiredEntry(stationId);
            }
        }
    }

    private void deleteExpiredEntry(String stationId) {
        File file = new File(storageDirectory, stationId + ".json");
        if (file.exists() && file.delete()) {
            lastUpdateTimestamps.remove(stationId);
            logger.info("Removed expired file for station ID: " + stationId);
        }
    }

    private void deleteOldestEntry(String stationId) {
        File file = new File(storageDirectory, stationId + ".json");
        if (file.exists() && file.delete()) {
            lastUpdateTimestamps.remove(stationId);
            logger.info("Removed oldest file to maintain entry limit for station ID: " + stationId);
        }
    }

    // Process remaining requests during shutdown
    private void processRemainingRequests() {
        while (!writeQueue.isEmpty()) {
            try {
                WriteRequest request = writeQueue.take();
                processWriteRequest(request);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.log(Level.SEVERE, "Interrupted during final write request processing.", e);
            }
        }
    }
}