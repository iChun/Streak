package streak.common;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ichun.common.core.updateChecker.ModVersionChecker;
import ichun.common.core.updateChecker.ModVersionInfo;
import ichun.common.core.util.ResourceHelper;
import ichun.common.core.config.Config;
import ichun.common.core.config.ConfigHandler;
import ichun.common.core.config.IConfigUser;
import ichun.common.iChunUtil;
import net.minecraftforge.common.config.Property;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import streak.common.core.TickHandlerClient;
import streak.common.entity.EntityStreak;
import streak.common.render.RenderStreak;

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
        dependencies = "required-after:iChunUtil@[" + iChunUtil.versionMC +".0.0,)"
            )
public class Streak 
	implements IConfigUser
{

    public static final String version = iChunUtil.versionMC + ".0.0";

	@Instance("Streak")
	public static Streak instance;
	
	private static final Logger logger = LogManager.getLogger("Streak");
	
	public static Config config;

	public static TickHandlerClient tickHandlerClient;
	
	public static boolean hasMorphMod;
	
	public static HashMap<String, Integer> flavourNames = new HashMap<String, Integer>();
	public static HashMap<Integer, BufferedImage> flavours = new HashMap<Integer, BufferedImage>();
	public static HashMap<BufferedImage, Integer> flavourImageId = new HashMap<BufferedImage, Integer>();
	
	@Override
	public boolean onConfigChange(Config cfg, Property prop) { return true; }
	
	@EventHandler
	public void preLoad(FMLPreInitializationEvent event)
	{
		if(FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			console("You're loading Streak on a server! This is a client-only mod!", true);
			return;
		}

		config = ConfigHandler.createConfig(event.getSuggestedConfigurationFile(), "streak", "Streak", logger, instance);
        config.setCurrentCategory("basics", "streak.config.cat.basics.name", "streak.config.cat.basics.comment");
		config.createIntProperty("streakTime", "streak.config.prop.streakTime.name", "streak.config.prop.streakTime.comment", true, false, 100, 5, Integer.MAX_VALUE);
		config.createIntProperty("playersFollowYourFavouriteFlavour", "streak.config.prop.playersFollowYourFavouriteFlavour.name", "streak.config.prop.playersFollowYourFavouriteFlavour.comment", true, false, 0, 0, 1);
		config.createIntProperty("sprintTrail", "streak.config.prop.sprintTrail.name", "streak.config.prop.sprintTrail.comment", true, false, 1, 0, 1);
		config.createStringProperty("favouriteFlavour", "streak.config.prop.favouriteFlavour.name", "streak.config.prop.favouriteFlavour.comment", true, false, "");

        ModVersionChecker.register_iChunMod(new ModVersionInfo("Streak", iChunUtil.versionOfMC, version, false));

        //TODO check out this: http://www.youtube.com/watch?v=F-S0AB49HbA Its gradients are smooth.
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
		
		hasMorphMod = Loader.isModLoaded("Morph");
	}
	
    public static void console(String s, boolean warning)
    {
    	StringBuilder sb = new StringBuilder();
    	logger.log(warning ? Level.WARN : Level.INFO, sb.append("[").append(version).append("] ").append(s).toString());
    }
}
