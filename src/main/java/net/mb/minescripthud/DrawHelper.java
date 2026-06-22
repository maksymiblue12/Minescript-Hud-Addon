package net.mb.minescripthud;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.mb.minescripthud.util.MouseListener;
import net.mb.minescripthud.util.MouseTracker;
import net.mb.minescripthud.util.ShapeGuiElementRenderState;
import net.mb.minescripthud.util.Vertex;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStringReader;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minescript.common.Jsonable;
import net.minescript.common.ScriptValue;
import org.joml.*;

import java.util.*;
import java.util.stream.Collectors;

public class DrawHelper {
	private static final DrawHelper INSTANCE=new DrawHelper();
	private final Map<Integer,Layered> elements=new HashMap<>();
	private final Map<Integer,LayeredUpdate> elementUpdates=new HashMap<>();
	private final Map<Integer, MouseListener> mouseListeners=new HashMap<>();
	private int currentId=0;
	public int windowWidth=0;
	public int windowHeight=0;

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
		mouseListeners.clear();
	}

	public void removeElement(int id) {
		elements.remove(id);
		elementUpdates.remove(id);
		mouseListeners.remove(id);
	}

	public boolean stillExists(int id) {
		return elements.containsKey(id);
	}

	private ItemStack getItemStack(String item) throws CommandSyntaxException {
		int amount=1;
		if (item.matches(".* [0-9]+")) {
			amount=Integer.parseInt(item.substring(item.strip().lastIndexOf(' ')+1));
		}
		//noinspection DataFlowIssue
		ItemStringReader.ItemResult itemResult=new ItemStringReader(MinecraftClient.getInstance().getNetworkHandler().getRegistryManager()).consume(new StringReader(item));
		return new ItemStackArgument(itemResult.item(),itemResult.components()).createStack(amount,false);
	}

	public static Matrix3x2f createMatrix(int x, int y, int w, int h, double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
		float cx=(w/2f);
		float cy=(h/2f);
		return new Matrix3x2f()
				.translate(cx,cy)
				.rotateAbout((float) rotation,x,y)
				.translate(-cx,-cy)
				.scale((float)scale_x,(float)scale_y)
				.translate((float) (diff_x-(double)x*((scale_x-1)/scale_x)),(float) (diff_y-(double)y*((scale_y-1)/scale_y)));
	}

	@SuppressWarnings("unchecked")
	public void batchUpdate(List<Map<String, Object>> updates) {
		for (Map<String, Object> upd:updates) {
			List<Object> data=(List<Object>)upd.get("data");
			switch ((String)upd.get("type")) {
				case "text" -> updateText(((Double)upd.get("id")).intValue(),(String)data.get(0),((Double)data.get(1)).intValue(),((Double)data.get(2)).intValue(),((Double)data.get(3)).intValue(),(boolean)data.get(4),(double)data.get(5),((Double)data.get(6)).intValue(),(double)data.get(7),(double)data.get(8),(double)data.get(9),(double)data.get(10),(double)data.get(11));
				case "rectangle" -> updateRectangle(((Double)upd.get("id")).intValue(),((Double)data.get(0)).intValue(),((Double)data.get(1)).intValue(),((Double)data.get(2)).intValue(),((Double)data.get(3)).intValue(),((Double)data.get(4)).intValue(),(double)data.get(5),((Double)data.get(6)).intValue());
				case "gradient_rectangle" -> updateGradientRectangle(((Double)upd.get("id")).intValue(),((Double)data.get(0)).intValue(),((Double)data.get(1)).intValue(),((Double)data.get(2)).intValue(),((Double)data.get(3)).intValue(),((Double)data.get(4)).intValue(),((Double)data.get(5)).intValue(),(double)data.get(6),((Double)data.get(7)).intValue());
				case "text_with_background" -> updateTextWithBackground(((Double)upd.get("id")).intValue(),(String)data.get(0),((Double)data.get(1)).intValue(),((Double)data.get(2)).intValue(),((Double)data.get(3)).intValue(),((Double)data.get(4)).intValue(),((Double)data.get(5)).intValue(),((Double)data.get(6)).intValue(),(boolean)data.get(7),(double)data.get(8),((Double)data.get(9)).intValue(),(double)data.get(10),(double)data.get(11),(double)data.get(12),(double)data.get(13),(double)data.get(14));
				case "item" -> updateItem(((Double)upd.get("id")).intValue(),(String)data.get(0),((Double)data.get(1)).intValue(),((Double)data.get(2)).intValue(),(double)data.get(3),((Double)data.get(4)).intValue(),(double)data.get(5),(double)data.get(6),(double)data.get(7),(double)data.get(8),(double)data.get(9));
				case "texture" -> updateTexture(((Double)upd.get("id")).intValue(),(String)data.get(0),(boolean)data.get(1),((Double)data.get(2)).intValue(),((Double)data.get(3)).intValue(),((Double)data.get(4)).intValue(),((Double)data.get(5)).intValue(),(double)data.get(6),(double)data.get(7),((Double)data.get(8)).intValue(),(double)data.get(9),(double)data.get(10),(double)data.get(11),(double)data.get(12),(double)data.get(13));
				case "shape" -> updateShape(((Double)upd.get("id")).intValue(),(List<Map<String,Double>>)data.get(0),(double)data.get(1),((Double)data.get(2)).intValue(),(double)data.get(3),(double)data.get(4),(double)data.get(5),(double)data.get(6),(double)data.get(7));
			}
		}
	}

	public Set<Integer> getStillExisting() {
		return elements.keySet();
	}

	public static class JsonableElements extends Jsonable {
		public Map<Integer,Jsonable> elements;
		public boolean successful;
		public JsonableElements(Map<Integer,Jsonable> elements,boolean successful) {
			this.elements=elements;
			this.successful=successful;
		}
	}

	public JsonableElements getElements(List<Double> ids) {
		Map<Integer,Jsonable> out=new HashMap<>();
		for (Double _id:ids) {
			int id=_id.intValue();
			if (!elements.containsKey(id)) {
				return new JsonableElements(out,false);
			}
			out.put(id,elements.get(id).toJsonable());
		}
		return new JsonableElements(out,true);
	}

	public void addMouseListener(int id, MouseListener listener) {
		mouseListeners.put(id,listener);
	}

	public MouseListener getMouseListener(int id) {
		return mouseListeners.get(id);
	}

	public Jsonable getElement(int id) {
		if (elements.containsKey(id)) {
			return elements.get(id).toJsonable();
		}
		return null;
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

	public void updateText(int id, String text, int x, int y, int color, boolean shadow, double displayDurationModifier, int layer, double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
		elementUpdates.put(id,new TextObjectUpdate(text,x,y,color,shadow,displayDurationModifier,layer,scale_x,scale_y,rotation,diff_x,diff_y));
	}





	public int addRectangle(int sx, int sy, int ex, int ey, int color, double displayDuration, int layer) {
		int i=this.getId();
		elements.put(i,new RectangleObject(sx,sy,ex,ey,color,displayDuration,layer));
		return i;
	}

	public void updateRectangle(int id, int sx, int sy, int ex, int ey, int color, double displayDurationModifier, int layer) {
		elementUpdates.put(id,new RectangleObjectUpdate(sx,sy,ex,ey,color,displayDurationModifier,layer));
	}

	public int addGradientRectangle(int sx, int sy, int ex, int ey, int startColor, int endColor, double displayDuration, int layer) {
		int i=this.getId();
		elements.put(i,new GradientRectangleObject(sx,sy,ex,ey,startColor,endColor,displayDuration,layer));
		return i;
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

	public void updateItem(int id, String item, int x, int y, double displayDurationModifier, int layer, double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
		ItemStack itemStack;
		try {
			itemStack=this.getItemStack(item);
		} catch (CommandSyntaxException e) {
			throw new NoSuchElementException("No item of name '"+item+"' exists!");
		}
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

	public void updateTexture(int id, String texture, boolean vanilla, int x, int y, int width, int height, double alpha, double displayDurationModifier, int layer, double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
		Identifier t;
		if (vanilla) {
			t=Identifier.ofVanilla(texture);
		} else {
			t=Identifier.of(MinescriptHUDAddon.MOD_ID,texture);
		}
		elementUpdates.put(id,new TextureObjectUpdate(t,x,y, width, height, (float) alpha,displayDurationModifier,layer,scale_x,scale_y,rotation,diff_x,diff_y));
	}





	public int addShape(List<Map<String, Double>> vertices, double displayDuration, int layer) {
		List<Vertex> verticesFormated=vertices.stream().map(v -> new Vertex(v.get("x").intValue(),v.get("y").intValue(),v.get("color").intValue())).toList();
		int i=this.getId();
		elements.put(i, new ShapeObject(verticesFormated, displayDuration, layer));
		return i;
	}

	public int addAdvancedShape(List<Map<String, Double>> vertices, double displayDuration, int layer, double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
		List<Vertex> verticesFormated=vertices.stream().map(v -> new Vertex(v.get("x").intValue(),v.get("y").intValue(),v.get("color").intValue())).toList();
		int i=this.getId();
		elements.put(i, new ShapeObject(verticesFormated, displayDuration, layer,scale_x,scale_y,rotation,diff_x,diff_y));
		return i;
	}

	public void updateShape(int id, List<Map<String, Double>> vertices, double displayDurationModifier, int layer, double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
		List<Vertex> verticesFormated=vertices.stream().map(v -> new Vertex(v.get("x").intValue(),v.get("y").intValue(),v.get("color").intValue())).toList();
		elementUpdates.put(id,new ShapeObjectUpdate(verticesFormated,displayDurationModifier,layer,scale_x,scale_y,rotation,diff_x,diff_y));
	}





	public void tick(RenderTickCounter renderTickCounter) {
		float deltaSeconds = renderTickCounter.getDynamicDeltaTicks()/20.0f;

		Iterator<Map.Entry<Integer, Layered>> iter=elements.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<Integer, Layered> entry=iter.next();
			Integer key=entry.getKey();
			Layered t=entry.getValue();
			t.setDisplayDuration(t.getDisplayDuration()-deltaSeconds);
			if (t.getDisplayDuration()<=0) {
				elementUpdates.remove(key);
				mouseListeners.remove(key);
				iter.remove();
			}
		}
		if (elements.isEmpty()) {
			currentId=0;
			clear();
		}
	}

	public void update() {
		for (Map.Entry<Integer,LayeredUpdate> entry:elementUpdates.entrySet()) {
			entry.getValue().applyTo(elements.get(entry.getKey()));
		}
	}

	public void checkMouse() {
		if (MouseTracker.getInstance().isCursorLocked()) {
			return;
		}
		for (Map.Entry<Integer, Layered> element:elements.entrySet()) {
			Integer id=element.getKey();
			Layered obj=element.getValue();
			if (!mouseListeners.containsKey(id)) {
				continue;
			} else if (!mouseListeners.get(id).isActive()) {
				continue;
			}
			if (obj.containsPoint(MouseTracker.getInstance().getX(), MouseTracker.getInstance().getY())) {
				obj.setHovering(true);
				JsonObject data=new JsonObject();
				data.add("object",obj.toJsonable().toJson());
				data.add("object_type",new JsonPrimitive(obj.getObjectType()));
				try {
					data.add("mouse",MouseTracker.getInstance().toJsonable().toJson());
				} catch (IllegalArgumentException e) {
					data.add("mouse", JsonNull.INSTANCE);
				}
				data.add("exited",new JsonPrimitive(false));
				data.add("event_type",new JsonPrimitive("hover"));
				mouseListeners.get(id).respond(ScriptValue.of(data,()-> data));
				if (MouseTracker.getInstance().anyButtonClicked()) {
					MinescriptHUDAddon.LOGGER.warn("clicked");
					data.add("event_type",new JsonPrimitive("click"));
					mouseListeners.get(id).respond(ScriptValue.of(data,()-> data));
				}
			} else if (obj.getHovering()) {
				obj.setHovering(false);
				JsonObject data=new JsonObject();
				data.add("object",obj.toJsonable().toJson());
				data.add("object_type",new JsonPrimitive(obj.getObjectType()));
				try {
					data.add("mouse", MouseTracker.getInstance().toJsonable().toJson());
				} catch (IllegalArgumentException e) {
					data.add("mouse",JsonNull.INSTANCE);
				}
				data.add("exited",new JsonPrimitive(true));
				data.add("event_type",new JsonPrimitive("hover"));
				mouseListeners.get(id).respond(ScriptValue.of(data,()->data));
			}
		}
	}

	private Map<Integer,List<Layered>> getElementsSortedByLayer() {
		return elements.values().stream().collect(Collectors.groupingBy(Layered::getLayer));
	}

	public void renderElement(DrawContext context, MinecraftClient client, Layered element) {
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
			case ItemObject i -> {
				context.drawItem(i.getItem(), i.getX(), i.getY());
				context.drawStackOverlay(client.textRenderer,i.getItem(),i.getX(),i.getY());
			}
			case TextureObject t ->
					context.drawGuiTexture(RenderPipelines.GUI_TEXTURED,t.getTexture(),t.getX(),t.getY(),t.getWidth(),t.getHeight(),t.getAlpha());
			case ShapeObject s ->
					context.state.addSimpleElement(new ShapeGuiElementRenderState(RenderPipelines.GUI, TextureSetup.empty(), new Matrix3x2f(context.getMatrices()), s.getVertices(), context.scissorStack.peekLast(), s.getBounds()));
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
		MouseTracker.getInstance().update();
		this.update();
		this.tick(renderTickCounter);
		this.render(context);
		this.checkMouse();
		windowWidth=context.getScaledWindowWidth();
		windowHeight=context.getScaledWindowHeight();
	}





	public static abstract class Layered {
		private double displayDuration;
		private int layer;
		private final Map<String, Double> matrix_info=new HashMap<>();
		private Matrix3x2f matrix;
		private boolean hovering=false;
		private Layered(double displayDuration, int layer) {
			this.displayDuration=displayDuration;
			this.layer=layer;
			this.matrix_info.put("scale_x",1d);this.matrix_info.put("scale_y",1d);this.matrix_info.put("rotation",0d);this.matrix_info.put("diff_x",0d);this.matrix_info.put("diff_y",0d);
			this.matrix=new Matrix3x2f();
		}

		private Layered(double displayDuration, int layer, double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
			this.displayDuration=displayDuration;
			this.layer=layer;
			this.matrix_info.put("scale_x",scale_x);this.matrix_info.put("scale_y",scale_y);this.matrix_info.put("rotation",rotation);this.matrix_info.put("diff_x",diff_x);this.matrix_info.put("diff_y",diff_y);
		}

		Matrix3x2f createMatrix(double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
			return new Matrix3x2f();
		}
		int getLayer() {
			return this.layer;
		}
		void setLayer(int newLayer) {
			this.layer=newLayer;
		}
		Matrix3x2f getMatrix() {
			return this.matrix;
		}
		void setMatrix(Matrix3x2f newMatrix) {
			this.matrix=newMatrix;
		}
		Map<String, Double> getMatrixInfo() {
			return this.matrix_info;
		}
		double getDisplayDuration() {
			return this.displayDuration;
		}
		void setDisplayDuration(double newDisplayDuration) {
			this.displayDuration=newDisplayDuration;
		}
		boolean getHovering() {
			return this.hovering;
		}
		void setHovering(boolean newHovering) {
			this.hovering=newHovering;
		}
		boolean containsPoint(double x, double y) {
			return false;
		}
		Jsonable toJsonable() {
			return null;
		}
		String getObjectType() {
			return "";
		}
	}

	public interface LayeredUpdate {
		void applyTo(Layered target);
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
			return new JsonableTextObject(this);
		}

		@Override
		public String getObjectType() {
			return "text";
		}

		@Override
		Matrix3x2f createMatrix(double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
			return DrawHelper.createMatrix(x,y,MinecraftClient.getInstance().textRenderer.getWidth(text),MinecraftClient.getInstance().textRenderer.fontHeight,scale_x,scale_y,rotation,diff_x,diff_y);
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
		public boolean containsPoint(double x, double y) {
			return sx<=x && x<=ex && sy<=y && y<=ey;
		}

		@Override
		public Jsonable toJsonable() {
			return new JsonableRectangleObject(this);
		}

		@Override
		public String getObjectType() {
			return "rectangle";
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
		public boolean containsPoint(double x, double y) {
			return sx<=x && x<=ex && sy<=y && y<=ey;
		}

		@Override
		public Jsonable toJsonable() {
			return new JsonableGradientRectangleObject(this);
		}

		@Override
		public String getObjectType() {
			return "gradient_rectangle";
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
			return new JsonableTextWithBackgroundObject(this);
		}

		@Override
		public String getObjectType() {
			return "text_with_bg";
		}

		@Override
		Matrix3x2f createMatrix(double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
			return DrawHelper.createMatrix(this.x,this.y,MinecraftClient.getInstance().textRenderer.getWidth(this.text),MinecraftClient.getInstance().textRenderer.fontHeight,scale_x,scale_y,rotation,diff_x,diff_y);
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
			to.setDisplayDuration(Double.min(to.getDisplayDuration()+this.displayDurationModifier,Double.MAX_VALUE-1));
			to.setLayer(this.layer);
			to.getMatrixInfo().put("scale_x",this.scale_x);to.getMatrixInfo().put("scale_y",this.scale_y);to.getMatrixInfo().put("rotation",this.rotation);to.getMatrixInfo().put("diff_x",this.diff_x);to.getMatrixInfo().put("diff_y",this.diff_y);
			to.setMatrix(DrawHelper.createMatrix(x,y,16,16,scale_x,scale_y,rotation,diff_x,diff_y));
		}
	}

	public static class ItemObject extends Layered {
		private ItemStack item;
		private int x;
		private int y;
		public ItemObject(ItemStack item, int x, int y, double displayDuration, int layer) {
			super(displayDuration, layer);
			this.item=item;
			this.x=x;
			this.y=y;
		}

		public ItemObject(ItemStack item, int x, int y, double displayDuration, int layer, double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
			super(displayDuration, layer, scale_x, scale_y, rotation, diff_x, diff_y);
			this.item=item;
			this.x=x;
			this.y=y;
			this.setMatrix(this.createMatrix(scale_x, scale_y, rotation, diff_x, diff_y));
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
		public boolean containsPoint(double x, double y) {
			Matrix3x2f inverse=new Matrix3x2f();
			this.getMatrix().invert(inverse);
			Vector2f point=new Vector2f((float)x,(float)y);
			Vector2f topLeft=new Vector2f((float)this.x,(float)this.y);
			Vector2f bottomRight=new Vector2f((float)this.x+16,(float)this.y+16);
			inverse.transformPosition(point);
			return topLeft.x<=point.x && point.x<=bottomRight.x && topLeft.y<=point.y && point.y<=bottomRight.y;
		}

		@Override
		public Jsonable toJsonable() {
			return new JsonableItemObject(this);
		}

		@Override
		public String getObjectType() {
			return "item";
		}

		@Override
		Matrix3x2f createMatrix(double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
			return DrawHelper.createMatrix(x,y,16,16,scale_x,scale_y,rotation,diff_x,diff_y);
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
			return new JsonableTextureObject(this);
		}

		@Override
		public String getObjectType() {
			return "texture";
		}

		@Override
		Matrix3x2f createMatrix(double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
			return DrawHelper.createMatrix(x,y,width,height,scale_x,scale_y,rotation,diff_x,diff_y);
		}
	}





	public static class JsonableShapeObject extends Jsonable {
		public List<Map<String, Integer>> vertices;
		public double displayDuration;
		public int layer;
		public double scale_x;
		public double scale_y;
		public double rotation;
		public double diff_x;
		public double diff_y;
		public JsonableShapeObject(ShapeObject from) {
			if (Objects.isNull(from)) {
				return;
			}
			this.vertices=from.getVertices().stream().map(v -> Map.of("x",v.x(),"y",v.y(),"color",v.color())).toList();
			this.scale_x=from.getMatrixInfo().get("scale_x");
			this.scale_y=from.getMatrixInfo().get("scale_y");
			this.rotation=from.getMatrixInfo().get("rotation");
			this.diff_x=from.getMatrixInfo().get("diff_x");
			this.diff_y=from.getMatrixInfo().get("diff_y");
			this.displayDuration=from.getDisplayDuration();
			this.layer=from.getLayer();
		}
	}

	public static class ShapeObjectUpdate implements LayeredUpdate {
		private final List<Vertex> vertices;
		private final double displayDurationModifier;
		private final int layer;
		private final double scale_x;
		private final double scale_y;
		private final double rotation;
		private final double diff_x;
		private final double diff_y;
		public ShapeObjectUpdate(List<Vertex> vertices, double displayDurationModifier, int layer, double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
			this.vertices=vertices;
			this.displayDurationModifier=displayDurationModifier;
			this.layer=layer;
			this.scale_x=scale_x;this.scale_y=scale_y;this.rotation=rotation;this.diff_x=diff_x;this.diff_y=diff_y;
		}

		@Override
		public void applyTo(Layered target) {
			if (Objects.isNull(target)) {
				return;
			}
			ShapeObject to=(ShapeObject)target;
			to.setVertices(vertices);
			to.updateBounds();
			to.setDisplayDuration(Double.min(to.getDisplayDuration()+this.displayDurationModifier,Double.MAX_VALUE-1));
			to.setLayer(this.layer);
			to.getMatrixInfo().put("scale_x",this.scale_x);to.getMatrixInfo().put("scale_y",this.scale_y);to.getMatrixInfo().put("rotation",this.rotation);to.getMatrixInfo().put("diff_x",this.diff_x);to.getMatrixInfo().put("diff_y",this.diff_y);
			to.setMatrix(DrawHelper.createMatrix(to.getBounds().getLeft(),to.getBounds().getTop(),to.getBounds().width(),to.getBounds().height(),scale_x,scale_y,rotation,diff_x,diff_y));
		}
	}

	public static class ShapeObject extends Layered {
		private List<Vertex> vertices;
		private ScreenRect bounds;
		public ShapeObject(List<Vertex> vertices, double displayDuration, int layer) {
			super(displayDuration, layer);
			this.vertices=vertices;
			this.bounds=this.createBounds();
		}

		public ShapeObject(List<Vertex> vertices, double displayDuration, int layer, double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
			super(displayDuration, layer, scale_x, scale_y, rotation, diff_x, diff_y);
			this.vertices=vertices;
			this.bounds=this.createBounds();
			this.setMatrix(this.createMatrix(scale_x, scale_y, rotation, diff_x, diff_y));
		}

		private ScreenRect createBounds() {
			int left=Integer.MAX_VALUE,right=0,top=Integer.MAX_VALUE,bottom=0;
			for (Vertex v:vertices) {
				if (v.x()>right) right=v.x();
				if (v.x()<left) left=v.x();
				if (v.y()>bottom) bottom=v.y();
				if (v.y()<top) top=v.y();
			}
			return new ScreenRect(left,top,right-left,bottom-top);
		}

		public List<Vertex> getVertices() {
			return vertices;
		}

		public void setVertices(List<Vertex> newVertices) {
			vertices=newVertices;
		}

		public ScreenRect getBounds() {
			return bounds;
		}

		public void updateBounds() {
			bounds=this.createBounds();
		}

		// https://stackoverflow.com/questions/2049582/how-to-determine-if-a-point-is-in-a-2d-triangle
		private boolean triangleContainsPoint(Vertex a, Vertex b, Vertex c, Vector2f point) {
			if (sign(a,b,c)==0) {
				return false;
			}

			Vertex pointVertex=new Vertex((int) point.x, (int) point.y,0);
			float d1=sign(pointVertex,a,b);
			float d2=sign(pointVertex,b,c);
			float d3=sign(pointVertex,c,a);

			boolean hasNeg=d1<0||d2<0||d3<0;
			boolean hasPos=d1>0||d2>0||d3>0;

			return !(hasNeg&&hasPos);
		}

		private float sign(Vertex a, Vertex b, Vertex c) {
			return (a.x()-c.x())*(b.y()-c.y())-(b.x()-c.x())*(a.y()-c.y());
		}

		@Override
		public boolean containsPoint(double x, double y) {
			if (vertices.size()<4) {
				return false;
			}

			Matrix3x2f inverse=new Matrix3x2f();
			this.getMatrix().invert(inverse);
			Vector2f point=new Vector2f((float)x,(float)y);
			inverse.transformPosition(point);

			for (int i=0; i<vertices.size(); i+=4) {
				Vertex a=vertices.get(i);
				Vertex b=vertices.get(i+1);
				Vertex c=vertices.get(i+2);
				Vertex d=vertices.get(i+3);

				if (triangleContainsPoint(a,b,c,point)||triangleContainsPoint(a,c,d,point)) {
					return true;
				}
			}

			return false;
		}

		@Override
		public Jsonable toJsonable() {
			return new JsonableShapeObject(this);
		}

		@Override
		public String getObjectType() {
			return "shape";
		}

		@Override
		Matrix3x2f createMatrix(double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
			return DrawHelper.createMatrix(bounds.getLeft(),bounds.getTop(),bounds.width(),bounds.height(),scale_x,scale_y,rotation,diff_x,diff_y);
		}
	}
}
