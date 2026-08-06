package net.mb.minescripthud.elements;

import net.mb.minescripthud.DrawHelper;
import net.mb.minescripthud.element.Element;
import net.mb.minescripthud.element.Layered;
import net.mb.minescripthud.element.LayeredUpdate;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minescript.common.Jsonable;
import net.minescript.common.ScriptFunctionCall;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

import java.util.Objects;

public class TextElement extends Element {
	public static final String NAME="text";

	public String getName() {
		return NAME;
	}

	@Override
	public Layered create(ScriptFunctionCall.ArgList args) {
		args.expectSize(7);
		return new TextObject(args.getString(0), args.getStrictInt(1), args.getStrictInt(2), args.getStrictInt(3), args.getBoolean(4), args.getDouble(5),args.getStrictInt(6));
	}

	@Override
	public Layered createAdvanced(ScriptFunctionCall.ArgList args) {
		args.expectSize(12);
		return new TextObject(args.getString(0), args.getStrictInt(1), args.getStrictInt(2), args.getStrictInt(3), args.getBoolean(4), args.getDouble(5),args.getStrictInt(6),args.getDouble(7),args.getDouble(8),args.getDouble(9),args.getDouble(10),args.getDouble(11));
	}

	@Override
	public LayeredUpdate createUpdate(ScriptFunctionCall.ArgList args) {
		args.expectSize(12);
		return new TextObjectUpdate(args.getString(0), args.getStrictInt(1), args.getStrictInt(2), args.getStrictInt(3), args.getBoolean(4), args.getDouble(5),args.getStrictInt(6),args.getDouble(7),args.getDouble(8),args.getDouble(9),args.getDouble(10),args.getDouble(11));
	}


	public static class JsonableTextObject extends Jsonable {
		public String text;
		public int x;
		public int y;
		public int color;
		public boolean shadow;
		public double displayDuration;
		public int layer;
		public double scale_x;
		public double scale_y;
		public double rotation;
		public double diff_x;
		public double diff_y;
		public int width;
		public int height;

		public JsonableTextObject(TextElement.TextObject from) {
			if (Objects.isNull(from)) {
				return;
			}
			this.text=from.getText();
			this.x=from.getX();
			this.y=from.getY();
			this.color=from.getColor();
			this.shadow=from.getShadow();
			this.displayDuration=from.getDisplayDuration();
			this.layer=from.getLayer();
			this.scale_x=from.getMatrixInfo().get("scale_x");
			this.scale_y=from.getMatrixInfo().get("scale_y");
			this.rotation=from.getMatrixInfo().get("rotation");
			this.diff_x=from.getMatrixInfo().get("diff_x");
			this.diff_y=from.getMatrixInfo().get("diff_y");
			this.width=(int)(MinecraftClient.getInstance().textRenderer.getWidth(this.text)*this.scale_x);
			this.height=(int)(MinecraftClient.getInstance().textRenderer.fontHeight*this.scale_y);
		}
	}

	public static class TextObjectUpdate implements LayeredUpdate {
		private final String text;
		private final int x;
		private final int y;
		private final int color;
		private final boolean shadow;
		private final double displayDurationModifier;
		private final int layer;
		private final double scale_x;
		private final double scale_y;
		private final double rotation;
		private final double diff_x;
		private final double diff_y;
		public TextObjectUpdate(String text, int x, int y, int color, boolean shadow, double displayDurationModifier, int layer, double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
			this.text=text;
			this.x=x;
			this.y=y;
			this.color=color;
			this.shadow=shadow;
			this.displayDurationModifier=displayDurationModifier;
			this.layer=layer;
			this.scale_x=scale_x;this.scale_y=scale_y;this.rotation=rotation;this.diff_x=diff_x;this.diff_y=diff_y;
		}

		@Override
		public void applyTo(Layered target) {
			if (Objects.isNull(target)) {
				return;
			}
			TextElement.TextObject to=(TextElement.TextObject)target;
			to.setText(this.text);
			to.setX(this.x);
			to.setY(this.y);
			to.setColor(this.color);
			to.setShadow(this.shadow);
			to.setDisplayDuration(Double.min(to.getDisplayDuration()+this.displayDurationModifier,Double.MAX_VALUE-1));
			to.setLayer(this.layer);
			to.getMatrixInfo().put("scale_x",this.scale_x);to.getMatrixInfo().put("scale_y",this.scale_y);to.getMatrixInfo().put("rotation",this.rotation);to.getMatrixInfo().put("diff_x",this.diff_x);to.getMatrixInfo().put("diff_y",this.diff_y);
			to.setMatrix(DrawHelper.createMatrix(x,y,MinecraftClient.getInstance().textRenderer.getWidth(text),MinecraftClient.getInstance().textRenderer.fontHeight,scale_x,scale_y,rotation,diff_x,diff_y));
		}
	}

	public static class TextObject extends Layered {
		private String text;
		private int x;
		private int y;
		private int color;
		private boolean shadow;
		public TextObject(String text, int x, int y, int color, boolean shadow, double displayDuration, int layer) {
			super(displayDuration,layer);
			this.text=text;
			this.x=x;
			this.y=y;
			this.color=color;
			this.shadow=shadow;
		}

		public TextObject(String text, int x, int y, int color, boolean shadow, double displayDuration, int layer, double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
			super(displayDuration,layer,scale_x,scale_y,rotation,diff_x,diff_y);
			this.text=text;
			this.x=x;
			this.y=y;
			this.color=color;
			this.shadow=shadow;
			this.setMatrix(this.createMatrix(scale_x, scale_y, rotation, diff_x, diff_y));
		}

		public String getText() {
			return text;
		}

		public void setText(String newText) {
			text=newText;
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

		public int getColor() {
			return color;
		}

		public void setColor(int newColor) {
			color=newColor;
		}

		public boolean getShadow() {
			return shadow;
		}

		public void setShadow(boolean newShadow) {
			shadow=newShadow;
		}

		@Override
		public void render(DrawContext context, MinecraftClient client) {
			context.drawText(client.textRenderer, text, x, y, color, shadow);
		}

		@Override
		public boolean containsPoint(double x, double y) {
			Matrix3x2f inverse=new Matrix3x2f();
			this.getMatrix().invert(inverse);
			Vector2f point=new Vector2f((float)x,(float)y);
			Vector2f topLeft=new Vector2f((float)this.x,(float)this.y);
			Vector2f bottomRight=new Vector2f((float)this.x+MinecraftClient.getInstance().textRenderer.getWidth(this.text),(float)this.y+MinecraftClient.getInstance().textRenderer.fontHeight);
			inverse.transformPosition(point);
			return topLeft.x<=point.x && point.x<=bottomRight.x && topLeft.y<=point.y && point.y<=bottomRight.y;
		}

		@Override
		public Jsonable toJsonable() {
			return new TextElement.JsonableTextObject(this);
		}

		@Override
		public String getObjectType() {
			return TextElement.NAME;
		}

		@Override
		public Matrix3x2f createMatrix(double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
			return DrawHelper.createMatrix(x,y,MinecraftClient.getInstance().textRenderer.getWidth(text),MinecraftClient.getInstance().textRenderer.fontHeight,scale_x,scale_y,rotation,diff_x,diff_y);
		}
	}
}
