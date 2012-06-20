/*
 * Copyright © 2012 jbundle.org. All rights reserved.
 */
package org.jbundle.util.osgi;

import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Class resource retrieval utilities.
 * WARNING: It is important that this interface has no direct connections to org.osgi!
 */
public interface ClassService
{
    /**
    * Get the Osgi class service.
    * NOTE: Don't import this package as the ClassService class may not be available until this service is started.
    * @param context The bundle context
    * @return The class finder
    */
   public ClassFinder getClassFinder(Object context);
   /**
    * Create this object given the class name.
    * @param className
    * @return The object
    */
   public Object makeObjectFromClassName(String className);
   /**
    * Create this object given the class name.
    * @param className
    * @param version Version range
    * @return The object
    * @throws RuntimeException If Error flag is set, return a runtime exception if object can't be created.
    */
   public Object makeObjectFromClassName(String className, String versionRange, boolean bErrorIfNotFound) throws RuntimeException;
   /**
    * Create this object given the class name.
    * @param filepath
    * @param version Version range
    * @return The resource url
    * @throws RuntimeException Not implemented yet
    */
   public URL getResourceURL(String filepath, URL urlCodeBase, String versionRange, ClassLoader classLoader) throws RuntimeException;
   /**
    * Gets a resource bundle using the specified base name and locale,
    * @param locale the locale for which a resource bundle is desired
    * @param version Version range
    * @param baseName the base name of the resource bundle, a fully qualified class name
    * @exception NullPointerException if <code>baseName</code> or <code>locale</code> is <code>null</code>
    * @exception MissingResourceException if no resource bundle for the specified base name can be found
    * @return a resource bundle for the given base name and locale
    */
   public ResourceBundle getResourceBundle(String className, Locale locale, String versionRange, ClassLoader classLoader) throws MissingResourceException;
   /**
    * Convert this encoded string back to a Java Object.
    * @param string The string to convert.
    * @param version Version range
    * @return The java object.
    * @throws RuntimeException Runtime errors
    * @throws ClassNotFoundException
    */
   public Object convertStringToObject(String string, String versionRange) throws ClassNotFoundException;
   /**
    * Get the bundle classloader for this package.
    * @param version Version range
    * @param string The class name to find the bundle for.
    * @return The class loader.
    * @throws ClassNotFoundException
    */
   public ClassLoader getBundleClassLoader(String packageName, String versionRange) throws ClassNotFoundException;
   /**
    * Shutdown the bundle for this service.
    * @param service The service object
    */
   public boolean shutdownService(String serviceClass, Object service);
}
