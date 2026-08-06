package net.mb.minescripthud;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.mb.minescripthud.element.ElementRegistry;
import net.mb.minescripthud.element.Layered;
import net.mb.minescripthud.element.LayeredUpdate;
import net.mb.minescripthud.util.MouseListener;
import net.mb.minescripthud.util.MouseTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minescript.common.Jsonable;
import net.minescript.common.ScriptFunctionCall;
import net.minescript.common.ScriptValue;
import org.joml.Matrix3x2f;

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
	public void batchUpdate(String func_name, List<Map<String, Object>> updates) {
		for (Map<String, Object> upd:updates) {
			List<Object> data=(List<Object>)upd.get("data");
			data.addFirst(upd.get("id"));
			data.addFirst(upd.get("type"));
			ScriptFunctionCall.ArgList args=new ScriptFunctionCall.ArgList(func_name,data);
			updateElement(func_name,args);
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



	public int addElement(String func_name, ScriptFunctionCall.ArgList args) {
		int i=this.getId();
		if (args.isEmpty()) {
			throw new IllegalArgumentException("Must have at least 1 argument.");
		}
		String name=args.getString(0);
		ScriptFunctionCall.ArgList new_args=new ScriptFunctionCall.ArgList(func_name,args.rawArgs().stream().skip(1).toList());
		elements.put(i, ElementRegistry.getElement(name).create(new_args));
		return i;
	}

	public int addAdvancedElement(String func_name, ScriptFunctionCall.ArgList args) {
		int i=this.getId();
		if (args.isEmpty()) {
			throw new IllegalArgumentException("Must have at least 1 argument.");
		}
		String name=args.getString(0);
		ScriptFunctionCall.ArgList new_args=new ScriptFunctionCall.ArgList(func_name,args.rawArgs().stream().skip(1).toList());
		elements.put(i, ElementRegistry.getElement(name).createAdvanced(new_args));
		return i;
	}

	public void updateElement(String func_name, ScriptFunctionCall.ArgList args) {
		if (args.size()<2) {
			throw new IllegalArgumentException("Must have at least 2 arguments.");
		}
		String name=args.getString(0);
		int id=args.getStrictInt(1);
		ScriptFunctionCall.ArgList new_args=new ScriptFunctionCall.ArgList(func_name,args.rawArgs().stream().skip(2).toList());
		elementUpdates.put(id,ElementRegistry.getElement(name).createUpdate(new_args));
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
					data.add("mouse",JsonNull.INSTANCE);
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

	public void render(DrawContext context) {
		final MinecraftClient client=MinecraftClient.getInstance();
		Map<Integer,List<Layered>> layers=this.getElementsSortedByLayer();
		context.getMatrices().pushMatrix();
		for (List<Layered> layer:layers.values()) {
			for (Layered element:layer) {
				context.getMatrices().set(element.getMatrix());
				element.render(context,client);
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
}
