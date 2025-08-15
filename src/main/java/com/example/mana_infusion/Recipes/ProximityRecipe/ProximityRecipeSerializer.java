package com.example.mana_infusion.Recipes.ProximityRecipe;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class ProximityRecipeSerializer implements RecipeSerializer<ProximityRecipe> {


    public static final Codec<ProximityRecipe> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(ProximityRecipe::getId),
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("input_block").forGetter(ProximityRecipe::getInputBlock),
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("output_block").forGetter(ProximityRecipe::getOutputBlock),
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("catalyst_block").forGetter(ProximityRecipe::getCatalystBlock),
                    Codec.INT.fieldOf("search_radius").forGetter(ProximityRecipe::getSearchRadius),
                    Codec.INT.fieldOf("base_process_time").forGetter(ProximityRecipe::getBaseProcessTime),
                    Codec.INT.fieldOf("max_catalysts").forGetter(ProximityRecipe::getMaxCatalysts)
            ).apply(instance, ProximityRecipe::new)
    );


    @Override
    public ProximityRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        String inputBlockName = GsonHelper.getAsString(json, "input_block");
        String outputBlockName = GsonHelper.getAsString(json, "output_block");
        String catalystBlockName = GsonHelper.getAsString(json, "catalyst_block");

        Block inputBlock = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(inputBlockName));
        Block outputBlock = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(outputBlockName));
        Block catalystBlock = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(catalystBlockName));

        int searchRadius = GsonHelper.getAsInt(json, "search_radius", 3);
        int baseProcessTime = GsonHelper.getAsInt(json, "base_process_time", 200);
        int maxCatalysts = GsonHelper.getAsInt(json, "max_catalysts", 8);

        return new ProximityRecipe(recipeId, inputBlock, outputBlock, catalystBlock,
                searchRadius, baseProcessTime, maxCatalysts);
    }

    @Override
    public @Nullable ProximityRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        ResourceLocation id = buffer.readResourceLocation();
        Block inputBlock = buffer.readById(BuiltInRegistries.BLOCK);
        Block outputBlock = buffer.readById(BuiltInRegistries.BLOCK);
        Block catalystBlock = buffer.readById(BuiltInRegistries.BLOCK);
        int searchRadius = buffer.readVarInt();
        int baseProcessTime = buffer.readVarInt();
        int maxCatalysts = buffer.readVarInt();

        return new ProximityRecipe(id, inputBlock, outputBlock, catalystBlock,
                searchRadius, baseProcessTime, maxCatalysts);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, ProximityRecipe recipe) {
        buffer.writeResourceLocation(recipe.getId());
        for (Block block : Arrays.asList(recipe.getInputBlock(), recipe.getOutputBlock(), recipe.getCatalystBlock())) {
            buffer.writeId(BuiltInRegistries.BLOCK, block);
        }
        buffer.writeVarInt(recipe.getSearchRadius());
        buffer.writeVarInt(recipe.getBaseProcessTime());
        buffer.writeVarInt(recipe.getMaxCatalysts());
    }
}
