package net.mb.minescripthud.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.mb.minescripthud.elements.AntiAliasedShapeElement;
import net.mb.minescripthud.util.AAShapeGuiElementRenderState;
import net.mb.minescripthud.util.AntiAliasedDraw;
import net.mb.minescripthud.util.AntiAliasedPreparation;
import net.mb.minescripthud.util.PolygonUniforms;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import org.joml.Vector2f;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin {
	private List<Vector2f> vertices;
	private float fade;
	private AntiAliasedPreparation currentPreparation;

	@ModifyExpressionValue(method="finishPreparation",at=@At(value="INVOKE",target="Ljava/util/Iterator;next()Ljava/lang/Object;"))
	private Object capturePreparation(Object preparation) {
		currentPreparation=(AntiAliasedPreparation)preparation;
		return preparation;
	}

	@Redirect(method="finishPreparation",at=@At(value="INVOKE",target="Ljava/util/List;add(Ljava/lang/Object;)Z"))
	private boolean addDraw(List<Object> draws, @Coerce Object draw) {
		AntiAliasedDraw antiAliasedDraw=(AntiAliasedDraw)draw;
		if (currentPreparation.hasVertices()) {
			antiAliasedDraw.setPolygonSlice(AntiAliasedShapeElement.getPolygonStorage().write(new PolygonUniforms(currentPreparation.getVertices(),currentPreparation.getFade())));
		}
		return draws.add(antiAliasedDraw);
	}

	@Inject(method="prepareSimpleElements",at=@At("HEAD"))
	private void prepareSimpleElements(GuiRenderState.LayerFilter filter, CallbackInfo ci) {
		this.vertices=null;
		this.fade=1.5f;
	}

	@Shadow
	private boolean scissorChanged(@Nullable ScreenRect oldScissorArea, @Nullable ScreenRect newScissorArea) {return false;}

	@Redirect(method="prepareSimpleElement",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/render/GuiRenderer;scissorChanged(Lnet/minecraft/client/gui/ScreenRect;Lnet/minecraft/client/gui/ScreenRect;)Z"))
	private boolean checkValues(GuiRenderer instance, @Nullable ScreenRect oldScissorArea, @Nullable ScreenRect newScissorArea, @Local(argsOnly=true) SimpleGuiElementRenderState state) {
		boolean original=this.scissorChanged(oldScissorArea,newScissorArea);

		if (state.pipeline()==AntiAliasedShapeElement.ANTI_ALIASED_PIPELINE) {
			AAShapeGuiElementRenderState aaState=(AAShapeGuiElementRenderState)state;
			return original||aaState.vertices()!=this.vertices||aaState.fade()!=this.fade;
		}

		return original;
	}

	@Inject(method="prepareSimpleElement",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/render/GuiRenderer;startBuffer(Lcom/mojang/blaze3d/pipeline/RenderPipeline;)Lnet/minecraft/client/render/BufferBuilder;"))
	private void prepareSimpleElement(SimpleGuiElementRenderState state, CallbackInfo ci) {
		if (state.pipeline()==AntiAliasedShapeElement.ANTI_ALIASED_PIPELINE) {
			AAShapeGuiElementRenderState aaState=(AAShapeGuiElementRenderState)state;
			this.vertices=aaState.vertices();
			this.fade=aaState.fade();
		}
	}

	@Redirect(method="endBuffer",at=@At(value="INVOKE",target="Ljava/util/List;add(Ljava/lang/Object;)Z"))
	private boolean addPreparation(List<Object> preparations, Object preparation) {
		AntiAliasedPreparation antiAliasedPreparation=(AntiAliasedPreparation)preparation;
		if (antiAliasedPreparation.getPipeline()==AntiAliasedShapeElement.ANTI_ALIASED_PIPELINE) {
			antiAliasedPreparation.setVertices(vertices);
			antiAliasedPreparation.setFade(fade);
		}
		return preparations.add(antiAliasedPreparation);
	}

	@Inject(method="render(Lnet/minecraft/client/gui/render/GuiRenderer$Draw;Lcom/mojang/blaze3d/systems/RenderPass;Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/vertex/VertexFormat$IndexType;)V",at=@At(value="INVOKE",target="Lcom/mojang/blaze3d/systems/RenderPass;setPipeline(Lcom/mojang/blaze3d/pipeline/RenderPipeline;)V",shift=At.Shift.AFTER))
	private void render(@Coerce Object draw, RenderPass pass, GpuBuffer indexBuffer, VertexFormat.IndexType indexType, CallbackInfo ci) {
		AntiAliasedDraw antiAliasedDraw=(AntiAliasedDraw)draw;
		if (!antiAliasedDraw.hasPolygonSlice()) return;
		pass.setUniform("PolygonData",antiAliasedDraw.getPolygonSlice());
	}
}
