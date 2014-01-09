package streak.common.core;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import streak.common.Streak;
import streak.common.entity.EntityStreak;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class TickHandlerClient implements ITickHandler {

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) 
	{
		if (type.equals(EnumSet.of(TickType.RENDER)))
		{
			if(Minecraft.getMinecraft().theWorld != null)
			{
				preRenderTick(Minecraft.getMinecraft(), Minecraft.getMinecraft().theWorld, (Float)tickData[0]); //only ingame
			}
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) 
	{
		if (type.equals(EnumSet.of(TickType.CLIENT)))
		{
			if(Minecraft.getMinecraft().theWorld != null)
			{      		
				worldTick(Minecraft.getMinecraft(), Minecraft.getMinecraft().theWorld);
			}
		}
		else if (type.equals(EnumSet.of(TickType.PLAYER)))
		{
			playerTick((World)((EntityPlayer)tickData[0]).worldObj, (EntityPlayer)tickData[0]);
		}
	}

	@Override
	public EnumSet<TickType> ticks() 
	{
		return EnumSet.of(TickType.CLIENT, TickType.PLAYER, TickType.RENDER);
	}

	@Override
	public String getLabel() 
	{
		return "Streak_TickHandlerClient";
	}
	
	public void preRenderTick(Minecraft mc, World world, float renderTick)
	{
		this.renderTick = renderTick;
		
		Iterator<Entry<String, EntityStreak>> iterator = streaks.entrySet().iterator();
		
		while(iterator.hasNext())
		{
			Entry<String, EntityStreak> e = iterator.next();
			if(e.getValue().parent != null)
			{
				EntityStreak streak = e.getValue();
				
				updatePos(streak, streak.parent);
			}
		}

	}
	
	public void worldTick(Minecraft mc, WorldClient world)
	{
		if(worldInstance != world)
		{
			worldInstance = world;
			streaks.clear();
		}
		
		Iterator<Entry<String, EntityStreak>> ite = streaks.entrySet().iterator();
		
		while(ite.hasNext())
		{
			Entry<String, EntityStreak> e = ite.next();
			if(e.getValue().worldObj.provider.dimensionId != world.provider.dimensionId || (world.getWorldTime() - e.getValue().lastUpdate) > 10L)
			{
				e.getValue().setDead();
				ite.remove();
			}
		}
	}
	
	public void playerTick(World world, EntityPlayer player)
	{
		EntityStreak hat = streaks.get(player.username);
		if(hat == null || hat.isDead)
		{
			if(player.username.equalsIgnoreCase(Minecraft.getMinecraft().thePlayer.username))
			{
				//Assume respawn
				for(Entry<String, EntityStreak> e : streaks.entrySet())
				{
					e.getValue().setDead();
				}
			}
			
			hat = new EntityStreak(world, player);
			streaks.put(player.username, hat);
			world.spawnEntityInWorld(hat);
		}
		
		ArrayList<LocationInfo> loc = getPlayerLocationInfo(player);
		LocationInfo oldest = loc.get(0);
		loc.remove(0);
		loc.add(oldest);
		oldest.update(player);
		LocationInfo newest = loc.get(loc.size() - 2);
		double distX = newest.posX - oldest.posX;
		double distZ = newest.posZ - oldest.posZ;
		oldest.startU = newest.startU + (Math.sqrt(distX * distX + distZ * distZ) / newest.height);
		while(oldest.startU > 1.0D)
		{
			oldest.startU--;
		}
	}
	
	public ArrayList<LocationInfo> getPlayerLocationInfo(EntityPlayer player)
	{
		ArrayList<LocationInfo> loc = playerLoc.get(player.username);//0 = oldest
		if(loc == null)
		{
			loc = new ArrayList<LocationInfo>();
			playerLoc.put(player.username, loc);
		}
		int time = Streak.config.getInt("streakTime");
		if(loc.size() < time)
		{
			for(int i = 0; i < (time - loc.size()); i++)
			{
				loc.add(0, new LocationInfo(player));
			}
		}
		else if(loc.size() > time)
		{
			loc.remove(0);
		}
		return loc;
	}
	
	public void updatePos(EntityStreak streak, EntityLivingBase parent)
	{
		streak.lastTickPosX = streak.parent.lastTickPosX;
		streak.lastTickPosY = streak.parent.lastTickPosY;
		streak.lastTickPosZ = streak.parent.lastTickPosZ;
		
		streak.prevPosX = streak.parent.prevPosX;
		streak.prevPosY = streak.parent.prevPosY;
		streak.prevPosZ = streak.parent.prevPosZ;
		
		streak.posX = streak.parent.posX;
		streak.posY = streak.parent.posY;
		streak.posZ = streak.parent.posZ;
	}
	
	public float renderTick;
	
	public WorldClient worldInstance;
	
	public HashMap<String, ArrayList<LocationInfo>> playerLoc = new HashMap<String, ArrayList<LocationInfo>>();
	
	public HashMap<String, EntityStreak> streaks = new HashMap<String, EntityStreak>();
}
