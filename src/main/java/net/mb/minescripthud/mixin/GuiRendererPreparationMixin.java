package net.mb.minescripthud.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.mb.minescripthud.util.AntiAliasedPreparation;
import org.joml.Vector2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(targets="net.minecraft.client.gui.render.GuiRenderer$Preparation")
public class GuiRendererPreparationMixin implements AntiAliasedPreparation {
	private List<Vector2f> vertices;
	private float fade;

	@Shadow
	public RenderPipeline pipeline() {return null;}

	@Override
	public RenderPipeline getPipeline() {
		return pipeline();
	}

	@Override
	public List<Vector2f> getVertices() {
		return vertices;
	}

	@Override
	public void setVertices(List<Vector2f> vertices) {
		this.vertices=vertices;
	}

	@Override
	public boolean hasVertices() {return vertices!=null;}

	@Override
	public float getFade() {
		return fade;
	}

	@Override
	public void setFade(float fade) {
		this.fade=fade;
	}
}
