package net.mb.minescripthud;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStringReader;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2f;

import java.util.*;
import java.util.stream.Collectors;

public class DrawHelper {
	private static final DrawHelper INSTANCE=new DrawHelper();
	private final Map<Integer,Layered> elements=new HashMap<>();
	private final Map<Integer,LayeredUpdate> elementUpdates=new HashMap<>();
	private int currentId=0;

	private DrawHelper() {}

	public static DrawHelper getInstance() {
		return INSTANCE;
	}

	private int getId() {
		return currentId++;
	}

	public void clear() {
		elements.clear();
		elementUpdates.clear();
	}

	public void removeElement(int id) {
		elements.remove(id);
		elementUpdates.remove(id);
	}

	public boolean stillExists(int id) {
		return elements.containsKey(id);
	}

	public JsonableMouseObject getMouse() {
		return new JsonableMouseObject(MinecraftClient.getInstance().mouse);
	}

	private ItemStack getItemStack(String item) throws CommandSyntaxException {
		ItemStringReader.ItemResult itemResult=new ItemStringReader(MinecraftClient.getInstance().getNetworkHandler().getRegistryManager()).consume(new StringReader(item));
		return new ItemStackArgument(itemResult.item(),itemResult.components()).createStack(1,false);
	}

	public static Matrix3x2f createMatrx(int x, int y, int w, int h, double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
		float cx=(w/2f);
		float cy=(h/2f);
		return new Matrix3x2f()
				.translate(cx,cy)
				.rotateAbout((float) rotation,x,y)
				.scale((float)scale_x,(float)scale_y)
				.translate(-cx,-cy)
				.translate((float) (diff_x-(double)x*((scale_x-1)/scale_x)),(float) (diff_y-(double)y*((scale_y-1)/scale_y)));
	}





	public int addText(String text, int x, int y, int color, boolean shadow, double displayDuration,int layer) {
		int i=this.getId();
		elements.put(i, new TextObject(text, x, y, color, shadow, displayDuration,layer));
		return i;
	}

	public int addAdvancedText(String text, int x, int y, int color, boolean shadow, double displayDuration, int layer, double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
		int i=this.getId();
		elements.put(i, new TextObject(text, x, y, color, shadow, displayDuration,layer,scale_x,scale_y,rotation,diff_x,diff_y));
		return i;
	}

	public JsonableTextObject getTextObject(int id) {
		return new JsonableTextObject((TextObject) elements.get(id));
	}

	public void updateText(int id, String text, int x, int y, int color, boolean shadow, double displayDurationModifier, int layer, double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
		elementUpdates.put(id,new TextObjectUpdate(text,x,y,color,shadow,displayDurationModifier,layer,scale_x,scale_y,rotation,diff_x,diff_y));
	}





	public int addRectangle(int sx, int sy, int ex, int ey, int color, double displayDuration, int layer) {
		int i=this.getId();
		elements.put(i,new RectangleObject(sx,sy,ex,ey,color,displayDuration,layer));
		return i;
	}

	public JsonableRectangleObject getRectangleObject(int id) {
		return new JsonableRectangleObject((RectangleObject) elements.get(id));
	}

	public void updateRectangle(int id, int sx, int sy, int ex, int ey, int color, double displayDurationModifier, int layer) {
		elementUpdates.put(id,new RectangleObjectUpdate(sx,sy,ex,ey,color,displayDurationModifier,layer));
	}

	public int addGradientRectangle(int sx, int sy, int ex, int ey, int startColor, int endColor, double displayDuration, int layer) {
		int i=this.getId();
		elements.put(i,new GradientRectangleObject(sx,sy,ex,ey,startColor,endColor,displayDuration,layer));
		return i;
	}

	public JsonableGradientRectangleObject getGradientRectangleObject(int id) {
		return new JsonableGradientRectangleObject((GradientRectangleObject) elements.get(id));
	}

	public void updateGradientRectangle(int id, int sx, int sy, int ex, int ey, int startColor, int endColor, double displayDurationModifier, int layer) {
		elementUpdates.put(id,new GradientRectangleObjectUpdate(sx,sy,ex,ey,startColor,endColor,displayDurationModifier,layer));
	}





