package io.jenkins.plugins.utils;

import java.io.PrintStream;
import java.util.logging.Level;

public class ContextLogger {

	final PrintStream stream;
	
	final String name;
	
	final Level defaultLevel;
	
	public ContextLogger(final PrintStream str, final String name, final Level defaultLevel) {
		this.stream = str;
		this.name = name;
		this.defaultLevel = defaultLevel;
	}
	
	public void log(final String message) {
		this.log(message, this.defaultLevel);
	}
	
	public void log(final String message, final Level level) {
		this.stream.append('[');
		this.stream.append(this.name);
		this.stream.append(']');
		this.stream.append('[');
		this.stream.append(level.toString());
		this.stream.append("]: ");
		this.stream.append(message);
		this.stream.flush();
		this.stream.println();
	}
}
