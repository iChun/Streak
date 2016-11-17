package me.ichun.mods.streak.common;

import me.ichun.mods.ichunutil.common.core.Logger;
import me.ichun.mods.ichunutil.common.core.config.ConfigHandler;
import me.ichun.mods.ichunutil.common.core.util.ResourceHelper;
import me.ichun.mods.ichunutil.common.iChunUtil;
import me.ichun.mods.ichunutil.common.module.update.UpdateChecker;
import me.ichun.mods.streak.common.core.Config;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import me.ichun.mods.streak.common.core.EventHandlerClient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Mod(modid = Streak.MOD_ID, name = Streak.MOD_NAME,
        version = Streak.VERSION,
        guiFactory = "me.ichun.mods.ichunutil.common.core.config.GenericModGuiFactory",
        dependencies = "required-after:ichunutil@[" + iChunUtil.VERSION_MAJOR +".0.0,)",
        clientSideOnly = true,
        acceptableRemoteVersions = "*",
        acceptedMinecraftVersions = "[1.9.4,1.10.2]"
)
public class Streak
{
    public static final String VERSION = iChunUtil.VERSION_MAJOR + ".0.0";
    public static final String MOD_NAME = "Streak";
    public static final String MOD_ID = "streak";

    @Instance(MOD_ID)
    public static Streak instance;

    public static final Logger LOGGER = Logger.createLogger(MOD_NAME);

    public static Config config;

    public static EventHandlerClient eventHandlerClient;

    public static HashMap<String, Integer> flavourNames = new HashMap<>();
    public static HashMap<Integer, BufferedImage> flavours = new HashMap<>();
    public static HashMap<BufferedImage, Integer> flavourImageId = new HashMap<>();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        config = (Config)ConfigHandler.registerConfig(new Config(event.getSuggestedConfigurationFile()));

        UpdateChecker.registerMod(new UpdateChecker.ModVersionInfo(MOD_NAME, iChunUtil.VERSION_OF_MC, VERSION, false));
    }

    @SideOnly(Side.CLIENT)
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        eventHandlerClient = new EventHandlerClient();
        MinecraftForge.EVENT_BUS.register(eventHandlerClient);

        File streakDir = new File(ResourceHelper.getModsFolder(), "/Streak Flavours");
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
                    Streak.LOGGER.info("Extracted " + Integer.toString(extractCount) + (extractCount == 1 ? " flavour" : " flavours" + " from mod zip."));
                }
            }
            catch(NullPointerException | IOException e)
            {
                Streak.LOGGER.warn("Failed to extract streak flavours from mod jar");
                e.printStackTrace();
            }
        }

        for(File file : streakDir.listFiles())
        {
            if(file.getName().endsWith(".png"))
            {
                try
                {
                    BufferedImage img = ImageIO.read(file);
                    if(img != null)
                    {
                        flavourNames.put(file.getName().substring(0, file.getName().length() - 4).toLowerCase(), flavours.size());
                        flavours.put(flavours.size(), img);
                        flavourImageId.put(img, -1);
                    }
                }
                catch(IOException e)
                {
                }
            }
        }
    }
}
