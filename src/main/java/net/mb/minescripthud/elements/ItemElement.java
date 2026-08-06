package net.mb.minescripthud.elements;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.mb.minescripthud.DrawHelper;
import net.mb.minescripthud.element.Element;
import net.mb.minescripthud.element.Layered;
import net.mb.minescripthud.element.LayeredUpdate;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStringReader;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minescript.common.Jsonable;
import net.minescript.common.ScriptFunctionCall;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

import java.util.NoSuchElementException;
import java.util.Objects;

public class ItemElement extends Element {
	public static final String NAME="item";

	public String getName() {
		return NAME;
	}

	private static ItemStack parseItemStack(String item) throws CommandSyntaxException {
		int amount=1;
		if (item.matches(".* [0-9]+")) {
			amount=Integer.parseInt(item.substring(item.strip().lastIndexOf(' ')+1));
		}
		//noinspection DataFlowIssue
		ItemStringReader.ItemResult itemResult=new ItemStringReader(MinecraftClient.getInstance().getNetworkHandler().getRegistryManager()).consume(new StringReader(item));
		return new ItemStackArgument(itemResult.item(),itemResult.components()).createStack(amount,false);
	}

	private static ItemStack getItemStack(String item) {
		ItemStack itemStack;
		try {
			itemStack=parseItemStack(item);
		} catch (CommandSyntaxException e) {
			throw new NoSuchElementException("No item of name '"+item+"' exists!");
		}
		return itemStack;
	}

	@Override
	public Layered create(ScriptFunctionCall.ArgList args) {
		args.expectSize(5);
		return new ItemObject(getItemStack(args.getString(0)), args.getStrictInt(1), args.getStrictInt(2), args.getDouble(3), args.getStrictInt(4));
	}

	@Override
	public Layered createAdvanced(ScriptFunctionCall.ArgList args) {
		args.expectSize(10);
		return new ItemObject(getItemStack(args.getString(0)), args.getStrictInt(1), args.getStrictInt(2), args.getDouble(3), args.getStrictInt(4), args.getDouble(5), args.getDouble(6), args.getDouble(7), args.getDouble(8), args.getDouble(9));
	}

	@Override
	public LayeredUpdate createUpdate(ScriptFunctionCall.ArgList args) {
		args.expectSize(10);
		return new ItemObjectUpdate(getItemStack(args.getString(0)), args.getStrictInt(1), args.getStrictInt(2), args.getDouble(3), args.getStrictInt(4), args.getDouble(5), args.getDouble(6), args.getDouble(7), args.getDouble(8), args.getDouble(9));
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
		public JsonableItemObject(ItemElement.ItemObject from) {
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
			ItemElement.ItemObject to=(ItemElement.ItemObject)target;
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
			return new ItemElement.JsonableItemObject(this);
		}

		@Override
		public String getObjectType() {
			return ItemElement.NAME;
		}

		@Override
		public void render(DrawContext context, MinecraftClient client) {
			context.drawItem(item, x, y);
			context.drawStackOverlay(client.textRenderer,item,x,y);
		}

		@Override
		public Matrix3x2f createMatrix(double scale_x, double scale_y, double rotation, double diff_x, double diff_y) {
			return DrawHelper.createMatrix(x,y,16,16,scale_x,scale_y,rotation,diff_x,diff_y);
		}
	}
}
