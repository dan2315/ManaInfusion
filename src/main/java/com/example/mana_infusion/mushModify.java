package com.example.mana_infusion;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.lang.reflect.Field;

public class mushModify {
    public static void makeMushoomsEdible() {
        try {
            FoodProperties redMushroomFood = new FoodProperties.Builder()
                    .nutrition(2)
                    .saturationMod(0.1f)
                    .build();

            FoodProperties brownMushroomFood = new FoodProperties.Builder()
                    .nutrition(1)
                    .saturationMod(0.5f)
                    .build();
            
            setFoodProperties(Items.RED_MUSHROOM, redMushroomFood);
            setFoodProperties(Items.BROWN_MUSHROOM, brownMushroomFood);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setFoodProperties(Item item, FoodProperties foodProperties) {
        try {
            Field foodField = Item.class.getDeclaredField("foodProperties");
            foodField.setAccessible(true);
            foodField.set(item, foodProperties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public static void onFurnaceFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
        Item item = event.getItemStack().getItem();
        
        if (item == Items.RED_MUSHROOM) {
            event.setBurnTime(200);
        } else if (item == Items.BROWN_MUSHROOM) {
            event.setBurnTime(100);
        }
    }
}
