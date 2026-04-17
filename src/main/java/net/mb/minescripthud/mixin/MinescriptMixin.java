package net.mb.minescripthud.mixin;

import net.mb.minescripthud.DrawHelper;
import net.mb.minescripthud.MinescriptHUDAddon;
import net.mb.minescripthud.ScriptFrameWaiter;
import net.minecraft.client.MinecraftClient;
import net.minescript.common.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Mixin(Minescript.class)
public class MinescriptMixin {
    @Inject(method = "runScriptFunction",at=@At("HEAD"),cancellable = true)
	@SuppressWarnings("unchecked")
    private static void runScriptFunction(JobControl job, long funcCallId, ScriptFunctionCall functionCall, CallbackInfoReturnable<ScriptValue> cir) {
        String name = functionCall.name();
        ScriptFunctionCall.ArgList args = functionCall.args();
		switch (name) {
			case "add_text" -> {
				args.expectSize(7);
				int id=DrawHelper.getInstance().addText(args.getString(0), args.getStrictInt(1), args.getStrictInt(2), args.getStrictInt(3), args.getBoolean(4), args.getDouble(5),args.getStrictInt(6));
				cir.setReturnValue(ScriptValue.of(id));
				cir.cancel();
			}
			case "add_advanced_text" -> {
				args.expectSize(12);
				int id=DrawHelper.getInstance().addAdvancedText(args.getString(0), args.getStrictInt(1), args.getStrictInt(2), args.getStrictInt(3), args.getBoolean(4), args.getDouble(5),args.getStrictInt(6),args.getDouble(7),args.getDouble(8),args.getDouble(9),args.getDouble(10),args.getDouble(11));
				cir.setReturnValue(ScriptValue.of(id));
				cir.cancel();
			}
			case "get_text_object" -> {
				args.expectSize(1);
				cir.setReturnValue(ScriptValue.of(DrawHelper.getInstance().getTextObject(args.getStrictInt(0))));
				cir.cancel();
			}




			case "add_rectangle" -> {
				args.expectSize(7);
				int id=DrawHelper.getInstance().addRectangle(args.getStrictInt(0),args.getStrictInt(1),args.getStrictInt(2),args.getStrictInt(3),args.getStrictInt(4),args.getDouble(5),args.getStrictInt(6));
				cir.setReturnValue(ScriptValue.of(id));
				cir.cancel();
			}
			case "get_rectangle_object" -> {
				args.expectSize(1);
				cir.setReturnValue(ScriptValue.of(DrawHelper.getInstance().getRectangleObject(args.getStrictInt(0))));
				cir.cancel();
			}
			case "add_gradient_rectangle" -> {
				args.expectSize(8);
				int id=DrawHelper.getInstance().addGradientRectangle(args.getStrictInt(0),args.getStrictInt(1),args.getStrictInt(2),args.getStrictInt(3),args.getStrictInt(4),args.getStrictInt(5),args.getDouble(6),args.getStrictInt(7));
				cir.setReturnValue(ScriptValue.of(id));
				cir.cancel();
			}
			case "get_gradient_rectangle_object" -> {
				args.expectSize(1);
				cir.setReturnValue(ScriptValue.of(DrawHelper.getInstance().getGradientRectangleObject(args.getStrictInt(0))));
				cir.cancel();
			}
			case "add_stroked_rectangle" -> {
				args.expectSize(7);
				int id=DrawHelper.getInstance().addStrokedRectangle(args.getStrictInt(0),args.getStrictInt(1),args.getStrictInt(2),args.getStrictInt(3),args.getStrictInt(4),args.getDouble(5),args.getStrictInt(6));
				cir.setReturnValue(ScriptValue.of(id));
				cir.cancel();
			}
			case "get_stroked_rectangle_object" -> {
				args.expectSize(1);
				cir.setReturnValue(ScriptValue.of(DrawHelper.getInstance().getStrokedRectangleObject(args.getStrictInt(0))));
				cir.cancel();
			}





			case "add_text_with_background" -> {
				args.expectSize(10);
				int id=DrawHelper.getInstance().addTextWithBackground(args.getString(0), args.getStrictInt(1), args.getStrictInt(2), args.getStrictInt(3), args.getStrictInt(4), args.getStrictInt(5), args.getStrictInt(6), args.getBoolean(7), args.getDouble(8), args.getStrictInt(9));
				cir.setReturnValue(ScriptValue.of(id));
				cir.cancel();
			}
			case "add_advanced_text_with_background" -> {
				args.expectSize(15);
				int id=DrawHelper.getInstance().addAdvancedTextWithBackground(args.getString(0), args.getStrictInt(1), args.getStrictInt(2), args.getStrictInt(3), args.getStrictInt(4), args.getStrictInt(5), args.getStrictInt(6), args.getBoolean(7), args.getDouble(8), args.getStrictInt(9), args.getDouble(10), args.getDouble(11), args.getDouble(12), args.getDouble(13), args.getDouble(14));
				cir.setReturnValue(ScriptValue.of(id));
				cir.cancel();
			}
			case "get_text_with_background_object" -> {
				args.expectSize(1);
				cir.setReturnValue(ScriptValue.of(DrawHelper.getInstance().getTextWithBackgroundObject(args.getStrictInt(0))));
				cir.cancel();
			}





			case "add_item" -> {
				args.expectSize(5);
				int id=DrawHelper.getInstance().addItem(args.getString(0), args.getStrictInt(1), args.getStrictInt(2), args.getDouble(3), args.getStrictInt(4));
				cir.setReturnValue(ScriptValue.of(id));
				cir.cancel();
			}
			case "add_advanced_item" -> {
				args.expectSize(10);
				int id=DrawHelper.getInstance().addAdvancedItem(args.getString(0), args.getStrictInt(1), args.getStrictInt(2), args.getDouble(3), args.getStrictInt(4), args.getDouble(5), args.getDouble(6), args.getDouble(7), args.getDouble(8), args.getDouble(9));
				cir.setReturnValue(ScriptValue.of(id));
				cir.cancel();
			}
			case "get_item_object" -> {
				args.expectSize(1);
				cir.setReturnValue(ScriptValue.of(DrawHelper.getInstance().getItemObject(args.getStrictInt(0))));
				cir.cancel();
			}





			case "add_texture" -> {
				args.expectSize(9);
				int id=DrawHelper.getInstance().addTexture(args.getString(0), args.getBoolean(1), args.getStrictInt(2), args.getStrictInt(3), args.getStrictInt(4), args.getStrictInt(5), args.getDouble(6), args.getDouble(7), args.getStrictInt(8));
				cir.setReturnValue(ScriptValue.of(id));
				cir.cancel();
			}
			case "add_advanced_texture" -> {
				args.expectSize(14);
				int id=DrawHelper.getInstance().addAdvancedTexture(args.getString(0), args.getBoolean(1), args.getStrictInt(2), args.getStrictInt(3), args.getStrictInt(4), args.getStrictInt(5), args.getDouble(6), args.getDouble(7), args.getStrictInt(8), args.getDouble(9), args.getDouble(10), args.getDouble(11), args.getDouble(12), args.getDouble(13));
				cir.setReturnValue(ScriptValue.of(id));
				cir.cancel();
			}
			case "get_texture_object" -> {
				args.expectSize(1);
				cir.setReturnValue(ScriptValue.of(DrawHelper.getInstance().getTextureObject(args.getStrictInt(0))));
				cir.cancel();
			}





			case "add_shape" -> {
				args.expectSize(3);
				int id=DrawHelper.getInstance().addShape((List<Map<String, Double>>)args.get(0), args.getDouble(1), args.getStrictInt(2));
				cir.setReturnValue(ScriptValue.of(id));
				cir.cancel();
			}
			case "add_advanced_shape" -> {
				args.expectSize(13);
				int id=DrawHelper.getInstance().addAdvancedShape((List<Map<String, Double>>)args.get(0), args.getDouble(1), args.getStrictInt(2), args.getDouble(3), args.getDouble(4), args.getDouble(5), args.getDouble(6), args.getDouble(7));
				cir.setReturnValue(ScriptValue.of(id));
				cir.cancel();
			}
			case "get_shape_object" -> {
				args.expectSize(1);
				cir.setReturnValue(ScriptValue.of(DrawHelper.getInstance().getShapeObject(args.getStrictInt(0))));
				cir.cancel();
			}





			case "get_screen_width" -> {
				cir.setReturnValue(ScriptValue.of(DrawHelper.getInstance().windowWidth));
				cir.cancel();
			}
			case "get_screen_height" -> {
				cir.setReturnValue(ScriptValue.of(DrawHelper.getInstance().windowHeight));
				cir.cancel();
			}
			case "get_still_existing" -> {
				cir.setReturnValue(ScriptValue.of(DrawHelper.getInstance().getStillExisting().toArray()));
				cir.cancel();
			}
			case "get_elements" -> {
				args.expectSize(1);
				cir.setReturnValue(ScriptValue.of(DrawHelper.getInstance().getElements((List<Double>)args.get(0))));
				cir.cancel();
			}
			case "still_exists" -> {
				args.expectSize(1);
				cir.setReturnValue(ScriptValue.of(DrawHelper.getInstance().stillExists(args.getStrictInt(0))));
				cir.cancel();
			}
			case "get_mouse" -> {
				cir.setReturnValue(ScriptValue.of(DrawHelper.getInstance().getMouse()));
				cir.cancel();
			}
			case "get_font_height" -> {
				cir.setReturnValue(ScriptValue.of(MinecraftClient.getInstance().textRenderer.fontHeight));
				cir.cancel();
			}
            case "wait_next_frame" -> {
				job.suspend();
				ScriptFrameWaiter.getInstance().waitNextFrame(funcCallId, job::resume);
                cir.setReturnValue(ScriptValue.TRUE);
                cir.cancel();
            }
		}
    }

