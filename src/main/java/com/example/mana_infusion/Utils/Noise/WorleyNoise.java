package com.example.mana_infusion.Utils.Noise;

import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.*;


public class WorleyNoise {

    private final int DOTS_PER_REGION ;
    private final int REGION_SIZE;
    private final int LEAST_DISTANCE_BETWEEN_DOTS;

    private final List<Vector2i> tempPointList = new ArrayList<>();
    private final Map<Long, List<Vector2i>> cachedPoints = new HashMap<>();

    private final long worldSeed;

    public WorleyNoise(long seed, int dotsPerRegion, int regionSize) {
        this.worldSeed = seed;
        DOTS_PER_REGION = dotsPerRegion;
        REGION_SIZE = regionSize;

        LEAST_DISTANCE_BETWEEN_DOTS = (int) (REGION_SIZE / 2.2f);
    }

    public class WorleyResult {
        public final float noiseValue;
        public final Vector2i closestPoint;

        public WorleyResult(float noiseValue, Vector2i closestPoint) {
            this.noiseValue = noiseValue;
            this.closestPoint = closestPoint;
        }
    }

    public synchronized WorleyResult GetWorleyData(int x, int z) {
        Tuple<Float, Vector2i> f0 = DistanceToNthPoint(x, z, 0);
        Tuple<Float, Vector2i> f1 = DistanceToNthPoint(x, z, 1);
        var worleyNoiseValue = f0.getA() / f1.getA();

        Vector2i center = f0.getB();

        if (center.x == x && center.y == z) {
            return new WorleyResult(worleyNoiseValue, f0.getB());
        }

        return new WorleyResult(worleyNoiseValue, f0.getB());
    }

    private Tuple<Float, Vector2i> DistanceToNthPoint(int x, int z, int n) {
        GetPointsWithNeighbors(x, z);

        List<Tuple<Float, Vector2i>> distanceToPoints = new ArrayList<>();

        for (Vector2i point : tempPointList) {
            float dx = x - point.x;
            float dz = z - point.y;
            float distance = (float) Math.sqrt(dx * dx + dz * dz);
            distanceToPoints.add(new Tuple<>(distance, point));
        }

        distanceToPoints.sort((a, b) -> Float.compare(a.getA(), b.getA()));

        if (n < distanceToPoints.size()) {
            return new Tuple<Float, Vector2i> (distanceToPoints.get(n).getA(), distanceToPoints.get(n).getB());
        } else {
            return distanceToPoints.isEmpty() ? new Tuple<>(0F, null)  : (distanceToPoints.get(distanceToPoints.size() - 1));
        }
    }

    private void GetPointsWithNeighbors(int x, int z) {
        tempPointList.clear();

        int regionX = Math.floorDiv(x, REGION_SIZE);
        int regionZ = Math.floorDiv(z, REGION_SIZE);

        // Check current region and 8 neighboring regions
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                List<Vector2i> regionPoints = GetPointsForRegion(regionX + dx, regionZ + dz);
                tempPointList.addAll(regionPoints);
            }
        }
    }

    private List<Vector2i> GetPointsForRegion(int regionX, int regionZ) {
        long key = regionKey(regionX, regionZ);

        if (cachedPoints.containsKey(key)) {
            return cachedPoints.get(key);
        }

        long regionSpecificSeed = worldSeed;
        regionSpecificSeed ^= regionX * 341873128712L;
        regionSpecificSeed ^= regionZ * 132897987541L;
        RandomSource random = RandomSource.create(regionSpecificSeed);

        int attempts = 0;
        int maxAttempts = DOTS_PER_REGION * 20; // Avoid infinite loops

        List<Vector2i> dots = new ArrayList<>();
        while (dots.size() < DOTS_PER_REGION && attempts < maxAttempts) {
            int localX = random.nextInt(REGION_SIZE );
            int localZ = random.nextInt(REGION_SIZE );
            int dotX = regionX * REGION_SIZE  + localX;
            int dotZ = regionZ * REGION_SIZE  + localZ;

            boolean tooClose = false;
            for (Vector2i other : dots) {
                double dx = other.x - dotX;
                double dz = other.y - dotZ;
                if ((dx * dx + dz * dz) < (LEAST_DISTANCE_BETWEEN_DOTS * LEAST_DISTANCE_BETWEEN_DOTS)) {
                    tooClose = true;
                    break;
                }
            }

            if (!tooClose) {
                dots.add(new Vector2i(dotX, dotZ));
            }

            attempts++;
        }

        cachedPoints.put(key, dots);
        return dots;
    }

    public static long regionKey(int x, int z) {
        return ((long) x << 32) | (z & 0xffffffffL);
    }
}