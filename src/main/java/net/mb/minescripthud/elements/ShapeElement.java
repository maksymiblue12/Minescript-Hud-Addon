package net.mb.minescripthud.elements;

import net.mb.minescripthud.DrawHelper;
import net.mb.minescripthud.element.Element;
import net.mb.minescripthud.element.Layered;
import net.mb.minescripthud.element.LayeredUpdate;
import net.mb.minescripthud.util.ShapeGuiElementRenderState;
import net.mb.minescripthud.util.Vertex;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.texture.TextureSetup;
import net.minescript.common.Jsonable;
import net.minescript.common.ScriptFunctionCall;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ShapeElement extends Element {
	public static final String NAME="shape";

	public String getName() {
		return NAME;
	}

	private static List<Vertex> formatVertices(List<Map<String, Double>> vertices) {
		return vertices.stream().map(v -> new Vertex(v.get("x").intValue(),v.get("y").intValue(),v.get("color").intValue())).toList();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Layered create(ScriptFunctionCall.ArgList args) {
		args.expectSize(3);
		return new ShapeObject(formatVertices((List<Map<String, Double>>)args.get(0)), args.getDouble(1), args.getStrictInt(2));
	}

	@Override
	@SuppressWarnings("unchecked")
	public Layered createAdvanced(ScriptFunctionCall.ArgList args) {
		args.expectSize(8);
		return new ShapeObject(formatVertices((List<Map<String, Double>>)args.get(0)), args.getDouble(1), args.getStrictInt(2), args.getDouble(3), args.getDouble(4), args.getDouble(5), args.getDouble(6), args.getDouble(7));
	}

	@Override
	@SuppressWarnings("unchecked")
	public LayeredUpdate createUpdate(ScriptFunctionCall.ArgList args) {
		args.expectSize(8);
		return new ShapeObjectUpdate(formatVertices((List<Map<String, Double>>)args.get(0)), args.getDouble(1), args.getStrictInt(2), args.getDouble(3), args.getDouble(4), args.getDouble(5), args.getDouble(6), args.getDouble(7));
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
		public JsonableShapeObject(ShapeElement.ShapeObject from) {
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
			ShapeElement.ShapeObject to=(ShapeElement.ShapeObject)target;
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
			return new ShapeElement.JsonableShapeObject(this);
		}

		@Override
		public String getObjectType() {
			return ShapeElement.NAME;
		}

		@Override
		public void render(DrawContext context, MinecraftClient client) {
			context.state.addSimpleElement(new ShapeGuiElementRenderState(RenderPipelines.GUI, TextureSetup.empty(), new Matrix3x2f(context.getMatrices()), vertices, context.scissorStack.peekLast(), bounds));
		}

		@Override
		public Matrix3x2f createMatrix(double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
			return DrawHelper.createMatrix(bounds.getLeft(),bounds.getTop(),bounds.width(),bounds.height(),scale_x,scale_y,rotation,diff_x,diff_y);
		}
	}
}