	@Inject(method = "runNoReturnScriptFunction",at=@At("HEAD"),cancellable = true)
	@SuppressWarnings("unchecked")
	private static void runNoReturnScriptFunction(ScriptFunctionCall functionCall, CallbackInfoReturnable<Boolean> cir) {
		String functionName = functionCall.name();
		ScriptFunctionCall.ArgList args = functionCall.args();
		switch (functionName) {
			case "update_text" -> {
				args.expectSize(13);
				DrawHelper.getInstance().updateText(args.getStrictInt(0), args.getString(1), args.getStrictInt(2), args.getStrictInt(3), args.getStrictInt(4), args.getBoolean(5), args.getDouble(6),args.getStrictInt(7),args.getDouble(8),args.getDouble(9),args.getDouble(10),args.getDouble(11),args.getDouble(12));
				cir.setReturnValue(true);
				cir.cancel();
			}
			case "update_rectangle" -> {
				args.expectSize(8);
				DrawHelper.getInstance().updateRectangle(args.getStrictInt(0),args.getStrictInt(1),args.getStrictInt(2),args.getStrictInt(3),args.getStrictInt(4),args.getStrictInt(5),args.getDouble(6),args.getStrictInt(7));
				cir.setReturnValue(true);
				cir.cancel();
			}
			case "update_gradient_rectangle" -> {
				args.expectSize(9);
				DrawHelper.getInstance().updateGradientRectangle(args.getStrictInt(0),args.getStrictInt(1),args.getStrictInt(2),args.getStrictInt(3),args.getStrictInt(4),args.getStrictInt(5),args.getStrictInt(6),args.getDouble(7),args.getStrictInt(8));
				cir.setReturnValue(true);
				cir.cancel();
			}
			case "update_stroked_rectangle" -> {
				args.expectSize(9);
				DrawHelper.getInstance().updateStrokedRectangle(args.getStrictInt(0),args.getStrictInt(1),args.getStrictInt(2),args.getStrictInt(3),args.getStrictInt(4),args.getStrictInt(5),args.getDouble(6),args.getStrictInt(7));
				cir.setReturnValue(true);
				cir.cancel();
			}
			case "update_text_with_background" -> {
				args.expectSize(16);
				DrawHelper.getInstance().updateTextWithBackground(args.getStrictInt(0), args.getString(1), args.getStrictInt(2), args.getStrictInt(3), args.getStrictInt(4), args.getStrictInt(5), args.getStrictInt(6), args.getStrictInt(7), args.getBoolean(8), args.getDouble(9), args.getStrictInt(10), args.getDouble(11), args.getDouble(12), args.getDouble(13), args.getDouble(14), args.getDouble(15));
				cir.setReturnValue(true);
				cir.cancel();
			}
			case "update_item" -> {
				args.expectSize(11);
				DrawHelper.getInstance().updateItem(args.getStrictInt(0), args.getString(1), args.getStrictInt(2), args.getStrictInt(3), args.getDouble(4), args.getStrictInt(5), args.getDouble(6), args.getDouble(7), args.getDouble(8), args.getDouble(9), args.getDouble(10));
				cir.setReturnValue(true);
				cir.cancel();
			}
			case "update_texture" -> {
				args.expectSize(15);
				DrawHelper.getInstance().updateTexture(args.getStrictInt(0), args.getString(1), args.getBoolean(2), args.getStrictInt(3), args.getStrictInt(4), args.getStrictInt(5), args.getStrictInt(6), args.getDouble(7), args.getDouble(8), args.getStrictInt(9), args.getDouble(10), args.getDouble(11), args.getDouble(12), args.getDouble(13), args.getDouble(14));
				cir.setReturnValue(true);
				cir.cancel();
			}
			case "update_shape" -> {
				args.expectSize(9);
				DrawHelper.getInstance().updateShape(args.getStrictInt(0), (List<Map<String, Double>>)args.get(1), args.getDouble(2), args.getStrictInt(3), args.getDouble(4), args.getDouble(5), args.getDouble(6), args.getDouble(7), args.getDouble(8));
				cir.setReturnValue(true);
				cir.cancel();
			}
			case "batch_update" -> {
				args.expectSize(1);
				DrawHelper.getInstance().batchUpdate((List<Map<String, Object>>)args.get(0));
				cir.setReturnValue(true);
				cir.cancel();
			}
			case "remove_element" -> {
				args.expectSize(1);
				DrawHelper.getInstance().removeElement(args.getStrictInt(0));
				cir.setReturnValue(true);
				cir.cancel();
			}
			case "clear" -> {
				DrawHelper.getInstance().clear();
				cir.setReturnValue(true);
				cir.cancel();
			}
			case "suppress_done_message" -> {
				MinescriptHUDAddon.silent=true;
				cir.setReturnValue(true);
				cir.cancel();
			}
		}
	}
}
