package me.ichun.mods.streak.common.tracker;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.ichun.mods.ichunutil.client.render.RenderHelper;
import me.ichun.mods.ichunutil.client.tracker.entity.EntityTracker;
import me.ichun.mods.ichunutil.client.tracker.tag.Tag;
import me.ichun.mods.streak.common.Streak;
import me.ichun.mods.streak.common.core.EventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class StreakTag
        implements Tag
{
    @Nonnull
    public EventHandler.FlavourInfo flavour;

    public HashMap<EntityTracker.EntityInfo, StreakInfo> texUs = new HashMap<>();

    @Override
    public int maxTracks()
    {
        return Math.max(Streak.config.streakTime, Streak.config.sprintTrail);
    }

    @Override
    public int maxDeathPersist()
    {
        return Math.max(Streak.config.streakTime, Streak.config.sprintTrail);
    }

    @Override
    public void init(EntityTracker tracker)
    {
        flavour = Streak.eventHandler.getFlavourFor(tracker.parent);
    }

    @Override
    public void tick(EntityTracker tracker)
    {
    }

    @Override
    public void addInfo(EntityTracker tracker, EntityTracker.EntityInfo info)
    {
        if(texUs.isEmpty()) //our first one.
        {
            texUs.put(info, createFor((LivingEntity)tracker.parent, 0F));
        }
        else //not our first, and there's a second element
        {
            EntityTracker.EntityInfo lastInfo = tracker.trackedInfo.get(1);
            double x = (lastInfo.posX - info.posX);
            double z = (lastInfo.posZ - info.posZ);
            double dist = Math.sqrt(x * x + z * z);
            double minHeight = Math.min(lastInfo.height, info.height);
            texUs.put(info, createFor((LivingEntity)tracker.parent, texUs.getOrDefault(lastInfo, createFor((LivingEntity)tracker.parent, 0F)).texU + (float)(dist / minHeight)));
        }
    }

    public StreakInfo createFor(LivingEntity entityIn, float texU)
    {
        float f8 = 0.0F;
        float f5 = 0.0F;
        if (entityIn.isAlive()) {
            f8 = entityIn.limbSwingAmount;
            f5 = entityIn.limbSwing;
            if (entityIn.isChild()) {
                f5 *= 3.0F;
            }

            if (f8 > 1.0F) {
                f8 = 1.0F;
            }
        }

        return new StreakInfo(texU, entityIn.isSprinting() && entityIn.isAlive(), entityIn.getSwingProgress(1F), f5, f8, entityIn.ticksExisted, entityIn.renderYawOffset, entityIn.rotationYawHead - entityIn.renderYawOffset, entityIn.rotationPitch);
    }

    @Override
    public void removeInfo(EntityTracker tracker, EntityTracker.EntityInfo info)
    {
        texUs.remove(info);
    }

    @Override
    public boolean ignoreFrustumCheck()
    {
        return true;
    }

    @Override
    public void render(EntityTracker tracker, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn)
    {
        if(tracker.trackedInfo.size() <= 1)
        {
            return;
        }
        //        boolean firstPerson = (tracker.parent == Minecraft.getInstance().getRenderViewEntity() && Minecraft.getInstance().gameSettings.thirdPersonView == 0 && !(Minecraft.getInstance().currentScreen instanceof InventoryScreen || Minecraft.getInstance().currentScreen instanceof CreativeScreen));
        boolean firstPerson = (tracker.parent == Minecraft.getInstance().getRenderViewEntity() && Minecraft.getInstance().gameSettings.thirdPersonView == 0);
        if(firstPerson && !Streak.config.renderInFirstPerson)
        {
            return;
        }

        matrixStackIn.push();

        double d0 = MathHelper.lerp((double)partialTicks, tracker.parent.lastTickPosX, tracker.parent.getPosX());
        double d1 = MathHelper.lerp((double)partialTicks, tracker.parent.lastTickPosY, tracker.parent.getPosY());
        double d2 = MathHelper.lerp((double)partialTicks, tracker.parent.lastTickPosZ, tracker.parent.getPosZ());

        //        RenderType.State rendertype$state = RenderType.State.getBuilder().texture(new RenderState.TextureState(flavour.getResourceLocation(), false, false)).transparency(RenderType.TRANSLUCENT_TRANSPARENCY).diffuseLighting(RenderType.DIFFUSE_LIGHTING_ENABLED).alpha(RenderType.DEFAULT_ALPHA).cull(RenderType.CULL_DISABLED).lightmap(RenderType.LIGHTMAP_ENABLED).overlay(RenderType.OVERLAY_ENABLED).build(true);
        //        IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.makeType("entity_translucent_triangle", DefaultVertexFormats.ENTITY, GL11.GL_TRIANGLE_STRIP, 256, true, true, rendertype$state));
        MatrixStack.Entry matrixstack$entry = matrixStackIn.getLast();
        Matrix4f matrix4f = matrixstack$entry.getMatrix();
        Matrix3f matrix3f = matrixstack$entry.getNormal();

        float opacity = Streak.config.streakOpacity / 100F;

        IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getEntityTranslucent(flavour.getResourceLocation()));
        for(int i = Math.min(Streak.config.streakTime, tracker.trackedInfo.size()) - 1 - tracker.timeAfterDeath; i >= 1; i--)
        {
            EntityTracker.EntityInfo info = tracker.trackedInfo.get(i);
            StreakInfo si = texUs.get(info);
            if(si == null)
            {
                continue;
            }

            float u = si.texU;

            EntityTracker.EntityInfo nextInfo = tracker.trackedInfo.get(i - 1);
            StreakInfo si1 = texUs.get(nextInfo);
            if(si1 == null)
            {
                continue;
            }

            float u1 = si1.texU;

            if(u == u1) //we've not moved
            {
                continue;
            }
            if(info.invisible && nextInfo.invisible)
            {
                continue;
            }

            float x = (float)(info.posX - d0);
            float y = (float)(info.posY - d1);
            float z = (float)(info.posZ - d2);

            float x1 = (float)(nextInfo.posX - d0);
            float y1 = (float)(nextInfo.posY - d1);
            float z1 = (float)(nextInfo.posZ - d2);

            int alpha = 255;
            int alpha1 = 255;
            int trailingAlpha = Math.min(Streak.config.streakTime, tracker.trackedInfo.size()) - 21 - tracker.timeAfterDeath;
            if(info.invisible)
            {
                alpha = 0;
            }
            if(i < 5)
            {
                if(tracker.parent.isAlive())
                {
                    alpha1 = (int)(((i + partialTicks) / 5F) * 255F);
                    alpha = (int)(((i + partialTicks - 1) / 5F) * 255F);
                }
                else
                {
                    alpha1 = (int)(((i) / 5F) * 255F);
                    alpha = (int)(((i - 1) / 5F) * 255F);
                }
            }
            if(i > trailingAlpha)
            {
                alpha = (int)((1F - ((i + partialTicks - trailingAlpha) / 20F)) * alpha);
                alpha1 = (int)((1F - (((i + partialTicks - 1) - trailingAlpha) / 20F)) * alpha1);
            }
            if(nextInfo.invisible)
            {
                alpha1 = 0;
            }

            alpha = (int)(alpha * opacity);
            alpha1 = (int)(alpha1 * opacity);

            ivertexbuilder.pos(matrix4f, x, y              , z).color(255, 255, 255, alpha).tex(u, 1F).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
            ivertexbuilder.pos(matrix4f, x, y + info.height, z).color(255, 255, 255, alpha).tex(u, 0F).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
            ivertexbuilder.pos(matrix4f, x1, y1 + info.height, z1).color(255, 255, 255, alpha1).tex(u1, 0F).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
            ivertexbuilder.pos(matrix4f, x1, y1              , z1).color(255, 255, 255, alpha1).tex(u1, 1F).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
        }

        LivingRenderer renderer = null;
        EntityRenderer<?> entRend = Minecraft.getInstance().getRenderManager().getRenderer(tracker.parent);
        if(entRend instanceof LivingRenderer)
        {
            renderer = (LivingRenderer<?, ?>)entRend;
        }
        if(renderer != null)
        {
            ivertexbuilder = bufferIn.getBuffer(RenderType.getEntityTranslucent(renderer.getEntityTexture(tracker.parent)));
            for(int i = Math.min(Streak.config.sprintTrail, tracker.trackedInfo.size()) - 1 - tracker.timeAfterDeath; i >= 1; i--)
            {
                EntityTracker.EntityInfo info = tracker.trackedInfo.get(i);
                StreakInfo si = texUs.get(info);
                if(si == null)
                {
                    continue;
                }

                if(si.sprint)// render sprint trail
                {
                    float x = (float)(info.posX - d0);
                    float y = (float)(info.posY - d1);
                    float z = (float)(info.posZ - d2);

                    EntityModel model = renderer.getEntityModel();
                    model.swingProgress = si.swingProg;
                    model.setLivingAnimations(tracker.parent, si.swing, si.revSwing, 1F);
                    model.setRotationAngles(tracker.parent, si.swing, si.revSwing, si.age, si.yaw, si.pitch);
                    matrixStackIn.push();
                    matrixStackIn.translate(x, y, z);
                    matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180.0F - si.yawOffset));
                    matrixStackIn.scale(-1.0F, -1.0F, 1.0F);
                    RenderHelper.invokePreRenderCallback(renderer, (LivingEntity)tracker.parent, matrixStackIn, partialTicks);
                    matrixStackIn.translate(0.0D, (double)-1.501F, 0.0D);
                    model.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, ((Streak.config.sprintTrail + 1) - (i + partialTicks)) / (float)(Streak.config.sprintTrail) * 1F);
                    matrixStackIn.pop();
                }
            }
        }

        matrixStackIn.pop();
    }

    public static class StreakInfo
    {
        public final float texU;
        public final boolean sprint;
        public final float swingProg;
        public final float swing;
        public final float revSwing;
        public final float age;
        public final float yawOffset;
        public final float yaw;
        public final float pitch;

        public StreakInfo(float texU, boolean sprint, float swingProg, float swing, float revSwing, float age, float yawOffset, float yaw, float pitch) {
            this.texU = texU;
            this.sprint = sprint;
            this.swingProg = swingProg;
            this.swing = swing;
            this.revSwing = revSwing;
            this.age = age;
            this.yawOffset = yawOffset;
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }
}
