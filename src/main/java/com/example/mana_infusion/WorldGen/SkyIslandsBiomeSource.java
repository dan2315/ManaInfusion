package com.example.mana_infusion.WorldGen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.example.mana_infusion.ManaInfusion.MODID;

public class SkyIslandsBiomeSource extends BiomeSource {
    private final List<Holder<Biome>> islandBiomes;
    private final Holder<Biome> voidBiome;
    private final HolderSet<Biome> biomes;

    public static final Codec<SkyIslandsBiomeSource> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    RegistryCodecs.homogeneousList(Registries.BIOME, Biome.DIRECT_CODEC)
                            .fieldOf("biomes")
                            .forGetter(SkyIslandsBiomeSource::getBiomes)
            ).apply(instance, SkyIslandsBiomeSource::new));

    public SkyIslandsBiomeSource(HolderGetter<Biome> biomeGetter) {
        List<Holder<Biome>> islandBiomesList = Arrays.asList(
//                biomeGetter.getOrThrow(Biomes.PLAINS),
//                biomeGetter.getOrThrow(Biomes.FOREST),
//                biomeGetter.getOrThrow(Biomes.BIRCH_FOREST),
//                biomeGetter.getOrThrow(Biomes.DARK_FOREST),
//                biomeGetter.getOrThrow(Biomes.DESERT),
//                biomeGetter.getOrThrow(Biomes.SAVANNA),
//                biomeGetter.getOrThrow(Biomes.MEADOW),
//                biomeGetter.getOrThrow(Biomes.CHERRY_GROVE),
//                biomeGetter.getOrThrow(Biomes.TAIGA),
//                biomeGetter.getOrThrow(Biomes.SNOWY_PLAINS),
//                biomeGetter.getOrThrow(Biomes.ICE_SPIKES),
                biomeGetter.getOrThrow(Biomes.MUSHROOM_FIELDS)
        );

        Holder<Biome> voidBiomeHolder = biomeGetter.getOrThrow(Biomes.THE_VOID);

        List<Holder<Biome>> allBiomes = new ArrayList<>(islandBiomesList);
        allBiomes.add(voidBiomeHolder);
        HolderSet<Biome> biomeSet = HolderSet.direct(allBiomes);

        this.islandBiomes = islandBiomesList;
        this.voidBiome = voidBiomeHolder;
        this.biomes = biomeSet;
    }

    // Constructor for codec deserialization
    public SkyIslandsBiomeSource(HolderSet<Biome> biomes) {
        this.biomes = biomes;
        this.islandBiomes = biomes.stream().limit(1).toList(); // Turn back to 12
        this.voidBiome = biomes.stream().findFirst().orElse(null);
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
        return voidBiome;
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