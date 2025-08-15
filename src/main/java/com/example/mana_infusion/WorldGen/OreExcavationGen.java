package com.example.mana_infusion.WorldGen;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.example.mana_infusion.ManaInfusion.LOGGER;
import static com.example.mana_infusion.ManaInfusion.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class OreExcavationGen {
    public static ResourceLocation INDUSTRIAL_VEIN = ResourceLocation.fromNamespaceAndPath("uf", "industrial_vein");
    public static ResourceLocation LIGHT_METALS_VEIN = ResourceLocation.fromNamespaceAndPath("uf", "light_metals_vein");
    public static ResourceLocation CRYSTAL_VEIN = ResourceLocation.fromNamespaceAndPath("uf", "crystal_vein");
    public static ResourceLocation GEM_VEIN = ResourceLocation.fromNamespaceAndPath("uf", "gem_vein");

    public static ResourceLocation GetVein(int n) {
        return switch (n) {
            case 1 -> LIGHT_METALS_VEIN;
            case 2 -> CRYSTAL_VEIN;
            case 3 -> GEM_VEIN;
            default -> INDUSTRIAL_VEIN; // 0
        };
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        ChunkAccess chunk = event.getChunk();

        if (chunk instanceof LevelChunk levelChunk) {

            ChunkGenerator generator = serverLevel.getChunkSource().getGenerator();
            if (generator instanceof SkyIslandsChunkGenerator skyGenerator) {
                skyGenerator.GenerateOreVeinsForChunk(levelChunk);
            } else {
                LOGGER.warn("Chunk generator is not SkyIslandsChunkGenerator: " +
                        generator.getClass().getSimpleName());
            }
        }
    }
}
