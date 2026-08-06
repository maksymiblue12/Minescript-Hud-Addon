package net.mb.minescripthud.elements;

import net.mb.minescripthud.element.Element;
import net.mb.minescripthud.element.Layered;
import net.mb.minescripthud.element.LayeredUpdate;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minescript.common.Jsonable;
import net.minescript.common.ScriptFunctionCall;

import java.util.Objects;

public class RectangleElement extends Element {
	private static final String NAME="rectangle";

	public String getName() {
		return NAME;
	}

	@Override
	public Layered create(ScriptFunctionCall.ArgList args) {
		args.expectSize(7);
		return new RectangleObject(args.getStrictInt(0),args.getStrictInt(1),args.getStrictInt(2),args.getStrictInt(3),args.getStrictInt(4),args.getDouble(5),args.getStrictInt(6));
	}

	@Override
	public Layered createAdvanced(ScriptFunctionCall.ArgList args) {
		throw new UnsupportedOperationException("This element does not support advanced objects.");
	}

	@Override
	public LayeredUpdate createUpdate(ScriptFunctionCall.ArgList args) {
		args.expectSize(7);
		return new RectangleObjectUpdate(args.getStrictInt(0),args.getStrictInt(1),args.getStrictInt(2),args.getStrictInt(3),args.getStrictInt(4),args.getDouble(5),args.getStrictInt(6));
	}

	public static class JsonableRectangleObject extends Jsonable {
		public int sx;
		public int sy;
		public int ex;
		public int ey;
		public int color;
		public double displayDuration;
		public int layer;

		public JsonableRectangleObject(RectangleElement.RectangleObject from) {
			if (Objects.isNull(from)) {
				return;
			}
			this.sx=from.getStartX();
			this.sy=from.getStartY();
			this.ex=from.getEndX();
			this.ey=from.getEndY();
			this.color=from.getColor();
			this.displayDuration=from.getDisplayDuration();
			this.layer=from.getLayer();
		}
	}

	public static class RectangleObjectUpdate implements LayeredUpdate {
		private final int sx;
		private final int sy;
		private final int ex;
		private final int ey;
		private final int color;
		public double displayDurationModifier;
		private final int layer;
		public RectangleObjectUpdate(int sx, int sy, int ex, int ey, int color, double displayDurationModifier, int layer) {
			this.sx=sx;
			this.sy=sy;
			this.ex=ex;
			this.ey=ey;
			this.color=color;
			this.displayDurationModifier=displayDurationModifier;
			this.layer=layer;
		}

		@Override
		public void applyTo(Layered target) {
			if (Objects.isNull(target)) {
				return;
			}
			RectangleElement.RectangleObject to=(RectangleElement.RectangleObject)target;
			to.setStartX(this.sx);
			to.setStartY(this.sy);
			to.setEndX(this.ex);
			to.setEndY(this.ey);
			to.setColor(this.color);
			to.setDisplayDuration(Double.min(to.getDisplayDuration()+this.displayDurationModifier,Double.MAX_VALUE-1));
			to.setLayer(this.layer);
		}
	}

	public static class RectangleObject extends Layered {
		private int sx;
		private int sy;
		private int ex;
		private int ey;
		private int color;

		public RectangleObject(int sx, int sy, int ex, int ey, int color, double displayDuration, int layer) {
			super(displayDuration,layer);
			this.sx=sx;
			this.sy=sy;
			this.ex=ex;
			this.ey=ey;
			this.color=color;
		}

		public int getStartX() {
			return sx;
		}

		public void setStartX(int newStartX) {
			sx=newStartX;
		}

		public int getStartY() {
			return sy;
		}

		public void setStartY(int newStartY) {
			sy=newStartY;
		}

		public int getEndX() {
			return ex;
		}

		public void setEndX(int newEndX) {
			ex=newEndX;
		}

		public int getEndY() {
			return ey;
		}

		public void setEndY(int newEndY) {
			ey=newEndY;
		}

		public int getColor() {
			return color;
		}

		public void setColor(int newColor) {
			color=newColor;
		}

		@Override
		public void render(DrawContext context, MinecraftClient client) {
			context.fill(sx, sy, ex, ey, color);
		}

		@Override
		public boolean containsPoint(double x, double y) {
			return sx<=x && x<=ex && sy<=y && y<=ey;
		}

		@Override
		public Jsonable toJsonable() {
			return new RectangleElement.JsonableRectangleObject(this);
		}

		@Override
		public String getObjectType() {
			return RectangleElement.NAME;
		}
	}
}
