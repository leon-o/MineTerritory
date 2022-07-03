package top.leonx.territory.config;

final class ServerConfig {

	/*final ForgeConfigSpec.DoubleValue               expNeededPerChunk;
	final ForgeConfigSpec.ConfigValue<List<String>> powerProvider;
	final ForgeConfigSpec.ConfigValue<List<String>> permissionWhiteList;
	final ForgeConfigSpec.BooleanValue				addToDefault;
	final ForgeConfigSpec.ConfigValue<List<String>> defaultPermission;
	final ForgeConfigSpec.BooleanValue              preventFire;
	final ForgeConfigSpec.BooleanValue              preventExplosion;
	ServerConfig(final ForgeConfigSpec.Builder builder) {
		builder.push("general");
		expNeededPerChunk = builder
				.comment("This value determines how much experience is required to add a chunk. Default Value: 0.5")
				.translation(TerritoryMod.MODID + ".config.expNeededPerChunk")
				.defineInRange("expNeededPerChunk", 0.5d, 0, Double.MAX_VALUE);
		List<String> providerDefault=new ArrayList<>();
		providerDefault.add("tag:minecraft:banners;1");
		providerDefault.add("item:minecraft:yellow_banner{display:{Name:'{\"text\":\"template_territory\"}'}};3");
		powerProvider = builder
				.comment("This value determines which blocks can provide protect power.\n Template: \"item|tag:id of item or tag{nbt};[power]\"")
				.translation(TerritoryMod.MODID + ".config.powerProvider")
				.define("powerProvider", providerDefault);
		StringBuilder allPermissionsStr = new StringBuilder();
		List<String> defaultPermissionWhiteList=new ArrayList<>();
		for (PermissionFlag flag : PermissionFlag.basicFlag) {
			allPermissionsStr.append(flag.getNameKey()).append(" ");
			defaultPermissionWhiteList.add(flag.getNameKey());
		}
		permissionWhiteList =builder
				.comment("Only show these permissions in territory table gui.",
						 "Available value:"+ allPermissionsStr)
				.translation(TerritoryMod.MODID + ".config.permissionBlackList")
				.define("permissionBlackList",defaultPermissionWhiteList);

		addToDefault=builder
				.comment("Those permissions that are not in the whitelist will be added to the default permissions list.")
				.translation(TerritoryMod.MODID + ".config.addToDefault")
				.define("addToDefault",true);

		List<String> defaultPermissionList=new ArrayList<>();
		defaultPermissionList.add(PermissionFlag.ENTER.getNameKey());
		defaultPermission =builder
				.comment("These permissions will default allow when players add territory, unless players change them manually.",
						 "Available value:"+ allPermissionsStr)
				.translation(TerritoryMod.MODID + ".config.defaultPermission")
				.define("defaultPermission",defaultPermissionList);
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
	}*/

}
