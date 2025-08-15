package com.example.mana_infusion.Recipes.ProximityRecipe;

import com.example.mana_infusion.ManaInfusion;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModRecipes {
    public static DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, ManaInfusion.MODID);
    public static DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, ManaInfusion.MODID);

    public static final RegistryObject<RecipeType<Recipe<?>>> PROXIMITY_RECIPE_TYPE =
            RECIPE_TYPES.register("proximity", () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(ManaInfusion.MODID, "proximity")));

    public static final Supplier<RecipeSerializer<ProximityRecipe>> PROXIMITY_RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register("proximity", ProximityRecipeSerializer::new);
}
