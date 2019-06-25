package coordinating;

import java.io.File;

public class TotalFileSizeSequential {
    public static void main(String[] args) {
        final long start = System.nanoTime();
        final long total = new TotalFileSizeSequential()
                .getTotalSizeOfFilesDir(new File(args[0]));
        final long end = System.nanoTime();
        System.out.println("Total Size: " + total);
        System.out.println("Time taken: " + (end - start)/1.0e9);
    }

    private long getTotalSizeOfFilesDir(final File file) {
        if (file.isFile()) return file.length();

        final File[] children = file.listFiles();
        long total = 0;
        if (children != null){
            for (final File child : children) {
                total += getTotalSizeOfFilesDir(child);
            }
        }
        return total;
    }
}
