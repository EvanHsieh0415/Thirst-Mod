package dev.ghen.thirst.common.damagesource;

import net.minecraft.world.damagesource.DamageSource;

public class ModDamageSource
{
    public static final DamageSource DEHYDRATE = (new DamageSource("dehydrate")).bypassArmor().bypassMagic();
}
