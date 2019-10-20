package com.erigitic.shops;

import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.immutable.ImmutableValue;

public interface ImmutableShopData extends ImmutableDataManipulator<ImmutableShopData, ShopData> {

    ImmutableValue<Shop> shop();
}
