package com.ollieread.tmgdo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import net.minecraft.block.Block;
import net.minecraft.block.BlockOre;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = TMGDO.MODID, name = TMGDO.MODID, version = TMGDO.VERSION)
public class TMGDO {

	public static final String MODID = "TooManyGodDamnOres";
	public static final String VERSION = "1.0-alpha";
	public static ConfigCategory replacements;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		
		config.load();
		
		replacements = config.getCategory(Configuration.CATEGORY_GENERAL);
		
		config.save();
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void harvestBlock(HarvestDropsEvent event)
	{
		if(replacements != null) {
			ItemStack oreStack = new ItemStack(event.block);
			int oreID = OreDictionary.getOreID(oreStack);
			String oreName = OreDictionary.getOreName(oreID);
			
			if(replacements.containsKey(oreName)) {
				String replacementName = replacements.get(oreName).getString();
				ItemStack replacementStack = null;
				
				for(ItemStack i : OreDictionary.getOres(oreID)) {
					if(GameRegistry.findUniqueIdentifierFor(i.getItem()).modId.contains(replacementName)) {
						replacementStack = i.copy();
					}
				}
				
				if(replacementStack != null) {	
					for(int i = 0; i < event.drops.size(); i++) {
						if(event.drops.get(i).isItemEqual(oreStack)) {
							event.drops.set(i, replacementStack);
						}
					}
				}
			}
		}
	}
	
}