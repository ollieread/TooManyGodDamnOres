TooManyGodDamnOres
==================

Utility mod for minecraft to help enforce the ore dictionary, so you don't have hundreds of types of the same ore.

Hooks into the `HarvestDropsEvent` which is fired when a block is harvested. It'll replace the dropped block with 
that of the mod specified in the configuration. 

    # Configuration file

    exclusions {
        S:oreCopper <
            TConstruct:GravelOre
        >
        S:oreTin <
            TConstruct:GravelOre
        >
    }	
	
    replacements {
        S:oreCopper=IC2
        S:oreTin=IC2
    }
    
This basically makes it so that when a block is mined belong to the ore dictionary of either of those entries, it'll 
replace it with the block for the same ore dictionary entry belonging to the mod with modid specified, so IC2 (IndustrialCraft 2).

A better explanation and link to download is available on the [minecraft forums post](http://www.minecraftforum.net/topic/2582075-172forge-toomanygoddamnores-ore-dictionary-enforcement/).
