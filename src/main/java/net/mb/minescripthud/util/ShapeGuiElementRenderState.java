package net.mb.minescripthud.util;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.gui.render.TextureSetup;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record ShapeGuiElementRenderState(
		RenderPipeline pipeline,
		TextureSetup textureSetup,
		Matrix3x2f pose,
		List<Vertex> vertices,
		@Nullable ScreenRectangle scissorArea,
		@Nullable ScreenRectangle bounds
	) implements GuiElementRenderState {

	@Override
	public void buildVertices(@NotNull VertexConsumer vertexConsumer) {
		for (Vertex v:vertices) {
			vertexConsumer.addVertexWith2DPose(this.pose(),v.x(),v.y()).setColor(v.color());
		}
	}
}
