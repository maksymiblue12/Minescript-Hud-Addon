package net.mb.minescripthud;

import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.util.Identifier;
import net.minescript.common.Minescript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MinescriptHUDAddon implements ClientModInitializer {
	public static final String MOD_ID="minescripthud";
	public static final Logger LOGGER=LoggerFactory.getLogger(MOD_ID);
	public static boolean silent=false;

	@Override
	public void onInitializeClient(){
		HudElementRegistry.addLast(Identifier.of(MOD_ID,"text_drawer"),DrawHelper.getInstance()::draw);
		if (!getCurrentVersion().equals(getLastRunVersion())) {
			LOGGER.info("Updating files!");
			deleteMinescriptFile(Paths.get("minescript", "system"), "version.txt");
			deleteMinescriptFile(Paths.get("minescript", "system", "lib"), "hud_renderer.py");
			deleteMinescriptFile(Paths.get("minescript", "system", "lib"), "draw_text.py");
			copyJarResourceToFile("hud_version.txt", Paths.get("minescript", "system"));
			copyJarResourceToFile("system/lib/hud_renderer.py",Paths.get("minescript", "system", "lib"));
			copyJarResourceToFile("system/exec/clear_screen.py",Paths.get("minescript", "system", "exec"));
		}
	}

	private static void deleteMinescriptFile(Path dir, String fileName) {
		File fileToDelete = new File(dir.resolve(fileName).toString());
		if (fileToDelete.exists() && fileToDelete.delete()) {
			LOGGER.info("Deleted obsolete file: `{}`", fileToDelete.getPath());
		}

	}

	private static void copyJarResourceToFile(String resourceName, Path dir) {
		String fileName = resourceName.substring(resourceName.lastIndexOf(47) + 1);
		Path filePath = dir.resolve(fileName);
		if (Files.exists(filePath, new LinkOption[0])) {
			try {
				Files.delete(filePath);
			} catch (IOException var15) {
				MinescriptHUDAddon.LOGGER.error("Failed to delete file to be overwritten: {}", filePath);
				return;
			}

			MinescriptHUDAddon.LOGGER.info("Deleted outdated file: {}", filePath);
		}


		try {
			InputStream in=MinescriptHUDAddon.class.getResourceAsStream("/" + resourceName);

			try {
				//noinspection DataFlowIssue
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));

				try {
					FileWriter writer = new FileWriter(filePath.toString());

					try {
						reader.transferTo(writer);
						MinescriptHUDAddon.LOGGER.info("Copied jar resource \"{}\" to \"{}\"", resourceName, filePath);
					} catch (Throwable var13) {
						try {
							writer.close();
						} catch (Throwable var12) {
							var13.addSuppressed(var12);
						}

						throw var13;
					}

					writer.close();
				} catch (Throwable var14) {
					try {
						reader.close();
					} catch (Throwable var11) {
						var14.addSuppressed(var11);
					}

					throw var14;
				}

				reader.close();
			} catch (Throwable var16) {
				if (in != null) {
					try {
						in.close();
					} catch (Throwable var10) {
						var16.addSuppressed(var10);
					}
				}

				throw var16;
			}

			//noinspection ConstantValue
			if (in != null) {
				in.close();
			}
		} catch (IOException ignored) {
			MinescriptHUDAddon.LOGGER.error("Failed to copy jar resource \"{}\" to \"{}\"", resourceName, filePath);
		}
	}

	private static String getLastRunVersion() {
		Path versionPath = Paths.get("minescript", "system", "hud_version.txt");
		if (!Files.exists(versionPath, new LinkOption[0])) {
			return "";
		} else {
			try {
				return Files.readString(versionPath).strip();
			} catch (IOException var3) {
				LOGGER.error("Exception loading version file: {}", var3.toString());
				return "";
			}
		}
	}

	private static String getCurrentVersion() {
		try {
			InputStream in = MinescriptHUDAddon.class.getResourceAsStream("/hud_version.txt");

			String var2;
			try {
				//noinspection DataFlowIssue
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));

				try {
					var2 = reader.readLine().strip();
				} catch (Throwable var6) {
					try {
						reader.close();
					} catch (Throwable var5) {
						var6.addSuppressed(var5);
					}

					throw var6;
				}

				reader.close();
			} catch (Throwable var7) {
				if (in != null) {
					try {
						in.close();
					} catch (Throwable var4) {
						var7.addSuppressed(var4);
					}
				}

				throw var7;
			}

			//noinspection ConstantValue
			if (in != null) {
				in.close();
			}

			return var2;
		} catch (IOException var8) {
			LOGGER.error("Exception loading version resource: {}", var8.toString());
			return "";
		}
	}
}