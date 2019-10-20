package com.erigitic.shops;

import com.erigitic.main.TotalEconomy;
import com.erigitic.shops.data.ShopDataImpl;
import com.erigitic.shops.data.ShopKeys;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class ShopManager {

    public static void open(Player player) {
        Inventory inventory = Inventory.builder()
                .of(InventoryArchetypes.DOUBLE_CHEST)
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(TextColors.RED, "Shops")))
                .withCarrier(player)
                .build(TotalEconomy.getTotalEconomy());
        player.openInventory(inventory);
    }

    @Listener
    public void onBlockPlaceDebug(ChangeBlockEvent.Place event, @Root Player player) {
        ItemStackSnapshot item = event.getCause().getContext().get(EventContextKeys.USED_ITEM).get();

        // TODO: If item has ShopKeys.SHOP, then run code

        ShopData shopData = new ShopDataImpl();
        shopData.set(ShopKeys.SHOP, item.get(ShopKeys.SHOP).get());

        Optional<TileEntity> tileEntityOpt = event.getTransactions().get(0).getFinal().getLocation().get().getTileEntity();
        if (tileEntityOpt.isPresent()) {
            TileEntity tileEntity = tileEntityOpt.get();
            tileEntity.offer(shopData);

            TotalEconomy.getTotalEconomy().getLogger().info("" + event.getTransactions().get(0).getFinal().getLocation().get().getTileEntity().get().get(ShopKeys.SHOP).isPresent());
        }
    }

    @Listener
    public void onBlockBreakDebug(InteractBlockEvent.Primary event) {
        TotalEconomy.getTotalEconomy().getLogger().info("" + event.getTargetBlock().getLocation().get().getTileEntity().get().get(ShopKeys.SHOP).isPresent());
    }

//    inventory.builder().listener(AffectSlotEvent.class, event -> {
//        //Code for when the event fires on the build inventory the builder provides
//        //This gets around needing to check the event for if its your inventory
//    }
}
