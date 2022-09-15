package dev.ghen.thirst.mixin;

import com.mojang.logging.LogUtils;
import dev.ghen.thirst.common.event.WaterPurity;
import dev.ghen.thirst.util.MathHelper;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.FurnaceScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BottleItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BucketItem.class)
public class MixinBucketItem
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private boolean shouldModify;
    private int purity;

    @Inject(method = "use", at = @At("HEAD"))
    public void setPurity(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir)
    {
        BlockPos blockPos = MathHelper.getPlayerPOVHitResult(player.getLevel(), player, ClipContext.Fluid.SOURCE_ONLY).getBlockPos();

        shouldModify = level.getFluidState(blockPos).is(FluidTags.WATER) && level.getFluidState(blockPos).isSource();
        if(shouldModify)
            purity = WaterPurity.getWaterPurity(level, blockPos);
    }

    @ModifyArg(method = "use", index = 2, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemUtils;createFilledResult(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack addPurity(ItemStack result)
    {
        if(shouldModify)
        {
            CompoundTag tag = result.getOrCreateTag();
            tag.putInt("Purity", purity);
        }

        return result;
    }
}