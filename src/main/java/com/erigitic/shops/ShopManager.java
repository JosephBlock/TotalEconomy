package com.erigitic.shops;

import com.erigitic.shops.data.ShopDataImpl;
import com.erigitic.shops.data.ShopKeys;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.Chest;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.inventory.*;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Optional;

public class ShopManager {

    @Listener
    public void onBlockPlaceDebug(ChangeBlockEvent.Place event, @First Player player) {
        ItemStackSnapshot usedItem = event.getCause().getContext().get(EventContextKeys.USED_ITEM).get();
        Optional<Shop> usedItemShopData = usedItem.get(ShopKeys.SHOP);

        if (!usedItemShopData.isPresent()) {
            return;
        }

        ShopData shopData = new ShopDataImpl();
        shopData.set(ShopKeys.SHOP, usedItemShopData.get());

        Optional<TileEntity> tileEntityOpt = event.getTransactions().get(0).getFinal().getLocation().get().getTileEntity();
        if (tileEntityOpt.isPresent()) {
            Chest tileEntity = (Chest) tileEntityOpt.get();
            tileEntity.offer(shopData);
        }
    }

    // Cancel every click event within a shop inventory
    @Listener
    public void onShopClickDebug(ClickInventoryEvent event, @First Player player) {
//        TODO: If owner, don't cancel event but remove pricing and amount data if item removed from shop
//        TODO: If owner, only allow items that have been priced and amount has been set to be put into shop. Otherwise, cancel.

        String title = event.getTargetInventory().getName().get();
        Text titleText = TextSerializers.LEGACY_FORMATTING_CODE.deserialize(title);

        boolean isChest = (event.getTargetInventory().getArchetype() == InventoryArchetypes.CHEST || event.getTargetInventory().getArchetype() == InventoryArchetypes.DOUBLE_CHEST);
        if (isChest && titleText.getColor().equals(TextColors.GOLD) && title.contains("Shop")) {
            if (event.getContext().get(EventContextKeys.OWNER).get().getName() != player.getName()) {
                event.setCancelled(true);
            }
        }
    }
}
