package org.jbundle.thin.base.util.osgi.finder;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import org.jbundle.thin.base.util.osgi.bundle.BundleService;

public interface ClassFinder {
	
    /**
     * Find, resolve, and return this class definition.
     * Static convenience method.
     * @param className
     * @return The class definition or null if not found.
     */
    public Class<?> findClassBundle(String className);
    /**
     * Find, resolve, and return this resource's URL.
     * Static convenience method.
     * @param className
     * @return The class definition or null if not found.
     */
    public URL findBundleResource(String className);
    /**
     * Find, resolve, and return this ResourceBundle.
     * Static convenience method.
     * @param className
     * @return The class definition or null if not found.
     */
    public ResourceBundle findResourceBundle(String className, Locale locale);

    /**
     * Convert this encoded string back to a Java Object.
     * TODO This is expensive, I need to synchronize and use a static writer.
     * @param className TODO
     * @param string The string to convert.
     * @return The java object.
     */
    public Object findResourceConvertStringToObject(String className, String string);

    /**
     * Find this class's class access registered class access service in the current workspace.
     * @param interfaceName The registered object name
     * @param className The class name (that has the package that the object was registered under)
     * @return
     */
    public BundleService getClassBundleService(String interfaceName, String className);
    /**
     * Shutdown the bundle for this service.
     * @param service The service object
     */
    public void shutdownService(Object service);
}
