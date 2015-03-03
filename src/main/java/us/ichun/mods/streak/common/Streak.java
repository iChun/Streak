package us.ichun.mods.streak.common;

import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import us.ichun.mods.ichunutil.common.core.config.ConfigHandler;
import us.ichun.mods.ichunutil.common.core.updateChecker.ModVersionChecker;
import us.ichun.mods.ichunutil.common.core.updateChecker.ModVersionInfo;
import us.ichun.mods.ichunutil.common.core.util.ResourceHelper;
import us.ichun.mods.ichunutil.common.iChunUtil;
import us.ichun.mods.streak.common.core.Config;
import us.ichun.mods.streak.common.core.TickHandlerClient;
import us.ichun.mods.streak.common.entity.EntityStreak;
import us.ichun.mods.streak.common.render.RenderStreak;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Mod(modid = "Streak", name = "Streak",
        version = Streak.version,
        dependencies = "required-after:iChunUtil@[" + iChunUtil.versionMC +".0.0,)",
        clientSideOnly = true
)
public class Streak
{

    public static final String version = iChunUtil.versionMC + ".0.1";

    @Instance("Streak")
    public static Streak instance;

    private static final Logger logger = LogManager.getLogger("Streak");

    public static Config config;

    public static TickHandlerClient tickHandlerClient;

    public static HashMap<String, Integer> flavourNames = new HashMap<String, Integer>();
    public static HashMap<Integer, BufferedImage> flavours = new HashMap<Integer, BufferedImage>();
    public static HashMap<BufferedImage, Integer> flavourImageId = new HashMap<BufferedImage, Integer>();

    @EventHandler
    public void preLoad(FMLPreInitializationEvent event)
    {
        if(FMLCommonHandler.instance().getEffectiveSide().isServer())
        {
            console("You're loading Streak on a server! This is a client-only mod!", true);
            return;
        }

        config = (Config)ConfigHandler.registerConfig(new Config(event.getSuggestedConfigurationFile()));

        ModVersionChecker.register_iChunMod(new ModVersionInfo("Streak", iChunUtil.versionOfMC, version, false));
    }

    @SideOnly(Side.CLIENT)
    @EventHandler
    public void load(FMLInitializationEvent event)
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityStreak.class, new RenderStreak());

        tickHandlerClient = new TickHandlerClient();
        FMLCommonHandler.instance().bus().register(tickHandlerClient);

        File streakDir = new File(ResourceHelper.getModsFolder(), "/Streak Flavours");
        if(!streakDir.exists() && streakDir.mkdirs())
        {
            try
            {
                InputStream in = Streak.class.getResourceAsStream("/flavours.zip");
                if(in != null)
                {
                    ZipInputStream zipStream = new ZipInputStream(in);
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
                    zipStream.close();

                    if(extractCount > 0)
                    {
                        Streak.console("Extracted " + Integer.toString(extractCount) + (extractCount == 1 ? " flavour" : " flavours" + " from mod zip."), false);
                    }
                }
            }
            catch(IOException e)
            {
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

    public static void console(String s, boolean warning)
    {
        StringBuilder sb = new StringBuilder();
        logger.log(warning ? Level.WARN : Level.INFO, sb.append("[").append(version).append("] ").append(s).toString());
    }
}
