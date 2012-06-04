/*
 * Copyright © 2012 jbundle.org. All rights reserved.
 */
package org.jbundle.util.osgi;

import java.net.URL;

/**
 * Bundle resource access utilities for a service.

 * WARNING: It is important that this class has no direct connections to org.osgi!
 * @author don
 */
public interface BundleActivatorModel {
	
	/**
	 * Right now, services are registered under their class name. May want to change to package name.
	 */
    public static final String PACKAGE_NAME = "packageName";	// className is reserved
    public static final String INTERFACE = "interface";
    public static final String ACTIVATOR = "activator";
    public static final String TYPE = "type";
    public static final String SERVICE_PID = "service.pid"; // The id of the data in the config registry
	public static final String SERVICE_CLASS = "serviceClass"; // Optional class name for single servlets

	/**
	 * Given this class name, create the Class.
	 * @param className The full class name.
	 * @return The class or null if not found.
	 */
    public Class<?> makeClass(String className) throws ClassNotFoundException;
	/**
	 * Get the URL to the resource with this name.
	 * @param name The full resource path.
	 * @return The resource URL (usually bundle:more).
	 */
    public URL getResource(String className);
    /**
     * Convert this encoded string back to a Java Object.
     * @param string The string to convert.
     * @return The java object.
     */
    public Object convertStringToObject(String string)
    	throws ClassNotFoundException;
    
}
