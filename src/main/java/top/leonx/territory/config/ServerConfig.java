package top.leonx.territory.config;

import net.minecraftforge.common.ForgeConfigSpec;
import top.leonx.territory.TerritoryMod;

import java.util.ArrayList;
import java.util.List;

final class ServerConfig {

	final ForgeConfigSpec.DoubleValue               expNeededPerChunk;
	final ForgeConfigSpec.ConfigValue<List<String>> powerProvider;
	final ForgeConfigSpec.BooleanValue              preventFire;
	final ForgeConfigSpec.BooleanValue              preventExplosion;
	ServerConfig(final ForgeConfigSpec.Builder builder) {
		builder.push("general");
		expNeededPerChunk = builder
				.comment("This value determines how much experience is required to add a chunk. Default Value: 0.5")
				.translation(TerritoryMod.MODID + ".config.expNeededPerChunk")
				.defineInRange("expNeededPerChunk", 0.5d, 0, Double.MAX_VALUE);
		powerProvider = builder
				.comment("This value determines which blocks can provide protect power")
				.translation(TerritoryMod.MODID + ".config.powerProvider")
				.define("powerProvider", new ArrayList<>());

		builder.pop();
		builder.push("default protect");
		preventFire = builder
				.comment("If set to true, no fire will appear in the territory. Except above Obsidian and NetherRack")
				.translation(TerritoryMod.MODID + ".config.preventFire")
				.define("preventFire",true);
		preventExplosion = builder
				.comment("If set to true, prevent explosion damage to blocks.")
				.translation(TerritoryMod.MODID + ".config.preventExplosion")
				.define("preventExplosion",true);
		builder.pop();
	}

}
