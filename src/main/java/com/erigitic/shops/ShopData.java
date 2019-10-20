package com.erigitic.shops;

import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.value.mutable.Value;

public interface ShopData extends DataManipulator<ShopData, ImmutableShopData> {

    Value<Shop> shop();
}
