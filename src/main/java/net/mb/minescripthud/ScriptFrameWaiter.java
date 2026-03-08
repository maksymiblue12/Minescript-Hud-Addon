package net.mb.minescripthud;

import java.util.HashMap;
import java.util.Map;

public class ScriptFrameWaiter {
	private static final ScriptFrameWaiter INSTANCE = new ScriptFrameWaiter();
	Map<Long,Runnable> waitingScripts=new HashMap<>();

	public static ScriptFrameWaiter getInstance() {
		return INSTANCE;
	}

	public void waitNextFrame(long scriptId,Runnable resume) {
		waitingScripts.put(scriptId,resume);
	}

	public void onEndFrame() {
		for (Runnable resume:waitingScripts.values()) {
			resume.run();
		}
		waitingScripts.clear();
	}
}
