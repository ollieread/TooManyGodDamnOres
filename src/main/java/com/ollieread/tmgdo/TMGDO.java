package com.ollieread.tmgdo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = "TooManyGodDamnOres", name = "TooManyGodDamnOres", version = "${version}")
public class TMGDO {

	public static final Logger logger = LogManager.getLogger("TooManyGodDamnOres");

	public static ConfigCategory replacements;
	public static ConfigCategory exclusions;
	
	public static ConfigCategory settings;
	public static boolean dumpOreDictOnLoad = false;
	public static Configuration config;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		config = new Configuration(event.getSuggestedConfigurationFile());

		config.load();

		replacements = config.getCategory("replacements");
		exclusions = config.getCategory("exclusions");
				
		settings = config.getCategory("settings");
		
		Property dumpOreDict = config.get("settings", "dumpOreDictOnLoad", false);
		
		if (dumpOreDict.getBoolean()) {
			dumpOreDictOnLoad = true;
			dumpOreDict.set(false);
		}

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
		if(replacements != null && replacements.size() > 0) {
			if (event.getState().getBlock() != null) {			
				if (event.getDrops() != null && event.getDrops().size() > 0) {
					for (ItemStack s : event.getDrops() ) {
						if (s.getItem() instanceof ItemBlock) {
							ItemStack oreStack = s;
							if(oreStack.getItem() != null) {
								int[] oreIDs = OreDictionary.getOreIDs(oreStack);
								int oreID;

								if(oreIDs.length > 0) {
									for(int i = 0; i < oreIDs.length; i++) {
										oreID = oreIDs[i];

										if(oreID > -1) {
											String oreName = OreDictionary.getOreName(oreID);

											if(oreName != null && replacements.containsKey(oreName)) {
												String replacementName = replacements.get(oreName).getString();
												Property exclusionDirty = exclusions.get(oreName);
												List<String> exclusionNames = null;

												if(exclusionDirty != null) {
													exclusionNames = Arrays.asList(exclusionDirty.getStringList());
												} else {
													exclusionNames = new ArrayList<String>();
												}

												ResourceLocation ore = getNameForObject(oreStack.getItem());

												if(ore == null) {
													return;
												}

												if (ore.getResourceDomain().toLowerCase().equals(replacementName.toLowerCase())) {
													return;
												}

												if(exclusionNames.contains(ore.getResourceDomain() + ":" + ore.getResourcePath())) {
													return;
												}

												ItemStack replacementStack = null;

												for(ItemStack i1 : OreDictionary.getOres(oreName)) {
													ResourceLocation item = getNameForObject(i1.getItem());
													if(item != null && !exclusionNames.contains(item.getResourceDomain() + ":" + item.getResourcePath()) && item.getResourceDomain().toLowerCase().equals(replacementName.toLowerCase())) {
														replacementStack = i1.copy();
														break;
													}
												}

												if(replacementStack != null) {	
													event.getDrops().add(replacementStack);
													event.getDrops().remove(s);
												}
											}
										}
									}
								}
							} else {
								logger.warn("A HarvestDropsEvent was fired with no subject block");
							}
						}
					}
				}
			}
		}
	}

	/**
	 * 
	 * Utility method to use the Block/Item registries to get the modid and name
	 * 
	 * @param item - Either an item or itemblock
	 * @return ResourceLocation containing the modid and name
	 */
	public ResourceLocation getNameForObject(Item item) {		

		if (item instanceof ItemBlock) {
			return Block.REGISTRY.getNameForObject(((ItemBlock) item).getBlock());
		} else if (item instanceof Item) {
			return Item.REGISTRY.getNameForObject(item);
		}		

		return null;		
	}
	
	@EventHandler
	public void postinit(FMLPostInitializationEvent event)
	{
		if (dumpOreDictOnLoad) {
			
			ArrayList<String> lines = new ArrayList<String>();
			String[] names = OreDictionary.getOreNames();
			
			for (String name : names) {
				lines.add(name);
				String tab = "  ";
				
				List<ItemStack> ores = OreDictionary.getOres(name);
				
				for (ItemStack ore : ores) {
					ResourceLocation resLoc = getNameForObject(ore.getItem());
					lines.add(tab + resLoc.getResourceDomain() + ":" + resLoc.getResourcePath() );
				}
				
				lines.add("" + System.lineSeparator());
			}
						
			Path out = Paths.get(config.getConfigFile().getParentFile().getParentFile() + File.separator + "TMGDO_OreDictDump.txt");
			try {
				Files.write(out, lines, Charset.defaultCharset());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}