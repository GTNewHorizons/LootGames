package ru.timeconqueror.lootgames.registry.internal;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import ru.timeconqueror.lootgames.api.packet.GamePacketRegistry;
import ru.timeconqueror.lootgames.api.task.TaskCreateExplosion;
import ru.timeconqueror.lootgames.api.task.TaskRegistry;
import ru.timeconqueror.timecore.api.registry.Initable;
import ru.timeconqueror.timecore.api.registry.TimeAutoRegistrable;

@TimeAutoRegistrable
public class LGMiscRegistry implements Initable {
    @Override
    public void onInit(FMLCommonSetupEvent fmlCommonSetupEvent) {
        TaskRegistry.registerTaskFactory(TaskCreateExplosion.class, TaskCreateExplosion::new);

        GamePacketRegistry.GamePacketManager manager = GamePacketRegistry.getManager();

        int id = -1;
//        manager.registerPacket(++id, SPMSacketChangeStage.class);
//        manager.registerPacket(++id, SPMSFieldChanged.class);
//        manager.registerPacket(++id, SPMSGenBoard.class);
//        manager.registerPacket(++id, SPMSResetFlags.class);
//        manager.registerPacket(++id, SPMSSpawnLevelBeatParticles.class);
    }
}