package com.example.mana_infusion.WorldGen;

import com.example.mana_infusion.ManaInfusion;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import java.util.HashMap;
import java.util.Map;

public class SkyIslandsWorldPreset {

    public static final ResourceKey<WorldPreset> SKY_ISLANDS_PRESET = ResourceKey.create(Registries.WORLD_PRESET,
            ResourceLocation.fromNamespaceAndPath(ManaInfusion.MODID, "sky_islands_preset"));

    public static void bootstrap(BootstapContext<WorldPreset> context) {
        System.out.println("[MI] Bootstrapping SkyIslandsWorldPreset...");
        context.register(SKY_ISLANDS_PRESET, createPreset(context));
        System.out.println("[MI] SkyIslandsWorldPreset registered successfully!");
    }

    private static WorldPreset createPreset(BootstapContext<WorldPreset> context) {
        HolderGetter<DimensionType> holdergetter = context.lookup(Registries.DIMENSION_TYPE);
        var dimensionType = holdergetter.getOrThrow(SkyIslandsDimension.SKY_ISLANDS_TYPE);

        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        Holder<NoiseGeneratorSettings> overworldSettings =
                context.lookup(Registries.NOISE_SETTINGS).getOrThrow(NoiseGeneratorSettings.OVERWORLD);

        SkyIslandsChunkGenerator skyIslandsChunkGenerator = new SkyIslandsChunkGenerator(new SkyIslandsBiomeSource(biomes), overworldSettings);

        LevelStem islandsStem = new LevelStem(dimensionType, skyIslandsChunkGenerator);
        Map<ResourceKey<LevelStem>, LevelStem> dimensions = new HashMap<>();
        dimensions.put(LevelStem.OVERWORLD, islandsStem);
        return new WorldPreset(dimensions);
    }
}
