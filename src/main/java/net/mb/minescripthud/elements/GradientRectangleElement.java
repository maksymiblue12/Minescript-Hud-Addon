package net.mb.minescripthud.elements;

import net.mb.minescripthud.element.Element;
import net.mb.minescripthud.element.Layered;
import net.mb.minescripthud.element.LayeredUpdate;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minescript.common.Jsonable;
import net.minescript.common.ScriptFunctionCall;

import java.util.Objects;

public class GradientRectangleElement extends Element {
	private static final String NAME="gradient_rectangle";

	public String getName() {
		return NAME;
	}

	@Override
	public Layered create(ScriptFunctionCall.ArgList args) {
		args.expectSize(8);
		return new GradientRectangleObject(args.getStrictInt(0),args.getStrictInt(1),args.getStrictInt(2),args.getStrictInt(3),args.getStrictInt(4),args.getStrictInt(5),args.getDouble(6),args.getStrictInt(7));
	}

	@Override
	public Layered createAdvanced(ScriptFunctionCall.ArgList args) {
		throw new UnsupportedOperationException("This element does not support advanced objects.");
	}

	@Override
	public LayeredUpdate createUpdate(ScriptFunctionCall.ArgList args) {
		args.expectSize(8);
		return new GradientRectangleObjectUpdate(args.getStrictInt(0),args.getStrictInt(1),args.getStrictInt(2),args.getStrictInt(3),args.getStrictInt(4),args.getStrictInt(5),args.getDouble(6),args.getStrictInt(7));
	}

	public static class JsonableGradientRectangleObject extends Jsonable {
		public int sx;
		public int sy;
		public int ex;
		public int ey;
		public int startColor;
		public int endColor;
		public double displayDuration;
		public int layer;

		public JsonableGradientRectangleObject(GradientRectangleElement.GradientRectangleObject from) {
			if (Objects.isNull(from)) {
				return;
			}
			this.sx=from.getStartX();
			this.sy=from.getStartY();
			this.ex=from.getEndX();
			this.ey=from.getEndY();
			this.startColor=from.getStartColor();
			this.endColor=from.getEndColor();
			this.displayDuration=from.getDisplayDuration();
			this.layer=from.getLayer();
		}
	}

	public static class GradientRectangleObjectUpdate implements LayeredUpdate {
		private final int sx;
		private final int sy;
		private final int ex;
		private final int ey;
		private final int startColor;
		private final int endColor;
		public double displayDurationModifier;
		private final int layer;
		public GradientRectangleObjectUpdate(int sx, int sy, int ex, int ey, int startColor, int endColor, double displayDurationModifier, int layer) {
			this.sx=sx;
			this.sy=sy;
			this.ex=ex;
			this.ey=ey;
			this.startColor=startColor;
			this.endColor=endColor;
			this.displayDurationModifier=displayDurationModifier;
			this.layer=layer;
		}

		@Override
		public void applyTo(Layered target) {
			if (Objects.isNull(target)) {
				return;
			}
			GradientRectangleElement.GradientRectangleObject to=(GradientRectangleElement.GradientRectangleObject)target;
			to.setStartX(this.sx);
			to.setStartY(this.sy);
			to.setEndX(this.ex);
			to.setEndY(this.ey);
			to.setStartColor(this.startColor);
			to.setEndColor(this.endColor);
			to.setDisplayDuration(Double.min(to.getDisplayDuration()+this.displayDurationModifier,Double.MAX_VALUE-1));
			to.setLayer(this.layer);
		}
	}

	public static class GradientRectangleObject extends Layered {
		private int sx;
		private int sy;
		private int ex;
		private int ey;
		private int startColor;
		private int endColor;

		public GradientRectangleObject(int sx, int sy, int ex, int ey, int startColor, int endColor, double displayDuration, int layer) {
			super(displayDuration, layer);
			this.sx=sx;
			this.sy=sy;
			this.ex=ex;
			this.ey=ey;
			this.startColor=startColor;
			this.endColor=endColor;
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

		public int getStartColor() {
			return startColor;
		}

		public void setStartColor(int newStartColor) {
			startColor=newStartColor;
		}

		public int getEndColor() {
			return endColor;
		}

		public void setEndColor(int newEndColor) {
			endColor=newEndColor;
		}

		@Override
		public void render(DrawContext context, MinecraftClient client) {
			context.fillGradient(sx,sy,ex,ey,startColor,endColor);
		}

		@Override
		public boolean containsPoint(double x, double y) {
			return sx<=x && x<=ex && sy<=y && y<=ey;
		}

		@Override
		public Jsonable toJsonable() {
			return new GradientRectangleElement.JsonableGradientRectangleObject(this);
		}

		@Override
		public String getObjectType() {
			return GradientRectangleElement.NAME;
		}
	}
}
