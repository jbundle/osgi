package org.jbundle.util.osgi;

import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Thin specific static utility methods.
 */
public interface ClassService
{
    /**
     * The byte to char and back encoding that I use.
     */
    public static final String OBJECT_ENCODING = "ISO-8859-1";
    public static String ROOT_PACKAGE = "org.jbundle.";  // Default package prefix

   /**
    * Get the Osgi class service.
    * NOTE: Don't import this package as the ClassService class may not be available until this service is started.
    * @param context TODO
 * @return
    */
   public ClassFinder getClassFinder(Object context);
   /**
    * Create this object given the class name.
    * @param className
    * @return
    */
   public Object makeObjectFromClassName(String className);
   /**
    * Create this object given the class name.
    * @param className
    * @return
    * @throws RuntimeException If Error flag is set, return a runtime exception if object can't be created.
    */
   public Object makeObjectFromClassName(String className, boolean bErrorIfNotFound) throws RuntimeException;
   /**
    * Create this object given the class name.
    * @param filepath
    * @return
    * @throws RuntimeException Not implemented yet
    */
   public URL getResourceURL(String filepath, URL urlCodeBase, ClassLoader classLoader) throws RuntimeException;
   /**
    * Gets a resource bundle using the specified base name and locale,
 * @param locale the locale for which a resource bundle is desired
 * @param baseName the base name of the resource bundle, a fully qualified class name
 * @throws MissingResourceException TODO
 * @exception NullPointerException if <code>baseName</code> or <code>locale</code> is <code>null</code>
 * @exception MissingResourceException if no resource bundle for the specified base name can be found
 * @return a resource bundle for the given base name and locale
    */
   public ResourceBundle getResourceBundle(String className, Locale locale, ClassLoader classLoader) throws MissingResourceException;
   /**
    * Convert this encoded string back to a Java Object.
    * @param string The string to convert.
    * @return The java object.
    * @throws RuntimeException Runtime errors
    * @throws ClassNotFoundException
    */
   public Object convertStringToObject(String string) throws ClassNotFoundException;
   /**
    * Get the bundle classloader for this package.
    * @param string The class name to find the bundle for.
    * @return The class loader.
    * @throws ClassNotFoundException
    */
   public ClassLoader getBundleClassLoader(String packageName) throws ClassNotFoundException;
   /**
    * Shutdown the bundle for this service.
    * @param service The service object
    */
   public void shutdownService(Object service);
}
