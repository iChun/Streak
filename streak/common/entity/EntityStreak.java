package streak.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityStreak extends Entity 
{

	public EntityLivingBase parent;
	
	public long lastUpdate;
	
	public EntityStreak(World par1World) 
	{
		super(par1World);
		setSize(0.1F, 0.1F);
		
		lastUpdate = par1World.getWorldTime();
		ignoreFrustumCheck = true;
		renderDistanceWeight = 10D;
	}
	
	public EntityStreak(World par1World, EntityLivingBase ent) 
	{
		super(par1World);
		setSize(0.1F, 0.1F);
		parent = ent;
		setLocationAndAngles(parent.posX, parent.boundingBox.minY, parent.posZ, parent.rotationYaw, parent.rotationPitch);
		lastUpdate = par1World.getWorldTime();
		ignoreFrustumCheck = true;
		renderDistanceWeight = 10D;
	}
	
	@Override
	public void onUpdate()
	{
		ticksExisted++;
		
		if(parent == null || !parent.isEntityAlive() || parent.isChild())
		{
			setDead();
			return;
		}
		
		lastUpdate = worldObj.getWorldTime();
	}
	
	@Override
    public int getBrightnessForRender(float par1)
    {
		return 15728880;
    }
	
	@Override
	public void setDead()
	{
		super.setDead();
	}
	
	@Override
	public void entityInit() 
	{
	}
	
	@Override
    public boolean writeToNBTOptional(NBTTagCompound par1NBTTagCompound)//disable saving of the entity
    {
    	return false;
    }

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound) 
	{
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound) 
	{
	}

}
