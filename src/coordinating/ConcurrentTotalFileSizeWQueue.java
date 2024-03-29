package coordinating;

import java.io.File;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class ConcurrentTotalFileSizeWQueue {
    private ExecutorService service;
    final private BlockingQueue<Long> fileSizes = new ArrayBlockingQueue<>(500);
    final AtomicLong pendingFileVisits = new AtomicLong();

    private void startExploreDir(final File file){
        pendingFileVisits.incrementAndGet();
        service.execute(()->{
            exploreDir(file);
        });
    }

    private void exploreDir(final File file){
        long fileSize = 0;
        if (file.isFile()){
            fileSize = file.length();
        } else {
            final File[] children = file.listFiles();
            if (children != null){
                for (File child : children) {
                    if (child.isFile()){
                        fileSize += child.length();
                    } else {
                        startExploreDir(child);
                    }
                }
            }
        }

        try {
            fileSizes.put(fileSize);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        pendingFileVisits.decrementAndGet();
    }

    private long getTotalSizeOfFile(final String fileName) throws InterruptedException {
        service = Executors.newFixedThreadPool(100);
        try {
            startExploreDir(new File(fileName));
            long totalSize = 0;
            while (pendingFileVisits.get()>0 || fileSizes.size()>0){
                final long size = fileSizes.poll(10, TimeUnit.SECONDS);
                totalSize += size;
            }
            return totalSize;
        } finally {
            service.shutdown();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        final long start = System.nanoTime();
        final long total = new ConcurrentTotalFileSizeWQueue()
                .getTotalSizeOfFile(args[0]);
        final long end = System.nanoTime();
        System.out.println("Total Size: " + total);
        System.out.println("Time taken: " + (end - start)/1.0e9);
    }
}
