package net.mb.minescripthud.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import net.minescript.common.Jsonable;
import org.lwjgl.glfw.GLFW;

public class MouseTracker {
	private static final MouseTracker INSTANCE=new MouseTracker();
	private boolean isLeftDown=false;
	private boolean isMiddleDown=false;
	private boolean isRightDown=false;
	private boolean leftClicked=false;
	private boolean middleClicked=false;
	private boolean rightClicked=false;
	private double x=0;
	private double y=0;
	private boolean isCursorLocked=false;

	private MouseTracker() {}

	public static MouseTracker getInstance() {return INSTANCE;}

	public JsonableMouse toJsonable() {
		return new JsonableMouse(getInstance());
	}

	public double getX() {return x;}

	public double getY() {return y;}

	public boolean anyButtonClicked() {return leftClicked||middleClicked||rightClicked;}

	public boolean isCursorLocked() {return isCursorLocked;}

	public void update() {
		Mouse m=MinecraftClient.getInstance().mouse;
		Window window=MinecraftClient.getInstance().getWindow();
		x=m.getScaledX(window);
		y=m.getScaledY(window);
		isCursorLocked=m.isCursorLocked();

		boolean left=GLFW.glfwGetMouseButton(window.getHandle(),GLFW.GLFW_MOUSE_BUTTON_LEFT)==GLFW.GLFW_PRESS;
		leftClicked=left&&!isLeftDown;
		isLeftDown=left;

		boolean middle=GLFW.glfwGetMouseButton(window.getHandle(),GLFW.GLFW_MOUSE_BUTTON_MIDDLE)==GLFW.GLFW_PRESS;
		middleClicked=middle&&!isMiddleDown;
		isMiddleDown=middle;

		boolean right=GLFW.glfwGetMouseButton(window.getHandle(),GLFW.GLFW_MOUSE_BUTTON_RIGHT)==GLFW.GLFW_PRESS;
		rightClicked=right&&!isRightDown;
		isRightDown=right;
	}

	public static class JsonableMouse extends Jsonable {
		public double x;
		public double y;
		public boolean isLeftDown;
		public boolean isMiddleDown;
		public boolean isRightDown;
		public boolean wasLeftJustClicked;
		public boolean wasMiddleJustClicked;
		public boolean wasRightJustClicked;
		public JsonableMouse(MouseTracker mouse) {
			this.x=mouse.x;
			this.y=mouse.y;
			this.isLeftDown=mouse.isLeftDown;
			this.isMiddleDown=mouse.isMiddleDown;
			this.isRightDown=mouse.isRightDown;
			this.wasLeftJustClicked=mouse.leftClicked;
			this.wasMiddleJustClicked=mouse.middleClicked;
			this.wasRightJustClicked=mouse.rightClicked;
		}
	}
}
