package com.example.mana_infusion.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;



public class CrystalBlock extends Block {

    public CrystalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (canPlaceExtensions(level, pos)) {
            return this.defaultBlockState();
        }

        return null;
    }

    private boolean canPlaceExtensions(Level level, BlockPos centerPos) {
        BlockPos[] extensionPositions = {
//                centerPos.offset(1, 0, 0),  // East
//                centerPos.offset(-1, 0, 0), // West
//                centerPos.offset(0, 0, 1),  // South
//                centerPos.offset(0, 0, -1), // North
//                centerPos.offset(0, 1, 0),  // Up
                centerPos.offset(0, 1, 0),
                centerPos.offset(0, 2, 0),
                centerPos.offset(0, 3, 0),
                centerPos.offset(0, 4, 0),
                centerPos.offset(0, 5, 0)
        };

        for (BlockPos pos : extensionPositions) {
            if (!level.getBlockState(pos).canBeReplaced()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide()) {

            BlockPos[] extensionPositions = {
                    pos.offset(0, 1, 0),
                    pos.offset(0, 2, 0),
                    pos.offset(0, 3, 0),
                    pos.offset(0, 4, 0),
                    pos.offset(0, 5, 0)
            };

            for (BlockPos extPos : extensionPositions) {
                if (level.getBlockState(extPos).canBeReplaced()) {

                    level.setBlock(extPos, Blocks.BARRIER.defaultBlockState(), 3);
                }
            }
        }
    }

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos pos, BlockState p_60518_, boolean p_60519_) {
        if (!level.isClientSide()) {
            BlockPos[] extensionPositions = {
                    pos.offset(0, 1, 0),
                    pos.offset(0, 2, 0),
                    pos.offset(0, 3, 0),
                    pos.offset(0, 4, 0),
                    pos.offset(0, 5, 0)
            };

            for (BlockPos extPos : extensionPositions) {
                if (level.getBlockState(extPos).getBlock() == Blocks.BARRIER) {
                    level.destroyBlock(extPos, false);
                }
            }
        }
    }
}
