package net.mb.minescripthud;

import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinescriptHUDAddon implements ClientModInitializer {
	public static final String MOD_ID="minescripthud";
	public static final Logger LOGGER=LoggerFactory.getLogger(MOD_ID);
	public static boolean silent=false;

	@Override
	public void onInitializeClient(){
		HudElementRegistry.addLast(Identifier.of(MOD_ID,"text_drawer"),DrawHelper.getInstance()::draw);
	}
}