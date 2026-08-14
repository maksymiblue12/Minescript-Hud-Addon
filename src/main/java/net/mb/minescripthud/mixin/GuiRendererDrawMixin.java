package net.mb.minescripthud.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.mb.minescripthud.util.AntiAliasedDraw;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets="net.minecraft.client.gui.render.GuiRenderer$Draw")
public class GuiRendererDrawMixin implements AntiAliasedDraw {
	private GpuBufferSlice polygonSlice;

	@Override
	public GpuBufferSlice getPolygonSlice() {
		return polygonSlice;
	}

	@Override
	public void setPolygonSlice(GpuBufferSlice polygonSlice) {
		this.polygonSlice=polygonSlice;
	}

	@Override
	public boolean hasPolygonSlice() {
		return polygonSlice!=null;
	}
}
