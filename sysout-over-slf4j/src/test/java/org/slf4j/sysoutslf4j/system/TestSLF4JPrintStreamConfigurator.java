package org.slf4j.sysoutslf4j.system;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.slf4j.testutils.Assert.assertNotInstantiable;

import java.io.PrintStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.sysoutslf4j.SysOutOverSLF4JTestCase;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ SLF4JPrintStreamConfigurator.class, SLF4JPrintStreamImpl.class })
public class TestSLF4JPrintStreamConfigurator extends SysOutOverSLF4JTestCase {

	@Test
	public void notInstantiable() throws Throwable {
		assertNotInstantiable(SLF4JPrintStreamConfigurator.class);
	}
	
	@Test
	public void replaceSystemOutputsWithSLF4JPrintStreams() throws Exception {
		SLF4JPrintStreamImpl outSlf4jPrintStreamImpl = expectSLF4JPrintStreamToBeBuilt(System.out);
		SLF4JPrintStreamImpl errSlf4jPrintStreamImpl = expectSLF4JPrintStreamToBeBuilt(System.err);
		replayAll();
		
		SLF4JPrintStreamConfigurator.replaceSystemOutputsWithSLF4JPrintStreams();
		assertEquals(outSlf4jPrintStreamImpl, System.out);
		assertEquals(errSlf4jPrintStreamImpl, System.err);
	}
	
	@Test
	public void restoreOriginalSystemOutputs() throws Exception {
		SLF4JPrintStreamConfigurator.replaceSystemOutputsWithSLF4JPrintStreams();
		SLF4JPrintStreamConfigurator.restoreOriginalSystemOutputs();
		assertEquals(SYS_OUT, System.out);
		assertEquals(SYS_ERR, System.err);
	}

	private SLF4JPrintStreamImpl expectSLF4JPrintStreamToBeBuilt(PrintStream originalPrintStream) throws Exception {
		LoggerAppenderStore loggerAppenderStoreMock = createMock(LoggerAppenderStore.class);
		expectNew(LoggerAppenderStore.class).andReturn(loggerAppenderStoreMock);
		SLF4JPrintStreamDelegater slf4jPrintStreamDelegaterMock = createMock(SLF4JPrintStreamDelegater.class);
		expectNew(SLF4JPrintStreamDelegater.class, originalPrintStream, loggerAppenderStoreMock).andReturn(slf4jPrintStreamDelegaterMock);
		SLF4JPrintStreamImpl slf4jPrintStreamImplMock = createMock(SLF4JPrintStreamImpl.class);
		expectNew(SLF4JPrintStreamImpl.class, originalPrintStream, slf4jPrintStreamDelegaterMock).andReturn(slf4jPrintStreamImplMock);
		return slf4jPrintStreamImplMock;
	}
}