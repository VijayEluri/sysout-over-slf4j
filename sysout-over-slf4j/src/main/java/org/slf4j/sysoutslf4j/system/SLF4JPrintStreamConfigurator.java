package org.slf4j.sysoutslf4j.system;

import java.io.PrintStream;

import org.slf4j.sysoutslf4j.common.SLF4JPrintStream;
import org.slf4j.sysoutslf4j.common.SystemOutput;

public final class SLF4JPrintStreamConfigurator {
	
	public static void replaceSystemOutputsWithSLF4JPrintStreams() {
		for (SystemOutput systemOutput : SystemOutput.values()) {
			replaceSystemOutputWithSLF4JPrintStream(systemOutput);
		}
	}

	private static void replaceSystemOutputWithSLF4JPrintStream(final SystemOutput systemOutput) {
		final SLF4JPrintStreamImpl slf4jPrintStream = buildSLF4JPrintStream(systemOutput.get());
		systemOutput.set(slf4jPrintStream);
	}

	private static SLF4JPrintStreamImpl buildSLF4JPrintStream(final PrintStream originalPrintStream) {
		final LoggerAppenderStore loggerAppenderStore = new LoggerAppenderStore();
		final SLF4JPrintStreamDelegater delegater = new SLF4JPrintStreamDelegater(originalPrintStream, loggerAppenderStore);
		return new SLF4JPrintStreamImpl(originalPrintStream, delegater);
	}

	public static void restoreOriginalSystemOutputs() {
		for (SystemOutput systemOutput : SystemOutput.values()) {
			restoreSystemOutput(systemOutput);
		}
	}

	private static void restoreSystemOutput(final SystemOutput systemOutput) {
		final SLF4JPrintStream slf4jPrintStream = (SLF4JPrintStream) systemOutput.get();
		systemOutput.set(slf4jPrintStream.getOriginalPrintStream());
	}

	private SLF4JPrintStreamConfigurator() {
		throw new UnsupportedOperationException("Not instantiable");
	}
}