package com.example.mana_infusion.WorldGen;

import com.example.mana_infusion.Utils.Noise.EasingFunctions;
import com.example.mana_infusion.Utils.Noise.Mathd;
import com.example.mana_infusion.Utils.Noise.WorleyNoise;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.example.mana_infusion.ManaInfusion.MODID;

public class SkyIslandsChunkGenerator extends NoiseBasedChunkGenerator {

    // Biome assignments for different island types
    public static long SEED = 12345L;
    private final WorleyNoise worleyNoiseMainLayer;
    private final WorleyNoise worleyNoiseSecondaryLayer;
    private final PerlinNoise terrainNoise;

    private final Map<Long, RandomSource> cachedRandomSources = new HashMap<>();

    private final BiomeSource biomeSource;
    private final Holder<NoiseGeneratorSettings> settings;

    public SkyIslandsChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        super(biomeSource, settings);
        this.biomeSource = biomeSource;

        this.settings = settings;

        this.worleyNoiseMainLayer = new WorleyNoise(SEED, 3, 1536);
        this.worleyNoiseSecondaryLayer = new WorleyNoise(SEED, 5, 512);

        terrainNoise = PerlinNoise.create(RandomSource.create(SEED), List.of(-6, -5, -4, -3));
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

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor heightAccessor, RandomState randomState) {
        return calculateHeightForLayer(x, z, worleyNoiseMainLayer, 64, 48, 128, 0.6f, 0.8f, 0.04f);
    }

    private int calculateHeightForLayer(int x, int z, WorleyNoise worleyNoise, int baseHeight, int upwardTendency, int downwardTendency, float minSize, float maxSize, float detailsOffset) {
        var worleyResult = worleyNoise.GetWorleyData(x, z);
        long islandSeed = WorleyNoise.regionKey(worleyResult.closestPoint.x, worleyResult.closestPoint.y);

        maxSize = 1 - maxSize;
        minSize = 1 - minSize;

        RandomSource random = RandomSource.create(islandSeed);
        var edgeValue = random.nextFloat() * (maxSize - minSize) + minSize;

        PerlinNoise radialNoise = PerlinNoise.create(random, List.of(0));
        Vector2f directionVector = new Vector2f(worleyResult.closestPoint.x - x, worleyResult.closestPoint.y - z).normalize();
        float radialNoiseValue = ((float) radialNoise.getValue(directionVector.x * 2, edgeValue, directionVector.y * 2) + 1) / 2;

        float invertedNoise = 1 - worleyResult.noiseValue;
        float mergePoint = Mathd.clamp(worleyResult.noiseValue * (1/edgeValue), 0f, 1f);
        float mergedNoise = Mathd.lerp(invertedNoise, radialNoiseValue * invertedNoise * 0.5f, mergePoint);

        if (x == worleyResult.closestPoint.x && z == worleyResult.closestPoint.y) {
            mergedNoise = invertedNoise;
        }

        float islandValue = Math.max(0f, (mergedNoise - edgeValue) * (1 / (1 - edgeValue)));
        float rawNoiseValue = (float) terrainNoise.getValue(x + 100 * baseHeight, 69, z + 100 * baseHeight);
        float terrainNoiseValue = (rawNoiseValue + 1) / 2;

        float detailValue = 1.0f - worleyResult.noiseValue;

        int maxY = 0;

        // Details Gen
        if (islandValue <= 0 && detailValue > 0) {
            float detailHeight = detailValue * rawNoiseValue - detailsOffset;

            if (detailHeight > 0) {
                int up = Math.round(detailHeight * upwardTendency);
                int down = -Math.round((Mathd.clamp(detailHeight, 0f, 1f)) * downwardTendency/2);
                maxY = Math.max(maxY, baseHeight + up);
            }
        }

        // Island Gen
        if (islandValue > 0) {
            int elevation = Math.round(random.nextFloat() * 64);
            int up = Math.round(islandValue * terrainNoiseValue * upwardTendency);
            int down = (int) -Math.round(EasingFunctions.easeOutCirc(islandValue) * downwardTendency);
            maxY = Math.max(maxY, baseHeight + elevation + up);
        }

        return maxY;
    }

    private RandomSource getOrCreateRandomSource(long seed) {
        return cachedRandomSources.computeIfAbsent(seed, RandomSource::create);
    }

    @Override
    public void createStructures(RegistryAccess registryAccess, ChunkGeneratorStructureState structureState, StructureManager structureManager, ChunkAccess chunkAccess, StructureTemplateManager templateManager) {
        super.createStructures(registryAccess, structureState, structureManager, chunkAccess, templateManager);
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunkAccess) {
        return CompletableFuture.supplyAsync(() -> {
            GenerateLandmassForChunk(chunkAccess);
            return chunkAccess;
        }, executor);
    }

    @Override
    public void applyCarvers(WorldGenRegion worldGenRegion, long seed, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {
        super.applyCarvers(worldGenRegion, seed, randomState, biomeManager, structureManager, chunkAccess, carving);
    }

    @Override
    public void buildSurface(
            @NotNull ChunkAccess chunkAccess,
            @NotNull WorldGenerationContext generationContext,
            @NotNull RandomState randomState,
            @NotNull StructureManager structureManager,
            @NotNull BiomeManager biomeManager,
            @NotNull Registry<Biome> biomes,
            @NotNull Blender blender) {
        super.buildSurface(chunkAccess, generationContext, randomState, structureManager, biomeManager, biomes, blender);
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel worldGenLevel, ChunkAccess chunkAccess, StructureManager structureManager) {
        super.applyBiomeDecoration(worldGenLevel, chunkAccess, structureManager);
    }

    public Holder<NoiseGeneratorSettings> getSettings() {
        return settings;
    }

    private void GenerateLandmassForChunk(ChunkAccess chunk) {

        var layers = 2;
        for (int l = 0; l < layers; l++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    switch (l) {
                        case 0: PlaceLandColumn(chunk, x, z, worleyNoiseMainLayer,64, 48, 128, 0.6f,0.8f, 0.04f);
                            break;
                        case 1: PlaceLandColumn(chunk, x, z, worleyNoiseSecondaryLayer,192, 8, 32, 0.35f, 0.45f, 0.0f);
                            break;
                    }
                }
            }
        }
    }

    private void PlaceLandColumn(ChunkAccess chunk, int x, int z, WorleyNoise worleyNoise, int baseHeight, int upwardTendency, int downwardTendency, float minSize, float maxSize, float detailsOffset) {
        int blockX = chunk.getPos().getBlockX(x);
        int blockZ = chunk.getPos().getBlockZ(z);
        var worleyResult = worleyNoise.GetWorleyData(blockX, blockZ);

        long islandSeed = WorleyNoise.regionKey(worleyResult.closestPoint.x, worleyResult.closestPoint.y);
        maxSize = 1 - maxSize;
        minSize = 1 - minSize;

        RandomSource random = RandomSource.create(islandSeed);
        var edgeValue = random.nextFloat() * (maxSize - minSize) + minSize;

        PerlinNoise radialNoise = PerlinNoise.create(random, List.of(0));
        Vector2f directionVector = new Vector2f(worleyResult.closestPoint.x - blockX, worleyResult.closestPoint.y - blockZ).normalize();
        float radialNoiseValue = ((float) radialNoise.getValue(directionVector.x * 2, edgeValue, directionVector.y * 2) + 1) / 2;

        float invertedNoise = 1 - worleyResult.noiseValue;
        float mergePoint = Mathd.clamp(worleyResult.noiseValue  * (1/edgeValue), 0f, 1f);
        float mergedNoise = Mathd.lerp(invertedNoise, radialNoiseValue * invertedNoise * 0.5f, mergePoint);
        if (blockX == worleyResult.closestPoint.x && blockZ == worleyResult.closestPoint.y) {
            mergedNoise = invertedNoise;
        }
        float islandValue = Math.max(0f, (mergedNoise - edgeValue) * (1 / (1 - edgeValue)));
        float rawNoiseValue = (float) terrainNoise.getValue(blockX + 100 * baseHeight, 69, blockZ + 100 * baseHeight);
        float terrainNoiseValue = (rawNoiseValue + 1) / 2;

        float detailValue = 1.0f - worleyResult.noiseValue;

        // Details Gen
        if (islandValue <= 0 && detailValue > 0) {
            float heightVariation = (float) terrainNoise.getValue((double) blockX / 2, 322, (double) blockZ / 2);
            float detailHeight = detailValue * rawNoiseValue - detailsOffset;

            if (detailHeight > 0) {
                int up = Math.round(detailHeight * upwardTendency);
                int down = -Math.round((Mathd.clamp(detailHeight, 0f, 1f)) * (downwardTendency < 24 ? downwardTendency * 2 : downwardTendency));
                Holder<Biome> biome = biomeSource.getNoiseBiome(blockX >> 2, 96 >> 2, blockZ >> 2, null);

                for (int y = down; y <= up; y++) {
                    BlockState blockState = getBaseBlockForBiome(biome);
                    chunk.setBlockState(new BlockPos(blockX, y + baseHeight + Math.round(heightVariation * 16), blockZ), blockState, true);
                }
            }
        }

        //Island Gen
        if (islandValue > 0) {
            int elevation = Math.round(random.nextFloat() * 64);

            int up = Math.round(islandValue * terrainNoiseValue * upwardTendency);
            int down = (int) -Math.round(EasingFunctions.easeOutCirc(islandValue) * downwardTendency);

            Holder<Biome> biome = biomeSource.getNoiseBiome(blockX >> 2, 96 >> 2, blockZ >> 2, null);

            for (int y = down; y < up; y++) {
                BlockState blockState = getBaseBlockForBiome(biome);
                chunk.setBlockState(new BlockPos(blockX, y + baseHeight + elevation, blockZ), blockState, true);
            }
        }
    }

    private BlockState getBaseBlockForBiome(Holder<Biome> biome) {
        if (biome.is(BiomeTags.IS_NETHER)) {
            return getNetherBlockForBiome(biome);
        }

        if (biome.is(BiomeTags.IS_END)) {
            return Blocks.END_STONE.defaultBlockState();
        }

        return Blocks.STONE.defaultBlockState();
    }

    private BlockState getNetherBlockForBiome(Holder<Biome> biome) {
        String biomeKey = biome.unwrapKey().orElse(null) != null ?
                biome.unwrapKey().get().location().toString() : "";

        if (biomeKey.contains("soul_sand_valley")) {
            return Blocks.SOUL_SAND.defaultBlockState();
        } else if (biomeKey.contains("basalt_deltas")) {
            return Blocks.BASALT.defaultBlockState();
        } else if (biomeKey.contains("warped_forest") || biomeKey.contains("crimson_forest")) {
            return Blocks.NETHERRACK.defaultBlockState();
        }

        return Blocks.NETHERRACK.defaultBlockState();
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