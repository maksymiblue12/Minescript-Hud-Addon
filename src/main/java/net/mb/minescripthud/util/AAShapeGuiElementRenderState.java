package net.mb.minescripthud.util;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record AAShapeGuiElementRenderState(
		RenderPipeline pipeline,
		TextureSetup textureSetup,
		Matrix3x2f pose,
		List<Vector2f> vertices,
		int color,
		float fade,
		@Nullable ScreenRect scissorArea,
		@Nullable ScreenRect bounds
) implements SimpleGuiElementRenderState {

	@Override
	public void setupVertices(VertexConsumer vertexConsumer) {
		assert bounds!=null;
		float left=bounds.getLeft()-fade-1;
		float top=bounds.getTop()-fade-1;
		float right=bounds.getRight()+fade+1;
		float bottom=bounds.getBottom()+fade+1;

		vertexConsumer.vertex(this.pose(),left,top).color(color);
		vertexConsumer.vertex(this.pose(),left,bottom).color(color);

		vertexConsumer.vertex(this.pose(),left,bottom).color(color);
		vertexConsumer.vertex(this.pose(),right,bottom).color(color);

		vertexConsumer.vertex(this.pose(),left,top).color(color);
		vertexConsumer.vertex(this.pose(),right,bottom).color(color);

		vertexConsumer.vertex(this.pose(),right,bottom).color(color);
		vertexConsumer.vertex(this.pose(),right,top).color(color);
	}
}
