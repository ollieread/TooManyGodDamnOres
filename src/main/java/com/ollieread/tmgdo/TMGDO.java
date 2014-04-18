package com.ollieread.tmgdo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = "TooManyGodDamnOres", name = "TooManyGodDamnOres", version = "${version}")
public class TMGDO {

	public static ConfigCategory replacements;
	public static ConfigCategory exclusions;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		
		config.load();
		
		replacements = config.getCategory("replacements");
		exclusions = config.getCategory("exclusions");
		
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
			ItemStack oreStack = new ItemStack(event.block, 1, event.blockMetadata);
			int oreID = OreDictionary.getOreID(oreStack);
			String oreName = OreDictionary.getOreName(oreID);
			
			if(replacements.containsKey(oreName)) {
				String replacementName = replacements.get(oreName).getString();
				List<String> exclusionNames = Arrays.asList(exclusions.get(oreName).getStringList());
				UniqueIdentifier ore = GameRegistry.findUniqueIdentifierFor(oreStack.getItem());
				
				if(exclusionNames.contains(ore.modId + ":" + ore.name)) {
					return;
				}
				
				ItemStack replacementStack = null;
				
				for(ItemStack i : OreDictionary.getOres(oreID)) {
					UniqueIdentifier item = GameRegistry.findUniqueIdentifierFor(i.getItem());
					if(!exclusionNames.contains(item.modId + ":" + item.name) && item.modId.contains(replacementName)) {
						replacementStack = i.copy();
						break;
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