package fi.dy.masa.litematica.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Dependency-free regression tests, executable with "java ...FillVolumeSplitterTest".
 * These are run explicitly in GitHub Actions, without requiring JUnit.
 */
public final class FillVolumeSplitterTest
{
    private FillVolumeSplitterTest() { }

    public static void main(String[] args)
    {
        assertCoverage(0, 0, 0, 0, 0, 0, 32768, 1);
        assertCoverage(0, 0, 0, 127, 0, 255, 32768, 1);
        assertCoverage(0, 0, 0, 255, 2, 255, 32768, 6);
        assertCoverage(-100, -2, -100, 99, 1, 99, 32768, -1);
        assertCoverage(-7, -12, 4, 5, 16, 20, 79, -1);
        assertCoverage(12, Integer.MAX_VALUE - 9, -12, 12, Integer.MAX_VALUE, -12, 3, 4);
        assertCoverage(0, 0, 0, 10, 10, 10, 1, 1331);

        Random random = new Random(0x411);
        for (int i = 0; i < 200; i++)
        {
            int x = random.nextInt(15) - 7;
            int y = random.nextInt(15) - 7;
            int z = random.nextInt(15) - 7;
            int dx = 1 + random.nextInt(9);
            int dy = 1 + random.nextInt(9);
            int dz = 1 + random.nextInt(9);
            int limit = 1 + random.nextInt(200);
            assertCoverage(x, y, z, x + dx - 1, y + dy - 1, z + dz - 1, limit, -1);
        }

        assertInvalid(0, 0, 0, 0, 0, 0, 0);
        assertInvalid(1, 0, 0, 0, 0, 0, 10);
        System.out.println("FillVolumeSplitter: all regression cases passed");
    }

    private static void assertInvalid(int minX, int minY, int minZ,
                                      int maxX, int maxY, int maxZ, int limit)
    {
        try
        {
            FillVolumeSplitter.forEachBox(minX, minY, minZ, maxX, maxY, maxZ, limit,
                    (x1, y1, z1, x2, y2, z2) -> { });
            throw new AssertionError("Invalid input accepted");
        }
        catch (IllegalArgumentException expected)
        {
            // Expected.
        }
    }

    private static void assertCoverage(int minX, int minY, int minZ,
                                       int maxX, int maxY, int maxZ, int limit, int expectedBoxes)
    {
        List<int[]> boxes = new ArrayList<>();
        FillVolumeSplitter.forEachBox(minX, minY, minZ, maxX, maxY, maxZ, limit,
                (x1, y1, z1, x2, y2, z2) ->
                        boxes.add(new int[] { x1, y1, z1, x2, y2, z2 }));

        if (expectedBoxes >= 0 && boxes.size() != expectedBoxes)
        {
            throw new AssertionError("Expected " + expectedBoxes + " boxes; got " + boxes.size());
        }

        long width = (long) maxX - minX + 1;
        long height = (long) maxY - minY + 1;
        long depth = (long) maxZ - minZ + 1;
        long expectedVolume = width * height * depth;
        long actualVolume = 0;
        Set<Long> visited = new HashSet<>();

        for (int[] b : boxes)
        {
            if (b[0] < minX || b[1] < minY || b[2] < minZ ||
                b[3] > maxX || b[4] > maxY || b[5] > maxZ ||
                b[0] > b[3] || b[1] > b[4] || b[2] > b[5])
            {
                throw new AssertionError("Out-of-bounds or inverted fragment");
            }

            long fragment = ((long) b[3] - b[0] + 1) *
                            ((long) b[4] - b[1] + 1) *
                            ((long) b[5] - b[2] + 1);
            if (fragment > limit)
            {
                throw new AssertionError("Oversized /fill fragment: " + fragment + " > " + limit);
            }
            actualVolume += fragment;

            for (int x = b[0]; x <= b[3]; x++)
            {
                for (int y = b[1]; y <= b[4]; y++)
                {
                    for (int z = b[2]; z <= b[5]; z++)
                    {
                        long index = ((long) (x - minX) * height + (y - (long) minY)) * depth + (z - (long) minZ);
                        if (!visited.add(index))
                        {
                            throw new AssertionError("Duplicate block at " + x + " " + y + " " + z);
                        }
                    }
                }
            }
        }

        if (actualVolume != expectedVolume || visited.size() != expectedVolume)
        {
            throw new AssertionError("Incomplete coverage: " + actualVolume + " / " + expectedVolume);
        }
    }
}
