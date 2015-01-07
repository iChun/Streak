package us.ichun.mods.streak.common.core;

import net.minecraft.entity.player.EntityPlayer;

public class LocationInfo 
{
	public double posX;
	public double posY;
	public double posZ;
	
	public float renderYawOffset;
	public float rotationYawHead;
	public float rotationPitch;
	
	public float limbSwing;
	public float limbSwingAmount;
	
	public boolean isSprinting;
	
	public long lastTick;
	public float height;
	
	public double startU;

	public LocationInfo(EntityPlayer player) 
	{
		update(player);
	}
	
	public void update(EntityPlayer player)
	{
		posX = player.posX;
		posY = player.getEntityBoundingBox().minY;
		posZ = player.posZ;
		
		renderYawOffset = player.renderYawOffset;
		rotationYawHead = player.rotationYawHead;
		rotationPitch = player.rotationPitch;
		
		limbSwing = player.limbSwing;
		limbSwingAmount = player.limbSwingAmount;
		
		isSprinting = player.isSprinting();
		
		lastTick = player.worldObj.getWorldTime();
		
		height = player.height;
	}

	public boolean hasSameCoords(LocationInfo info) 
	{
		return info.posX == posX && info.posY == posY && info.posZ == posZ && info.height == height;
	}
}
