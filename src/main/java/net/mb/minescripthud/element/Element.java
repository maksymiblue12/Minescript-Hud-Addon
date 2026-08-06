package net.mb.minescripthud.element;

import net.minescript.common.ScriptFunctionCall;

public abstract class Element {
	public abstract String getName();

	public abstract Layered create(ScriptFunctionCall.ArgList args);
	public abstract Layered createAdvanced(ScriptFunctionCall.ArgList args);
	public abstract LayeredUpdate createUpdate(ScriptFunctionCall.ArgList args);
}
