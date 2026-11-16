package com.arthurchenevans.arsbotania.registry;

import com.arthurchenevans.arsbotania.ArsBotania;
import com.arthurchenevans.arsbotania.entity.MichaelEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, ArsBotania.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<MichaelEntity>> MICHAEL = ENTITIES.register("michael",
            () -> EntityType.Builder.of(MichaelEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F).fireImmune().clientTrackingRange(10)
                    .build(ArsBotania.prefix("michael").toString()));

    public static final DeferredHolder<Item, DeferredSpawnEggItem> MICHAEL_SPAWN_EGG = DivineItems.ITEMS.register("michael_spawn_egg",
            () -> new DeferredSpawnEggItem(MICHAEL, 0xF5F0E6, 0xE8B23A, new Item.Properties()));

    private ModEntities() {
    }

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
        modBus.addListener((EntityAttributeCreationEvent e) -> e.put(MICHAEL.get(), MichaelEntity.createAttributes().build()));
    }
}
