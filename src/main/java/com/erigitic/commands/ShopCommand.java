package com.erigitic.commands;

import com.erigitic.main.TotalEconomy;
import com.erigitic.shops.Shop;
import com.erigitic.shops.ShopData;
import com.erigitic.shops.data.ShopDataImpl;
import com.erigitic.shops.data.ShopKeys;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class ShopCommand implements CommandExecutor  {

    public CommandSpec commandSpec() {
        BuyShop buyShopCommand = new BuyShop();

        // TODO: If error, send chat message
        // TODO: If /shop is run, explain command

        return CommandSpec.builder()
                .child(buyShopCommand.getCommandSpec(), "buy", "b")
                .permission("totaleconomy.command.shop")
                .executor(this)
                .build();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        return CommandResult.success();
    }

    private class BuyShop implements CommandExecutor {

        public CommandSpec getCommandSpec() {
            return CommandSpec.builder()
                    .description(Text.of("Buy a shop"))
                    .permission("totaleconomy.command.shop.buy")
                    .executor(this)
                    .build();
        }

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            Player player = ((Player) src).getPlayer().get();

            ItemStack shopItem = ItemStack.builder()
                    .itemType(ItemTypes.CHEST)
                    .build();
            shopItem.offer(Keys.DISPLAY_NAME, Text.builder("Shop [" + player.getName() + "]").color(TextColors.GOLD).build().toText());

            ShopData shopData = new ShopDataImpl();
            shopData.set(ShopKeys.SHOP, new Shop(player.getTransform(), player));
            shopItem.offer(shopData);

            player.getInventory().offer(shopItem);

            return CommandResult.success();
        }
    }
}
