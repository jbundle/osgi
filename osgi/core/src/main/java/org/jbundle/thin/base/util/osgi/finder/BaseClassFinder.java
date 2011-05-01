package org.jbundle.thin.base.util.osgi.finder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.jbundle.thin.base.util.osgi.bundle.BundleService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * OsgiClassService - Service to find and load bundle classes and resources.
 * 
 * @author don
 * 
 */
public abstract class BaseClassFinder extends Object
	implements BundleActivator, ClassFinder
{
	/**
	 * Good from start to stop.
	 */
    protected BundleContext bundleContext = null;

    /**
     * Service to find resources by class name.
     * Singleton.
     */
    protected BaseClassFinder()
    {
        super();
    }
    
    /**
     * Bundle starting.
     * If the service listener is up, register me, else wait.
     */
    public void start(BundleContext context) throws Exception
    {
        System.out.println("Starting and registering the (repository) ClassService");
        
        bundleContext = context;

        context.registerService(ClassFinder.class.getName(), this, null);	// Should be only one of these
    }
    /**
     * Bundle shutting down.
     */
    public void stop(BundleContext context) throws Exception {
        System.out.println("Stopping ClassService bundle");
        // I'm unregistered automatically

        bundleContext = null;
    }
    /**
     * Find, resolve, and return this class definition.
     * @param className
     * @return The class definition or null if not found.
     */
    public Class<?> findClassBundle(String className)
    {
        //if (ClassServiceBootstrap.repositoryAdmin == null)
        //    return null;

        Class<?> c = this.getClassFromBundle(null, className);

        if (c == null) {
            Object resource = this.deployThisResource(className, true, false);
            if (resource != null)
            {
            	c = this.getClassFromBundle(null, className);	// It is possible that the newly started bundle registered itself
            	if (c == null)
            		c = this.getClassFromBundle(resource, className);
            }
        }

        return c;
    }
    /**
     * Find, resolve, and return this resource's URL.
     * @param className
     * @return The class definition or null if not found.
     */
    public URL findBundleResource(String className)
    {
        //if (ClassServiceBootstrap.repositoryAdmin == null)
        //    return null;

        URL url = this.getResourceFromBundle(null, className);

        if (url == null) {
            Object resource = this.deployThisResource(className, true, true);
            if (resource != null)
            	url = this.getResourceFromBundle(resource, className);
        }

        return url;
    }
    /**
     * Find, resolve, and return this ResourceBundle.
     * @param className
     * @return The class definition or null if not found.
     * TODO: Need to figure out how to get the bundle's class loader, so I can set up the resource chain
     */
    public ResourceBundle findResourceBundle(String className, Locale locale)
    {
        //if (ClassServiceBootstrap.repositoryAdmin == null)
        //    return null;

        ResourceBundle resourceBundle = this.getResourceBundleFromBundle(null, className, locale);

        if (resourceBundle == null) {
            Object resource = this.deployThisResource(className, true, true);
            if (resource != null)
            {
            	resourceBundle = this.getResourceBundleFromBundle(resource, className, locale);
            	if (resourceBundle == null)
            	{
            		Class<?> c = this.getClassFromBundle(resource, className);
            		if (c != null)
            		{
					   try {
						   resourceBundle = (ResourceBundle)c.newInstance();
					   } catch (InstantiationException e)   {
					       e.printStackTrace();	// Never
					   } catch (IllegalAccessException e)   {
					       e.printStackTrace();	// Never
					   } catch (Exception e) {
					       e.printStackTrace();	// Never
					   }            			
            		}
            	}
            }
        }

        return resourceBundle;
    }
    /**
     * Convert this encoded string back to a Java Object.
     * @param string The string to convert.
     * @return The java object.
     */
    public Object findResourceConvertStringToObject(String className, String string)
    {
    	Object object = this.getResourceConvertStringToObject(null, className, string);

        if (object == null) {
            Object resource = this.deployThisResource(className, true, false);
            if (resource != null)
            {
            	object = this.getResourceConvertStringToObject(null, className, string);	// It is possible that the newly started bundle registered itself
            	if (object == null)
            		object = this.getResourceConvertStringToObject(resource, className, string);
            }
        }

        return object;
    }
    /**
     * Convert this encoded string back to a Java Object.
     * TODO This is expensive, I need to synchronize and use a static writer.
     * @param string The string to convert.
     * @return The java object.
     */
    public Object getResourceConvertStringToObject(Object resource, String className, String string)
    {
        if ((string == null) || (string.length() == 0))
            return null;
        Object object = null;
        try {
            if (resource == null)
            {
                BundleService classAccess = this.getClassBundleService(null, className);
                if (classAccess != null)
                	object = classAccess.convertStringToObject(string);
            }
            else
            {
            	Bundle bundle = this.getBundleFromResource(resource, bundleContext, ClassFinderUtility.getPackageName(className, false));
	            object = this.convertStringToObject(string);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
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
    public Object convertStringToObject(String string)
    	throws ClassNotFoundException
    {
        if ((string == null) || (string.length() == 0))
            return null;
        try {
            InputStream reader = new ByteArrayInputStream(string.getBytes(ClassFinderUtility.OBJECT_ENCODING));//Constants.STRING_ENCODING));
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
     * Find this class's class access registered class access service in the current workspace.
     * @param className
     * @return
     */
    public BundleService getClassBundleService(String interfaceName, String className)
    {
        try {
            String filter = "(" + BundleService.PACKAGE_NAME + "=" + ClassFinderUtility.getPackageName(className, true) + ")";
            if (interfaceName == null)
            	interfaceName = className;
            if (interfaceName == null)
            	interfaceName = BundleService.class.getName();	// Never
            ServiceReference[] refs = bundleContext.getServiceReferences(interfaceName, filter);

            if ((refs != null) && (refs.length > 0))
                return (BundleService)bundleContext.getService(refs[0]);
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * Find this class's bundle in the repository
     * @param className
     * @return
     */
    private Class<?> getClassFromBundle(Object resource, String className)
    {
        Class<?> c = null;
        try {
            if (resource == null)
            {
                BundleService classAccess = this.getClassBundleService(null, className);
                if (classAccess != null)
                	c = classAccess.makeClass(className);
            }
            else
            {
            	Bundle bundle = this.getBundleFromResource(resource, bundleContext, ClassFinderUtility.getPackageName(className, false));
	            c = bundle.loadClass(className);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return c;
    }
    /**
     * makeClassFromBundle
     * 
     * @param className
     * @return
     */
    private URL getResourceFromBundle(Object resource, String className)
    {
        URL url = null;
        if (resource == null)
        {
            BundleService classAccess = this.getClassBundleService(null, className);
            if (classAccess != null)
                url = classAccess.getResource(className);
        }
        else
        {
        	Bundle bundle = this.getBundleFromResource(resource, bundleContext, ClassFinderUtility.getPackageName(className, true));
            url = bundle.getEntry(className);
        }
        return url;
    }
    /**
     * makeClassFromBundle
     * 
     * @param className
     * @return
     */
    boolean USE_NO_RESOURCE_HACK = true; // TODO - There must be a way to get the class loader????
    private ResourceBundle getResourceBundleFromBundle(Object resource, String baseName, Locale locale)
    {
    	ResourceBundle resourceBundle = null;
        if (resource == null)
        {
            BundleService classAccess = this.getClassBundleService(null, baseName);
            if (classAccess != null)
            {
                if (USE_NO_RESOURCE_HACK)
                {
	                try {
						URL url = classAccess.getResource(baseName);
						InputStream stream = url.openStream();
						resourceBundle = new PropertyResourceBundle(stream);
					} catch (IOException e) {
						e.printStackTrace();
					}
                }
                else
                {
                    ClassLoader loader = classAccess.getClass().getClassLoader();
                	resourceBundle = ResourceBundle.getBundle(baseName, locale, loader);
                }
            }
        }
        else
        {
        	Bundle bundle = this.getBundleFromResource(resource, bundleContext, ClassFinderUtility.getPackageName(baseName, true));
            if (USE_NO_RESOURCE_HACK)
            {
                try {
                	// TODO - If I have to do this, then I will have to link up the resourcebundle using the locales.
                	baseName = baseName.replace('.', File.separatorChar) + ClassFinderUtility.PROPERTIES;
					URL url = bundle.getEntry(baseName);
					if (url != null)
						resourceBundle = new PropertyResourceBundle(url.openStream());
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
            else
            {
            	ClassLoader loader = bundle.getClass().getClassLoader();
            	resourceBundle = ResourceBundle.getBundle(baseName, locale, loader);
            }
        }
        return resourceBundle;
    }
    /**
     * Find this resource in the repository, then deploy and optionally start it.
     * @param className
     * @param options 
     * @return
     */
    public abstract Object deployThisResource(String className, boolean start, boolean resourceType);
    /**
     * Find the currently installed bundle that exports this package.
     * NOTE: This is stupid, there has to be a way to do this.
     * @param resource
     * @param context
     * @return
     */
    public abstract Bundle getBundleFromResource(Object resource, BundleContext context, String packageName);

    /**
     * Start up a basebundle service.
     * Note: You will probably want to call this from a thread and attach a service
     * listener since this may take some time.
     * @param className
     * @return true If I'm up already
     * @return false If I had a problem.
     */
    public boolean startBaseBundle(BundleContext context, String dependentBaseBundleClassName)
    {
    	BundleService bundleService = this.getClassBundleService(null, dependentBaseBundleClassName);
    	if (bundleService != null)
    		return true;	// Already up!
        // If the repository is not up, but the bundle is deployed, this will find it
        Object resource = this.deployThisResource(dependentBaseBundleClassName, false, false);  // Get the bundle info from the repos
        
        String packageName = ClassFinderUtility.getPackageName(dependentBaseBundleClassName, false);
        Bundle bundle = this.getBundleFromResource(resource, context, packageName);
        
        if (bundle != null)
        {
            if (((bundle.getState() & Bundle.ACTIVE) == 0)
            		&& ((bundle.getState() & Bundle.STARTING) == 0))
            {
                try {
                    bundle.start();
                } catch (BundleException e) {
                    e.printStackTrace();
                }
            }
            bundleService = this.getClassBundleService(null, dependentBaseBundleClassName);	// This will wait until it is active to return
            return (bundleService != null);	// Success
        }
        return false;	// Error! Where is my bundle?
    }
    /**
     * Shutdown the bundle for this service.
     * @param service The service object
     */
    public void shutdownService(Object service)
    {
    	// Lame code
    	String dependentBaseBundleClassName = service.getClass().getName();
        Object resource = this.deployThisResource(dependentBaseBundleClassName, false, false);  // Get the bundle info from the repos
    	Bundle bundle = this.getBundleFromResource(resource, bundleContext, ClassFinderUtility.getPackageName(dependentBaseBundleClassName, false));
    	if (bundle != null)
    		if (bundle.getState() == Bundle.ACTIVE)
    		{
    			try {
					bundle.stop();
				} catch (BundleException e) {
					e.printStackTrace();
				}
    		}
    }
}
