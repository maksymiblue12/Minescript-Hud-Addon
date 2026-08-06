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

public class TextWithBackgroundElement extends Element {
	private static final String NAME="text_with_background";

	public String getName() {
		return NAME;
	}

	@Override
	public Layered create(ScriptFunctionCall.ArgList args) {
		args.expectSize(10);
		return new TextWithBackgroundObject(args.getString(0), args.getStrictInt(1), args.getStrictInt(2), args.getStrictInt(3), args.getStrictInt(4), args.getStrictInt(5), args.getStrictInt(6), args.getBoolean(7), args.getDouble(8), args.getStrictInt(9));
	}

	@Override
	public Layered createAdvanced(ScriptFunctionCall.ArgList args) {
		args.expectSize(15);
		return new TextWithBackgroundObject(args.getString(0), args.getStrictInt(1), args.getStrictInt(2), args.getStrictInt(3), args.getStrictInt(4), args.getStrictInt(5), args.getStrictInt(6), args.getBoolean(7), args.getDouble(8), args.getStrictInt(9), args.getDouble(10), args.getDouble(11), args.getDouble(12), args.getDouble(13), args.getDouble(14));
	}

	@Override
	public LayeredUpdate createUpdate(ScriptFunctionCall.ArgList args) {
		args.expectSize(15);
		return new TextWithBackgroundObjectUpdate(args.getString(0), args.getStrictInt(1), args.getStrictInt(2), args.getStrictInt(3), args.getStrictInt(4), args.getStrictInt(5), args.getStrictInt(6), args.getBoolean(7), args.getDouble(8), args.getStrictInt(9), args.getDouble(10), args.getDouble(11), args.getDouble(12), args.getDouble(13), args.getDouble(14));
	}

	public static class JsonableTextWithBackgroundObject extends Jsonable {
		public String text;
		public int x;
		public int y;
		public int marginX;
		public int marginY;
		public int color;
		public int bgColor;
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

		public JsonableTextWithBackgroundObject(TextWithBackgroundElement.TextWithBackgroundObject from) {
			if (Objects.isNull(from)) {
				return;
			}
			this.text=from.getText();
			this.x=from.getX();
			this.y=from.getY();
			this.marginX=from.getMarginX();
			this.marginY=from.getMarginY();
			this.color=from.getColor();
			this.bgColor=from.getBgColor();
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

	public static class TextWithBackgroundObjectUpdate implements LayeredUpdate {
		private final String text;
		private final int x;
		private final int y;
		private final int marginX;
		private final int marginY;
		private final int color;
		private final int bgColor;
		private final boolean shadow;
		private final double displayDurationModifier;
		private final int layer;
		private final double scale_x;
		private final double scale_y;
		private final double rotation;
		private final double diff_x;
		private final double diff_y;
		public TextWithBackgroundObjectUpdate(String text, int x, int y, int marginX, int marginY, int color, int bgColor, boolean shadow, double displayDurationModifier, int layer, double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
			this.text=text;
			this.x=x;
			this.y=y;
			this.marginX=marginX;
			this.marginY=marginY;
			this.color=color;
			this.bgColor=bgColor;
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
			TextWithBackgroundElement.TextWithBackgroundObject to=(TextWithBackgroundElement.TextWithBackgroundObject)target;
			to.setText(this.text);
			to.setX(this.x);
			to.setY(this.y);
			to.setMarginX(this.marginX);
			to.setMarginY(this.marginY);
			to.setColor(this.color);
			to.setBgColor(bgColor);
			to.setShadow(this.shadow);
			to.setDisplayDuration(Double.min(to.getDisplayDuration()+this.displayDurationModifier,Double.MAX_VALUE-1));
			to.setLayer(this.layer);
			to.getMatrixInfo().put("scale_x",this.scale_x);to.getMatrixInfo().put("scale_y",this.scale_y);to.getMatrixInfo().put("rotation",this.rotation);to.getMatrixInfo().put("diff_x",this.diff_x);to.getMatrixInfo().put("diff_y",this.diff_y);
			to.setMatrix(DrawHelper.createMatrix(x,y,MinecraftClient.getInstance().textRenderer.getWidth(text),MinecraftClient.getInstance().textRenderer.fontHeight,scale_x,scale_y,rotation,diff_x,diff_y));
		}
	}

	public static class TextWithBackgroundObject extends Layered {
		private String text;
		private int x;
		private int y;
		private int marginX;
		private int marginY;
		private int color;
		private int bgColor;
		private boolean shadow;
		public TextWithBackgroundObject(String text, int x, int y, int marginX, int marginY, int color, int bgColor, boolean shadow, double displayDuration, int layer) {
			super(displayDuration,layer);
			this.text=text;
			this.x=x;
			this.y=y;
			this.marginX=marginX;
			this.marginY=marginY;
			this.color=color;
			this.bgColor=bgColor;
			this.shadow=shadow;
		}

		public TextWithBackgroundObject(String text, int x, int y, int marginX, int marginY, int color, int bgColor, boolean shadow, double displayDuration, int layer, double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
			super(displayDuration, layer, scale_x, scale_y, rotation, diff_x, diff_y);
			this.text=text;
			this.x=x;
			this.y=y;
			this.marginX=marginX;
			this.marginY=marginY;
			this.color=color;
			this.bgColor=bgColor;
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

		public int getMarginX() {
			return marginX;
		}

		public void setMarginX(int newMarginX) {
			marginX=newMarginX;
		}

		public int getMarginY() {
			return marginY;
		}

		public void setMarginY(int newMarginY) {
			marginY=newMarginY;
		}

		public int getColor() {
			return color;
		}

		public void setColor(int newColor) {
			color=newColor;
		}

		public int getBgColor() {
			return bgColor;
		}

		public void setBgColor(int newBgColor) {
			bgColor=newBgColor;
		}

		public boolean getShadow() {
			return shadow;
		}

		public void setShadow(boolean newShadow) {
			shadow=newShadow;
		}

		@Override
		public boolean containsPoint(double x, double y) {
			Matrix3x2f inverse=new Matrix3x2f();
			this.getMatrix().invert(inverse);
			Vector2f point=new Vector2f((float)x,(float)y);
			Vector2f topLeft=new Vector2f((float)this.x-this.marginX,(float)this.y-this.marginY);
			Vector2f bottomRight=new Vector2f((float)this.x+MinecraftClient.getInstance().textRenderer.getWidth(this.text)-1+this.marginX,(float)this.y+MinecraftClient.getInstance().textRenderer.fontHeight-2+this.marginY);
			inverse.transformPosition(point);
			return topLeft.x<=point.x && point.x<=bottomRight.x && topLeft.y<=point.y && point.y<=bottomRight.y;
		}

		@Override
		public Jsonable toJsonable() {
			return new TextWithBackgroundElement.JsonableTextWithBackgroundObject(this);
		}

		@Override
		public String getObjectType() {
			return TextWithBackgroundElement.NAME;
		}

		@Override
		public void render(DrawContext context, MinecraftClient client) {
			context.fill(x-marginX, y-marginY, x+client.textRenderer.getWidth(text)-1+marginX, y+client.textRenderer.fontHeight-2+marginY, bgColor);
			context.drawText(client.textRenderer, text, x, y, color, shadow);
		}

		@Override
		public Matrix3x2f createMatrix(double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
			return DrawHelper.createMatrix(this.x,this.y,MinecraftClient.getInstance().textRenderer.getWidth(this.text),MinecraftClient.getInstance().textRenderer.fontHeight,scale_x,scale_y,rotation,diff_x,diff_y);
		}
	}
}
