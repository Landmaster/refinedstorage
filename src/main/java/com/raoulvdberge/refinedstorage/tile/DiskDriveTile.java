package com.raoulvdberge.refinedstorage.tile;

import com.raoulvdberge.refinedstorage.RSTiles;
import com.raoulvdberge.refinedstorage.api.storage.AccessType;
import com.raoulvdberge.refinedstorage.api.storage.disk.IStorageDisk;
import com.raoulvdberge.refinedstorage.apiimpl.network.node.diskdrive.DiskDriveNetworkNode;
import com.raoulvdberge.refinedstorage.tile.config.*;
import com.raoulvdberge.refinedstorage.tile.data.RSSerializers;
import com.raoulvdberge.refinedstorage.tile.data.TileDataParameter;
import com.raoulvdberge.refinedstorage.util.WorldUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class DiskDriveTile extends NetworkNodeTile<DiskDriveNetworkNode> {
    public static final TileDataParameter<Integer, DiskDriveTile> PRIORITY = IPrioritizable.createParameter();
    public static final TileDataParameter<Integer, DiskDriveTile> COMPARE = IComparable.createParameter();
    public static final TileDataParameter<Integer, DiskDriveTile> WHITELIST_BLACKLIST = IWhitelistBlacklist.createParameter();
    public static final TileDataParameter<Integer, DiskDriveTile> TYPE = IType.createParameter();
    public static final TileDataParameter<AccessType, DiskDriveTile> ACCESS_TYPE = IAccessType.createParameter();
    public static final TileDataParameter<Long, DiskDriveTile> STORED = new TileDataParameter<>(RSSerializers.LONG_SERIALIZER, 0L, t -> {
        long stored = 0;

        for (IStorageDisk storage : t.getNode().getItemDisks()) {
            if (storage != null) {
                stored += storage.getStored();
            }
        }

        for (IStorageDisk storage : t.getNode().getFluidDisks()) {
            if (storage != null) {
                stored += storage.getStored();
            }
        }

        return stored;
    });
    public static final TileDataParameter<Long, DiskDriveTile> CAPACITY = new TileDataParameter<>(RSSerializers.LONG_SERIALIZER, 0L, t -> {
        long capacity = 0;

        for (IStorageDisk storage : t.getNode().getItemDisks()) {
            if (storage != null) {
                if (storage.getCapacity() == -1) {
                    return -1L;
                }

                capacity += storage.getCapacity();
            }
        }

        for (IStorageDisk storage : t.getNode().getFluidDisks()) {
            if (storage != null) {
                if (storage.getCapacity() == -1) {
                    return -1L;
                }

                capacity += storage.getCapacity();
            }
        }

        return capacity;
    });

    private static final String NBT_DISK_STATE = "DiskStates";
    public static final ModelProperty<DiskDriveNetworkNode.DiskState[]> DISK_STATE_PROPERTY = new ModelProperty<>();

    private DiskDriveNetworkNode.DiskState[] diskState = new DiskDriveNetworkNode.DiskState[8];

    public DiskDriveTile() {
        super(RSTiles.DISK_DRIVE);

        dataManager.addWatchedParameter(PRIORITY);
        dataManager.addWatchedParameter(COMPARE);
        dataManager.addWatchedParameter(WHITELIST_BLACKLIST);
        dataManager.addWatchedParameter(TYPE);
        dataManager.addWatchedParameter(ACCESS_TYPE);
        dataManager.addWatchedParameter(STORED);
        dataManager.addWatchedParameter(CAPACITY);

        Arrays.fill(diskState, DiskDriveNetworkNode.DiskState.NONE);
    }

    @Override
    public CompoundNBT writeUpdate(CompoundNBT tag) {
        super.writeUpdate(tag);

        ListNBT list = new ListNBT();

        for (DiskDriveNetworkNode.DiskState state : getNode().getDiskState()) {
            list.add(new IntNBT(state.ordinal()));
        }

        tag.put(NBT_DISK_STATE, list);

        return tag;
    }

    @Override
    public void readUpdate(CompoundNBT tag) {
        super.readUpdate(tag);

        ListNBT list = tag.getList(NBT_DISK_STATE, Constants.NBT.TAG_INT);

        for (int i = 0; i < list.size(); ++i) {
            diskState[i] = DiskDriveNetworkNode.DiskState.values()[list.getInt(i)];
        }

        requestModelDataUpdate();

        WorldUtils.updateBlock(world, pos);
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        return new ModelDataMap.Builder().withInitial(DISK_STATE_PROPERTY, diskState).build();
    }

    /* TODO
    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable Direction facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(getNode().getDisks());
        }

        return super.getCapability(capability, facing);
    }*/

    @Override
    @Nonnull
    public DiskDriveNetworkNode createNode(World world, BlockPos pos) {
        return new DiskDriveNetworkNode(world, pos);
    }
}
