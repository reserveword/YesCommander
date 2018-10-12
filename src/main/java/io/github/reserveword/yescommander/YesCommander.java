package io.github.reserveword.yescommander;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = YesCommander.MOD_ID,
        name = YesCommander.MOD_NAME,
        version = YesCommander.VERSION
)
public class YesCommander {

    public static final String MOD_ID = "yes-commander";
    public static final String MOD_NAME = "Yes Commander";
    public static final String VERSION = "1.0-SNAPSHOT";

    public static Logger LOGGER;
    /**
     * This is the instance of your mod as created by Forge. It will never be null.
     */
    @Mod.Instance(MOD_ID)
    public static YesCommander INSTANCE;

    /**
     * This is the first initialization event. Register tile entities here.
     * The registry events below will have fired prior to entry to this method.
     */
    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();
        ClientCommandHandler.instance.registerCommand(new Commands.WindowClick());
        ClientCommandHandler.instance.registerCommand(new Commands.WorldClick());
    }

    /**
     * This is the second initialization event. Register custom recipes
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {

    }

    /**
     * This is the final initialization event. Register actions from other mods here
     */
    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent event) {

    }

    /**
     * This is a special class that listens to registry events, to allow creation of mod blocks and items at the proper time.
     */
    @Mod.EventBusSubscriber
    public static class ObjectRegistryHandler {
        @SubscribeEvent
        public static void listen(PlayerEvent event) {
            Minecraft mc = FMLClientHandler.instance().getClient();
            if (mc.player != null && mc.player.openContainer != null){

            }
        }
    }
}
