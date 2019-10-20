package com.erigitic.shops.data;

import com.erigitic.shops.ImmutableShopData;
import com.erigitic.shops.Shop;
import com.erigitic.shops.ShopData;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.Optional;

public class ShopDataBuilder extends AbstractDataBuilder<ShopData> implements DataManipulatorBuilder<ShopData, ImmutableShopData> {

    public static final int CONTENT_VERSION = 1;

    public ShopDataBuilder() {
        super(ShopData.class, CONTENT_VERSION);
    }

    @Override
    public ShopData create() {
        return new ShopDataImpl();
    }

    @Override
    public Optional<ShopData> createFrom(DataHolder dataHolder) {
        return create().fill(dataHolder);
    }

    @Override
    protected Optional<ShopData> buildContent(DataView container) throws InvalidDataException {
        if (!container.contains(ShopKeys.SHOP)) {
            return Optional.empty();
        }

        ShopData data = new ShopDataImpl();

        container.getSerializable(ShopKeys.SHOP.getQuery(), Shop.class).ifPresent(shop -> {
            data.set(ShopKeys.SHOP, shop);
        });

        return Optional.of(data);
    }
}
