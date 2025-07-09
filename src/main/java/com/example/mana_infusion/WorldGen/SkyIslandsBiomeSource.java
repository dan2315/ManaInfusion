package com.example.mana_infusion.WorldGen;

import com.example.mana_infusion.Utils.Noise.WorleyNoise;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.example.mana_infusion.ManaInfusion.MODID;

public class SkyIslandsBiomeSource extends BiomeSource {
    private final List<Holder<Biome>> islandBiomes;
    private final Holder<Biome> voidBiome;
    private final HolderSet<Biome> biomes;
    private WorleyNoise worleyNoise;
    private long seed;
    // Cache to store biome assignments for each island
    private final Map<Vector2i, Holder<Biome>> islandBiomeCache;

    public static final Codec<SkyIslandsBiomeSource> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    RegistryCodecs.homogeneousList(Registries.BIOME, Biome.DIRECT_CODEC)
                            .fieldOf("biomes")
                            .forGetter(SkyIslandsBiomeSource::getBiomes)
            ).apply(instance, SkyIslandsBiomeSource::new));

    public SkyIslandsBiomeSource(HolderGetter<Biome> biomeGetter) {
        this.islandBiomeCache = new HashMap<>();

        List<Holder<Biome>> islandBiomesList = Arrays.asList(
                biomeGetter.getOrThrow(Biomes.PLAINS),
                biomeGetter.getOrThrow(Biomes.FOREST),
                biomeGetter.getOrThrow(Biomes.BIRCH_FOREST),
                biomeGetter.getOrThrow(Biomes.DARK_FOREST),
                biomeGetter.getOrThrow(Biomes.DESERT),
                biomeGetter.getOrThrow(Biomes.SAVANNA),
                biomeGetter.getOrThrow(Biomes.MEADOW),
                biomeGetter.getOrThrow(Biomes.CHERRY_GROVE),
                biomeGetter.getOrThrow(Biomes.TAIGA),
                biomeGetter.getOrThrow(Biomes.SNOWY_PLAINS),
                biomeGetter.getOrThrow(Biomes.ICE_SPIKES),
                biomeGetter.getOrThrow(Biomes.MUSHROOM_FIELDS),
                biomeGetter.getOrThrow(Biomes.NETHER_WASTES),
                biomeGetter.getOrThrow(Biomes.WARPED_FOREST),
                biomeGetter.getOrThrow(Biomes.END_MIDLANDS),
                biomeGetter.getOrThrow(Biomes.END_HIGHLANDS)
        );

        this.seed = SkyIslandsChunkGenerator.SEED;
        this.worleyNoise = new WorleyNoise(seed, 3, 1024);
        Holder<Biome> voidBiomeHolder = biomeGetter.getOrThrow(Biomes.THE_VOID);

        List<Holder<Biome>> allBiomes = new ArrayList<>(islandBiomesList);
        allBiomes.add(voidBiomeHolder);
        HolderSet<Biome> biomeSet = HolderSet.direct(allBiomes);

        this.islandBiomes = islandBiomesList;
        this.voidBiome = voidBiomeHolder;
        this.biomes = biomeSet;
    }

    public SkyIslandsBiomeSource(HolderSet<Biome> biomes) {
        this.biomes = biomes;
        this.islandBiomes = biomes.stream().limit(16).toList();
        this.voidBiome = biomes.stream().findFirst().orElse(null);
        this.worleyNoise = new WorleyNoise(SkyIslandsChunkGenerator.SEED, 3, 1024);
        this.islandBiomeCache = new HashMap<>();
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.concat(
                islandBiomes.stream(),
                Stream.of(voidBiome)
        );
    }

    @Override
    public Holder<Biome> getNoiseBiome(int quartX, int quartY, int quartZ, Climate.Sampler sampler) {
        int blockX = quartX << 2;
        int blockZ = quartZ << 2;
        return getBiomeFromIslandType(blockX, blockZ);
    }

    private Holder<Biome> getBiomeFromIslandType(int blockX, int blockZ) {
        WorleyNoise.WorleyResult worleyResult = worleyNoise.GetWorleyData(blockX, blockZ);
        var invertedNoise = 1 - worleyResult.noiseValue;
        Vector2i closestPoint = worleyResult.closestPoint;

        Holder<Biome> cachedBiome = islandBiomeCache.get(closestPoint);
        if (cachedBiome != null) {
            return cachedBiome;
        }

        long islandSeed = (long)closestPoint.x * 374761393L + (long)closestPoint.y * 668265263L + seed;
        RandomSource islandRandom = RandomSource.create(islandSeed);
        int islandType = islandRandom.nextInt(islandBiomes.size());

        Holder<Biome> selectedBiome = islandBiomes.get(islandType);

        islandBiomeCache.put(new Vector2i(closestPoint.x, closestPoint.y), selectedBiome);

        return selectedBiome;
    }

    private Holder<Biome> getBiome(int num) {
        return biomes.get(num);
    }

    public List<Holder<Biome>> getIslandBiomes() {
        return islandBiomes;
    }

    public Holder<Biome> getVoidBiome() {
        return voidBiome;
    }

    public HolderSet<Biome> getBiomes() {
        return biomes;
    }

    public static class CodecRegistry {
        public static final DeferredRegister<Codec<? extends BiomeSource>> BIOME_SOURCES =
                DeferredRegister.create(Registries.BIOME_SOURCE, MODID);

        public static final RegistryObject<Codec<SkyIslandsBiomeSource>> SKY_ISLANDS_BIOME_SOURCE =
                BIOME_SOURCES.register("sky_islands", () -> SkyIslandsBiomeSource.CODEC);

        public static void register(IEventBus eventBus) {
            BIOME_SOURCES.register(eventBus);
        }
    }
}