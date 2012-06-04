/*
 * Copyright © 2012 jbundle.org. All rights reserved.
 */
package org.jbundle.util.osgi;


/**
 * Constants.
 * @author don <don@tourgeek.com>
 */
public interface BundleConstants {
	
	/**
	 * Right now, services are registered under their class name. May want to change to package name.
	 */
    public static final String PACKAGE = "package";	// className is reserved
    public static final String INTERFACE = "interface";
    public static final String ACTIVATOR = "activator";
    public static final String TYPE = "type";
    public static final String SERVICE_PID = "service.pid"; // The id of the data in the config registry
	public static final String SERVICE_CLASS = "serviceClass"; // Optional class name for single servlets
	public static String ROOT_PACKAGE = "org.jbundle.";  // Default package prefix
	/**
     * The byte to char encoding that I use for object serialization.
     */
    public static final String OBJECT_ENCODING = "ISO-8859-1";

}
