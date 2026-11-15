package fi.dy.masa.litematica.util;

import java.util.Objects;

/**
 * Splits an inclusive cuboid into disjoint, volume-limited /fill command boxes.
 *
 * <p>Horizontal subdivisions are needed when a single Y layer exceeds the
 * configured limit. Use long arithmetic to avoid overflow for large areas.
 */
public final class FillVolumeSplitter
{
    private FillVolumeSplitter() { }

    @FunctionalInterface
    public interface BoxConsumer
    {
        void accept(int minX, int minY, int minZ, int maxX, int maxY, int maxZ);
    }

    public static void forEachBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
                                  int maxVolume, BoxConsumer consumer)
    {
        Objects.requireNonNull(consumer, "consumer");

        if (maxVolume < 1)
        {
            throw new IllegalArgumentException("maxVolume must be positive");
        }

        if (minX > maxX || minY > maxY || minZ > maxZ)
        {
            throw new IllegalArgumentException("Inverted bounds");
        }

        split(minX, minY, minZ, maxX, maxY, maxZ, maxVolume, consumer);
    }

    private static void split(int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
                              int maxVolume, BoxConsumer consumer)
    {
        long width = (long) maxX - minX + 1L;
        long depth = (long) maxZ - minZ + 1L;

        // Check via division so even an extreme horizontal area cannot overflow.
        if (width > maxVolume / depth)
        {
            // Split along the longer horizontal axis until one layer fits.
            // Each recursive call strictly reduces its box width or depth.
            if (width >= depth && width > 1L)
            {
                int midpoint = (int) (minX + (width - 1L) / 2L);
                split(minX, minY, minZ, midpoint, maxY, maxZ, maxVolume, consumer);
                split(midpoint + 1, minY, minZ, maxX, maxY, maxZ, maxVolume, consumer);
            }
            else
            {
                int midpoint = (int) (minZ + (depth - 1L) / 2L);
                split(minX, minY, minZ, maxX, maxY, midpoint, maxVolume, consumer);
                split(minX, minY, midpoint + 1, maxX, maxY, maxZ, maxVolume, consumer);
            }

            return;
        }

        long area = width * depth; // Guaranteed to be <= maxVolume.
        long heightPerBox = maxVolume / area;

        for (long y = minY; y <= maxY;)
        {
            long endY = Math.min((long) maxY, y + heightPerBox - 1L);
            consumer.accept(minX, (int) y, minZ, maxX, (int) endY, maxZ);
            y = endY + 1L;
        }
    }
}
