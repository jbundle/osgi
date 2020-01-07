/*
 * Copyright © 2012 jbundle.org. All rights reserved.
 */
package org.jbundle.util.osgi;

import java.net.URL;
import java.util.Dictionary;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Service to find and load bundle classes and resources.
 * @author don
 *
 * WARNING: It is important that this class has no direct connections to org.osgi!
 */
public interface ClassFinder {
	
    /**
     * Find, resolve, and return this class definition.
     * Static convenience method.
     * @param className
     * @param versionRange Version range
     * @return The class definition or null if not found.
     */
    public Class<?> findClass(String className, String versionRange);
    /**
     * Find, resolve, and return this resource's URL.
     * Static convenience method.
     * @param resourcePath
     * @param versionRange Version range
     * @return The class definition or null if not found.
     */
    public URL findResourceURL(String resourcePath, String versionRange);
    /**
     * Find, resolve, and return this ResourceBundle.
     * Static convenience method.
     * @param resourcePath
     * @param versionRange Version range
     * @return The class definition or null if not found.
     */
    public ResourceBundle findResourceBundle(String resourcePath, Locale locale, String versionRange);

    /**
     * Convert this encoded string back to a Java Object.
     * TODO This is expensive, I need to synchronize and use a static writer.
     * @param className
     * @param versionRange Version range
     * @param string The string to convert.
     * @return The java object.
     */
    public Object findConvertStringToObject(String className, String versionRange, String string);

    /**
     * Find the currently installed bundle that exports this package.
     * @param context
     * @param versionRange
     * @param objResource
     * @return
     */
    public Object findBundle(Object objResource, Object context, String packageName, String versionRange);

    /**
     * Get the bundle classloader for this package.
     * @param versionRange Version range
     * @param packageName The class name to find the bundle for.
     * @return The class loader.
     * @throws ClassNotFoundException
     */
    public ClassLoader findBundleClassLoader(String packageName, String versionRange);

    /**
     * Find this class's class access registered class access service in the current workspace.
     * @param interfaceClassName The class name (that has the package that the object was registered under)
     * @param serviceClassName The service (or activator) class name.
     * @param versionRange Version range
     * @param filter Other filters to use to find the service
     * @param secsToWait Time to wait for service to start (0=don't wait) WARNING: This may take a while, so don't run this in your main thread.
     * @return The service
     */
    public Object getClassBundleService(String interfaceClassName, String serviceClassName, String versionRange, Dictionary<String, Object> filter, int secsToWait);
    /**
     * Find this resource in the repository, then deploy and optionally start it.
     * @param versionRange Version range
     * @param packageName
     * @param start
     * @return
     */
    public Object deployThisResource(String packageName, String versionRange, boolean start);
    /**
     * Shutdown the bundle for this service.
     * @param service The interface the service is registered under.
     * @param service The service or the package name of the service (service pid) that I'm looking for.
     * @return True if successful
     */
    public boolean shutdownService(String serviceClass, Object service);
    /**
     * Log this message.
     * @param context
     * @param level
     * @param message
     */
    public boolean log(Object context, int level, String message);
    /**
     * Get the configuration properties for this Pid.
     * @param servicePid The service Pid
     * @return The properties or null if they don't exist.
     */
    public Dictionary<String, Object> getProperties(String servicePid);
    /**
     * Set the configuration properties for this Pid.
     * @param servicePid The service Pid
     * @param properties The properties to save.
     * @return True if successful
     */
    public boolean saveProperties(String servicePid, Dictionary<String, Object> properties);
}
