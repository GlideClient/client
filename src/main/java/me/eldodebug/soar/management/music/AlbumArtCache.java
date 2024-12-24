package me.eldodebug.soar.management.music;

import me.eldodebug.soar.logger.GlideLogger;
import me.eldodebug.soar.management.file.FileManager;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;

public class AlbumArtCache implements AutoCloseable {
    private static final int MAX_CACHE_SIZE_MB = 100;
    private static final int MAX_IMAGE_SIZE = 300;
    private static final String CACHE_DIR = "album_art_cache";
    private static final Duration CACHE_DURATION = Duration.ofDays(30);
    private static final int MAX_CONCURRENT_DOWNLOADS = 3;

    private final FileManager fileManager;
    private final File cacheDir;
    private final ExecutorService downloadExecutor;
    private final ConcurrentHashMap<String, CompletableFuture<String>> inProgressDownloads;
    private final ScheduledExecutorService maintenanceExecutor;

    public AlbumArtCache(FileManager fileManager) {
        this.fileManager = fileManager;
        this.cacheDir = new File(fileManager.getMusicDir(), CACHE_DIR);
        this.inProgressDownloads = new ConcurrentHashMap<>();
        this.downloadExecutor = new ThreadPoolExecutor(
            1, MAX_CONCURRENT_DOWNLOADS,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            new ThreadFactory() {
                private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = defaultFactory.newThread(r);
                    thread.setDaemon(true);
                    thread.setName("AlbumArt-Download-" + thread.getId());
                    return thread;
                }
            }
        );
        
        this.maintenanceExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "AlbumArt-Maintenance");
            thread.setDaemon(true);
            return thread;
        });

        initializeCache();
        scheduleMaintenance();
    }

    private void initializeCache() {
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            GlideLogger.error("Failed to create album art cache directory");
        }
    }

    private void scheduleMaintenance() {
        maintenanceExecutor.scheduleAtFixedRate(
            this::performMaintenance,
            1, 24, TimeUnit.HOURS
        );
    }

    public CompletableFuture<String> getCachedAlbumArtUrlAsync(String id, String imageUrl) {
        return inProgressDownloads.computeIfAbsent(id, key -> {
            File cachedFile = getCacheFile(id);
            if (cachedFile.exists() && isValidCacheFile(cachedFile)) {
                return CompletableFuture.completedFuture(cachedFile.getAbsolutePath());
            }

            return CompletableFuture.supplyAsync(
                () -> downloadAndCacheImage(id, imageUrl),
                downloadExecutor
            ).whenComplete((result, ex) -> inProgressDownloads.remove(id));
        });
    }

    public String getAlbumArt(String imageUrl) {
        String id = String.valueOf(imageUrl.hashCode());
        return getCachedAlbumArtUrlAsync(id, imageUrl)
               .join(); // Convert Future to direct result - be careful with this in UI code
    }

    public void cleanup() {
        performMaintenance();
        close();
    }

    private String downloadAndCacheImage(String id, String imageUrl) {
        File cacheFile = getCacheFile(id);
        try {
            BufferedImage image = ImageIO.read(new URL(imageUrl));
            if (image != null) {
                BufferedImage resizedImage = resizeImage(image);
                cacheFile.getParentFile().mkdirs();
                ImageIO.write(resizedImage, "png", cacheFile);
                return cacheFile.getAbsolutePath();
            }
            throw new IOException("Failed to read image from URL");
        } catch (Exception e) {
            GlideLogger.error("Failed to download and cache album art: " + id, e);
            return imageUrl; // Fallback to original URL
        }
    }

    private BufferedImage resizeImage(BufferedImage original) {
        Image resultingImage = original.getScaledInstance(
            MAX_IMAGE_SIZE, MAX_IMAGE_SIZE, 
            Image.SCALE_SMOOTH
        );
        BufferedImage outputImage = new BufferedImage(
            MAX_IMAGE_SIZE, MAX_IMAGE_SIZE,
            BufferedImage.TYPE_INT_ARGB
        );
        outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
        return outputImage;
    }

    private File getCacheFile(String id) {
        return new File(cacheDir, id + ".png");
    }

    private boolean isValidCacheFile(File file) {
        return file.exists() && 
               System.currentTimeMillis() - file.lastModified() < CACHE_DURATION.toMillis();
    }

    private void performMaintenance() {
        try {
            long totalSize = 0;
            List<File> expiredFiles = new ArrayList<>();
            File[] files = cacheDir.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (!isValidCacheFile(file)) {
                        expiredFiles.add(file);
                    } else {
                        totalSize += file.length();
                    }
                }

                // Delete expired files
                for (File file : expiredFiles) {
                    if (!file.delete()) {
                        GlideLogger.warn("Failed to delete expired cache file: " + file.getName());
                    }
                }

                // If still over size limit, delete oldest files
                if (totalSize > MAX_CACHE_SIZE_MB * 1024 * 1024) {
                    Arrays.sort(files, Comparator.comparingLong(File::lastModified));
                    for (File file : files) {
                        if (totalSize <= MAX_CACHE_SIZE_MB * 1024 * 1024) break;
                        if (file.delete()) {
                            totalSize -= file.length();
                        }
                    }
                }
            }
        } catch (Exception e) {
            GlideLogger.error("Error during cache maintenance", e);
        }
    }

    @Override
    public void close() {
        downloadExecutor.shutdownNow();
        maintenanceExecutor.shutdownNow();
        try {
            downloadExecutor.awaitTermination(5, TimeUnit.SECONDS);
            maintenanceExecutor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}