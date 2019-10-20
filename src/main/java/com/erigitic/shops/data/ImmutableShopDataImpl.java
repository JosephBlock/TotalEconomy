package com.erigitic.shops.data;

import com.erigitic.shops.ImmutableShopData;
import com.erigitic.shops.Shop;
import com.erigitic.shops.ShopData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;

import static com.google.common.base.Preconditions.checkNotNull;

public class ImmutableShopDataImpl extends AbstractImmutableData<ImmutableShopData, ShopData> implements ImmutableShopData {

    private final Shop shop;

    private final ImmutableValue<Shop> shopValue;

    public ImmutableShopDataImpl(Shop shop) {
        this.shop = checkNotNull(shop);

        this.shopValue = Sponge.getRegistry().getValueFactory()
                .createValue(ShopKeys.SHOP, shop)
                .asImmutable();
    }

    public ImmutableShopDataImpl() {
        this(null);
    }

    @Override
    public ImmutableValue<Shop> shop() {
        return this.shopValue;
    }

    private Shop getShop() {
        return this.shop;
    }

    @Override
    protected void registerGetters() {
        registerKeyValue(ShopKeys.SHOP, this::shop);

        registerFieldGetter(ShopKeys.SHOP, this::getShop);
    }

    @Override
    public int getContentVersion() {
        return ShopDataBuilder.CONTENT_VERSION;
    }

    @Override
    public ShopDataImpl asMutable() {
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
