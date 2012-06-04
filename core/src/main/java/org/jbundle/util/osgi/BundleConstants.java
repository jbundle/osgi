/*
 * Copyright © 2012 jbundle.org. All rights reserved.
 */
package org.jbundle.util.osgi;

import java.net.URL;

/**
 * A bundle activator class.
 * Provides resource access utilities for a service.
 * 
 * WARNING: It is important that this class has no direct connections to org.osgi!
 * @author don
 */
public interface BundleConstants {
	
	/**
	 * Right now, services are registered under their class name. May want to change to package name.
	 */
    public static final String PACKAGE_NAME = "packageName";	// className is reserved
    public static final String INTERFACE = "interface";
    public static final String ACTIVATOR = "activator";
    public static final String TYPE = "type";
    public static final String SERVICE_PID = "service.pid"; // The id of the data in the config registry
	public static final String SERVICE_CLASS = "serviceClass"; // Optional class name for single servlets

}
