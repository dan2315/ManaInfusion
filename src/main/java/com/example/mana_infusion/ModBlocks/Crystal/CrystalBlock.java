package com.example.mana_infusion.ModBlocks.Crystal;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public class CrystalBlock extends BaseEntityBlock {

    public CrystalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrystalBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof CrystalBlockEntity) {
                NetworkHooks.openScreen(((ServerPlayer) player), (CrystalBlockEntity) entity, pos);
            } else {
                throw new IllegalStateException("Container provider is missing!");
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
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
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof CrystalBlockEntity) {
                CrystalBlockEntity crystalEntity = (CrystalBlockEntity) blockEntity;
                for (int i = 0; i < crystalEntity.getItemHandler().getSlots(); i++) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(),
                            crystalEntity.getItemHandler().getStackInSlot(i));
                }
            }

            // Remove barrier blocks
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
        super.onRemove(state, level, pos, newState, isMoving);
    }
}