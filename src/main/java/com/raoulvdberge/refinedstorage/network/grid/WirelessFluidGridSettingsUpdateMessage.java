package com.raoulvdberge.refinedstorage.network.grid;

import com.raoulvdberge.refinedstorage.api.network.grid.IGrid;
import com.raoulvdberge.refinedstorage.apiimpl.network.node.GridNetworkNode;
import com.raoulvdberge.refinedstorage.container.GridContainer;
import com.raoulvdberge.refinedstorage.tile.grid.WirelessFluidGrid;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class WirelessFluidGridSettingsUpdateMessage {
    private int sortingDirection;
    private int sortingType;
    private int searchBoxMode;
    private int size;
    private int tabSelected;
    private int tabPage;

    public WirelessFluidGridSettingsUpdateMessage(int sortingDirection, int sortingType, int searchBoxMode, int size, int tabSelected, int tabPage) {
        this.sortingDirection = sortingDirection;
        this.sortingType = sortingType;
        this.searchBoxMode = searchBoxMode;
        this.size = size;
        this.tabSelected = tabSelected;
        this.tabPage = tabPage;
    }

    public static WirelessFluidGridSettingsUpdateMessage decode(PacketBuffer buf) {
        return new WirelessFluidGridSettingsUpdateMessage(
            buf.readInt(),
            buf.readInt(),
            buf.readInt(),
            buf.readInt(),
            buf.readInt(),
            buf.readInt()
        );
    }

    public static void encode(WirelessFluidGridSettingsUpdateMessage message, PacketBuffer buf) {
        buf.writeInt(message.sortingDirection);
        buf.writeInt(message.sortingType);
        buf.writeInt(message.searchBoxMode);
        buf.writeInt(message.size);
        buf.writeInt(message.tabSelected);
        buf.writeInt(message.tabPage);
    }

    public static void handle(WirelessFluidGridSettingsUpdateMessage message, Supplier<NetworkEvent.Context> ctx) {
        PlayerEntity player = ctx.get().getSender();

        if (player != null) {
            ctx.get().enqueueWork(() -> {
                if (player.openContainer instanceof GridContainer) {
                    IGrid grid = ((GridContainer) player.openContainer).getGrid();

                    if (grid instanceof WirelessFluidGrid) {
                        ItemStack stack = ((WirelessFluidGrid) grid).getStack();

                        if (IGrid.isValidSortingDirection(message.sortingDirection)) {
                            stack.getTag().putInt(GridNetworkNode.NBT_SORTING_DIRECTION, message.sortingDirection);
                        }

                        if (IGrid.isValidSortingType(message.sortingType)) {
                            stack.getTag().putInt(GridNetworkNode.NBT_SORTING_TYPE, message.sortingType);
                        }

                        if (IGrid.isValidSearchBoxMode(message.searchBoxMode)) {
                            stack.getTag().putInt(GridNetworkNode.NBT_SEARCH_BOX_MODE, message.searchBoxMode);
                        }

                        if (IGrid.isValidSize(message.size)) {
                            stack.getTag().putInt(GridNetworkNode.NBT_SIZE, message.size);
                        }

                        stack.getTag().putInt(GridNetworkNode.NBT_TAB_SELECTED, message.tabSelected);
                        stack.getTag().putInt(GridNetworkNode.NBT_TAB_PAGE, message.tabPage);
                    }
                }
            });
        }

        ctx.get().setPacketHandled(true);
    }
}
