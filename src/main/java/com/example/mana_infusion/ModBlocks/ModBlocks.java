package com.example.mana_infusion.ModBlocks;

import com.example.mana_infusion.ManaInfusion;

import com.example.mana_infusion.ModBlocks.Crystal.CrystalBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ManaInfusion.MODID);

    public static final RegistryObject<Block> CRYSTAL_BLOCK = BLOCKS.register("crystal",
            () -> new CrystalBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));

}
