package net.mb.minescripthud.util;

import net.minecraft.client.gl.DynamicUniformStorage;
import org.joml.Vector2f;

import java.nio.ByteBuffer;
import java.util.List;

public record PolygonUniforms(List<Vector2f> points, float fade) implements DynamicUniformStorage.Uploadable {
	@Override
	public void write(ByteBuffer buffer) {
		for (Vector2f point:points) {
			buffer.putFloat(point.x);
			buffer.putFloat(point.y);

			// std140 padding
			buffer.putFloat(0f);
			buffer.putFloat(0f);
		}

		buffer.position(2048);

		buffer.putInt(points.size());
		buffer.putFloat(fade);
		buffer.putFloat(0f);
		buffer.putFloat(0f);
	}
}
