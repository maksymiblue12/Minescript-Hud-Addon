package net.mb.minescripthud.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.mb.minescripthud.DrawHelper;
import net.mb.minescripthud.MinescriptHUDAddon;
import net.mb.minescripthud.ScriptFrameWaiter;
import net.mb.minescripthud.util.MouseListener;
import net.mb.minescripthud.util.MouseTracker;
import net.minecraft.client.MinecraftClient;
import net.minescript.common.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mixin(Minescript.class)
public class MinescriptMixin {
    @Inject(method = "runScriptFunction",at=@At("HEAD"),cancellable = true)
	@SuppressWarnings("unchecked")
    private static void runScriptFunction(JobControl job, long funcCallId, ScriptFunctionCall functionCall, CallbackInfoReturnable<ScriptValue> cir) {
        String name = functionCall.name();
        ScriptFunctionCall.ArgList args = functionCall.args();
		switch (name) {
			case "add_element" -> {
				int id=DrawHelper.getInstance().addElement(name,args);
				cir.setReturnValue(ScriptValue.of(id));
				cir.cancel();
			}
			case "add_advanced_element" -> {
				int id=DrawHelper.getInstance().addAdvancedElement(name,args);
				cir.setReturnValue(ScriptValue.of(id));
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
			case "get_element" -> {
				args.expectSize(1);
				cir.setReturnValue(ScriptValue.of(DrawHelper.getInstance().getElement(args.getStrictInt(0))));
				cir.cancel();
			}
			case "still_exists" -> {
				args.expectSize(1);
				cir.setReturnValue(ScriptValue.of(DrawHelper.getInstance().stillExists(args.getStrictInt(0))));
				cir.cancel();
			}
			case "get_mouse" -> {
				cir.setReturnValue(ScriptValue.of(MouseTracker.getInstance().toJsonable()));
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
			case "update_element" -> {
				DrawHelper.getInstance().updateElement(functionName,args);
				cir.setReturnValue(true);
				cir.cancel();
			}
			case "batch_update" -> {
				args.expectSize(1);
				DrawHelper.getInstance().batchUpdate(functionName,(List<Map<String, Object>>)args.get(0));
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

	@Inject(method = "runExternalScriptFunction",at=@At("HEAD"),cancellable = true)
	private static void runExternalScriptFunction(Job.SubprocessJob job, long funcCallId, ScriptFunctionCall functionCall, CallbackInfoReturnable<Optional<JsonElement>> cir) {
		String functionName = functionCall.name();
		ScriptFunctionCall.ArgList args = functionCall.args();
		switch (functionName) {
			case "register_mouse_listener" -> {
				int elementId=args.getStrictInt(0);
				DrawHelper.getInstance().addMouseListener(elementId,new MouseListener(job));
				cir.setReturnValue(Optional.of(new JsonPrimitive(funcCallId)));
				cir.cancel();
			}
			case "start_mouse_listener" -> {
				int elementId=args.getStrictInt(0);
				long listenerId=args.getStrictLong(1);
				MouseListener h=DrawHelper.getInstance().getMouseListener(elementId);
				job.addOperation(listenerId,h);
				h.start(funcCallId,listenerId);
				cir.setReturnValue(Optional.empty());
				cir.cancel();
			}
		}
	}
}
