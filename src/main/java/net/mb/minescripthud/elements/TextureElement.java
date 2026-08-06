package net.mb.minescripthud.elements;

import net.mb.minescripthud.DrawHelper;
import net.mb.minescripthud.MinescriptHUDAddon;
import net.mb.minescripthud.element.Element;
import net.mb.minescripthud.element.Layered;
import net.mb.minescripthud.element.LayeredUpdate;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minescript.common.Jsonable;
import net.minescript.common.ScriptFunctionCall;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

import java.util.Objects;

public class TextureElement extends Element {
	public static final String NAME="texture";

	public String getName() {
		return NAME;
	}

	private static Identifier getIdentifier(String texture, boolean vanilla) {
		Identifier t;
		if (vanilla) {
			t=Identifier.ofVanilla(texture);
		} else {
			t=Identifier.of(MinescriptHUDAddon.MOD_ID,texture);
		}
		return t;
	}

	@Override
	public Layered create(ScriptFunctionCall.ArgList args) {
		args.expectSize(9);
		return new TextureObject(getIdentifier(args.getString(0), args.getBoolean(1)), args.getStrictInt(2), args.getStrictInt(3), args.getStrictInt(4), args.getStrictInt(5), (float) args.getDouble(6), args.getDouble(7), args.getStrictInt(8));
	}

	@Override
	public Layered createAdvanced(ScriptFunctionCall.ArgList args) {
		args.expectSize(14);
		return new TextureObject(getIdentifier(args.getString(0), args.getBoolean(1)), args.getStrictInt(2), args.getStrictInt(3), args.getStrictInt(4), args.getStrictInt(5), (float) args.getDouble(6), args.getDouble(7), args.getStrictInt(8), args.getDouble(9), args.getDouble(10), args.getDouble(11), args.getDouble(12), args.getDouble(13));
	}

	@Override
	public LayeredUpdate createUpdate(ScriptFunctionCall.ArgList args) {
		args.expectSize(14);
		return new TextureObjectUpdate(getIdentifier(args.getString(0), args.getBoolean(1)), args.getStrictInt(2), args.getStrictInt(3), args.getStrictInt(4), args.getStrictInt(5), (float) args.getDouble(6), args.getDouble(7), args.getStrictInt(8), args.getDouble(9), args.getDouble(10), args.getDouble(11), args.getDouble(12), args.getDouble(13));
	}

	public static class JsonableTextureObject extends Jsonable {
		public String texture;
		public boolean vanilla;
		public int x;
		public int y;
		public int width;
		public int height;
		public float alpha;
		public double displayDuration;
		public int layer;
		public double scale_x;
		public double scale_y;
		public double rotation;
		public double diff_x;
		public double diff_y;
		public JsonableTextureObject(TextureElement.TextureObject from) {
			if (Objects.isNull(from)) {
				return;
			}
			this.texture=from.getTexture().getPath();
			this.vanilla=from.getTexture().getNamespace().equals("minecraft");
			this.x=from.getX();
			this.y=from.getY();
			this.width=from.getWidth();
			this.height=from.getHeight();
			this.alpha=from.getAlpha();
			this.scale_x=from.getMatrixInfo().get("scale_x");
			this.scale_y=from.getMatrixInfo().get("scale_y");
			this.rotation=from.getMatrixInfo().get("rotation");
			this.diff_x=from.getMatrixInfo().get("diff_x");
			this.diff_y=from.getMatrixInfo().get("diff_y");
			this.displayDuration=from.getDisplayDuration();
			this.layer=from.getLayer();
		}
	}