	public int addTextWithBackground(String text, int x, int y, int marginX, int marginY, int color, int bgColor, boolean shadow, double displayDuration,int layer) {
		int i=this.getId();
		elements.put(i, new TextWithBackgroundObject(text, x, y, marginX, marginY, color, bgColor, shadow, displayDuration, layer));
		return i;
	}

	public int addAdvancedTextWithBackground(String text, int x, int y, int marginX, int marginY, int color, int bgColor, boolean shadow, double displayDuration,int layer, double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
		int i=this.getId();
		elements.put(i, new TextWithBackgroundObject(text, x, y, marginX, marginY, color, bgColor, shadow, displayDuration, layer,scale_x,scale_y,rotation,diff_x,diff_y));
		return i;
	}

	public JsonableTextWithBackgroundObject getTextWithBackgroundObject(int id) {
		return new JsonableTextWithBackgroundObject((TextWithBackgroundObject) elements.get(id));
	}

	public void updateTextWithBackground(int id, String text, int x, int y, int marginX, int marginY, int color, int bgColor, boolean shadow, double displayDurationModifier, int layer, double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
		elementUpdates.put(id,new TextWithBackgroundObjectUpdate(text,x,y,marginX,marginY,color,bgColor,shadow,displayDurationModifier,layer,scale_x,scale_y,rotation,diff_x,diff_y));
	}





	public int addItem(String item, int x, int y, double displayDuration, int layer) {
		ItemStack itemStack;
		try {
			itemStack=this.getItemStack(item);
		} catch (CommandSyntaxException e) {
			throw new NoSuchElementException("No item of name '"+item+"' exists!");
		}
		int i=this.getId();
		elements.put(i, new ItemObject(itemStack, x, y, displayDuration, layer));
		return i;
	}

	public int addAdvancedItem(String item, int x, int y, double displayDuration, int layer, double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
		ItemStack itemStack;
		try {
			itemStack=this.getItemStack(item);
		} catch (CommandSyntaxException e) {
			throw new NoSuchElementException("No item of name '"+item+"' exists!");
		}
		int i=this.getId();
		elements.put(i, new ItemObject(itemStack, x, y, displayDuration, layer,scale_x,scale_y,rotation,diff_x,diff_y));
		return i;
	}

	public JsonableItemObject getItemObject(int id) {
		return new JsonableItemObject((ItemObject) elements.get(id));
	}

	public void updateItem(int id, String item, int x, int y, double displayDurationModifier, int layer, double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
		if (!Registries.ITEM.containsId(Identifier.ofVanilla(item))) {
			throw new NoSuchElementException("No item of name '"+item+"' exists!");
		}
		ItemStack itemStack=new ItemStack(Registries.ITEM.get(Identifier.ofVanilla(item)));
		elementUpdates.put(id,new ItemObjectUpdate(itemStack,x,y,displayDurationModifier,layer,scale_x,scale_y,rotation,diff_x,diff_y));
	}





	public int addTexture(String texture, boolean vanilla, int x, int y, int width, int height, double alpha, double displayDuration, int layer) {
		Identifier t;
		if (vanilla) {
			t=Identifier.ofVanilla(texture);
		} else {
			t=Identifier.of(MinescriptHUDAddon.MOD_ID,texture);
		}
		int i=this.getId();
		elements.put(i, new TextureObject(t, x, y, width, height, (float) alpha, displayDuration, layer));
		return i;
	}

	public int addAdvancedTexture(String texture, boolean vanilla, int x, int y, int width, int height, double alpha, double displayDuration, int layer, double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
		Identifier t;
		if (vanilla) {
			t=Identifier.ofVanilla(texture);
		} else {
			t=Identifier.of(MinescriptHUDAddon.MOD_ID,texture);
		}
		int i=this.getId();
		elements.put(i, new TextureObject(t, x, y, width, height, (float) alpha, displayDuration, layer,scale_x,scale_y,rotation,diff_x,diff_y));
		return i;
	}

