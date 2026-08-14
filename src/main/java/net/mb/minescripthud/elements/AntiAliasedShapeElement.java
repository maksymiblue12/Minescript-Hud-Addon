package net.mb.minescripthud.elements;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import net.mb.minescripthud.DrawHelper;
import net.mb.minescripthud.MinescriptHUDAddon;
import net.mb.minescripthud.element.Element;
import net.mb.minescripthud.element.Layered;
import net.mb.minescripthud.element.LayeredUpdate;
import net.mb.minescripthud.util.AAShapeGuiElementRenderState;
import net.mb.minescripthud.util.PolygonUniforms;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.DynamicUniformStorage;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.util.Identifier;
import net.minescript.common.Jsonable;
import net.minescript.common.ScriptFunctionCall;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AntiAliasedShapeElement extends Element {
	public static final String NAME="aa_shape";
	public static final RenderPipeline ANTI_ALIASED_PIPELINE=RenderPipelines.register(RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
			.withLocation(Identifier.of(MinescriptHUDAddon.MOD_ID,"pipeline/anti_aliased"))
			.withFragmentShader(Identifier.of(MinescriptHUDAddon.MOD_ID,"anti_aliased"))
			.withVertexShader(Identifier.of(MinescriptHUDAddon.MOD_ID,"anti_aliased"))
			.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
			.withUniform("PolygonData",UniformType.UNIFORM_BUFFER)
			.build());
	private static final int MAX_VERTICES=128;
	private static DynamicUniformStorage<PolygonUniforms> polygonStorage;

	public static DynamicUniformStorage<PolygonUniforms> getPolygonStorage() {
		if (polygonStorage==null) {
			polygonStorage=new DynamicUniformStorage<>("PolygonData",MAX_VERTICES*16+16,256);
		}
		return polygonStorage;
	}

	public String getName() {
		return NAME;
	}

	private static List<Vector2f> formatVertices(List<Map<String, Double>> vertices) {
		if (vertices.size()>MAX_VERTICES) {
			throw new IllegalArgumentException("Too many vertices given: %d, MAX: %d".formatted(vertices.size(),MAX_VERTICES));
		}
		return vertices.stream().map(v -> new Vector2f(v.get("x").floatValue(),v.get("y").floatValue())).toList();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Layered create(ScriptFunctionCall.ArgList args) {
		args.expectSize(5);
		return new AntiAliasedShapeObject(formatVertices((List<Map<String, Double>>)args.get(0)), args.getStrictInt(1), (float) args.getDouble(2), args.getDouble(3), args.getStrictInt(4));
	}

	@Override
	@SuppressWarnings("unchecked")
	public Layered createAdvanced(ScriptFunctionCall.ArgList args) {
		args.expectSize(10);
		return new AntiAliasedShapeObject(formatVertices((List<Map<String, Double>>)args.get(0)), args.getStrictInt(1), (float) args.getDouble(2), args.getDouble(3), args.getStrictInt(4), args.getDouble(5), args.getDouble(6), args.getDouble(7), args.getDouble(8), args.getDouble(9));
	}

	@Override
	@SuppressWarnings("unchecked")
	public LayeredUpdate createUpdate(ScriptFunctionCall.ArgList args) {
		args.expectSize(10);
		return new AntiAliasedShapeObjectUpdate(formatVertices((List<Map<String, Double>>)args.get(0)), args.getStrictInt(1), (float) args.getDouble(2), args.getDouble(3), args.getStrictInt(4), args.getDouble(5), args.getDouble(6), args.getDouble(7), args.getDouble(8), args.getDouble(9));
	}

	public static class JsonableAntiAliasedShapeObject extends Jsonable {
		public List<Map<String,Integer>> vertices;
		public int color;
		public float fade;
		public double displayDuration;
		public int layer;
		public double scale_x;
		public double scale_y;
		public double rotation;
		public double diff_x;
		public double diff_y;
		public JsonableAntiAliasedShapeObject(AntiAliasedShapeElement.AntiAliasedShapeObject from) {
			if (Objects.isNull(from)) {
				return;
			}
			this.vertices=from.getVertices().stream().map(v -> Map.of("x",(int) v.x(),"y",(int) v.y())).toList();
			this.color=from.getColor();
			this.fade=from.getFade();
			this.scale_x=from.getMatrixInfo().get("scale_x");
			this.scale_y=from.getMatrixInfo().get("scale_y");
			this.rotation=from.getMatrixInfo().get("rotation");
			this.diff_x=from.getMatrixInfo().get("diff_x");
			this.diff_y=from.getMatrixInfo().get("diff_y");
			this.displayDuration=from.getDisplayDuration();
			this.layer=from.getLayer();
		}
	}

	public static class AntiAliasedShapeObjectUpdate implements LayeredUpdate {
		private final List<Vector2f> vertices;
		private final int color;
		private final float fade;
		private final double displayDurationModifier;
		private final int layer;
		private final double scale_x;
		private final double scale_y;
		private final double rotation;
		private final double diff_x;
		private final double diff_y;
		public AntiAliasedShapeObjectUpdate(List<Vector2f> vertices, int color, float fade, double displayDurationModifier, int layer, double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
			this.vertices=vertices;
			this.color=color;
			this.fade=fade;
			this.displayDurationModifier=displayDurationModifier;
			this.layer=layer;
			this.scale_x=scale_x;this.scale_y=scale_y;this.rotation=rotation;this.diff_x=diff_x;this.diff_y=diff_y;
		}

		@Override
		public void applyTo(Layered target) {
			if (Objects.isNull(target)) {
				return;
			}
			AntiAliasedShapeElement.AntiAliasedShapeObject to=(AntiAliasedShapeElement.AntiAliasedShapeObject)target;
			to.setVertices(vertices);
			to.setColor(color);
			to.setFade(fade);
			to.updateBounds();
			to.setDisplayDuration(to.getDisplayDuration()+this.displayDurationModifier);
			to.setLayer(this.layer);
			to.getMatrixInfo().put("scale_x",this.scale_x);to.getMatrixInfo().put("scale_y",this.scale_y);to.getMatrixInfo().put("rotation",this.rotation);to.getMatrixInfo().put("diff_x",this.diff_x);to.getMatrixInfo().put("diff_y",this.diff_y);
			to.setMatrix(DrawHelper.createMatrix(to.getBounds().getLeft(),to.getBounds().getTop(),to.getBounds().width(),to.getBounds().height(),scale_x,scale_y,rotation,diff_x,diff_y));
		}
	}

	public static class AntiAliasedShapeObject extends Layered {
		private List<Vector2f> vertices;
		private int color;
		private float fade;
		private ScreenRect bounds;
		public AntiAliasedShapeObject(List<Vector2f> vertices, int color, float fade, double displayDuration, int layer) {
			super(displayDuration, layer);
			this.vertices=vertices;
			this.color=color;
			this.fade=fade;
			this.bounds=this.createBounds();
		}

		public AntiAliasedShapeObject(List<Vector2f> vertices, int color, float fade, double displayDuration, int layer, double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
			super(displayDuration, layer, scale_x, scale_y, rotation, diff_x, diff_y);
			this.vertices=vertices;
			this.color=color;
			this.fade=fade;
			this.bounds=this.createBounds();
			this.setMatrix(this.createMatrix(scale_x, scale_y, rotation, diff_x, diff_y));
		}

		private ScreenRect createBounds() {
			int left=Integer.MAX_VALUE,right=0,top=Integer.MAX_VALUE,bottom=0;
			for (Vector2f v:vertices) {
				if (v.x>right) right=(int) v.x;
				if (v.x<left) left=(int) v.x;
				if (v.y>bottom) bottom=(int) v.y;
				if (v.y<top) top=(int) v.y;
			}
			return new ScreenRect(left,top,right-left,bottom-top);
		}

		public List<Vector2f> getVertices() {
			return vertices;
		}

		public void setVertices(List<Vector2f> newVertices) {
			vertices=newVertices;
		}

		public int getColor() {
			return color;
		}

		public void setColor(int newColor) {
			color=newColor;
		}

		public float getFade() {
			return fade;
		}

		public void setFade(float newFade) {
			fade=newFade;
		}

		public ScreenRect getBounds() {
			return bounds;
		}

		public void updateBounds() {
			bounds=this.createBounds();
		}

		// https://stackoverflow.com/questions/3838329/how-can-i-check-if-two-segments-intersect
		private boolean ccw(Vector2f a, Vector2f b, Vector2f c) {
			return (c.y-a.y)*(b.x-a.x)>(b.y-a.y)*(c.x-a.x);
		}

		private boolean intersects(Vector2f a, Vector2f b, Vector2f c, Vector2f d) {
			return ccw(a,c,d)!=ccw(b,c,d) && ccw(a,b,c)!=ccw(a,b,d);
		}

		// https://stackoverflow.com/questions/217578/how-can-i-determine-whether-a-2d-point-is-within-a-polygon
		@Override
		public boolean containsPoint(double x, double y) {
			Vector2f p=new Vector2f((float) x, (float) y);
			Vector2f lineStart=new Vector2f(p.x,0);

			int hits=0;
			for (int i=0;i<vertices.size();i++) {
				if (intersects(vertices.get(i),vertices.get((i+1)%vertices.size()),lineStart,p)) hits++;
			}

			return hits%2==1;
		}

		@Override
		public Jsonable toJsonable() {
			return new AntiAliasedShapeElement.JsonableAntiAliasedShapeObject(this);
		}

		@Override
		public String getObjectType() {
			return AntiAliasedShapeElement.NAME;
		}

		@Override
		public void render(DrawContext context, MinecraftClient client) {
			context.state.addSimpleElement(new AAShapeGuiElementRenderState(
					AntiAliasedShapeElement.ANTI_ALIASED_PIPELINE,
					TextureSetup.empty(),
					new Matrix3x2f(context.getMatrices()),
					vertices,
					color,
					fade,
					context.scissorStack.peekLast(),
					bounds
			));
		}

		@Override
		public Matrix3x2f createMatrix(double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
			return DrawHelper.createMatrix(bounds.getLeft(),bounds.getTop(),bounds.width(),bounds.height(),scale_x,scale_y,rotation,diff_x,diff_y);
		}
	}
}
