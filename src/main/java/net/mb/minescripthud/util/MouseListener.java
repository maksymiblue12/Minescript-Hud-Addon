package net.mb.minescripthud.util;

import net.minescript.common.JobControl;
import net.minescript.common.ScriptValue;

import java.util.OptionalLong;

public class MouseListener implements JobControl.Operation {
	private final JobControl job;
	private OptionalLong funcCallId = OptionalLong.empty();
	private OptionalLong listenerId = OptionalLong.empty();
	private State state;
	private boolean suspended;

	public MouseListener(JobControl job) {
		this.job=job;
		this.state = State.IDLE;
		this.suspended = false;
	}

	JobControl job() {
		return this.job;
	}

	@Override
	public String name() {
		return "hover_listener";
	}

	public boolean isActive() {
		return !this.suspended && this.state == State.ACTIVE;
	}

	@Override
	public void suspend() {
		this.suspended = true;
	}

	@Override
	public boolean resumeAndCheckDone() {
		if (this.state == State.CANCELLED) {
			return true;
		} else {
			this.suspended = false;
			return false;
		}
	}

	public void start(long funcCallId, long listenerId) {
		if (this.state != State.CANCELLED) {
			this.funcCallId = OptionalLong.of(funcCallId);
			this.listenerId = OptionalLong.of(listenerId);
			this.state = State.ACTIVE;
		}

	}

	@Override
	public void cancel() {
		this.state = State.CANCELLED;
	}

	public void respond(ScriptValue value) {
		if (this.funcCallId.isPresent() && this.state==State.ACTIVE) {
			this.job.respond(this.funcCallId.getAsLong(), value, false);
		}
	}

	public enum State {
		IDLE,
		ACTIVE,
		CANCELLED;

		State() {
		}
	}
}
