package net.mb.minescripthud.element;

import net.mb.minescripthud.elements.*;

import java.util.HashMap;

public class ElementRegistry {
	private static final HashMap<String,Element> elements=new HashMap<>();

	private ElementRegistry() {}

	public static Element getElement(String name) {
		return elements.get(name);
	}

	public static void register(Element element) {
		elements.put(element.getName(),element);
	}

	public static void registerElements() {
		register(new TextElement());
		register(new RectangleElement());
		register(new GradientRectangleElement());
		register(new StrokedRectangleElement());
		register(new StrokedRectangleElement());
		register(new TextWithBackgroundElement());
		register(new ItemElement());
		register(new TextureElement());
		register(new ShapeElement());
	}
}
