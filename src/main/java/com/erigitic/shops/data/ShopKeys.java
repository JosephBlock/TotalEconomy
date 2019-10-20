package com.erigitic.shops.data;

import com.erigitic.shops.Shop;
import com.google.common.reflect.TypeToken;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.mutable.Value;

public class ShopKeys {

    public static Key<Value<Shop>> SHOP = Key.builder()
            .type(new TypeToken<Value<Shop>>() {})
            .id("shop")
            .name("shop")
            .query(DataQuery.of("shop"))
            .build();
}
