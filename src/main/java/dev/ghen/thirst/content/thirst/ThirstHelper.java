package dev.ghen.thirst.content.thirst;

import com.mojang.logging.LogUtils;
import dev.ghen.thirst.foundation.config.CommonConfig;
import dev.ghen.thirst.foundation.config.ItemSettingsConfig;
import dev.ghen.thirst.foundation.util.ConfigHelper;
import dev.ghen.thirst.foundation.util.LoadedValue;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.api.util.TempHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.slf4j.Logger;

import java.util.Map;

import static dev.ghen.thirst.content.purity.WaterPurity.hasPurity;

public class ThirstHelper
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean useColdSweatCaps = false;
    private static final float MODIFIER_HARSHNESS = 0.5f;
    public static LoadedValue<Map<Item, Number[]>> VALID_DRINKS = LoadedValue.of(() -> ConfigHelper.getItemsWithValues(ItemSettingsConfig.getInstance().getDrinks()));
    public static LoadedValue<Map<Item, Number[]>> VALID_FOODS = LoadedValue.of(() -> ConfigHelper.getItemsWithValues(ItemSettingsConfig.getInstance().getFoods()));

    public static boolean itemRestoresThirst(ItemStack itemStack)
    {
        return  VALID_DRINKS.get().containsKey(itemStack.getItem()) ||
                VALID_FOODS.get().containsKey(itemStack.getItem());
    }

    public static boolean isDrink(ItemStack itemStack)
    {
        return  VALID_DRINKS.get().containsKey(itemStack.getItem());
    }

    public static boolean isFood(ItemStack itemStack)
    {
        return  VALID_FOODS.get().containsKey(itemStack.getItem());
    }

    public static int getThirst(ItemStack itemStack)
    {
        Item item = itemStack.getItem();

        if(VALID_DRINKS.get().containsKey(item))
            return VALID_DRINKS.get().get(item)[0].intValue();
        else
            return VALID_FOODS.get().get(item)[0].intValue();
    }

    public static int getQuenched(ItemStack itemStack)
    {
        Item item = itemStack.getItem();

        if(VALID_DRINKS.get().containsKey(item))
            return VALID_DRINKS.get().get(item)[1].intValue();
        else
            return VALID_FOODS.get().get(item)[1].intValue();
    }

    public static int getPurity(ItemStack item)
    {
        if(!hasPurity(item))
            return -1;
        else
            return item.getTag().getInt("Purity");
    }

    public static void shouldUseColdSweatCaps(boolean should)
    {
        useColdSweatCaps = should;
    }

    /**
     * Calculates the thirst depletion speed modifier based on the player's
     * temperature and humidity. If the mod "Cold Sweat" is present, the temperature used is
     * the one calculated from the mod, otherwise both parameters are entirely
     * dependent on the biome the player is standing in.
     */
    public static float getExhaustionBiomeModifier(Player player)
    {
        BlockPos pos = player.getOnPos();
        Level level = player.getLevel();

        if(level.dimensionType().ultraWarm())
            return 3.0f;
        else
        {
            Biome biome = level.getBiome(pos).value();

            //humidity range: 0 - 0.8 == 0.8 midpoint: 0.4
            float humidity = biome.getDownfall() + 0.6f;
            if(humidity <= 0.6)
                humidity += 0.5;

            //temperature range: -0.8 - 2 == 2.8 midpoint: 0.8
            float temp = biome.getBaseTemperature() + 0.2f;

            if(useColdSweatCaps)
                temp = (float) (TempHelper.getTemperature(player, Temperature.Type.BODY).get() / 100f);
            else
            {
                if(temp <= 0)
                    temp = (float) Math.exp(temp);
                else if(temp > 1)
                    temp /= 2;
            }

            float thirstModifier = CommonConfig.getInstance().getThirstDepletionModifier() * (temp  / humidity);

            if(thirstModifier < 1)
            {
                float modifierOffset = 1 - thirstModifier;
                modifierOffset *= MODIFIER_HARSHNESS;
                thirstModifier = 1 - modifierOffset;
            }

            return thirstModifier;
        }
    }
}
