package com.example.mana_infusion.Commands;

import com.example.mana_infusion.WorldGen.SkyIslandsChunkGenerator;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import com.tom.createores.OreDataCapability;

public class OreVeinCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("oreveins")
                .requires(source -> source.hasPermission(2)) // OP level 2
                .then(Commands.argument("radius", IntegerArgumentType.integer(1, 50))
                        .executes(context -> checkOreVeins(context, IntegerArgumentType.getInteger(context, "radius"))))
                .executes(context -> checkOreVeins(context, 5)) // Default radius of 5 chunks
        );

        dispatcher.register(Commands.literal("findveins")
                .requires(source -> source.hasPermission(2))
                .executes(context -> findIslandVeins(context))
        );
    }

    private static int checkOreVeins(CommandContext<CommandSourceStack> context, int radiusChunks) {
        CommandSourceStack source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players"));
            return 0;
        }

        ServerLevel level = player.serverLevel();
        BlockPos playerPos = player.blockPosition();
        ChunkPos playerChunk = new ChunkPos(playerPos);

        source.sendSuccess(() -> Component.literal("§6Checking ore veins in " + radiusChunks + " chunk radius..."), false);

        int veinsFound = 0;

        for (int x = -radiusChunks; x <= radiusChunks; x++) {
            for (int z = -radiusChunks; z <= radiusChunks; z++) {
                ChunkPos checkPos = new ChunkPos(playerChunk.x + x, playerChunk.z + z);

                // Check if chunk is loaded
                if (!level.isLoaded(checkPos.getWorldPosition())) {
                    continue;
                }

                LevelChunk chunk = level.getChunk(checkPos.x, checkPos.z);

                try {
                    OreDataCapability.OreData oreData = OreDataCapability.getData(chunk);

                    if (oreData != null && oreData.isLoaded()) {
                        veinsFound++;

                        double distance = Math.sqrt(
                                Math.pow(checkPos.getMiddleBlockX() - playerPos.getX(), 2) +
                                        Math.pow(checkPos.getMiddleBlockZ() - playerPos.getZ(), 2)
                        );

                        String recipeInfo = "Unknown";
                        try {
                            var recipeManager = level.getRecipeManager();
                            if (oreData.getRecipe(recipeManager) != null) {
                                recipeInfo = oreData.getRecipe(recipeManager).toString();
                            }
                        } catch (Exception e) {
                            recipeInfo = "Error reading recipe";
                        }

                        String finalRecipeInfo = recipeInfo;
                        source.sendSuccess(() -> Component.literal(
                                String.format("§a[%d, %d] §7Distance: §f%.1fm §7Recipe: §e%s",
                                        checkPos.x, checkPos.z, distance, finalRecipeInfo)
                        ), false);
                    }
                } catch (Exception e) {
                    // Silently continue if chunk doesn't have ore data
                }
            }
        }

        final int finalVeinsFound = veinsFound;
        source.sendSuccess(() -> Component.literal("§6Found §a" + finalVeinsFound + "§6 ore veins in radius"), false);

        return veinsFound;
    }

    private static int findIslandVeins(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players"));
            return 0;
        }

        ServerLevel level = player.serverLevel();
        BlockPos playerPos = player.blockPosition();

        // Try to get the chunk generator
        if (!(level.getChunkSource().getGenerator() instanceof SkyIslandsChunkGenerator generator)) {
            source.sendFailure(Component.literal("§cThis command only works with Sky Islands chunk generator"));
            return 0;
        }

        try {
            ChunkPos[] veinPositions = generator.getVeinPositionsForIsland(playerPos.getX(), playerPos.getZ());

            source.sendSuccess(() -> Component.literal("§6Ore veins for current island:"), false);

            String[] veinTypes = {"Iron", "Gold", "Diamond", "Redstone"};

            for (int i = 0; i < 4; i++) {
                if (veinPositions[i] != null) {
                    ChunkPos veinPos = veinPositions[i];
                    double distance = Math.sqrt(
                            Math.pow(veinPos.getMiddleBlockX() - playerPos.getX(), 2) +
                                    Math.pow(veinPos.getMiddleBlockZ() - playerPos.getZ(), 2)
                    );

                    final int index = i;
                    source.sendSuccess(() -> Component.literal(
                            String.format("§e%s Vein: §f[%d, %d] §7Distance: §a%.1fm",
                                    veinTypes[index], veinPos.x, veinPos.z, distance)
                    ), false);
                } else {
                    final int index = i;
                    source.sendSuccess(() -> Component.literal(
                            String.format("§c%s Vein: §7Not found", veinTypes[index])
                    ), false);
                }
            }

            // Find closest vein
            var closestVein = generator.getClosestOreVein(playerPos.getX(), playerPos.getZ());
            if (closestVein != null) {
                ChunkPos closest = closestVein.getFirst();
                int veinType = closestVein.getSecond();
                double distance = Math.sqrt(
                        Math.pow(closest.getMiddleBlockX() - playerPos.getX(), 2) +
                                Math.pow(closest.getMiddleBlockZ() - playerPos.getZ(), 2)
                );

                source.sendSuccess(() -> Component.literal(
                        String.format("§6Closest vein: §e%s §7at §f[%d, %d] §7(%.1fm away)",
                                veinTypes[veinType], closest.x, closest.z, distance)
                ), false);
            } else {
                source.sendSuccess(() -> Component.literal("§cNo veins found for current island"), false);
            }

            return 1;

        } catch (Exception e) {
            source.sendFailure(Component.literal("§cError finding island veins: " + e.getMessage()));
            return 0;
        }
    }
}