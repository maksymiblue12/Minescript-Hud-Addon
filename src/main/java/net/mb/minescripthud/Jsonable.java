package net.mb.minescripthud;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public class Jsonable {
	public Jsonable() {
	}

	public JsonElement toJson() {
		return (new GsonBuilder()).serializeNulls().create().toJsonTree(this);
	}

	public String toString() {
		String var10000 = this.getClass().getSimpleName();
		return var10000 + "(" + this.toJson().toString() + ")";
	}
}
