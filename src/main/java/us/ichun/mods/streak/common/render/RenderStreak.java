package us.ichun.mods.streak.common.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import us.ichun.mods.streak.common.Streak;
import us.ichun.mods.streak.common.core.LocationInfo;
import us.ichun.mods.streak.common.entity.EntityStreak;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class RenderStreak extends Render
{
	public RenderStreak()
	{
		super(Minecraft.getMinecraft().getRenderManager());
		shadowSize = 0.0F;
	}

	public void renderStreak(EntityStreak hat, double par2, double par4, double par6, float par8, float par9)
	{
		if(!(hat.parent instanceof AbstractClientPlayer) || Streak.flavours.isEmpty() || hat.parent.isInvisible())
		{
			return;
		}

		AbstractClientPlayer player = (AbstractClientPlayer)hat.parent;

		if(player.isInvisible() || player == Minecraft.getMinecraft().thePlayer && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0)
		{
			return;
		}

		ArrayList<LocationInfo> loc = Streak.tickHandlerClient.getPlayerLocationInfo(player);

		GlStateManager.pushMatrix();

		Minecraft mc = Minecraft.getMinecraft();

		BufferedImage image;

		Integer flavour = Streak.flavourNames.get(Streak.config.getString("favouriteFlavour").toLowerCase());
		if(flavour != null && (Streak.config.getInt("playersFollowYourFavouriteFlavour") == 1 && player != Minecraft.getMinecraft().thePlayer || player == Minecraft.getMinecraft().thePlayer))
		{
			image = Streak.flavours.get(flavour);
		}
		else
		{
			image = Streak.flavours.get(hat.flavour);
		}

		GlStateManager.disableCull();

		GlStateManager.disableAlpha();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		float startGrad = 5 - par9;
		float endGrad = 20 - par9;

		//        Streak.config.getInt("sprintTrail");

		for(int i = loc.size() - 2; i >=0 ; i--)
		{
			int start = i;
			LocationInfo infoStart = loc.get(i);

			float startAlpha = i > loc.size() - 2 - startGrad ? (MathHelper.clamp_float(0.8F * (float)(loc.size() - 2 - i) / (float)startGrad, 0.0F, 0.8F)) : i < endGrad ? MathHelper.clamp_float(0.8F * (float)i / endGrad, 0.0F, 0.8F) : 0.8F;

			if(player.worldObj.getWorldTime() - infoStart.lastTick > Streak.config.getInt("streakTime") + 20)
			{
				break;
			}

			LocationInfo infoEnd = null;

			double grad = 500D;

			i--;
			while(i >= 0)
			{
				LocationInfo infoPoint = loc.get(i);
				if(Streak.config.getInt("sprintTrail") == 1 && infoStart.isSprinting && (loc.size() - 2 - i) < 6 && (Streak.hasMorphMod && (morph.api.Api.hasMorph(player.getName(), true) && morph.api.Api.getMorphEntity(player.getName(), true) instanceof EntityPlayer && morph.api.Api.morphProgress(player.getName(), true) >= 1.0F || !morph.api.Api.hasMorph(player.getName(), true)) || !Streak.hasMorphMod))
				{
					infoEnd = infoPoint;
					start--;
					i--;
					break;
				}
				if(infoPoint.hasSameCoords(infoStart))
				{
					start--;
					i--;
					continue;
				}
				double grad1 = infoPoint.posZ - infoStart.posZ / (infoPoint.posX - infoStart.posX);
				if(grad == grad1 && infoPoint.posY == infoStart.posY)
				{
					infoEnd = infoPoint;
					start--;
					i--;
					continue;
				}
				if(grad == 500D)
				{
					grad = grad1;
				}
				else
				{
					break;
				}
				infoEnd = infoPoint;
				i--;
			}
			if(infoEnd != null)
			{
				i += 2;

				float endAlpha = i > loc.size() - 1 - startGrad ? (MathHelper.clamp_float(0.8F * (float)(loc.size() - 1 - i) / (float)startGrad, 0.0F, 0.8F)) : i < endGrad ? MathHelper.clamp_float(0.8F * (float)(i - 1) / endGrad, 0.0F, 0.8F) : 0.8F;

				RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();

				double posX = infoStart.posX - renderManager.renderPosX;
				double posY = infoStart.posY - renderManager.renderPosY;
				double posZ = infoStart.posZ - renderManager.renderPosZ;

				double nextPosX = infoEnd.posX - renderManager.renderPosX;
				double nextPosY = infoEnd.posY - renderManager.renderPosY;
				double nextPosZ = infoEnd.posZ - renderManager.renderPosZ;

				Tessellator tessellator = Tessellator.getInstance();
				WorldRenderer worldRenderer = tessellator.getWorldRenderer();

				GlStateManager.pushMatrix();

				GlStateManager.translate(posX, posY, posZ);

				int ii = hat.getBrightnessForRender(par9);
				int j = ii % 65536;
				int k = ii / 65536;
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

				worldRenderer.startDrawingQuads();
				worldRenderer.setColorRGBA_F(1.0F, 1.0F, 1.0F, startAlpha);
				worldRenderer.addVertexWithUV(0		, 0					, 0, infoStart.startU, 1.0D);
				worldRenderer.addVertexWithUV(0		, 0 + infoStart.height	, 0, infoStart.startU, 0.0D);
				worldRenderer.setColorRGBA_F(1.0F, 1.0F, 1.0F, endAlpha);
				double endTex = infoEnd.startU - start + i;
				if(endTex > infoStart.startU)
				{
					endTex--;
				}
				//		        while(endTex < 0)
				//		        {
				//		        	endTex++;
				//		        }
				double distX = infoStart.posX - infoEnd.posX;
				double distZ = infoStart.posZ - infoEnd.posZ;
				double scales = Math.sqrt(distX * distX + distZ * distZ) / infoStart.height;
				boolean far = scales > 1D;
				if(scales < 1)
				{
					//					System.out.println(infoStart.startU);
					//					System.out.println(endTex);
				}
				while(scales > 1D)
				{
					endTex++;
					scales--;
				}
				worldRenderer.addVertexWithUV(nextPosX - posX	, nextPosY - posY + infoEnd.height	, nextPosZ - posZ, endTex, 0.0D);
				worldRenderer.addVertexWithUV(nextPosX - posX	, nextPosY - posY					, nextPosZ - posZ, endTex, 1.0D);
				tessellator.draw();

				GlStateManager.enableLighting();
				RenderHelper.enableStandardItemLighting();

				//				if(Streak.config.getInt("sprintTrail") == 1 && (Streak.hasMorphMod && (morph.api.Api.hasMorph(player.getName(), true) && morph.api.Api.getMorphEntity(player.getName(), true) instanceof EntityPlayer && morph.api.Api.morphProgress(player.getName(), true) >= 1.0F || !morph.api.Api.hasMorph(player.getName(), true)) || !Streak.hasMorphMod))
				if(Streak.config.getInt("sprintTrail") == 1 && infoStart.isSprinting && (loc.size() - 2 - i) < 6 && (Streak.hasMorphMod && (morph.api.Api.hasMorph(player.getName(), true) && morph.api.Api.getMorphEntity(player.getName(), true) instanceof EntityPlayer && morph.api.Api.morphProgress(player.getName(), true) >= 1.0F || !morph.api.Api.hasMorph(player.getName(), true)) || !Streak.hasMorphMod))
				{
					ModelBase biped = ((RenderPlayer)Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(player)).mainModel;
					ii = player.getBrightnessForRender(par9);
					j = ii % 65536;
					k = ii / 65536;
					OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j / 1.0F, (float)k / 1.0F);

					GlStateManager.pushMatrix();
					GlStateManager.rotate(infoStart.renderYawOffset, 0.0F, -1.0F, 0.0F);

					float scalee = 0.9375F;
					GlStateManager.scale(scalee, -scalee, -scalee);

					GlStateManager.translate(0.0F, -1.5F, 0.0F);

					float alpha = 1.0F - MathHelper.clamp_float(((float)(loc.size() - 2 - i) + par9) / (float)((loc.size() - 2) > 5 ? 5 : loc.size() - 2), 0.0F, 1.0F);
					//					float alpha = 1.0F;

					GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);

					mc.getTextureManager().bindTexture(player.getLocationSkin());
					float f2 = infoStart.renderYawOffset;
					float f3 = infoStart.rotationYawHead;

					float f7 = infoStart.limbSwingAmount;

					float f8 = infoStart.limbSwing - infoStart.limbSwingAmount;

					if (f7 > 1.0F)
					{
						f7 = 1.0F;
					}

					float f4 = (float)player.ticksExisted - (loc.size() - 2 - i);

					float f5 = infoStart.rotationPitch;

					biped.render(player, f8, f7, f4, f3 - f2, f5, 0.0625F);

					GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

					GlStateManager.popMatrix();
				}

				GlStateManager.popMatrix();
			}
		}

		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();

		GlStateManager.enableCull();

		GlStateManager.popMatrix();
	}

	@Override
	public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9)
	{
		this.renderStreak((EntityStreak)par1Entity, par2, par4, par6, par8, par9);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity)
	{
		return DefaultPlayerSkin.getDefaultSkinLegacy();
	}

}
