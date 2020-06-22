package me.ichun.mods.streak.common;

import me.ichun.mods.ichunutil.client.tracker.ClientEntityTracker;
import me.ichun.mods.streak.common.core.Config;
import me.ichun.mods.streak.common.core.EventHandler;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Mod(Streak.MOD_ID)
public class Streak
{
    public static final String MOD_ID = "streak";
    public static final String MOD_NAME = "Streak";

    public static final Logger LOGGER = LogManager.getLogger();

    public static Config config;

    public static EventHandler eventHandler;

    public Streak() //TODO add iChunUtil dep. Version checker???
    {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            config = new Config().init();
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
            ClientEntityTracker.init(FMLJavaModLoadingContext.get().getModEventBus());
            MinecraftForge.EVENT_BUS.register(eventHandler = new EventHandler());
            ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> me.ichun.mods.ichunutil.client.core.EventHandlerClient::getConfigGui);
        });
        DistExecutor.runWhenOn(Dist.DEDICATED_SERVER, () -> () -> LOGGER.log(Level.ERROR, "You are loading " + MOD_NAME + " on a server. " + MOD_NAME + " is a client only mod!"));

        //Make sure the mod being absent on the other network side does not cause the client to display the server as incompatible
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
    }

    private void onClientSetup(FMLClientSetupEvent event)
    {
        unpackFlavours();
    }

    private void unpackFlavours()
    {
        File streakDir = new File(FMLPaths.MODSDIR.get().toFile(), "/Streak Flavours");
        if(!streakDir.exists() && streakDir.mkdirs())
        {
            try(InputStream in = Streak.class.getResourceAsStream("/flavours.zip"); ZipInputStream zipStream = new ZipInputStream(in))
            {
                ZipEntry entry = null;

                int extractCount = 0;

                while((entry = zipStream.getNextEntry()) != null)
                {
                    File file = new File(streakDir, entry.getName());
                    if(file.exists() && file.length() > 3L)
                    {
                        continue;
                    }
                    FileOutputStream out = new FileOutputStream(file);

                    byte[] buffer = new byte[8192];
                    int len;
                    while((len = zipStream.read(buffer)) != -1)
                    {
                        out.write(buffer, 0, len);
                    }
                    out.close();

                    extractCount++;
                }

                if(extractCount > 0)
                {
                    Streak.LOGGER.info("Extracted flavours from mod zip: {}", extractCount);
                }
            }
            catch(NullPointerException | IOException e)
            {
                Streak.LOGGER.warn("Failed to extract streak flavours from mod jar");
                e.printStackTrace();
            }
        }

        if(streakDir.exists())
        {
            for(File file : streakDir.listFiles())
            {
                if(file.getName().endsWith(".png"))
                {
                    try
                    {
                        NativeImage img = NativeImage.read(new FileInputStream(file));
                        eventHandler.flavours.add(new EventHandler.FlavourInfo(file.getName().substring(0, file.getName().length() - 4).toLowerCase(), img));
                    }
                    catch(IOException e)
                    {
                        Streak.LOGGER.error("Error reading file: " + file.getName());
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
