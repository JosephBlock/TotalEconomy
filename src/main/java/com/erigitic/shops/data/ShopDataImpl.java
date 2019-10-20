package com.erigitic.shops.data;

import com.erigitic.shops.ImmutableShopData;
import com.erigitic.shops.Shop;
import com.erigitic.shops.ShopData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.Value;

import javax.annotation.Nullable;
import java.util.Optional;

public class ShopDataImpl extends AbstractData<ShopData, ImmutableShopData> implements ShopData {

    private Shop shop;

    public ShopDataImpl(Shop shop) {
        this.shop = shop;
        registerGettersAndSetters();
    }

    public ShopDataImpl() {
        this(null);
        registerGettersAndSetters();
    }

    @Override
    public Value<Shop> shop() {
        return Sponge.getRegistry().getValueFactory().createValue(ShopKeys.SHOP, this.shop);
    }

    private Shop getShop() {
        return this.shop;
    }

    private void setShop(@Nullable Shop shop) {
        this.shop = shop;
    }

    @Override
    protected void registerGettersAndSetters() {
        registerKeyValue(ShopKeys.SHOP, this::shop);

        registerFieldGetter(ShopKeys.SHOP, this::getShop);

        registerFieldSetter(ShopKeys.SHOP, this::setShop);
    }

    @Override
    public int getContentVersion() {
        return ShopDataBuilder.CONTENT_VERSION;
    }

    @Override
    public ImmutableShopData asImmutable() {
        return new ImmutableShopDataImpl(this.shop);
    }

    @Override
    public Optional<ShopData> fill(DataHolder dataHolder, MergeFunction overlap) {
        ShopData merged = overlap.merge(this, dataHolder.get(ShopData.class).orElse(null));
        this.shop = merged.shop().get();

        return Optional.of(this);
    }

    @Override
    public Optional<ShopData> from(DataContainer container) {
        if (!container.contains(ShopKeys.SHOP)) {
            return Optional.empty();
        }

        this.shop = container.getSerializable(ShopKeys.SHOP.getQuery(), Shop.class).get();

        return Optional.of(this);
    }

    @Override
    public ShopData copy() {
        return new ShopDataImpl(this.shop);
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = super.toContainer();

        if (this.shop != null) {
            container.set(ShopKeys.SHOP, this.shop);
        }

        return container;
    }
}
