package me.ichun.mods.streak.common.core;

import me.ichun.mods.ichunutil.client.render.entity.RenderLatchedRenderer;
import me.ichun.mods.ichunutil.common.core.tracker.EntityTrackerRegistry;
import me.ichun.mods.ichunutil.common.iChunUtil;
import me.ichun.mods.morph.api.MorphApi;
import me.ichun.mods.streak.common.Streak;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class EventHandlerClient
{
    public Random rand = new Random();

    @SubscribeEvent
    public void onLatchedRendererRender(RenderLatchedRenderer.RenderLatchedRendererEvent event)
    {
        if(!(event.ent.latchedEnt instanceof AbstractClientPlayer) || Streak.flavours.isEmpty() || event.ent.latchedEnt.isInvisible())
        {
            return;
        }

        AbstractClientPlayer player = (AbstractClientPlayer)event.ent.latchedEnt;
        if(player == Minecraft.getMinecraft().thePlayer && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0)
        {
            return;
        }

        EntityTrackerRegistry.Entry entry = iChunUtil.eventHandlerClient.getEntityTrackerRegistry().getOrCreateEntry(player, Streak.config.streakTime).addAdditionalTrackerInfo(TextureTracker.class);
        ArrayList<EntityTrackerRegistry.EntityInfo> loc = entry.trackedInfo;

        BufferedImage image;

        Integer flavour = Streak.flavourNames.get(Streak.config.favouriteFlavour.toLowerCase());
        if(flavour != null && (Streak.config.playersFollowYourFavouriteFlavour == 1 && player != Minecraft.getMinecraft().thePlayer || player == Minecraft.getMinecraft().thePlayer))
        {
            image = Streak.flavours.get(flavour);
        }
        else
        {
            rand.setSeed(Math.abs(entry.hashCode() * 84152L));
            image = Streak.flavours.get(rand.nextInt(Streak.flavours.size()));
        }

        int start = 1;
        if(loc.size() > start)
        {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.shadeModel(GL11.GL_SMOOTH);

            event.ent.setIgnoreFrustumCheck();

            GlStateManager.pushMatrix();
            GlStateManager.translate(event.x, event.y, event.z);

            double tX = event.ent.prevPosX + (event.ent.posX - event.ent.prevPosX) * event.partialTick;
            double tY = event.ent.prevPosY + (event.ent.posY - event.ent.prevPosY) * event.partialTick;
            double tZ = event.ent.prevPosZ + (event.ent.posZ - event.ent.prevPosZ) * event.partialTick;

            if(loc.size() > 6 && Streak.config.sprintTrail == 1)
            {
                GlStateManager.enableCull();

                AbstractClientPlayer playerRender = player;
                if(iChunUtil.hasMorphMod() && MorphApi.getApiImpl().hasMorph(player.getName(), Side.CLIENT))
                {
                    if(MorphApi.getApiImpl().getMorphEntity(player.worldObj, player.getName(), Side.CLIENT) instanceof AbstractClientPlayer && MorphApi.getApiImpl().morphProgress(player.getName(), Side.CLIENT) >= 1.0F)
                    {
                        playerRender = (AbstractClientPlayer)MorphApi.getApiImpl().getMorphEntity(player.worldObj, player.getName(), Side.CLIENT);
                    }
                    else
                    {
                        return;
                    }
                }
                ModelBase biped = ((RenderPlayer)Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(playerRender)).mainModel;

                ResourceLocation rl = playerRender.getLocationSkin();
                for(int ii = 1; ii < 6; ii++)
                {
                    EntityTrackerRegistry.EntityInfo entInfo = loc.get(ii);
                    if(!(entInfo.sprinting || entInfo.elytraFlying))
                    {
                        continue;
                    }
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(entInfo.posX - tX, entInfo.posY - tY, entInfo.posZ - tZ);

                    GlStateManager.rotate(entInfo.renderYawOffset, 0.0F, -1.0F, 0.0F);

                    //elytra rotation
                    if(entInfo.elytraFlying)
                    {
                        float f = (float)playerRender.getTicksElytraFlying() + event.partialTick;
                        float f1 = MathHelper.clamp_float(f * f / 100.0F, 0.0F, 1.0F);
                        GlStateManager.rotate(f1 * (-90.0F - playerRender.rotationPitch), -1.0F, 0.0F, 0.0F);
                        Vec3d vec3d = playerRender.getLook(event.partialTick);
                        double d0 = playerRender.motionX * playerRender.motionX + playerRender.motionZ * playerRender.motionZ;
                        double d1 = vec3d.xCoord * vec3d.xCoord + vec3d.zCoord * vec3d.zCoord;

                        if(d0 > 0.0D && d1 > 0.0D)
                        {
                            double d2 = (playerRender.motionX * vec3d.xCoord + playerRender.motionZ * vec3d.zCoord) / (Math.sqrt(d0) * Math.sqrt(d1));
                            double d3 = playerRender.motionX * vec3d.zCoord - playerRender.motionZ * vec3d.xCoord;
                            GlStateManager.rotate((float)(Math.signum(d3) * Math.acos(d2)) * 180.0F / (float)Math.PI, 0.0F, 1.0F, 0.0F);
                        }
                    }
                    //end elytra rotation

                    float scalee = 0.9375F;
                    GlStateManager.scale(scalee, -scalee, -scalee);

                    GlStateManager.translate(0.0F, -1.5F, 0.0F);

                    float alpha = 1.0F - MathHelper.clamp_float(((ii - 1) + event.partialTick) / 5F, 0.0F, 1.0F);//1.0F - MathHelper.clamp_float(((float)(loc.size() - 2 - i) + partialTick) / (float)((loc.size() - 2) > 5 ? 5 : loc.size() - 2), 0.0F, 1.0F);

                    GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);

                    Minecraft.getMinecraft().getTextureManager().bindTexture(rl);

                    float f2 = entInfo.renderYawOffset;
                    float f3 = entInfo.rotationYawHead;

                    float f7 = entInfo.limbSwingAmount;

                    float f8 = entInfo.limbSwing - entInfo.limbSwingAmount;

                    if (f7 > 1.0F)
                    {
                        f7 = 1.0F;
                    }

                    float f4 = (float)playerRender.ticksExisted - ii + event.partialTick;

                    float f5 = entInfo.rotationPitch;

                    biped.render(playerRender, f8, f7, f4, f3 - f2, f5, 0.0625F);

                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                    GlStateManager.popMatrix();
                }
            }
            GlStateManager.disableCull();

            int i = 0xF000F0;
            int j = i % 0x10000;
            int k = i / 0x10000;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j / 1.0F, (float)k / 1.0F);

            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableLighting();

            if (image != null)
            {
                if (Streak.flavourImageId.get(image) == -1)
                {
                    Streak.flavourImageId.put(image, TextureUtil.uploadTextureImage(TextureUtil.glGenTextures(), image));
                }
                GlStateManager.bindTexture(Streak.flavourImageId.get(image));
            }

            Tessellator tessellator = Tessellator.getInstance();
            VertexBuffer vertexbuffer = tessellator.getBuffer();

            vertexbuffer.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION_TEX_COLOR);

            int count = Math.min(loc.size(), Streak.config.streakTime);
            for(int index = count - 1; index >= start; index--)
            {
                EntityTrackerRegistry.EntityInfo info = loc.get(index);
                TextureTracker textureTracker = info.getTracker(TextureTracker.class);
                float alpha = 1F;
                if(index < start + 5)
                {
                    alpha = (index - start) / 5F;
                }
                if(index > count - 20 - start)
                {
                    alpha = ((count - start) - index) / 20F;
                }
                vertexbuffer.pos(info.posX - tX, info.posY - tY, info.posZ - tZ).tex(textureTracker.startU, 1D).color(1F, 1F, 1F, alpha).endVertex();
                vertexbuffer.pos(info.posX - tX, info.posY - tY + info.height, info.posZ - tZ).tex(textureTracker.startU, 0D).color(1F, 1F, 1F, alpha).endVertex();
            }
            tessellator.draw();

            GlStateManager.enableLighting();
            RenderHelper.enableStandardItemLighting();

            i = player.getBrightnessForRender(event.partialTick);
            j = i % 0x10000;
            k = i / 0x10000;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j / 1.0F, (float)k / 1.0F);

            GlStateManager.enableCull();

            GlStateManager.popMatrix();

            GlStateManager.shadeModel(GL11.GL_FLAT);
            GlStateManager.disableBlend();
        }
    }

    @SubscribeEvent
    public void playerTick(TickEvent.PlayerTickEvent event)
    {
        if(event.side == Side.CLIENT && event.phase == TickEvent.Phase.END)
        {
            EntityTrackerRegistry.Entry entry = iChunUtil.eventHandlerClient.getEntityTrackerRegistry().getOrCreateEntry(event.player, Streak.config.streakTime).addAdditionalTrackerInfo(TextureTracker.class);
            ArrayList<EntityTrackerRegistry.EntityInfo> loc = entry.trackedInfo;
            if(loc.size() > 1)
            {
                EntityTrackerRegistry.EntityInfo old = loc.get(1);
                EntityTrackerRegistry.EntityInfo newer = loc.get(0);

                double distX = old.posX - newer.posX;
                double distZ = old.posZ - newer.posZ;

                TextureTracker tracker1 = old.getTracker(TextureTracker.class);
                TextureTracker tracker2 = newer.getTracker(TextureTracker.class);
                if(tracker1 != null && tracker2 != null)
                {
                    tracker2.startU = tracker1.startU + (Math.sqrt(distX * distX + distZ * distZ) / old.height);
                }
            }
        }
    }
}
