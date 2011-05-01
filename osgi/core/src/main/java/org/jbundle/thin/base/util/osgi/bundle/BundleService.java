package org.jbundle.thin.base.util.osgi.bundle;

import java.net.URL;

/**
 * Bundle resource access utilities for a service.
 * @author don
 */
public interface BundleService {
	
	/**
	 * Right now, services are registered under their class name. May want to change to package name.
	 */
    public static final String PACKAGE_NAME = "packageName";	// className is reserved
    public static final String INTERFACE = "interface";
    public static final String TYPE = "type";

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
     * TODO This is expensive, I need to synchronize and use a static writer.
     * @param string The string to convert.
     * @return The java object.
     */
    public Object convertStringToObject(String string)
    	throws ClassNotFoundException;
}
