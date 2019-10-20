package com.erigitic.shops.data;

import com.erigitic.shops.Shop;
import com.erigitic.util.UserUtils;
import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.UUID;

public class ShopBuilder extends AbstractDataBuilder<Shop> {

    public static final int CONTENT_VERSION = 1;

    public ShopBuilder() {
        super(Shop.class, CONTENT_VERSION);
    }

    @Override
    protected Optional<Shop> buildContent(DataView content) throws InvalidDataException {
        if (!content.contains(Shop.WORLD_QUERY, Shop.POSITION_QUERY, Shop.OWNER_QUERY)) {
            return Optional.empty();
        }

        World world = Sponge.getServer().getWorld(content.getObject(Shop.WORLD_QUERY, UUID.class).get()).orElseThrow(InvalidDataException::new);
        Vector3d position = content.getObject(Shop.POSITION_QUERY, Vector3d.class).get();
        UUID owner = content.getObject(Shop.OWNER_QUERY, UUID.class).get();

        Transform<World> transform = new Transform<>(world, position);

        // NOTE: This may cause issues if it returns null.
        return Optional.of(new Shop(transform, UserUtils.getUser(owner).get()));
    }
}

