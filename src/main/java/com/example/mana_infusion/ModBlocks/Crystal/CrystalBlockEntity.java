package com.example.mana_infusion.ModBlocks.Crystal;

import com.example.mana_infusion.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class CrystalBlockEntity extends BlockEntity implements MenuProvider {

    private int pulseTimer = 0;
    private final int PULSE_INTERVAL = 100;

    private boolean obtained = false;
    private String owner = "none";

    public boolean isObtained() { return obtained; }
    public void setObtained(boolean obtained) { this.obtained = obtained; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    private final ItemStackHandler itemHandler = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            super.onContentsChanged(slot);
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public CrystalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRYSTAL_BLOCK_ENTITY.get(), pos, state);
    }

//    public static void tick(Level level, BlockPos pos, BlockState state, CrystalBlockEntity blockEntity) {
//        if (level.isClientSide) {
//            blockEntity.clientTick();
//        }
//    }
//
//    private void clientTick() {
//        pulseTimer++;
//
//        if (pulseTimer >= PULSE_INTERVAL) {
//            pulseTimer = 0;
//
//            // Create automatic pulse
//            PulseParticleManager.PulseConfig config = new PulseParticleManager.PulseConfig()
//                    .maxRadius(6.0f)
//                    .duration(70)
//                    .particle(ParticleTypes.PORTAL)
//                    .particleCount(28);
//
//            PulseParticleManager.startPulse(level, worldPosition, config);
//        }
//    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Crystal Block");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CrystalBlockMenu(containerId, inventory, this, new SimpleContainerData(0));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putBoolean("obtained", obtained);
        tag.putString("owner", owner);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        obtained = tag.getBoolean("obtained");
        owner = tag.getString("owner");
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }
}
