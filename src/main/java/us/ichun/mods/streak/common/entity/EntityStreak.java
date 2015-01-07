package us.ichun.mods.streak.common.entity;

import us.ichun.mods.streak.common.Streak;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityStreak extends Entity 
{

	public EntityLivingBase parent;
	
	public long lastUpdate;

	public int flavour;
	
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
		setLocationAndAngles(parent.posX, parent.getEntityBoundingBox().minY, parent.posZ, parent.rotationYaw, parent.rotationPitch);
		lastUpdate = par1World.getWorldTime();
		ignoreFrustumCheck = true;
		renderDistanceWeight = 10D;
		
		flavour = ent.getRNG().nextInt(Streak.flavours.size());
	}
	
	@Override
	public void onUpdate()
	{
		ticksExisted++;
		
		if(parent == null || !parent.isEntityAlive() || parent.isChild() || Streak.tickHandlerClient.streaks.get(parent.getName()) != this)
		{
			setDead();
			return;
		}
		
		lastUpdate = worldObj.getWorldTime();
	}
	
	@Override
    public boolean shouldRenderInPass(int pass)
    {
        return pass == 1;
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
