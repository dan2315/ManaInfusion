package com.example.mana_infusion.WorldGen;

import com.example.mana_infusion.ManaInfusion;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.OptionalLong;

public class SkyIslandsDimension {

    public static final ResourceKey<LevelStem> SKY_ISLANDS_LEVEL_STEM =
            ResourceKey.create(Registries.LEVEL_STEM, ResourceLocation.fromNamespaceAndPath(ManaInfusion.MODID, "sky_islands_dim"));
    public static final ResourceKey<Level> SKY_ISLANDS_LEVEL =
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(ManaInfusion.MODID, "sky_islands"));
    public static final ResourceKey<DimensionType> SKY_ISLANDS_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, ResourceLocation.fromNamespaceAndPath(ManaInfusion.MODID, "sky_islands_type"));

    public static void bootstrapType(BootstapContext<DimensionType> context) {
        context.register(SKY_ISLANDS_TYPE, new DimensionType(
                OptionalLong.of(12000), // fixedTime
                false, // hasSkylight
                false, // hasCeiling
                false, // ultraWarm
                false, // natural
                1.0, // coordinateScale
                true, // bedWorks
                false, // respawnAnchorWorks
                0, // minY
                256, // height
                256, // logicalHeight
                BlockTags.INFINIBURN_OVERWORLD, // infiniburn
                BuiltinDimensionTypes.OVERWORLD_EFFECTS, // effectsLocation
                1.0f, // ambientLight
                new DimensionType.MonsterSettings(false, false, ConstantInt.of(0), 0)));
    }


    public static void bootstrapStem(BootstapContext<LevelStem> context) {
        System.out.println("[MI] Bootstrapping SkyIslandsDimension...");
        context.register(SKY_ISLANDS_LEVEL_STEM, createDimension(context));
        System.out.println("[MI] SkyIslandsDimension registered successfully!");
    }

    public static LevelStem createDimension(BootstapContext<LevelStem> context) {
        var dimensionTypeRegistry = context.lookup(Registries.DIMENSION_TYPE);

        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        Holder<NoiseGeneratorSettings> overworldSettings =
                context.lookup(Registries.NOISE_SETTINGS).getOrThrow(NoiseGeneratorSettings.OVERWORLD);

        SkyIslandsChunkGenerator skyIslandsChunkGenerator = new SkyIslandsChunkGenerator(new SkyIslandsBiomeSource(biomes), overworldSettings);
        
        return new LevelStem(dimensionTypeRegistry.getOrThrow(SKY_ISLANDS_TYPE), skyIslandsChunkGenerator);
    }
}