package net.mb.minescripthud.element;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minescript.common.Jsonable;
import org.joml.Matrix3x2f;

import java.util.HashMap;
import java.util.Map;

public abstract class Layered {
	private double displayDuration;
	private int layer;
	private final Map<String, Double> matrix_info=new HashMap<>();
	private Matrix3x2f matrix;
	private boolean hovering=false;
	protected Layered(double displayDuration, int layer) {
		this.displayDuration=displayDuration;
		this.layer=layer;
		this.matrix_info.put("scale_x",1d);this.matrix_info.put("scale_y",1d);this.matrix_info.put("rotation",0d);this.matrix_info.put("diff_x",0d);this.matrix_info.put("diff_y",0d);
		this.matrix=new Matrix3x2f();
	}

	protected Layered(double displayDuration, int layer, double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
		this.displayDuration=displayDuration;
		this.layer=layer;
		this.matrix_info.put("scale_x",scale_x);this.matrix_info.put("scale_y",scale_y);this.matrix_info.put("rotation",rotation);this.matrix_info.put("diff_x",diff_x);this.matrix_info.put("diff_y",diff_y);
	}

	public abstract void render(DrawContext context, MinecraftClient client);

	public Matrix3x2f createMatrix(double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
		return new Matrix3x2f();
	}
	public int getLayer() {
		return this.layer;
	}
	public void setLayer(int newLayer) {
		this.layer=newLayer;
	}
	public Matrix3x2f getMatrix() {
		return this.matrix;
	}
	public void setMatrix(Matrix3x2f newMatrix) {
		this.matrix=newMatrix;
	}
	public Map<String, Double> getMatrixInfo() {
		return this.matrix_info;
	}
	public double getDisplayDuration() {
		return this.displayDuration;
	}
	public void setDisplayDuration(double newDisplayDuration) {
		this.displayDuration=newDisplayDuration;
	}
	public boolean getHovering() {
		return this.hovering;
	}
	public void setHovering(boolean newHovering) {
		this.hovering=newHovering;
	}
	public boolean containsPoint(double x, double y) {
		return false;
	}
	public Jsonable toJsonable() {
		return null;
	}
	public String getObjectType() {
		return "";
	}
}
