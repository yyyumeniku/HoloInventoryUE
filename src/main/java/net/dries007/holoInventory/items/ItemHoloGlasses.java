/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 - 2017 Dries K. Aka Dries007
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.dries007.holoInventory.items;

import java.util.List;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import baubles.api.cap.IBaublesItemHandler;
import net.dries007.holoInventory.HoloInventory;
import net.dries007.holoInventory.api.IHoloGlasses;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * HoloGlasses item that allows players to see holographic inventory displays.
 * Works as both armor (helmet slot) and as a Baubles accessory.
 */
@Optional.Interface(iface = "baubles.api.IBauble", modid = "baubles")
public class ItemHoloGlasses extends ItemArmor implements IHoloGlasses, IBauble
{
    // Custom armor material for Holo Glasses
    private static final ArmorMaterial HOLO_MATERIAL = EnumHelper.addArmorMaterial(
        "HOLO", 
        HoloInventory.MODID + ":hologlasses", 
        15, // durability multiplier
        new int[]{0, 0, 0, 1}, // damage reduction amounts (boots, legs, chest, helmet)
        25, // enchantability
        null, // sound event - using null for generic sound
        0.0F // toughness
    );

    public ItemHoloGlasses()
    {
        super(HOLO_MATERIAL, 0, EntityEquipmentSlot.HEAD);
        this.setUnlocalizedName("Hologlasses");
        this.setRegistryName(HoloInventory.MODID, "hologlasses");
        this.setCreativeTab(CreativeTabs.TOOLS);
        this.setMaxStackSize(1);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type)
    {
        return HoloInventory.MODID + ":textures/models/armor/hologlasses.png";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        tooltip.add(TextFormatting.GOLD + I18n.format("item.Hologlasses.tooltip"));
    }

    @Override
    public boolean shouldRender(ItemStack stack)
    {
        return true;
    }

    /**
     * Searches for HoloGlasses in player's equipment
     * @param player The player to check
     * @return ItemStack of HoloGlasses if found, null otherwise
     */
    public static ItemStack getHoloGlasses(EntityPlayer player)
    {
        // Check helmet slot first
        ItemStack helmet = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        if (!helmet.isEmpty() && helmet.getItem() instanceof IHoloGlasses)
        {
            return helmet;
        }

        // Check Baubles if mod is loaded
        if (Loader.isModLoaded("baubles"))
        {
            return getHoloGlassesFromBaubles(player);
        }

        return ItemStack.EMPTY;
    }

    @Optional.Method(modid = "baubles")
    private static ItemStack getHoloGlassesFromBaubles(EntityPlayer player)
    {
        IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
        for (int i = 0; i < baubles.getSlots(); i++)
        {
            ItemStack stack = baubles.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof IHoloGlasses)
            {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    // IBauble implementation for Baubles compatibility
    @Override
    @Optional.Method(modid = "baubles")
    public BaubleType getBaubleType(ItemStack itemstack)
    {
        return BaubleType.HEAD;
    }

    @Override
    @Optional.Method(modid = "baubles")
    public void onWornTick(ItemStack itemstack, EntityLivingBase player)
    {
        // No special tick behavior needed
    }

    @Override
    @Optional.Method(modid = "baubles")
    public void onEquipped(ItemStack itemstack, EntityLivingBase player)
    {
        // No special equip behavior needed
    }

    @Override
    @Optional.Method(modid = "baubles")
    public void onUnequipped(ItemStack itemstack, EntityLivingBase player)
    {
        // No special unequip behavior needed
    }

    @Override
    @Optional.Method(modid = "baubles")
    public boolean canEquip(ItemStack itemstack, EntityLivingBase player)
    {
        return true;
    }

    @Override
    @Optional.Method(modid = "baubles")
    public boolean canUnequip(ItemStack itemstack, EntityLivingBase player)
    {
        return true;
    }
}