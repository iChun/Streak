package me.ichun.mods.streak.common.core;

import me.ichun.mods.ichunutil.client.render.NativeImageTexture;
import me.ichun.mods.ichunutil.client.tracker.ClientEntityTracker;
import me.ichun.mods.ichunutil.client.tracker.entity.EntityTracker;
import me.ichun.mods.streak.common.Streak;
import me.ichun.mods.streak.common.tracker.StreakTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Random;

public class EventHandler
{
    public Random rand = new Random();
    public ArrayList<FlavourInfo> flavours = new ArrayList<>();

    @SubscribeEvent
    public void onEntityJoinedWorld(EntityJoinWorldEvent event)
    {
        if(event.getWorld().isRemote)
        {
            Entity ent = event.getEntity();
            if(Streak.config.attachments.contains(ent.getType().getRegistryName()) && ent instanceof LivingEntity)
            {
                EntityTracker tracker = ClientEntityTracker.getOrCreate(ent);
                tracker.addTag(new StreakTag());
            }
        }
    }

    public FlavourInfo getFlavourFor(Entity ent)
    {
        if(Streak.config.setFlavourMap.containsKey(ent.getName().getUnformattedComponentText()))
        {
            String preferredFlavour = Streak.config.setFlavourMap.get(ent.getName().getUnformattedComponentText());

            for(FlavourInfo flavour : flavours)
            {
                if(flavour.name.equals(preferredFlavour)) //both are lower case
                {
                    return flavour;
                }
            }
        }

        return flavours.get(rand.nextInt(flavours.size()));
    }

    public static class FlavourInfo
            implements Comparable<FlavourInfo>
    {
        @Nonnull
        public final String name;
        @Nonnull
        private final NativeImage image;

        public NativeImageTexture nativeImageTexture;

        public FlavourInfo(String name, NativeImage image) {
            this.name = name;
            this.image = image;
        }

        public ResourceLocation getResourceLocation()
        {
            if(nativeImageTexture == null)
            {
                nativeImageTexture = new NativeImageTexture(image);
                Minecraft.getInstance().getTextureManager().loadTexture(nativeImageTexture.getResourceLocation(), nativeImageTexture);
            }

            return nativeImageTexture.getResourceLocation();
        }

        @Override
        public int compareTo(FlavourInfo o)
        {
            return name.compareTo(o.name);
        }
    }
}
