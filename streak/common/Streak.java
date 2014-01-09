package streak.common;

import ichun.core.LoggerHelper;
import ichun.core.ResourceHelper;
import ichun.core.config.Config;
import ichun.core.config.ConfigHandler;
import ichun.core.config.IConfigUser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;

import net.minecraftforge.common.Property;
import streak.common.core.TickHandlerClient;
import streak.common.entity.EntityStreak;
import streak.common.render.RenderStreak;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = "Streak", name = "Streak", version = Streak.version, dependencies = "required-after:iChunUtil@[2.4.0,)")
public class Streak 
	implements IConfigUser
{

	public static final String version = "2.0.1";

	@Instance("Streak")
	public static Streak instance;
	
	private static final Logger logger = LoggerHelper.createLogger("Streak");
	
	public static Config config;

	public static TickHandlerClient tickHandlerClient;
	
	public static int flavourCount;
	
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
		config.createOrUpdateIntProperty("basics", "Basics", "streakTime", "Streak Time", "How long (in ticks) do streaks last?", true, 100, 0, Integer.MAX_VALUE);
		config.createOrUpdateIntProperty("basics", "Basics", "playersFollowYourFavouriteFlavour", "Player Flavour", "Do players follow your favourite flavour?", true, 0, 0, 1);
		config.createOrUpdateStringProperty("basics", "Basics", "favouriteFlavour", "Favourite Flavour", "What's your favourite flavour?\nPut the name of it as the config\nLeave it as a mismatching name for a random flavour per person.", true, "");
		
		RenderingRegistry.registerEntityRenderingHandler(EntityStreak.class, new RenderStreak());
	}
	
	@SideOnly(Side.CLIENT)
	@EventHandler
	public void load(FMLInitializationEvent event)
	{
		tickHandlerClient = new TickHandlerClient();
		TickRegistry.registerTickHandler(tickHandlerClient, Side.CLIENT);
		
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
    	logger.log(warning ? Level.WARNING : Level.INFO, sb.append("[").append(version).append("] ").append(s).toString());
    }
}
