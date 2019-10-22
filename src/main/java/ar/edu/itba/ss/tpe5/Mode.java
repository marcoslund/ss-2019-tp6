package ar.edu.itba.ss.tpe5;

import java.util.Arrays;
import java.util.Optional;

public enum Mode {
	SINGLE_RUN (0);
	
	private int mode;
	
	Mode(int mode) {
		this.mode = mode;
	}
	
	public int getMode() {
		return mode;
	}
	
	public static Optional<Mode> valueOf(int value) {
		return Arrays.stream(values()).filter(m -> m.getMode() == value).findFirst();
	}
}
