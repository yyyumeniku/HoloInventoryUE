/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 - 2017 Dries K. Aka Dries007
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.dries007.holoInventory;

import com.google.common.collect.Sets;
import net.dries007.holoInventory.client.ClientEventHandler;
import net.dries007.holoInventory.items.ItemHoloGlasses;
import net.dries007.holoInventory.network.request.EntityRequest;
import net.dries007.holoInventory.network.request.TileRequest;
import net.dries007.holoInventory.network.response.MerchantRecipes;
import net.dries007.holoInventory.network.response.PlainInventory;
import net.dries007.holoInventory.server.ServerEventHandler;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLModDisabledEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import org.apache.logging.log4j.Logger;

import static net.dries007.holoInventory.HoloInventory.*;

@Mod(modid = MODID, name = MODNAME, acceptableRemoteVersions = "*", canBeDeactivated = true, guiFactory = GUI_FACTORY)
public class HoloInventory
{
    public static final String MODID = "holoinventory";
    public static final String MODNAME = "HoloInventory";
    /** @see net.dries007.holoInventory.client.ConfigGuiFactory */
    public static final String GUI_FACTORY = "net.dries007.holoInventory.client.ConfigGuiFactory";

    @Mod.Instance(value = MODID)
    private static HoloInventory instance;

    @Mod.Metadata
    private ModMetadata metadata;

    private SimpleNetworkWrapper snw;
    private Logger logger;
    private Configuration config;
    
    public static ItemHoloGlasses holoGlasses;

    @Mod.EventHandler
    public void disableEvent(FMLModDisabledEvent event)
    {
        logger.info("Mod disabled via Mods list.");
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();

        config = new Configuration(event.getSuggestedConfigurationFile());
        updateConfig();

        // Initialize items
        holoGlasses = new ItemHoloGlasses();

        int id = 0;
        snw = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

        // Request packets (client -> server)
        snw.registerMessage(EntityRequest.Handler.class, EntityRequest.class, id++, Side.SERVER);
        snw.registerMessage(TileRequest.Handler.class, TileRequest.class, id++, Side.SERVER);

        // Response packets (server -> client)
        snw.registerMessage(PlainInventory.Handler.class, PlainInventory.class, id++, Side.CLIENT);
        snw.registerMessage(MerchantRecipes.Handler.class, MerchantRecipes.class, id++, Side.CLIENT);

        if (event.getSide().isClient())
        {
            //noinspection MethodCallSideOnly
            ClientEventHandler.init();
        }

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(ServerEventHandler.I);
    }

    @SubscribeEvent
    public void items(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(holoGlasses);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerModels(ModelRegistryEvent event)
    {
        ModelLoader.setCustomModelResourceLocation(holoGlasses, 0, 
            new ModelResourceLocation(holoGlasses.getRegistryName(), "inventory"));
    }

    @Mod.EventHandler
    public void serverStart(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new HICommand());
    }

    @SubscribeEvent
    public void updateConfig(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if (event.getModID().equals(MODID)) updateConfig();
    }

    public void saveBanned()
    {
        config.get(MODID, "banned", new String[0]).set(Helper.banned.toArray(new String[Helper.banned.size()]));

        if (config.hasChanged()) config.save();
    }

    private void updateConfig()
    {
        logger.info("Update config");

        Helper.showOnSneak = config.get(MODID, "showOnSneak", false, "Show on sneak, bypasses other keyboard settings.").setRequiresWorldRestart(false).setRequiresMcRestart(false).getBoolean();
        Helper.showOnSprint = config.get(MODID, "showOnSprint", false, "Show on sprint, bypasses other keyboard settings.").setRequiresWorldRestart(false).setRequiresMcRestart(false).getBoolean();
        Helper.banned = Sets.newHashSet(config.get(MODID, "banned", new String[0]).setRequiresWorldRestart(false).setRequiresMcRestart(false).getStringList());

        Helper.requireGlasses = config.get(MODID, "requireGlasses", true, "Require HoloGlasses to see holographic inventory displays.").setRequiresWorldRestart(false).setRequiresMcRestart(false).getBoolean();

        Helper.rotationSpeed = config.get(
            MODID,
            "rotationSpeed",
            1.0,
            "Set the rotation speed for rendered items. 1.0 = current speed, 1.5 = 150% of current speed, 0.0 to disable rotation.",
            0.0,
            5.0
        ).setRequiresWorldRestart(false).setRequiresMcRestart(false).getDouble();

        Helper.renderBlockName = config.get(
            MODID,
            "renderBlockName",
            true,
            "Render block name on top of rendered items."
        ).setRequiresWorldRestart(false).setRequiresMcRestart(false).getBoolean();

        Helper.renderMerchantName = config.get(
            MODID,
            "renderMerchantName",
            true,
            "Render merchant name on top of rendered trades."
        ).setRequiresWorldRestart(false).setRequiresMcRestart(false).getBoolean();

        if (config.hasChanged()) config.save();
    }

    public static String getVersion()
    {
        return instance.metadata.version;
    }

    public static HoloInventory getInstance()
    {
        return instance;
    }

    public static SimpleNetworkWrapper getSnw()
    {
        return instance.snw;
    }

    public static Logger getLogger()
    {
        return instance.logger;
    }

    public static Configuration getConfig()
    {
        return instance.config;
    }
}
