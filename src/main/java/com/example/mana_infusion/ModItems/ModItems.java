package com.example.mana_infusion.ModItems;

import com.example.mana_infusion.ManaInfusion;

import com.example.mana_infusion.ModBlocks.ModBlocks;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ManaInfusion.MODID);

    public static final RegistryObject<Item> CRYSTAL_BLOCK_ITEM = ITEMS.register("crystal", () -> new BlockItem(ModBlocks.CRYSTAL_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> EXAMPLE_ITEM = ITEMS.register("example_item", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().nutrition(1).saturationMod(2f).build())));
//    public static final RegistryObject<Item> INVISIBLE_COLLISION_BLOCK_ITEM = ITEMS.register("invisible_collision_block", () -> new BlockItem(ModBlocks.INVISIBLE_COLLISION_BLOCK.get(), new Item.Properties()));

}
