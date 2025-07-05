package com.example.mana_infusion.DataGen;

import com.example.mana_infusion.ManaInfusion;
import com.example.mana_infusion.WorldGen.SkyIslandsDimension;
import com.example.mana_infusion.WorldGen.SkyIslandsWorldPreset;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModWorldGenProvider extends DatapackBuiltinEntriesProvider {

    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.DIMENSION_TYPE, SkyIslandsDimension::bootstrapType)
            .add(Registries.LEVEL_STEM, SkyIslandsDimension::bootstrapStem)
            .add(Registries.WORLD_PRESET, SkyIslandsWorldPreset::bootstrap);

    public ModWorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(ManaInfusion.MODID));
        System.out.println("[MI] ModWorldGenProvider initialized for " + ManaInfusion.MODID);
    }
}
