/*
 * Copyright © 2011 jbundle.org. All rights reserved.
 */
package org.jbundle.util.osgi.finder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.jbundle.util.osgi.BundleService;
import org.jbundle.util.osgi.ClassFinder;
import org.jbundle.util.osgi.ClassService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.service.log.LogService;

/**
 * OsgiClassService - Service to find and load bundle classes and resources.
 * 
 * @author don
 * 
 */
public abstract class BaseClassFinderService extends Object
	implements BundleActivator, ClassFinder
{
    public static final String FAKE_CLASSNAME = ".FakeClass";

    /**
	 * Good from start to stop.
	 */
    protected BundleContext bundleContext = null;

    /**
     * Service to find resources by class name.
     * Singleton.
     */
    protected BaseClassFinderService()
    {
        super();
    }
    
    /**
     * Bundle starting.
     * If the service listener is up, register me, else wait.
     */
    public void start(BundleContext context) throws Exception
    {
        ClassServiceUtility.log(context, LogService.LOG_INFO, "Starting and registering the (repository) " + this.getClass().getName() + " ClassService ");
        
        bundleContext = context;

        context.registerService(ClassFinder.class.getName(), this, null);	// Should be only one of these
    }
    /**
     * Bundle shutting down.
     */
    public void stop(BundleContext context) throws Exception {
        ClassServiceUtility.log(context, LogService.LOG_INFO, "Stopping the " + this.getClass().getName() + " ClassService bundle");
        // I'm unregistered automatically

        bundleContext = null;
    }
    /**
     * Find, resolve, and return this bundle.
     * @param packageName
     */
    public Bundle findBundle(String packageName, String version)
    {
        Bundle bundle = this.findBundle(null, bundleContext, packageName, version);

        if (bundle == null) {
            Object resource = this.deployThisResource(packageName, version, true);
            if (resource != null)
            	bundle = this.findBundle(resource, bundleContext, packageName, version);
        }
        return bundle;
    }
    /**
     * Find, resolve, and return this class definition.
     * @param className
     * @return The class definition or null if not found.
     */
    public Class<?> findClass(String className, String version)
    {
        //if (ClassServiceBootstrap.repositoryAdmin == null)
        //    return null;

        Class<?> c = this.getClassFromBundle(null, className, version);

        if (c == null) {
            Object resource = this.deployThisResource(ClassFinderActivator.getPackageName(className, false), version, true);
            if (resource != null)
            {
            	c = this.getClassFromBundle(null, className, version);	// It is possible that the newly started bundle registered itself
            	if (c == null)
            		c = this.getClassFromBundle(resource, className, version);
            }
        }

        return c;
    }
    /**
     * Find, resolve, and return this resource's URL.
     * @param className
     * @return The class definition or null if not found.
     */
    public URL findResourceURL(String className, String version)
    {
        //if (ClassServiceBootstrap.repositoryAdmin == null)
        //    return null;

        URL url = this.getResourceFromBundle(null, className, version);

        if (url == null) {
            Object resource = this.deployThisResource(ClassFinderActivator.getPackageName(className, true), version, true);
            if (resource != null)
            	url = this.getResourceFromBundle(resource, className, version);
        }

        return url;
    }
    /**
     * Find, resolve, and return this ResourceBundle.
     * @param className
     * @return The class definition or null if not found.
     * TODO: Need to figure out how to get the bundle's class loader, so I can set up the resource chain
     */
    public ResourceBundle findResourceBundle(String className, Locale locale, String version)
    {
        //if (ClassServiceBootstrap.repositoryAdmin == null)
        //    return null;

        ResourceBundle resourceBundle = this.getResourceBundleFromBundle(null, className, locale, version);

        if (resourceBundle == null) {
            Object resource = this.deployThisResource(ClassFinderActivator.getPackageName(className, true), version, true);
            if (resource != null)
            {
            	resourceBundle = this.getResourceBundleFromBundle(resource, className, locale, version);
            	if (resourceBundle == null)
            	{
            		Class<?> c = this.getClassFromBundle(resource, className, version);
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
    public Object findConvertStringToObject(String className, String version, String string)
    {
    	Object object = this.convertStringToObject(null, className, version, string);

        if (object == null) {
            Object resource = this.deployThisResource(ClassFinderActivator.getPackageName(className, false), version, true);
            if (resource != null)
            {
            	object = this.convertStringToObject(null, className, version, string);	// It is possible that the newly started bundle registered itself
            	if (object == null)
            		object = this.convertStringToObject(resource, className, version, string);
            }
        }

        return object;
    }
    /**
     * Convert this encoded string back to a Java Object.
     * TODO This is expensive, I need to synchronize and use a static writer.
     * @param version version
     * @param string The string to convert.
     * @return The java object.
     */
    public Object convertStringToObject(Object resource, String className, String version, String string)
    {
        if ((string == null) || (string.length() == 0))
            return null;
        Object object = null;
        try {
            if (resource == null)
            {
                BundleService classAccess = this.getClassBundleService(className, version, null);
                if (classAccess != null)
                	object = classAccess.convertStringToObject(string);
            }
            else
            {
            	/*Bundle bundle =*/ this.findBundle(resource, bundleContext, ClassFinderActivator.getPackageName(className, false), version);
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
    private Object convertStringToObject(String string)
    	throws ClassNotFoundException
    {
        if ((string == null) || (string.length() == 0))
            return null;
        try {
            InputStream reader = new ByteArrayInputStream(string.getBytes(ClassService.OBJECT_ENCODING));//Constants.STRING_ENCODING));
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
     * Get the bundle classloader for this package.
     * @param string The class name to find the bundle for.
     * @return The class loader.
     * @throws ClassNotFoundException
     */
    public ClassLoader findBundleClassLoader(String packageName, String version)
    {
    	ClassLoader classLoader = this.getClassLoaderFromBundle(null, packageName, version);

        if (classLoader == null) {
            Object resource = this.deployThisResource(packageName, version, true);
            if (resource != null)
            	classLoader = this.getClassLoaderFromBundle(resource, packageName, version);
        }

        return classLoader;    	
    }
    /**
     * Find this class's bundle in the repository
     * @param version version
     * @param className
     * @return
     */
    private ClassLoader getClassLoaderFromBundle(Object resource, String packageName, String version)
    {
    	String className = packageName + FAKE_CLASSNAME;
    	ClassLoader classLoader = null;
        if (resource == null)
        {
            BundleService classAccess = this.getClassBundleService(className, version, null);
            if (classAccess != null)
            {
            	classLoader = classAccess.getClass().getClassLoader();
            }
        }
        else
        {
        	Bundle bundle = this.findBundle(resource, bundleContext, ClassFinderActivator.getPackageName(className, false), version);
        	if (bundle == null)
        		return null;
        	@SuppressWarnings("unchecked")
			Enumeration<URL> entries = bundle.findEntries(packageName.replace('.', '/'), "*.class", true);
        	if (entries != null)
        		if (entries.hasMoreElements())
        	{	// This is kind of a hokey way to get the classloader from a bundle - find a class file and load it - but it works.
        		URL url = entries.nextElement();
        		String path = url.getFile();
        		int start = path.startsWith("/") ? 1 : 0;	// Path should always start at the root.
        		path = path.substring(start, path.lastIndexOf('.')).replace('/', '.');
        		try {
					classLoader = bundle.loadClass(path).getClassLoader();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
        	}
        }
        return classLoader;
    }
    /**
     * Find this class's class access registered class access service in the current workspace.
     * @param className
     * @return
     */
    public BundleService getClassBundleService(String className, String version, Dictionary<String, String> filter)
    {
        try {
            String serviceFilter = ClassServiceUtility.addToFilter(null, BundleService.PACKAGE_NAME, ClassFinderActivator.getPackageName(className, true));
            String interfaceName = null;
            if (filter != null)
            {
                interfaceName = filter.get(BundleService.INTERFACE);

                Enumeration<String> keys = filter.keys();
                while (keys.hasMoreElements())
                {
                    String key = keys.nextElement();
                    if (key.equals(BundleService.INTERFACE))
                        continue;
                    serviceFilter = ClassServiceUtility.addToFilter(serviceFilter, key, filter.get(key));
                }
            }
            if (interfaceName == null)
            	interfaceName = className;
            if (interfaceName == null)
            	interfaceName = BundleService.class.getName();	// Never
            serviceFilter = ClassFinderActivator.addVersionFilter(serviceFilter, version, false);
            ServiceReference[] refs = bundleContext.getServiceReferences(interfaceName, serviceFilter);

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
     * @param version version
     * @return
     */
    private Class<?> getClassFromBundle(Object resource, String className, String version)
    {
        Class<?> c = null;
        if (resource == null)
        {
            BundleService classAccess = this.getClassBundleService(className, version, null);
            if (classAccess != null)
            {
            	try {
                	c = classAccess.makeClass(className);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        else
        {
        	Bundle bundle = this.findBundle(resource, bundleContext, ClassFinderActivator.getPackageName(className, false), version);
        	try {
	            c = bundle.loadClass(className);
            } catch (ClassNotFoundException e) {
                c = null;
            }
        }
        return c;
    }
    /**
     * makeClassFromBundle
     * @param className
     * @param version version
     * 
     * @return
     */
    private URL getResourceFromBundle(Object resource, String className, String version)
    {
        URL url = null;
        if (resource == null)
        {
            BundleService classAccess = this.getClassBundleService(className, version, null);
            if (classAccess != null)
                url = classAccess.getResource(className);
        }
        else
        {
        	Bundle bundle = this.findBundle(resource, bundleContext, ClassFinderActivator.getPackageName(className, true), version);
            url = bundle.getEntry(className);
        }
        return url;
    }
    /**
     * Get the Resource Bundle from the Bundle
     * 
     * @param className
     * @return
     */
    boolean USE_NO_RESOURCE_HACK = true; // TODO - There must be a way to get the class loader????
    private ResourceBundle getResourceBundleFromBundle(Object resource, String baseName, Locale locale, String version)
    {
    	ResourceBundle resourceBundle = null;
        if (resource == null)
        {
            BundleService classAccess = this.getClassBundleService(baseName, version, null);
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
        	Bundle bundle = this.findBundle(resource, bundleContext, ClassFinderActivator.getPackageName(baseName, true), version);
            if (USE_NO_RESOURCE_HACK)
            {
                try {
                	// TODO - If I have to do this, then I will have to link up the resourcebundle using the locales.
                	baseName = baseName.replace('.', File.separatorChar) + ClassFinderActivator.PROPERTIES;
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
    public abstract Object deployThisResource(String packageName, String version, boolean start);

    /**
     * Start up a basebundle service.
     * Note: You will probably want to call this from a thread and attach a service
     * listener since this may take some time.
     * @param version version
     * @param className
     * @return true If I'm up already
     * @return false If I had a problem.
     */
    public boolean startBaseBundle(BundleContext context, String dependentBaseBundleClassName, String version)
    {
    	BundleService bundleService = this.getClassBundleService(dependentBaseBundleClassName, version, null);
    	if (bundleService != null)
    		return true;	// Already up!
        // If the repository is not up, but the bundle is deployed, this will find it
        Object resource = this.deployThisResource(ClassFinderActivator.getPackageName(dependentBaseBundleClassName, false), version, false);  // Get the bundle info from the repos
        
        Bundle bundle = this.findBundle(resource, context, ClassFinderActivator.getPackageName(dependentBaseBundleClassName, false), version);
        
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
            bundleService = this.getClassBundleService(dependentBaseBundleClassName, version, null);	// This will wait until it is active to return
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
        Object resource = this.deployThisResource(ClassFinderActivator.getPackageName(dependentBaseBundleClassName, false), null, false);  // Get the bundle info from the repos
    	Bundle bundle = this.findBundle(resource, bundleContext, ClassFinderActivator.getPackageName(dependentBaseBundleClassName, false), null);
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
    
    /**
     * Resource cache code.
     * TODO(don) - Need to listen for stopped bundles.
     */
    protected Map<String,Object> resourceMap = new HashMap<String,Object>(); 
    public Object getResourceFromCache(String packageName)
    {
    	return resourceMap.get(packageName);
    }
    public void addResourceToCache(String packageName, Object resource)
    {
    	resourceMap.put(packageName, resource);
    }

    /**
     * Find the currently installed bundle that exports this package.
     * @param context
     * @param resource
     * @return
     */
    public Bundle findBundle(Object objResource, BundleContext context, String packageName, String version)
    {
        if (context == null)
            return null;
        Bundle[] bundles = context.getBundles();
        Bundle bestBundle = null;
        Version bestVersion = (version == null) ? null : new Version(version.replace(',', '.'));
        for (Bundle bundle : bundles)
        {
            if (objResource != null)
            {
            	if (this.isResourceBundleMatch(objResource, bundle))
                    return bundle;               
            }
            else if (packageName != null)
            {
                Dictionary<?, ?> dictionary = bundle.getHeaders();
                String packages = (String)dictionary.get(Constants.EXPORT_PACKAGE);
                Version bundleVersion = new Version((String)dictionary.get(Constants.BUNDLE_VERSION));
                if (packages != null)
                {
                    StringBuilder sb = new StringBuilder(packages);
                    while (true)
                    {
                        int start = sb.indexOf("=\"");
                        if (start == -1)
                            break;
                        int end = sb.indexOf("\"", start + 2);
                        if ((start > -1) && (end > -1))
                            sb.delete(start, end + 1);
                        else
                            break;  // never
                    }
                    while (true)
                    {
                        int semi = sb.indexOf(";");
                        if (semi == -1)
                            break;
                        int comma = sb.indexOf(",", semi);
                        if (comma == -1)
                            comma = sb.length();
                        else if (sb.charAt(comma + 1) == ' ')
                            comma++;
                        if ((semi > -1) && (comma > -1))
                            sb.delete(semi, comma);
                        else
                            break;  // never
                    }
                    
                    String[] packs = sb.toString().split(",");
                    for (String pack : packs)
                    {
                        if (packageName.equals(pack))
                        {
                        	if ((bestVersion == null)
                        		|| ((bestVersion.getMajor() == bundleVersion.getMajor())
                                	&& (bestVersion.getMinor() <= bundleVersion.getMinor())
                                	/*&& (bestVersion.getMicro() <= bundleVersion.getMicro())*/))
                        	{
                        		bestBundle = bundle;
                        		bestVersion = bundleVersion;
                        	}
                        }
                    }
                }
            }
        }
        
        return bestBundle;
    }
    /**
     * Is this the same bundle (override this if you use a persistence finder - like obr)
     * @param objResource
     * @param bundle
     * @return
     */
    public boolean isResourceBundleMatch(Object objResource, Bundle bundle)
    {
    	return false;	// Override this
    }

}
