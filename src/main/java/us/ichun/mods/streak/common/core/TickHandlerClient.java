package us.ichun.mods.streak.common.core;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import us.ichun.mods.streak.common.Streak;
import us.ichun.mods.streak.common.entity.EntityStreak;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class TickHandlerClient
{
    @SubscribeEvent
	public void renderTick(TickEvent.RenderTickEvent event)
	{
        if(event.phase == TickEvent.Phase.START)
        {
            this.renderTick = event.renderTickTime;

            Iterator<Entry<String, EntityStreak>> iterator = streaks.entrySet().iterator();

            while(iterator.hasNext())
            {
                Entry<String, EntityStreak> e = iterator.next();
                if(e.getValue().parent != null)
                {
                    EntityStreak streak = e.getValue();
                    if(e.getValue().parent.isDead)
                    {
                        streak.setDead();
                        iterator.remove();
                    }
                    else
                    {
                        updatePos(streak, streak.parent);
                    }
                }
            }
        }
	}

    @SubscribeEvent
	public void worldTick(TickEvent.ClientTickEvent event)
	{
        if(event.phase == TickEvent.Phase.END && Minecraft.getMinecraft().theWorld != null)
        {
            WorldClient world = Minecraft.getMinecraft().theWorld;
            if(worldInstance != world)
            {
                worldInstance = world;
                streaks.clear();
            }

            Iterator<Entry<String, EntityStreak>> ite = streaks.entrySet().iterator();

            while(ite.hasNext())
            {
                Entry<String, EntityStreak> e = ite.next();
                if(e.getValue().worldObj.provider.getDimensionId() != world.provider.getDimensionId() || (world.getWorldTime() - e.getValue().lastUpdate) > 10L)
                {
                    e.getValue().setDead();
                    ite.remove();
                }
            }
        }
	}

    @SubscribeEvent
	public void playerTick(TickEvent.PlayerTickEvent event)
	{
        if(event.side == Side.CLIENT && event.phase == TickEvent.Phase.END)
        {
            EntityPlayer player = event.player;
            if(player.worldObj.getPlayerEntityByName(player.getName()) != player)
            {
                return;
            }

            WorldClient world = Minecraft.getMinecraft().theWorld;

            EntityStreak hat = streaks.get(player.getName());
            if(hat == null || hat.isDead)
            {
                if(player.getName().equalsIgnoreCase(Minecraft.getMinecraft().thePlayer.getName()))
                {
                    //Assume respawn
                    for(Entry<String, EntityStreak> e : streaks.entrySet())
                    {
                        e.getValue().setDead();
                    }
                }

                hat = new EntityStreak(world, player);
                streaks.put(player.getName(), hat);
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
	}
	
	public ArrayList<LocationInfo> getPlayerLocationInfo(EntityPlayer player)
	{
		ArrayList<LocationInfo> loc = playerLoc.get(player.getName());//0 = oldest
		if(loc == null)
		{
			loc = new ArrayList<LocationInfo>();
			playerLoc.put(player.getName(), loc);
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
