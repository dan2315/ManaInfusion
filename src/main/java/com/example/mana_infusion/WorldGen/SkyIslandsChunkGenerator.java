package com.example.mana_infusion.WorldGen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;

import static com.example.mana_infusion.ManaInfusion.MODID;

public class SkyIslandsChunkGenerator extends NoiseBasedChunkGenerator {

    private static final int ISLAND_LAYER_HEIGHT = 70;
    private static final int ISLAND_SPACING = 128;
    private static final double CELLULAR_THRESHOLD = 0.3;
    private static final int CELLULAR_ITERATIONS = 3;

    // Noise generators for different purposes
    private final ImprovedNoise cellularNoise;
    private final ImprovedNoise islandShapeNoise;
    private final ImprovedNoise biomeNoise;
    private final ImprovedNoise heightVariationNoise;

    // Biome assignments for different island types
    private final List<Holder<Biome>> islandBiomes;
    private final Map<IslandId, Holder<Biome>> islandBiomeMap = new HashMap<>();

    private final Holder<NoiseGeneratorSettings> settings;

    public SkyIslandsChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        super(biomeSource, settings);
        this.settings = settings;

        // Initialize noise generators with different seeds
        RandomSource random = RandomSource.create(12345L);
        this.cellularNoise = new ImprovedNoise(random);
        this.islandShapeNoise = new ImprovedNoise(RandomSource.create(random.nextLong()));
        this.biomeNoise = new ImprovedNoise(RandomSource.create(random.nextLong()));
        this.heightVariationNoise = new ImprovedNoise(RandomSource.create(random.nextLong()));

