package streak.common.core;

import net.minecraft.entity.player.EntityPlayer;

public class LocationInfo 
{
	public double posX;
	public double posY;
	public double posZ;
	
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
		posY = player.boundingBox.minY;
		posZ = player.posZ;
		
		lastTick = player.worldObj.getWorldTime();
		
		height = player.height - player.ySize;
	}

	public boolean hasSameCoords(LocationInfo info) 
	{
		return info.posX == posX && info.posY == posY && info.posZ == posZ && info.height == height;
	}
}
