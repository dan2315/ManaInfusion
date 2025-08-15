package com.example.mana_infusion.ModBlocks.Crystal;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public class CrystalBlock extends BaseEntityBlock {

    public static final EnumProperty<CrystalModelState> MODEL_STATE = EnumProperty.create("model_state", CrystalModelState.class);

    public CrystalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(MODEL_STATE, CrystalModelState.MODEL1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MODEL_STATE);
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
            if (level.getBlockEntity(pos) instanceof CrystalBlockEntity blockEntity) {
                if (blockEntity.isObtained()) {
                    player.sendSystemMessage(Component.literal("Crystal's owner: " + blockEntity.getOwner()));
                    BlockEntity entity = level.getBlockEntity(pos);
                    if (entity instanceof CrystalBlockEntity) {
                        NetworkHooks.openScreen(((ServerPlayer) player), (CrystalBlockEntity) entity, pos);
                    } else {
                        throw new IllegalStateException("Container provider is missing!");
                    }
                } else {
                    CrystalModelState currentState = state.getValue(MODEL_STATE);
                    CrystalModelState newState = currentState == CrystalModelState.MODEL1 ? CrystalModelState.MODEL2 : CrystalModelState.MODEL1;

                    level.setBlock(pos, state.setValue(MODEL_STATE, newState), 3);
                    blockEntity.setObtained(true);
                    blockEntity.setOwner(player.getName().getString());
                }
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

            if (blockEntity instanceof CrystalBlockEntity crystalBlockEntity) {
                for (int i = 0; i < crystalBlockEntity.getItemHandler().getSlots(); i++) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(),
                            crystalBlockEntity.getItemHandler().getStackInSlot(i));
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