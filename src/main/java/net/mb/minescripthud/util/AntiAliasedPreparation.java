package net.mb.minescripthud.util;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import org.joml.Vector2f;

import java.util.List;

public interface AntiAliasedPreparation {
	List<Vector2f> getVertices();
	void setVertices(List<Vector2f> vertices);
	boolean hasVertices();

	float getFade();
	void setFade(float fade);

	RenderPipeline getPipeline();
}
