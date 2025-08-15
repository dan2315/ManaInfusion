package com.example.mana_infusion.Recipes.ProximityRecipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ProximityRecipe implements Recipe<ProximityRecipeInput> {

    private final ResourceLocation id;
    private final Block inputBlock;
    private final Block outputBlock;
    private final Block catalystBlock;
    private final int searchRadius;
    private final int baseProcessTime; // in ticks
    private final int maxCatalysts;

    public ProximityRecipe(ResourceLocation id, Block inputBlock, Block outputBlock,
                           Block catalystBlock, int searchRadius, int baseProcessTime, int maxCatalysts) {
        this.id = id;
        this.inputBlock = inputBlock;
        this.outputBlock = outputBlock;
        this.catalystBlock = catalystBlock;
        this.searchRadius = searchRadius;
        this.baseProcessTime = baseProcessTime;
        this.maxCatalysts = maxCatalysts;
    }

    public Block getInputBlock() { return inputBlock; }
    public Block getOutputBlock() { return outputBlock; }
    public Block getCatalystBlock() { return catalystBlock; }
    public int getSearchRadius() { return searchRadius; }
    public int getBaseProcessTime() { return baseProcessTime; }
    public int getMaxCatalysts() { return maxCatalysts; }

    @Override
    public boolean matches(ProximityRecipeInput input, Level level) {
        BlockPos pos = input.pos();
        BlockState state = level.getBlockState(pos);

        if (!state.is(inputBlock)) {
            return false;
        }

        return getCatalystCount(level, pos) > 0;
    }

    public int getCatalystCount(Level level, BlockPos centerPos) {
        int count = 0;

        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -searchRadius; y <= searchRadius; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    if (x == 0 && y == 0 && z == 0) continue; // Skip center block

                    BlockPos checkPos = centerPos.offset(x, y, z);
                    if (level.getBlockState(checkPos).is(catalystBlock)) {
                        count++;
                        if (count >= maxCatalysts) {
                            return maxCatalysts; // Cap the count
                        }
                    }
                }
            }
        }

        return count;
    }

    public int getProcessTime(Level level, BlockPos pos) {
        int catalystCount = getCatalystCount(level, pos);
        if (catalystCount == 0) return Integer.MAX_VALUE; // Won't process without catalysts

        double speedMultiplier = 1.0 + (catalystCount * 0.5);
        return Math.max(1, (int)(baseProcessTime / speedMultiplier));
    }

    public BlockState getResultBlock(Level level, BlockPos pos) {
        return outputBlock.defaultBlockState();
    }

    @Override
    public ItemStack assemble(ProximityRecipeInput p_44001_, RegistryAccess p_267165_) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess p_267052_) {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.PROXIMITY_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.PROXIMITY_RECIPE_TYPE.get();
    }
}
