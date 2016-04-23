package io.smartcat.cassandra_diagnostics;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

import io.smartcat.cassandra_diagnostics.config.Configuration;
import io.smartcat.cassandra_diagnostics.jmx.DiagnosticsMXBean;
import io.smartcat.cassandra_diagnostics.jmx.DiagnosticsMXBeanImpl;

public class Diagnostics {
	private static final Logger logger = LoggerFactory.getLogger(Diagnostics.class);
	private static final Injector injector = Guice.createInjector(new DiagnosticsModule());
	
	public static QueryProcessorWrapper queryProcessorWrapper = injector.getInstance(QueryProcessorWrapper.class);
	
	public static void init() {
		initMBean();
	}
	
	private static void initMBean() {
		final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		try {
			ObjectName objectName = new ObjectName(DiagnosticsMXBean.class.getPackage() + ":type=" + DiagnosticsMXBean.class.getSimpleName());
			final DiagnosticsMXBean mbean = new DiagnosticsMXBeanImpl(injector.getInstance(Configuration.class));
			server.registerMBean(mbean, objectName);
		} catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
			logger.error("Unable to register DiagnosticsMBean", e);
		}
	}
	
}
