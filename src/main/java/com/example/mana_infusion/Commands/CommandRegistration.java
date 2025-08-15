package com.example.mana_infusion.Commands;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "mana_infusion", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommandRegistration {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        OreVeinCommand.register(event.getDispatcher());
    }
}
