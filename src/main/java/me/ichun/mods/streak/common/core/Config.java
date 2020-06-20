package me.ichun.mods.streak.common.core;

import me.ichun.mods.ichunutil.common.config.ConfigBase;
import me.ichun.mods.ichunutil.common.config.annotations.CategoryDivider;
import me.ichun.mods.ichunutil.common.config.annotations.Prop;
import me.ichun.mods.streak.common.Streak;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraftforge.fml.config.ModConfig;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Config extends ConfigBase
{
    @CategoryDivider(name = "clientOnly")
    @Prop(min = 5)
    public int streakTime = 100;

    @Prop(min = 0, max = 100)
    public int streakOpacity = 100;

    @Prop(min = 0)
    public int sprintTrail = 6;

    @Prop
    public boolean renderInFirstPerson = false;

    @Prop(validator = "attachValidator")
    public List<String> attachTo = new ArrayList<String>(){{
        add("minecraft:player");
    }};

    @Prop(validator = "flavorValidator")
    public List<String> setFlavors = new ArrayList<>(); //we're using "flavor" because ehmerican Enghlessh

    public HashSet<ResourceLocation> attachments = new HashSet<>();
    public HashMap<String, String> setFlavourMap = new HashMap<>();

    public boolean attachValidator(Object o)
    {
        if(o instanceof String)
        {
            try
            {
                new ResourceLocation(((String)o));
                return true;
            }
            catch(ResourceLocationException ignored){}
        }
        return false;
    }

    public boolean flavorValidator(Object o)
    {
        return o instanceof String && ((String)o).split(":").length == 2;
    }

    @Nonnull
    @Override
    public String getModId()
    {
        return Streak.MOD_ID;
    }

    @Nonnull
    @Override
    public String getConfigName()
    {
        return Streak.MOD_NAME;
    }

    @Nonnull
    @Override
    public ModConfig.Type getConfigType()
    {
        return ModConfig.Type.CLIENT;
    }

    @Override
    public void onConfigLoaded()
    {
        Minecraft.getInstance().execute(this::parseConfig);
    }

    public void parseConfig()
    {
        attachments.clear();
        for(String s : attachTo)
        {
            attachments.add(new ResourceLocation(s));
        }

        setFlavourMap.clear();
        for(String s : setFlavors)
        {
            String[] split = s.split(":");
            setFlavourMap.put(split[0], split[1].toLowerCase());
        }
    }

}
