package com.erigitic.shops;

import com.erigitic.shops.data.ShopBuilder;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.world.World;

public class Shop implements DataSerializable {

    public static final DataQuery WORLD_QUERY = DataQuery.of("WorldUUID");
    public static final DataQuery POSITION_QUERY = DataQuery.of("Position");
    public static final DataQuery OWNER_QUERY = DataQuery.of("OwnerUUID");

    private Transform<World> transform;
    private User user;

    public Shop(Transform<World> transform, User user) {
        this.transform = transform;
        this.user = user;
    }

    @Override
    public int getContentVersion() {
        return ShopBuilder.CONTENT_VERSION;
    }

    public Transform<World> getTransform() {
        return transform;
    }

    public String getName() {
        return user.getName() + "'s Shop";
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew()
                .set(WORLD_QUERY, this.transform.getExtent().getUniqueId())
                .set(POSITION_QUERY, this.transform.getPosition())
                .set(OWNER_QUERY, this.user.getUniqueId())
                .set(Queries.CONTENT_VERSION, ShopBuilder.CONTENT_VERSION);
    }
}