        // Get biomes from the biome source
        if (biomeSource instanceof SkyIslandsBiomeSource skyIslandsBiomeSource) {
            this.islandBiomes = skyIslandsBiomeSource.getIslandBiomes();
        } else {
            // Fallback - this shouldn't happen with proper setup
            this.islandBiomes = List.of();
        }
    }

    public static final Codec<SkyIslandsChunkGenerator> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                BiomeSource.CODEC.fieldOf("biome_source").forGetter(SkyIslandsChunkGenerator::getBiomeSource),
                NoiseGeneratorSettings.CODEC.fieldOf("noise_settings").forGetter(SkyIslandsChunkGenerator::getSettings)
        ).apply(instance, SkyIslandsChunkGenerator::new)
    );

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    public Holder<NoiseGeneratorSettings> getSettings() {
        return settings;
    }

    private void generateFloatingIslands(ChunkAccess chunk) {
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;

        // Generate multiple island layers
        for (int layer = 0; layer < 3; layer++) {
            int layerHeight = ISLAND_LAYER_HEIGHT + (layer * 60);
            generateIslandLayer(chunk, chunkX, chunkZ, layer, layerHeight);
        }

        // Update heightmaps
        Heightmap.primeHeightmaps(chunk, EnumSet.of(Heightmap.Types.MOTION_BLOCKING,
                Heightmap.Types.WORLD_SURFACE));
    }

    private void generateIslandLayer(ChunkAccess chunk, int chunkX, int chunkZ, int layer, int baseHeight) {
        // Create cellular automata grid for this chunk
        boolean[][] cellularGrid = generateCellularGrid(chunkX, chunkZ, layer);

        // Apply cellular automata iterations
        for (int i = 0; i < CELLULAR_ITERATIONS; i++) {
            cellularGrid = applyCellularAutomata(cellularGrid);
        }

        // Generate islands based on cellular grid
        generateIslandsFromCellular(chunk, cellularGrid, chunkX, chunkZ, layer, baseHeight);
    }

    private boolean[][] generateCellularGrid(int chunkX, int chunkZ, int layer) {
        boolean[][] grid = new boolean[18][18]; // 16x16 chunk + 1 border on each side

        double layerOffset = layer * 1000.0; // Offset each layer in noise space

        for (int x = 0; x < 18; x++) {
            for (int z = 0; z < 18; z++) {
                int worldX = chunkX * 16 + x - 1;
                int worldZ = chunkZ * 16 + z - 1;

                // Sample cellular noise
                double noise = cellularNoise.noise(
                        worldX / 32.0,
                        layerOffset,
                        worldZ / 32.0
                );

                // Add some randomness based on island spacing
                double gridNoise = cellularNoise.noise(
                        worldX / (double)ISLAND_SPACING,
                        layerOffset + 500.0,
                        worldZ / (double)ISLAND_SPACING
                );

                grid[x][z] = (noise + gridNoise * 0.3) > CELLULAR_THRESHOLD;
            }
        }

        return grid;
    }

    private boolean[][] applyCellularAutomata(boolean[][] grid) {
        boolean[][] newGrid = new boolean[grid.length][grid[0].length];

        for (int x = 1; x < grid.length - 1; x++) {
            for (int z = 1; z < grid[0].length - 1; z++) {
                int neighbors = countNeighbors(grid, x, z);

                // Standard cellular automata rules
                if (grid[x][z]) {
                    newGrid[x][z] = neighbors >= 4; // Stay alive with 4+ neighbors
                } else {
                    newGrid[x][z] = neighbors >= 5; // Birth with 5+ neighbors
                }
            }
        }

        return newGrid;
    }

    private int countNeighbors(boolean[][] grid, int x, int z) {
        int count = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                if (grid[x + dx][z + dz]) count++;
            }
        }
        return count;
    }

    private void generateIslandsFromCellular(ChunkAccess chunk, boolean[][] cellularGrid,
                                             int chunkX, int chunkZ, int layer, int baseHeight) {

        // Find connected components (individual islands)
        List<IslandRegion> islands = findIslands(cellularGrid, chunkX, chunkZ, layer);

        // Generate terrain for each island
        for (IslandRegion island : islands) {
            generateIslandTerrain(chunk, island, baseHeight);
        }
    }

    private List<IslandRegion> findIslands(boolean[][] grid, int chunkX, int chunkZ, int layer) {
        List<IslandRegion> islands = new ArrayList<>();
        boolean[][] visited = new boolean[grid.length][grid[0].length];

        for (int x = 1; x < grid.length - 1; x++) {
            for (int z = 1; z < grid[0].length - 1; z++) {
                if (grid[x][z] && !visited[x][z]) {
                    IslandRegion island = new IslandRegion();
                    floodFill(grid, visited, x, z, island, chunkX, chunkZ, layer);

                    if (island.points.size() > 5) { // Minimum island size
                        islands.add(island);
                    }
                }
            }
        }

        return islands;
    }

    private void floodFill(boolean[][] grid, boolean[][] visited, int x, int z,
                           IslandRegion island, int chunkX, int chunkZ, int layer) {
        if (x < 0 || x >= grid.length || z < 0 || z >= grid[0].length ||
                visited[x][z] || !grid[x][z]) {
            return;
        }

        visited[x][z] = true;

        int worldX = chunkX * 16 + x - 1;
        int worldZ = chunkZ * 16 + z - 1;
        island.points.add(new IslandPoint(worldX, worldZ, x, z));

        // Assign biome to this island if not already assigned
        if (island.biome == null) {
            island.biome = selectIslandBiome(worldX, worldZ, layer);
        }

        // Recursively fill neighbors
        floodFill(grid, visited, x + 1, z, island, chunkX, chunkZ, layer);
        floodFill(grid, visited, x - 1, z, island, chunkX, chunkZ, layer);
        floodFill(grid, visited, x, z + 1, island, chunkX, chunkZ, layer);
        floodFill(grid, visited, x, z - 1, island, chunkX, chunkZ, layer);
    }

    private Holder<Biome> selectIslandBiome(int worldX, int worldZ, int layer) {
        // Use noise to select biome
        double biomeSelection = biomeNoise.noise(
                worldX / 200.0,
                layer * 100.0,
                worldZ / 200.0
        );

        // Map noise to biome index
        int biomeIndex = Mth.floor((biomeSelection + 1.0) * 0.5 * islandBiomes.size());
        biomeIndex = Mth.clamp(biomeIndex, 0, islandBiomes.size() - 1);

        return islandBiomes.get(biomeIndex);
    }

    private void generateIslandTerrain(ChunkAccess chunk, IslandRegion island, int baseHeight) {
        for (IslandPoint point : island.points) {
            // Only generate if point is within this chunk
            if (point.chunkX >= 0 && point.chunkX < 16 && point.chunkZ >= 0 && point.chunkZ < 16) {
                generateIslandColumn(chunk, point, island, baseHeight);
            }
        }
    }

    private void generateIslandColumn(ChunkAccess chunk, IslandPoint point, IslandRegion island, int baseHeight) {
        int x = point.chunkX;
        int z = point.chunkZ;

        // Calculate height variation
        double heightNoise = heightVariationNoise.noise(
                point.worldX / 16.0,
                baseHeight / 32.0,
                point.worldZ / 16.0
        );

        // Add island shape variation
        double shapeNoise = islandShapeNoise.noise(
                point.worldX / 8.0,
                baseHeight / 16.0,
                point.worldZ / 8.0
        );

        int height = baseHeight + (int)(heightNoise * 8) + (int)(shapeNoise * 4);
        height = Math.max(height, baseHeight - 5); // Minimum height

        // Generate terrain column
        BlockState stone = Blocks.STONE.defaultBlockState();
        BlockState dirt = Blocks.DIRT.defaultBlockState();
        BlockState grass = getGrassBlockForBiome(island.biome);

        // Fill from bottom to surface
        for (int y = Math.max(0, height - 10); y <= height; y++) {
            BlockState block;
            if (y == height) {
                block = grass; // Surface block
            } else if (y >= height - 3) {
                block = dirt; // Dirt layer
            } else {
                block = stone; // Stone core
            }

            chunk.setBlockState(chunk.getPos().getWorldPosition().offset(x, y, z), block, false);
        }
    }

    private BlockState getGrassBlockForBiome(Holder<Biome> biome) {
        // Return appropriate surface block based on biome
        // This is a simplified example - you might want more sophisticated logic
        return Blocks.GRASS_BLOCK.defaultBlockState();
    }

    private IslandId getIslandIdAt(int x, int z) {
        // Simple implementation - you might want to cache this
        return new IslandId(x / ISLAND_SPACING, z / ISLAND_SPACING, 0);
    }

    // Helper classes
    private static class IslandRegion {
        List<IslandPoint> points = new ArrayList<>();
        Holder<Biome> biome;
    }

    private static class IslandPoint {
        final int worldX, worldZ;
        final int chunkX, chunkZ;

        IslandPoint(int worldX, int worldZ, int chunkX, int chunkZ) {
            this.worldX = worldX;
            this.worldZ = worldZ;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }
    }

    private static class IslandId {
        final int x, z, layer;

        IslandId(int x, int z, int layer) {
            this.x = x;
            this.z = z;
            this.layer = layer;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IslandId islandId = (IslandId) o;
            return x == islandId.x && z == islandId.z && layer == islandId.layer;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, z, layer);
        }
    }

    public static class CodecRegistry {
        public static final DeferredRegister<Codec<? extends ChunkGenerator>> CHUNK_GENERATORS =
                DeferredRegister.create(Registries.CHUNK_GENERATOR, MODID);

        public static final RegistryObject<Codec<SkyIslandsChunkGenerator>> SKY_ISLANDS_CHUNK_GENERATOR =
                CHUNK_GENERATORS.register("sky_islands", () -> SkyIslandsChunkGenerator.CODEC);

        public static void register(IEventBus eventBus) {
            CHUNK_GENERATORS.register(eventBus);
        }
    }
}