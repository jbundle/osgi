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
    * @param waitForStart TODO
    * @return
    */
   public ClassFinder getClassFinder(Object context, boolean waitForStart);
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
    */
   public Object makeObjectFromClassName(String className, Object task, boolean bErrorIfNotFound);
   /**
    * Create this object given the class name.
    * @param filepath
    * @return
    */
   public URL getResourceFromPathName(String filepath, Object task, boolean bErrorIfNotFound, URL urlCodeBase, ClassLoader classLoader);
   /**
    * Gets a resource bundle using the specified base name and locale,
    * @param baseName the base name of the resource bundle, a fully qualified class name
    * @param locale the locale for which a resource bundle is desired
    * @exception NullPointerException if <code>baseName</code> or <code>locale</code> is <code>null</code>
    * @exception MissingResourceException if no resource bundle for the specified base name can be found
    * @return a resource bundle for the given base name and locale
    */
   public ResourceBundle getResourceBundle(String className, Locale locale, Object task, boolean bErrorIfNotFound, ClassLoader classLoader);
   /**
    * Convert this encoded string back to a Java Object.
    * @param string The string to convert.
    * @return The java object.
    */
   public Object convertStringToObject(String string, Object task, boolean bErrorIfNotFound);
   /**
    * Convert this encoded string back to a Java Object.
    * @param string The string to convert.
    * @return The java object.
    * @throws ClassNotFoundException 
    */
   public Object convertStringToObject(String string)
   		throws ClassNotFoundException;
   /**
    * Handle this error.
    * @param ex
    * @param className
    * @param task
    * @param bErrorIfNotFound
    */
   public void handleClassException(Exception ex, String className, Object task, boolean bErrorIfNotFound);
   /**
    * Shutdown the bundle for this service.
    * @param service The service object
    */
   public void shutdownService(Object service);
}