	public static class TextureObjectUpdate implements LayeredUpdate {
		private final Identifier texture;
		private final int x;
		private final int y;
		private final int width;
		private final int height;
		private final float alpha;
		private final double displayDurationModifier;
		private final int layer;
		private final double scale_x;
		private final double scale_y;
		private final double rotation;
		private final double diff_x;
		private final double diff_y;
		public TextureObjectUpdate(Identifier texture, int x, int y, int width, int height, float alpha, double displayDurationModifier, int layer, double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
			this.texture=texture;
			this.x=x;
			this.y=y;
			this.width=width;
			this.height=height;
			this.alpha=alpha;
			this.displayDurationModifier=displayDurationModifier;
			this.layer=layer;
			this.scale_x=scale_x;this.scale_y=scale_y;this.rotation=rotation;this.diff_x=diff_x;this.diff_y=diff_y;
		}

		@Override
		public void applyTo(Layered target) {
			if (Objects.isNull(target)) {
				return;
			}
			TextureElement.TextureObject to=(TextureElement.TextureObject)target;
			to.setTexture(this.texture);
			to.setX(this.x);
			to.setY(this.y);
			to.setWidth(this.width);
			to.setHeight(this.height);
			to.setAlpha(this.alpha);
			to.setDisplayDuration(Double.min(to.getDisplayDuration()+this.displayDurationModifier,Double.MAX_VALUE-1));
			to.setLayer(this.layer);
			to.getMatrixInfo().put("scale_x",this.scale_x);to.getMatrixInfo().put("scale_y",this.scale_y);to.getMatrixInfo().put("rotation",this.rotation);to.getMatrixInfo().put("diff_x",this.diff_x);to.getMatrixInfo().put("diff_y",this.diff_y);
			to.setMatrix(DrawHelper.createMatrix(x,y,width,height,scale_x,scale_y,rotation,diff_x,diff_y));
		}
	}

	public static class TextureObject extends Layered {
		private Identifier texture;
		private int x;
		private int y;
		private int width;
		private int height;
		private float alpha;
		public TextureObject(Identifier texture, int x, int y, int width, int height, float alpha, double displayDuration, int layer) {
			super(displayDuration, layer);
			this.texture=texture;
			this.x=x;
			this.y=y;
			this.width=width;
			this.height=height;
			this.alpha=alpha;
		}

		public TextureObject(Identifier texture, int x, int y, int width, int height, float alpha, double displayDuration, int layer, double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
			super(displayDuration, layer, scale_x, scale_y, rotation, diff_x, diff_y);
			this.texture=texture;
			this.x=x;
			this.y=y;
			this.width=width;
			this.height=height;
			this.alpha=alpha;
			this.setMatrix(this.createMatrix(scale_x, scale_y, rotation, diff_x, diff_y));
		}

		public Identifier getTexture() {
			return texture;
		}

		public void setTexture(Identifier newTexture) {
			texture=newTexture;
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

		public float getAlpha() {
			return alpha;
		}

		public void setAlpha(float newAlpha) {
			alpha=newAlpha;
		}

		@Override
		public boolean containsPoint(double x, double y) {
			Matrix3x2f inverse=new Matrix3x2f();
			this.getMatrix().invert(inverse);
			Vector2f point=new Vector2f((float)x,(float)y);
			Vector2f topLeft=new Vector2f((float)this.x,(float)this.y);
			Vector2f bottomRight=new Vector2f((float)this.x+this.width,(float)this.y+this.height);
			inverse.transformPosition(point);
			return topLeft.x<=point.x && point.x<=bottomRight.x && topLeft.y<=point.y && point.y<=bottomRight.y;
		}

		@Override
		public Jsonable toJsonable() {
			return new TextureElement.JsonableTextureObject(this);
		}

		@Override
		public String getObjectType() {
			return TextureElement.NAME;
		}

		@Override
		public void render(DrawContext context, MinecraftClient client) {
			context.drawGuiTexture(RenderPipelines.GUI_TEXTURED,texture,x,y,width,height,alpha);
		}

		@Override
		public Matrix3x2f createMatrix(double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
			return DrawHelper.createMatrix(x,y,width,height,scale_x,scale_y,rotation,diff_x,diff_y);
		}
	}
}