	public JsonableTextureObject getTextureObject(int id) {
		return new JsonableTextureObject((TextureObject) elements.get(id));
	}

	public void updateTexture(int id, String texture, boolean vanilla, int x, int y, int width, int height, double alpha, double displayDurationModifier, int layer, double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
		Identifier t;
		if (vanilla) {
			t=Identifier.ofVanilla(texture);
		} else {
			t=Identifier.of(MinescriptHUDAddon.MOD_ID,texture);
		}
		elementUpdates.put(id,new TextureObjectUpdate(t,x,y, width, height, (float) alpha,displayDurationModifier,layer,scale_x,scale_y,rotation,diff_x,diff_y));
	}





	public void tick(RenderTickCounter renderTickCounter) {
		float deltaSeconds = renderTickCounter.getDynamicDeltaTicks()/20.0f;

		Iterator<Map.Entry<Integer, Layered>> iter=elements.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<Integer, Layered> entry=iter.next();
			Layered t=entry.getValue();
			t.setDisplayDuration(t.getDisplayDuration()-deltaSeconds);
			if (t.getDisplayDuration() <= 0) {
				iter.remove();
			}
		}
		if (elements.isEmpty()) {
			currentId=0;
		}
	}

	public void update() {
		for (Map.Entry<Integer,LayeredUpdate> entry:elementUpdates.entrySet()) {
			entry.getValue().applyTo(elements.get(entry.getKey()));
		}
	}

	private Map<Integer,List<Layered>> getElementsSortedByLayer() {
		return elements.values().stream().collect(Collectors.groupingBy(Layered::getLayer));
	}

	public void renderElement(DrawContext context,MinecraftClient client, Layered element) {
		switch (element) {
			case TextObject t ->
				context.drawText(client.textRenderer, t.getText(), t.getX(), t.getY(), t.getColor(), t.getShadow());
			case RectangleObject b ->
				context.fill(b.getStartX(), b.getStartY(), b.getEndX(), b.getEndY(), b.getColor());
			case GradientRectangleObject b ->
				context.fillGradient(b.getStartX(),b.getStartY(),b.getEndX(),b.getEndY(),b.getStartColor(),b.getEndColor());
			case TextWithBackgroundObject t -> {
				context.fill(t.getX()-t.getMarginX(), t.getY()-t.getMarginY(), t.getX()+client.textRenderer.getWidth(t.getText())-1+t.getMarginX(), t.getY()+client.textRenderer.fontHeight-2+t.getMarginY(), t.getBgColor());
				context.drawText(client.textRenderer, t.getText(), t.getX(), t.getY(), t.getColor(), t.getShadow());
			}
			case ItemObject i ->
				context.drawItem(i.getItem(),i.getX(),i.getY());
			case TextureObject t ->
				context.drawGuiTexture(RenderPipelines.GUI_TEXTURED,t.getTexture(),t.getX(),t.getY(),t.getWidth(),t.getHeight(),t.getAlpha());
			default -> throw new IllegalStateException("Unexpected value: " + element);
		}
	}

	public void render(DrawContext context) {
		final MinecraftClient client=MinecraftClient.getInstance();
		Map<Integer,List<Layered>> layers=this.getElementsSortedByLayer();
		context.getMatrices().pushMatrix();
		for (List<Layered> layer:layers.values()) {
			for (Layered element:layer) {
				context.getMatrices().set(element.getMatrix());
				this.renderElement(context,client,element);
			}
		}
		context.getMatrices().popMatrix();
	}

	public void draw(DrawContext context, RenderTickCounter renderTickCounter) {
		ScriptFrameWaiter.getInstance().onEndFrame();
		this.update();
		this.tick(renderTickCounter);
		this.render(context);
	}





	public interface Layered {
		int getLayer();
		void setLayer(int newLayer);
		Matrix3x2f getMatrix();
		void setMatrix(Matrix3x2f newMatrix);
		Map<String,Double> getMatrixInfo();
		double getDisplayDuration();
		void setDisplayDuration(double newDisplayDuration);
	}

	public interface LayeredUpdate {
		void applyTo(Layered target);
	}

	public static class JsonableMouseObject extends Jsonable {
		public double x;
		public double y;
		public boolean left;
		public boolean middle;
		public boolean right;

		public JsonableMouseObject(Mouse from) {
			this.x=from.getScaledX(MinecraftClient.getInstance().getWindow());
			this.y=from.getScaledY(MinecraftClient.getInstance().getWindow());
			this.left=from.wasLeftButtonClicked();
			this.middle=from.wasMiddleButtonClicked();
			this.right=from.wasRightButtonClicked();
		}
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

		public JsonableTextObject(TextObject from) {
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
			TextObject to=(TextObject)target;
			to.setText(this.text);
			to.setX(this.x);
			to.setY(this.y);
			to.setColor(this.color);
			to.setShadow(this.shadow);
			to.setDisplayDuration(to.getDisplayDuration()+this.displayDurationModifier);
			to.setLayer(this.layer);
			to.getMatrixInfo().put("scale_x",this.scale_x);to.getMatrixInfo().put("scale_y",this.scale_y);to.getMatrixInfo().put("rotation",this.rotation);to.getMatrixInfo().put("diff_x",this.diff_x);to.getMatrixInfo().put("diff_y",this.diff_y);
			to.setMatrix(DrawHelper.createMatrx(x,y,MinecraftClient.getInstance().textRenderer.getWidth(text),MinecraftClient.getInstance().textRenderer.fontHeight,scale_x,scale_y,rotation,diff_x,diff_y));
		}
	}

	public static class TextObject implements Layered {
		private String text;
		private int x;
		private int y;
		private int color;
		private boolean shadow;
		private double displayDuration;
		private final Map<String, Double> matrix_info=new HashMap<>();
		private Matrix3x2f matrix;
		private int layer;
		public TextObject(String text, int x, int y, int color, boolean shadow, double displayDuration, int layer) {
			this.text=text;
			this.x=x;
			this.y=y;
			this.color=color;
			this.shadow=shadow;
			this.displayDuration=displayDuration;
			this.matrix_info.put("scale_x",1d);this.matrix_info.put("scale_y",1d);this.matrix_info.put("rotation",0d);this.matrix_info.put("diff_x",0d);this.matrix_info.put("diff_y",0d);
			this.matrix=new Matrix3x2f();
			this.layer=layer;
		}

		public TextObject(String text, int x, int y, int color, boolean shadow, double displayDuration, int layer, double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
			this.text=text;
			this.x=x;
			this.y=y;
			this.color=color;
			this.shadow=shadow;
			this.displayDuration=displayDuration;
			this.matrix_info.put("scale_x",scale_x);this.matrix_info.put("scale_y",scale_y);this.matrix_info.put("rotation",rotation);this.matrix_info.put("diff_x",diff_x);this.matrix_info.put("diff_y",diff_y);
			this.matrix=DrawHelper.createMatrx(x,y,MinecraftClient.getInstance().textRenderer.getWidth(text),MinecraftClient.getInstance().textRenderer.fontHeight,scale_x,scale_y,rotation,diff_x,diff_y);
			this.layer=layer;
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

		public Matrix3x2f getMatrix() {
			return matrix;
		}

		@Override
		public double getDisplayDuration() {
			return displayDuration;
		}

		@Override
		public void setDisplayDuration(double newDisplayDuration) {
			displayDuration=newDisplayDuration;
		}

		@Override
		public void setMatrix(Matrix3x2f newMatrix) {
			matrix=newMatrix;
		}

		@Override
		public Map<String,Double> getMatrixInfo() {
			return matrix_info;
		}

		@Override
		public int getLayer() {
			return layer;
		}

		@Override
		public void setLayer(int newLayer) {
			layer=newLayer;
		}
	}





	public static class JsonableRectangleObject extends Jsonable {
		public int sx;
		public int sy;
		public int ex;
		public int ey;
		public int color;
		public double displayDuration;
		public int layer;

		public JsonableRectangleObject(RectangleObject from) {
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
			RectangleObject to=(RectangleObject)target;
			to.setStartX(this.sx);
			to.setStartY(this.sy);
			to.setEndX(this.ex);
			to.setEndY(this.ey);
			to.setColor(this.color);
			to.setDisplayDuration(to.getDisplayDuration()+this.displayDurationModifier);
			to.setLayer(this.layer);
		}
	}

	public static class RectangleObject implements Layered {
		private int sx;
		private int sy;
		private int ex;
		private int ey;
		private int color;
		private double displayDuration;
		private int layer;

		public RectangleObject(int sx, int sy, int ex, int ey, int color, double displayDuration, int layer) {
			this.sx=sx;
			this.sy=sy;
			this.ex=ex;
			this.ey=ey;
			this.color=color;
			this.displayDuration=displayDuration;
			this.layer=layer;
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
		public int getLayer() {
			return layer;
		}

		@Override
		public void setLayer(int newLayer) {
			layer=newLayer;
		}

		@Override
		public Matrix3x2f getMatrix() {
			return new Matrix3x2f();
		}

		@Override
		public void setMatrix(Matrix3x2f newMatrix) {}

		@Override
		public Map<String, Double> getMatrixInfo() {
			return Map.of();
		}

		@Override
		public double getDisplayDuration() {
			return displayDuration;
		}

		@Override
		public void setDisplayDuration(double newDisplayDuration) {
			displayDuration=newDisplayDuration;
		}
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

		public JsonableGradientRectangleObject(GradientRectangleObject from) {
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
			GradientRectangleObject to=(GradientRectangleObject)target;
			to.setStartX(this.sx);
			to.setStartY(this.sy);
			to.setEndX(this.ex);
			to.setEndY(this.ey);
			to.setStartColor(this.startColor);
			to.setEndColor(this.endColor);
			to.setDisplayDuration(to.getDisplayDuration()+this.displayDurationModifier);
			to.setLayer(this.layer);
		}
	}

	public static class GradientRectangleObject implements Layered {
		private int sx;
		private int sy;
		private int ex;
		private int ey;
		private int startColor;
		private int endColor;
		private double displayDuration;
		private int layer;

		public GradientRectangleObject(int sx, int sy, int ex, int ey, int startColor, int endColor, double displayDuration, int layer) {
			this.sx=sx;
			this.sy=sy;
			this.ex=ex;
			this.ey=ey;
			this.startColor=startColor;
			this.endColor=endColor;
			this.displayDuration=displayDuration;
			this.layer=layer;
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
		public int getLayer() {
			return layer;
		}

		@Override
		public void setLayer(int newLayer) {
			layer=newLayer;
		}

		@Override
		public Matrix3x2f getMatrix() {
			return new Matrix3x2f();
		}

		@Override
		public void setMatrix(Matrix3x2f newMatrix) {}

		@Override
		public Map<String, Double> getMatrixInfo() {
			return Map.of();
		}

		@Override
		public double getDisplayDuration() {
			return displayDuration;
		}

		@Override
		public void setDisplayDuration(double newDisplayDuration) {
			displayDuration=newDisplayDuration;
		}
	}

	public static class JsonableStrokedRectangleObject extends Jsonable {
		public int x;
		public int y;
		public int width;
		public int height;
		public int color;
		public double displayDuration;
		public int layer;

		public JsonableStrokedRectangleObject(StrokedRectangleObject from) {
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
			StrokedRectangleObject to=(StrokedRectangleObject)target;
			to.setX(this.x);
			to.setY(this.y);
			to.setWidth(this.ex);
			to.setHeight(this.ey);
			to.setColor(this.color);
			to.setDisplayDuration(to.getDisplayDuration()+this.displayDurationModifier);
			to.setLayer(this.layer);
		}
	}

	public static class StrokedRectangleObject implements Layered {
		private int x;
		private int y;
		private int width;
		private int height;
		private int color;
		private double displayDuration;
		private int layer;

		public StrokedRectangleObject(int x, int y, int width, int height, int color, double displayDuration, int layer) {
			this.x=x;
			this.y=y;
			this.width=width;
			this.height=height;
			this.color=color;
			this.displayDuration=displayDuration;
			this.layer=layer;
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
		public int getLayer() {
			return layer;
		}

		@Override
		public void setLayer(int newLayer) {
			layer=newLayer;
		}

		@Override
		public Matrix3x2f getMatrix() {
			return new Matrix3x2f();
		}

		@Override
		public void setMatrix(Matrix3x2f newMatrix) {}

		@Override
		public Map<String, Double> getMatrixInfo() {
			return Map.of();
		}

		@Override
		public double getDisplayDuration() {
			return displayDuration;
		}

		@Override
		public void setDisplayDuration(double newDisplayDuration) {
			displayDuration=newDisplayDuration;
		}
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

		public JsonableTextWithBackgroundObject(TextWithBackgroundObject from) {
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
			TextWithBackgroundObject to=(TextWithBackgroundObject)target;
			to.setText(this.text);
			to.setX(this.x);
			to.setY(this.y);
			to.setMarginX(this.marginX);
			to.setMarginY(this.marginY);
			to.setColor(this.color);
			to.setBgColor(bgColor);
			to.setShadow(this.shadow);
			to.setDisplayDuration(to.getDisplayDuration()+this.displayDurationModifier);
			to.setLayer(this.layer);
			to.getMatrixInfo().put("scale_x",this.scale_x);to.getMatrixInfo().put("scale_y",this.scale_y);to.getMatrixInfo().put("rotation",this.rotation);to.getMatrixInfo().put("diff_x",this.diff_x);to.getMatrixInfo().put("diff_y",this.diff_y);
			to.setMatrix(DrawHelper.createMatrx(x,y,MinecraftClient.getInstance().textRenderer.getWidth(text),MinecraftClient.getInstance().textRenderer.fontHeight,scale_x,scale_y,rotation,diff_x,diff_y));
		}
	}

	public static class TextWithBackgroundObject implements Layered {
		private String text;
		private int x;
		private int y;
		private int marginX;
		private int marginY;
		private int color;
		private int bgColor;
		private boolean shadow;
		private double displayDuration;
		private final Map<String, Double> matrix_info=new HashMap<>();
		private Matrix3x2f matrix;
		private int layer;
		public TextWithBackgroundObject(String text, int x, int y, int marginX, int marginY, int color, int bgColor, boolean shadow, double displayDuration, int layer) {
			this.text=text;
			this.x=x;
			this.y=y;
			this.marginX=marginX;
			this.marginY=marginY;
			this.color=color;
			this.bgColor=bgColor;
			this.shadow=shadow;
			this.displayDuration=displayDuration;
			this.matrix_info.put("scale_x",1d);this.matrix_info.put("scale_y",1d);this.matrix_info.put("rotation",0d);this.matrix_info.put("diff_x",0d);this.matrix_info.put("diff_y",0d);
			this.matrix=new Matrix3x2f();
			this.layer=layer;
		}

		public TextWithBackgroundObject(String text, int x, int y, int marginX, int marginY, int color, int bgColor, boolean shadow, double displayDuration, int layer, double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
			this.text=text;
			this.x=x;
			this.y=y;
			this.marginX=marginX;
			this.marginY=marginY;
			this.color=color;
			this.bgColor=bgColor;
			this.shadow=shadow;
			this.displayDuration=displayDuration;
			this.matrix_info.put("scale_x",scale_x);this.matrix_info.put("scale_y",scale_y);this.matrix_info.put("rotation",rotation);this.matrix_info.put("diff_x",diff_x);this.matrix_info.put("diff_y",diff_y);
			this.matrix=DrawHelper.createMatrx(x,y,MinecraftClient.getInstance().textRenderer.getWidth(text),MinecraftClient.getInstance().textRenderer.fontHeight,scale_x,scale_y,rotation,diff_x,diff_y);
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

		public Matrix3x2f getMatrix() {
			return matrix;
		}

		@Override
		public double getDisplayDuration() {
			return displayDuration;
		}

		@Override
		public void setDisplayDuration(double newDisplayDuration) {
			displayDuration=newDisplayDuration;
		}

		@Override
		public void setMatrix(Matrix3x2f newMatrix) {
			matrix=newMatrix;
		}

		@Override
		public Map<String,Double> getMatrixInfo() {
			return matrix_info;
		}

		@Override
		public int getLayer() {
			return layer;
		}

		@Override
		public void setLayer(int newLayer) {
			layer=newLayer;
		}
	}





	public static class JsonableItemObject extends Jsonable {
		public String item;
		public int x;
		public int y;
		public double displayDuration;
		public int layer;
		public double scale_x;
		public double scale_y;
		public double rotation;
		public double diff_x;
		public double diff_y;
		public JsonableItemObject(ItemObject from) {
			if (Objects.isNull(from)) {
				return;
			}
			this.item=Registries.ITEM.getId(from.getItem().getItem()).getPath();
			this.x=from.getX();
			this.y=from.getY();
			this.scale_x=from.getMatrixInfo().get("scale_x");
			this.scale_y=from.getMatrixInfo().get("scale_y");
			this.rotation=from.getMatrixInfo().get("rotation");
			this.diff_x=from.getMatrixInfo().get("diff_x");
			this.diff_y=from.getMatrixInfo().get("diff_y");
			this.displayDuration=from.getDisplayDuration();
			this.layer=from.getLayer();
		}
	}

	public static class ItemObjectUpdate implements LayeredUpdate {
		private final ItemStack item;
		private final int x;
		private final int y;
		private final double displayDurationModifier;
		private final int layer;
		private final double scale_x;
		private final double scale_y;
		private final double rotation;
		private final double diff_x;
		private final double diff_y;
		public ItemObjectUpdate(ItemStack item, int x, int y, double displayDurationModifier, int layer, double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
			this.item=item;
			this.x=x;
			this.y=y;
			this.displayDurationModifier=displayDurationModifier;
			this.layer=layer;
			this.scale_x=scale_x;this.scale_y=scale_y;this.rotation=rotation;this.diff_x=diff_x;this.diff_y=diff_y;
		}

		@Override
		public void applyTo(Layered target) {
			if (Objects.isNull(target)) {
				return;
			}
			ItemObject to=(ItemObject)target;
			to.setItem(this.item);
			to.setX(this.x);
			to.setY(this.y);
			to.setDisplayDuration(to.getDisplayDuration()+displayDurationModifier);
			to.setLayer(this.layer);
			to.getMatrixInfo().put("scale_x",this.scale_x);to.getMatrixInfo().put("scale_y",this.scale_y);to.getMatrixInfo().put("rotation",this.rotation);to.getMatrixInfo().put("diff_x",this.diff_x);to.getMatrixInfo().put("diff_y",this.diff_y);
			to.setMatrix(DrawHelper.createMatrx(x,y,16,16,scale_x,scale_y,rotation,diff_x,diff_y));
		}
	}

	public static class ItemObject implements Layered {
		private ItemStack item;
		private int x;
		private int y;
		private double displayDuration;
		private final Map<String, Double> matrix_info=new HashMap<>();
		private Matrix3x2f matrix;
		private int layer;
		public ItemObject(ItemStack item, int x, int y, double displayDuration, int layer) {
			this.item=item;
			this.x=x;
			this.y=y;
			this.displayDuration=displayDuration;
			this.matrix_info.put("scale_x",1d);this.matrix_info.put("scale_y",1d);this.matrix_info.put("rotation",0d);this.matrix_info.put("diff_x",0d);this.matrix_info.put("diff_y",0d);
			this.matrix=new Matrix3x2f();
			this.layer=layer;
		}

		public ItemObject(ItemStack item, int x, int y, double displayDuration, int layer, double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
			this.item=item;
			this.x=x;
			this.y=y;
			this.displayDuration=displayDuration;
			this.matrix_info.put("scale_x",scale_x);this.matrix_info.put("scale_y",scale_y);this.matrix_info.put("rotation",rotation);this.matrix_info.put("diff_x",diff_x);this.matrix_info.put("diff_y",diff_y);
			this.matrix=DrawHelper.createMatrx(x,y,16,16,scale_x,scale_y,rotation,diff_x,diff_y);
			this.layer=layer;
		}

		public ItemStack getItem() {
			return item;
		}

		public void setItem(ItemStack newItem) {
			item=newItem;
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

		@Override
		public int getLayer() {
			return layer;
		}

		@Override
		public void setLayer(int newLayer) {
			layer=newLayer;
		}

		@Override
		public Matrix3x2f getMatrix() {
			return matrix;
		}

		@Override
		public void setMatrix(Matrix3x2f newMatrix) {
			matrix=newMatrix;
		}

		@Override
		public Map<String, Double> getMatrixInfo() {
			return matrix_info;
		}

		@Override
		public double getDisplayDuration() {
			return displayDuration;
		}

		@Override
		public void setDisplayDuration(double newDisplayDuration) {
			displayDuration=newDisplayDuration;
		}
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
		public JsonableTextureObject(TextureObject from) {
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
			TextureObject to=(TextureObject)target;
			to.setTexture(this.texture);
			to.setX(this.x);
			to.setY(this.y);
			to.setWidth(this.width);
			to.setHeight(this.height);
			to.setAlpha(this.alpha);
			to.setDisplayDuration(to.getDisplayDuration()+displayDurationModifier);
			to.setLayer(this.layer);
			to.getMatrixInfo().put("scale_x",this.scale_x);to.getMatrixInfo().put("scale_y",this.scale_y);to.getMatrixInfo().put("rotation",this.rotation);to.getMatrixInfo().put("diff_x",this.diff_x);to.getMatrixInfo().put("diff_y",this.diff_y);
			to.setMatrix(DrawHelper.createMatrx(x,y,width,height,scale_x,scale_y,rotation,diff_x,diff_y));
		}
	}

	public static class TextureObject implements Layered {
		private Identifier texture;
		private int x;
		private int y;
		private int width;
		private int height;
		private float alpha;
		private double displayDuration;
		private final Map<String, Double> matrix_info=new HashMap<>();
		private Matrix3x2f matrix;
		private int layer;
		public TextureObject(Identifier texture, int x, int y, int width, int height, float alpha, double displayDuration, int layer) {
			this.texture=texture;
			this.x=x;
			this.y=y;
			this.width=width;
			this.height=height;
			this.alpha=alpha;
			this.displayDuration=displayDuration;
			this.matrix_info.put("scale_x",1d);this.matrix_info.put("scale_y",1d);this.matrix_info.put("rotation",0d);this.matrix_info.put("diff_x",0d);this.matrix_info.put("diff_y",0d);
			this.matrix=new Matrix3x2f();
			this.layer=layer;
		}
		public TextureObject(Identifier texture, int x, int y, int width, int height, float alpha, double displayDuration, int layer, double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
			this.texture=texture;
			this.x=x;
			this.y=y;
			this.width=width;
			this.height=height;
			this.alpha=alpha;
			this.displayDuration=displayDuration;
			this.matrix_info.put("scale_x",scale_x);this.matrix_info.put("scale_y",scale_y);this.matrix_info.put("rotation",rotation);this.matrix_info.put("diff_x",diff_x);this.matrix_info.put("diff_y",diff_y);
			this.matrix=DrawHelper.createMatrx(x,y,width,height,scale_x,scale_y,rotation,diff_x,diff_y);
			this.layer=layer;
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
		public int getLayer() {
			return layer;
		}

		@Override
		public void setLayer(int newLayer) {
			layer=newLayer;
		}

		@Override
		public Matrix3x2f getMatrix() {
			return matrix;
		}

		@Override
		public void setMatrix(Matrix3x2f newMatrix) {
			matrix=newMatrix;
		}

		@Override
		public Map<String, Double> getMatrixInfo() {
			return matrix_info;
		}

		@Override
		public double getDisplayDuration() {
			return displayDuration;
		}

		@Override
		public void setDisplayDuration(double newDisplayDuration) {
			displayDuration=newDisplayDuration;
		}
	}
}
