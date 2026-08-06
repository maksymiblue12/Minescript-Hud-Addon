package net.mb.minescripthud.elements;

import net.mb.minescripthud.element.Element;
import net.mb.minescripthud.element.Layered;
import net.mb.minescripthud.element.LayeredUpdate;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minescript.common.Jsonable;
import net.minescript.common.ScriptFunctionCall;

import java.util.Objects;

public class StrokedRectangleElement extends Element {
	private static final String NAME="stroked_rectangle";

	public String getName() {
		return NAME;
	}

	@Override
	public Layered create(ScriptFunctionCall.ArgList args) {
		args.expectSize(7);
		return new StrokedRectangleObject(args.getStrictInt(0),args.getStrictInt(1),args.getStrictInt(2),args.getStrictInt(3),args.getStrictInt(4),args.getDouble(5),args.getStrictInt(6));
	}

	@Override
	public Layered createAdvanced(ScriptFunctionCall.ArgList args) {
		throw new UnsupportedOperationException("This element does not support advanced objects.");
	}

	@Override
	public LayeredUpdate createUpdate(ScriptFunctionCall.ArgList args) {
		args.expectSize(7);
		return new StrokedRectangleObjectUpdate(args.getStrictInt(0),args.getStrictInt(1),args.getStrictInt(2),args.getStrictInt(3),args.getStrictInt(4),args.getDouble(5),args.getStrictInt(6));
	}

	public static class JsonableStrokedRectangleObject extends Jsonable {
		public int x;
		public int y;
		public int width;
		public int height;
		public int color;
		public double displayDuration;
		public int layer;

		public JsonableStrokedRectangleObject(StrokedRectangleElement.StrokedRectangleObject from) {
			if (Objects.isNull(from)) {
				return;
			}
			this.x=from.getX();
			this.y=from.getY();
			this.width=from.getWidth();
			this.height=from.getHeight();
			this.color=from.getColor();
			this.displayDuration=from.getDisplayDuration();
			this.layer=from.getLayer();
		}
	}

	public static class StrokedRectangleObjectUpdate implements LayeredUpdate {
		private final int x;
		private final int y;
		private final int ex;
		private final int ey;
		private final int color;
		public double displayDurationModifier;
		private final int layer;
		public StrokedRectangleObjectUpdate(int x, int y, int ex, int ey, int color, double displayDurationModifier, int layer) {
			this.x=x;
			this.y=y;
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
			StrokedRectangleElement.StrokedRectangleObject to=(StrokedRectangleElement.StrokedRectangleObject)target;
			to.setX(this.x);
			to.setY(this.y);
			to.setWidth(this.ex);
			to.setHeight(this.ey);
			to.setColor(this.color);
			to.setDisplayDuration(Double.min(to.getDisplayDuration()+this.displayDurationModifier,Double.MAX_VALUE-1));
			to.setLayer(this.layer);
		}
	}

	public static class StrokedRectangleObject extends Layered {
		private int x;
		private int y;
		private int width;
		private int height;
		private int color;

		public StrokedRectangleObject(int x, int y, int width, int height, int color, double displayDuration, int layer) {
			super(displayDuration, layer);
			this.x=x;
			this.y=y;
			this.width=width;
			this.height=height;
			this.color=color;
		}

		public int getX() {
			return x;
		}

		public void setX(int newX) {
			x=newX;
		}

		public int getY() {
			return y;
		}

		public void setY(int newY) {
			y=newY;
		}

		public int getWidth() {
			return width;
		}

		public void setWidth(int newWidth) {
			width=newWidth;
		}

		public int getHeight() {
			return height;
		}

		public void setHeight(int newHeight) {
			height=newHeight;
		}

		public int getColor() {
			return color;
		}

		public void setColor(int newColor) {
			color=newColor;
		}

		@Override
		public void render(DrawContext context, MinecraftClient client) {
			context.drawStrokedRectangle(x,y,width,height,color);
		}

		@Override
		public boolean containsPoint(double x, double y) {
			return this.x<=x && x<=this.x+this.width && this.y<=y && y<=this.y+this.height;
		}

		@Override
		public Jsonable toJsonable() {
			return new StrokedRectangleElement.JsonableStrokedRectangleObject(this);
		}

		@Override
		public String getObjectType() {
			return StrokedRectangleElement.NAME;
		}
	}
}
