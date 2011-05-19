package org.jbundle.util.osgi.finder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.jbundle.util.osgi.ClassService;


/**
 * Thin specific static utility methods.
 */
public class ClassServiceUtility
implements ClassService
{

    static ClassService classService = null;
    public static ClassService getClassService()
    {
        if (classService == null)
            classService = new ClassServiceUtility();
        return classService;
    }

    /**
     * Get the Osgi class service.
     * NOTE: Don't import this package as the ClassService class may not be available until this service is started.
     * @return
     */
    public static boolean classServiceAvailable = true;
    public org.jbundle.util.osgi.ClassFinder getClassFinder(Object context, boolean waitForStart)
    {
        if (!classServiceAvailable)
            return null;
        try {
            Class.forName("org.osgi.framework.BundleActivator");	// This tests to see if osgi exists
            return (org.jbundle.util.osgi.ClassFinder)org.jbundle.util.osgi.finder.ClassFinderActivator.getClassFinder(context, waitForStart);
        } catch (ClassNotFoundException ex) {
            classServiceAvailable = false;	// Osgi is not installed, no need to keep trying
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    /**
     * Create this object given the class name.
     * @param className
     * @return
     */
    public Object makeObjectFromClassName(String className)
    {
        return this.makeObjectFromClassName(className, false);
    }
    /**
     * Create this object given the class name.
     * @param className
     * @return
     */
    public Object makeObjectFromClassName(String className, boolean bErrorIfNotFound) throws RuntimeException
    {
        if (className == null)
            return null;
        className = ClassServiceUtility.getFullClassName(className);

        Class<?> clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            if (this.getClassFinder(null, true) != null)
                clazz = this.getClassFinder(null, true).findClass(className);	// Try to find this class in the obr repos
            if (clazz == null)
                if (bErrorIfNotFound)
                    throw new RuntimeException(e.getMessage());
        }

        Object object = null;
        try {
            if (clazz != null)
                object = clazz.newInstance();
        } catch (InstantiationException e)   {
            if (bErrorIfNotFound)
                throw new RuntimeException(e.getMessage());
        } catch (IllegalAccessException e)   {
            if (bErrorIfNotFound)
                throw new RuntimeException(e.getMessage());
        } catch (Exception e) {
            if (bErrorIfNotFound)
                throw new RuntimeException(e.getMessage());
        }
        return object;
    }
    /**
     * Create this object given the class name.
     * @param filepath
     * @return
     */
    public URL getResourceURL(String filepath, URL urlCodeBase, ClassLoader classLoader) throws RuntimeException
    {
        if (filepath == null)
            return null;

        URL url = null;
        try {
            url = classLoader.getResource(filepath);
        } catch (Exception e) {
            // Keep trying
        }

        if (url == null)
        {
            if (this.getClassFinder(null, true) != null)
                url = this.getClassFinder(null, true).findResourceURL(filepath);	// Try to find this class in the obr repos
        }

        if (url == null)
        {
            try
            {
                if (urlCodeBase != null)
                    url = new URL(urlCodeBase, filepath);
            } catch(MalformedURLException ex) {
                // Keep trying
            } catch (Exception e) {
                // Keep trying
            }
        }
        return url;
    }
    /**
     * Gets a resource bundle using the specified base name and locale,
     * @param locale the locale for which a resource bundle is desired
     * @param baseName the base name of the resource bundle, a fully qualified class name
     * @exception NullPointerException if <code>baseName</code> or <code>locale</code> is <code>null</code>
     * @exception MissingResourceException if no resource bundle for the specified base name can be found
     * @return a resource bundle for the given base name and locale
     */
    public final ResourceBundle getResourceBundle(String className, Locale locale, ClassLoader classLoader) throws MissingResourceException
    {
        MissingResourceException ex = null;
        ResourceBundle resourceBundle = null;
        try {
            resourceBundle = ResourceBundle.getBundle(className, locale);
        } catch (MissingResourceException e) {
            ex = e;
        }

        if (resourceBundle == null)
        {
            try {
                if (this.getClassFinder(null, true) != null)
                    resourceBundle = this.getClassFinder(null, true).findResourceBundle(className, locale);	// Try to find this class in the obr repos
            } catch (MissingResourceException e) {
                ex = e;
            }
        }

        if (resourceBundle == null)
            if (ex != null)
                throw ex;

        return resourceBundle;
    }
    /**
     * Convert this encoded string back to a Java Object.
     * TODO This is expensive, I need to synchronize and use a static writer.
     * @param string The string to convert.
     * @return The java object.
     */
    public Object convertStringToObject(String string, boolean bErrorIfNotFound) throws RuntimeException
    {
        if (string == null)
            return null;

        Object object  = null;
        try {
            object = this.convertStringToObject(string);
        } catch (ClassNotFoundException e) {
            if (this.getClassFinder(null, true) != null)
            {
                String className = null;
                int startClass = e.getMessage().indexOf('\'') + 1;
                int endClass = e.getMessage().indexOf('\'', startClass);
                if (endClass != -1)
                    className = e.getMessage().substring(startClass, endClass);
                object = this.getClassFinder(null, true).findConvertStringToObject(className, string);	// Try to find this class in the obr repos
            }
            if (object == null)
                if (bErrorIfNotFound)
                    throw new RuntimeException(e.getMessage());
        }

        return object;
    }
    /**
     * Convert this encoded string back to a Java Object.
     * TODO This is expensive, I need to synchronize and use a static writer.
     * @param string The string to convert.
     * @return The java object.
     * @throws ClassNotFoundException 
     */
    private Object convertStringToObject(String string)
        throws ClassNotFoundException
    {
        if ((string == null) || (string.length() == 0))
            return null;
        try {
            InputStream reader = new ByteArrayInputStream(string.getBytes(OBJECT_ENCODING));//Constants.STRING_ENCODING));
            ObjectInputStream inStream = new ObjectInputStream(reader);
            Object obj = inStream.readObject();
            reader.close();
            inStream.close();
            return obj;
        } catch (IOException ex)    {
            ex.printStackTrace();   // Never
        }
        return null;
    }
    /**
     * If class name starts with '.' append base package.
     */
    public static String getFullClassName(String strClassName) {
        return ClassServiceUtility.getFullClassName(null, strClassName);
    }
    /**
     * If class name starts with '.' append base package.
     */
    public static String getFullClassName(String strPackage, String strClass) {
        if (strPackage != null)
            if (strPackage.length() > 0) {
                if (strPackage.charAt(strPackage.length() - 1) != '.')
                    strPackage = strPackage + '.';
            }
        if (strClass != null)
            if (strClass.length() > 0) {
                if (strClass.indexOf('.') == -1)
                    if (strPackage != null)
                        strClass = strPackage + strClass;
                if (strClass.charAt(0) == '.')
                    strClass = ClassService.ROOT_PACKAGE + strClass.substring(1);
            }
        return strClass;
    }	
    /**
     * Shutdown the bundle for this service.
     * @param service The service object
     */
    public void shutdownService(Object service)
    {
        if (this.getClassFinder(null, true) != null)
            this.getClassFinder(null, true).shutdownService(service);   // Shutdown the bundle for this service
    }
}
