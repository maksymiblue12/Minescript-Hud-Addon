package net.mb.minescripthud.util;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record ShapeGuiElementRenderState(
		RenderPipeline pipeline,
		TextureSetup textureSetup,
		Matrix3x2f pose,
		List<Vertex> vertices,
		@Nullable ScreenRect scissorArea,
		@Nullable ScreenRect bounds
	) implements SimpleGuiElementRenderState {

	@Override
	public void setupVertices(VertexConsumer vertexConsumer) {
		for (Vertex v:vertices) {
			vertexConsumer.vertex(this.pose(),v.x(),v.y()).color(v.color());
		}
	}
}
