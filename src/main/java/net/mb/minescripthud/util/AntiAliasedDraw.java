package net.mb.minescripthud.util;

import com.mojang.blaze3d.buffers.GpuBufferSlice;

public interface AntiAliasedDraw {
	GpuBufferSlice getPolygonSlice();
	void setPolygonSlice(GpuBufferSlice vertices);
	boolean hasPolygonSlice();
}
