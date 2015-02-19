package us.ichun.mods.streak.common.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import us.ichun.mods.ichunutil.common.iChunUtil;
import us.ichun.mods.ichunutil.common.tracker.EntityInfo;
import us.ichun.mods.ichunutil.common.tracker.IAdditionalTrackerInfo;
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

            ArrayList<EntityInfo> loc = iChunUtil.proxy.tickHandlerClient.getOrRegisterEntityTracker(player, Streak.config.streakTime, TextureTracker.class, true);
            if(loc.size() > 1)
            {
                EntityInfo newest = loc.get(1);
                EntityInfo newer = loc.get(0);

                double distX = newest.posX - newer.posX;
                double distZ = newest.posZ - newer.posZ;

                IAdditionalTrackerInfo tracker1 = newest.getTracker(TextureTracker.class);
                IAdditionalTrackerInfo tracker2 = newer.getTracker(TextureTracker.class);
                if(tracker1 != null && tracker2 != null)
                {
                    ((TextureTracker)tracker2).startU = ((TextureTracker)tracker1).startU + (Math.sqrt(distX * distX + distZ * distZ) / newest.height);
                    while(((TextureTracker)tracker2).startU > 1.0D)
                    {
                        ((TextureTracker)tracker2).startU--;
                    }
                }
            }
        }
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

    public HashMap<String, EntityStreak> streaks = new HashMap<String, EntityStreak>();
}
