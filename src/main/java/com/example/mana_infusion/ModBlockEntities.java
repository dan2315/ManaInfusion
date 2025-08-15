package com.example.mana_infusion;

import com.example.mana_infusion.ModBlocks.Crystal.CrystalBlockEntity;
import com.example.mana_infusion.ModBlocks.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ManaInfusion.MODID);

    public static final RegistryObject<BlockEntityType<CrystalBlockEntity>> CRYSTAL_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("crystal_block_entity", () ->
                    BlockEntityType.Builder.of(CrystalBlockEntity::new,
                            ModBlocks.CRYSTAL_BLOCK.get()).build(null));
}
