package org.slf4j.integration.sysoutslf4j;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import org.slf4j.LoggerFactory;
import org.slf4j.testutils.Assert;
import org.slf4j.testutils.CrossClassLoaderTestUtils;
import org.slf4j.testutils.LoggingUtils;
import org.slf4j.testutils.SimpleClassloader;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

public class TestSysOutOverSLF4JInClassLoader extends SysOutOverSlf4jIntegrationTestCase {

	private final ClassLoader app1ClassLoader = new SimpleClassloader();
	
	@Before
	public void turnOffRootLoggingInClassLoaders() {
		LoggingUtils.turnOffRootLogging(app1ClassLoader);
	}
	
	@Before
	public void resetLoggingInClassloader() {
		
	}
	
	@Test
	public void sysOutOverSLF4JWorksInsideAnotherClassLoader() throws Exception {
		callSendSystemOutAndErrToSLF4JInClassLoader(app1ClassLoader);
		
		resetSysOutUserAppender(app1ClassLoader);
		ISysOutUser sysOutUser1 = newInstanceInClassLoader(ISysOutUser.class, app1ClassLoader, SysOutUser.class, new Class[]{});
		
		Thread.currentThread().setContextClassLoader(app1ClassLoader);
		sysOutUser1.useSysOut();
		
		List<?> list1 = getRootAppender(app1ClassLoader);
		assertEquals(1, list1.size());
		ILoggingEvent loggingEvent = CrossClassLoaderTestUtils.moveToCurrentClassLoader(ILoggingEvent.class, list1.get(0));
		Assert.assertExpectedLoggingEvent(loggingEvent, "Logged", Level.INFO, null, SysOutUser.class.getName());
	}

	private <E> E newInstanceInClassLoader(
			Class<E> classToReturn, ClassLoader classLoader, Class<? extends E> classToGetInstanceOf,
			Class<?>[] constructorArgTypes, Object... constructorArgs) throws Exception {
		Class<?> class1 = classLoader.loadClass(classToGetInstanceOf.getName());
		Object newInstance = Whitebox.invokeConstructor(class1, constructorArgTypes, constructorArgs);
		return CrossClassLoaderTestUtils.moveToCurrentClassLoader(classToReturn, newInstance);
	}
	
	static void resetSysOutUserAppender(ClassLoader classLoader) throws Exception {
		Class<?> clazz = classLoader.loadClass(TestSysOutOverSLF4JInClassLoader.class.getName());
		clazz.getDeclaredMethod("resetSysOutUserAppender").invoke(clazz);
	}
	
	public static void resetSysOutUserAppender() {
		LoggerContext LC = (LoggerContext) LoggerFactory.getILoggerFactory();
		Logger rootLogger = LC.getLogger(SysOutUser.class.getName());
		rootLogger.detachAndStopAllAppenders();
		ListAppender<ILoggingEvent> appender = new ListAppender<ILoggingEvent>();
		appender.setName("list");
		appender.setContext(LC);
		appender.start();
		rootLogger.addAppender(appender);
		rootLogger.setLevel(Level.INFO);
	}
	
	static List<?> getRootAppender(ClassLoader classLoader) throws Exception {
		Class<?> clazz = classLoader.loadClass(TestSysOutOverSLF4JInClassLoader.class.getName());
		Object listAppender = clazz.getDeclaredMethod("getRootAppender").invoke(clazz);
		Class<?> listAppenderClass = classLoader.loadClass(ListAppender.class.getName());
		Field listField = listAppenderClass.getField("list");
		Object list = listField.get(listAppender);
		return CrossClassLoaderTestUtils.moveToCurrentClassLoader(List.class, list);
	}
	
	public static ListAppender<ILoggingEvent> getRootAppender() {
		LoggerContext LC = (LoggerContext) LoggerFactory.getILoggerFactory();
		Logger sysOutLogger = LC.getLogger(SysOutUser.class.getName());
		return (ListAppender<ILoggingEvent>) sysOutLogger.getAppender("list");
	}
}