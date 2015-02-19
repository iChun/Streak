package us.ichun.mods.streak.common.core;

import us.ichun.mods.ichunutil.common.core.config.ConfigBase;
import us.ichun.mods.ichunutil.common.core.config.annotations.ConfigProp;
import us.ichun.mods.ichunutil.common.core.config.annotations.IntBool;
import us.ichun.mods.ichunutil.common.core.config.annotations.IntMinMax;

import java.io.File;

public class Config extends ConfigBase
{
    @ConfigProp(category = "basics")
    @IntMinMax(min = 5)
    public int streakTime = 100;

    @ConfigProp(category = "basics")
    @IntBool
    public int playersFollowYourFavouriteFlavour = 0;

    @ConfigProp(category = "basics")
    @IntBool
    public int sprintTrail = 1;

    @ConfigProp(category = "basics")
    public String favouriteFlavour = "";

    public Config(File file, String... unhide)
    {
        super(file, unhide);
    }

    @Override
    public String getModId()
    {
        return "streak";
    }

    @Override
    public String getModName()
    {
        return "Streak";
    }
}
