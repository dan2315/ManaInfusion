package com.example.mana_infusion;

import com.example.mana_infusion.ModBlocks.Crystal.CrystalBlockMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MOD_MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ManaInfusion.MODID);

    public static final RegistryObject<MenuType<CrystalBlockMenu>> CRYSTAL_MENU =
            MOD_MENU_TYPES.register("crystal_menu", () ->
                    IForgeMenuType.create(CrystalBlockMenu::new));
}
