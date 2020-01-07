/*
 * Copyright © 2012 jbundle.org. All rights reserved.
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
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jbundle.util.osgi.BundleConstants;
import org.jbundle.util.osgi.ClassFinder;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * BaseClassFinderService - Service to find and load bundle classes and resources.
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
        this.log(context, LogService.LOG_INFO, "Starting and registering the (repository) " + this.getClass().getName() + " ClassService ");
        
        bundleContext = context;

        if (context != null)
            context.registerService(ClassFinder.class.getName(), this, null);	// Should be only one of these
    }
    /**
     * Bundle shutting down.
     */
    public void stop(BundleContext context) throws Exception {
        this.log(context, LogService.LOG_INFO, "Stopping the " + this.getClass().getName() + " ClassService bundle");
        // I'm unregistered automatically

        bundleContext = null;
    }
    /**
     * Find, resolve, and return this class definition.
     * @param className
     * @return The class definition or null if not found.
     */
    public Class<?> findClass(String className, String versionRange)
    {
        //if (ClassServiceBootstrap.repositoryAdmin == null)
        //    return null;

        Class<?> c = this.getClassFromBundle(null, className, versionRange);

        if (c == null) {
            Object resource = this.deployThisResource(ClassFinderActivator.getPackageName(className, false), versionRange, false);
            if (resource != null)
            {
            	c = this.getClassFromBundle(null, className, versionRange);	// It is possible that the newly started bundle registered itself
            	if (c == null)
            		c = this.getClassFromBundle(resource, className, versionRange);
            }
        }

        return c;
    }
    /**
     * Find, resolve, and return this resource's URL.
     * @param resourcePath
     * @return The class definition or null if not found.
     */
    public URL findResourceURL(String resourcePath, String versionRange)
    {
        //if (ClassServiceBootstrap.repositoryAdmin == null)
        //    return null;

        URL url = this.getResourceFromBundle(null, resourcePath, versionRange);

        if (url == null) {
            Object resource = this.deployThisResource(ClassFinderActivator.getPackageName(resourcePath, true), versionRange, false);
            if (resource != null)
            	url = this.getResourceFromBundle(resource, resourcePath, versionRange);
        }

        return url;
    }
    /**
     * Find, resolve, and return this ResourceBundle.
     * @param resourcePath
     * @return The class definition or null if not found.
     * TODO: Need to figure out how to get the bundle's class loader, so I can set up the resource chain
     */
    public ResourceBundle findResourceBundle(String resourcePath, Locale locale, String versionRange)
    {
        //if (ClassServiceBootstrap.repositoryAdmin == null)
        //    return null;

        ResourceBundle resourceBundle = this.getResourceBundleFromBundle(null, resourcePath, locale, versionRange);

        if (resourceBundle == null) {
            Object resource = this.deployThisResource(ClassFinderActivator.getPackageName(resourcePath, true), versionRange, false);
            if (resource != null)
            {
            	resourceBundle = this.getResourceBundleFromBundle(resource, resourcePath, locale, versionRange);
            	if (resourceBundle == null)
            	{
            		Class<?> c = this.getClassFromBundle(resource, resourcePath, versionRange);
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
    public Object findConvertStringToObject(String className, String versionRange, String string)
    {
    	Object object = this.convertStringToObject(null, className, versionRange, string);

        if (object == null) {
            Object resource = this.deployThisResource(ClassFinderActivator.getPackageName(className, false), versionRange, false);
            if (resource != null)
            {
            	object = this.convertStringToObject(null, className, versionRange, string);	// It is possible that the newly started bundle registered itself
            	if (object == null)
            		object = this.convertStringToObject(resource, className, versionRange, string);
            }
        }

        return object;
    }
    /**
     * Convert this encoded string back to a Java Object.
     * @param versionRange version
     * @param string The string to convert.
     * @return The java object.
     */
    public Object convertStringToObject(Object resource, String className, String versionRange, String string)
    {
        if ((string == null) || (string.length() == 0))
            return null;
        Object object = null;
        try {
            if (resource == null)
            {
                Object classAccess = this.getClassBundleService(null, className, versionRange, null, 0);
                if (classAccess != null)
                	object = this.convertStringToObject(string);
            }
            else
            {
            	/*Bundle bundle =*/ this.findBundle(resource, bundleContext, ClassFinderActivator.getPackageName(className, false), versionRange);
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
            InputStream reader = new ByteArrayInputStream(string.getBytes(BundleConstants.OBJECT_ENCODING));//Constants.STRING_ENCODING));
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
     * @param packageName The class name to find the bundle for.
     * @return The class loader.
     * @throws ClassNotFoundException
     */
    public ClassLoader findBundleClassLoader(String packageName, String versionRange)
    {
    	ClassLoader classLoader = this.getClassLoaderFromBundle(null, packageName, versionRange);

        if (classLoader == null) {
            Object resource = this.deployThisResource(packageName, versionRange, false);
            if (resource != null)
            	classLoader = this.getClassLoaderFromBundle(resource, packageName, versionRange);
        }

        return classLoader;    	
    }
    /**
     * Find this class's bundle in the repository
     * @param versionRange version
     * @param packageName
     * @return
     */
    private ClassLoader getClassLoaderFromBundle(Object resource, String packageName, String versionRange)
    {
    	ClassLoader classLoader = null;
        if (resource == null)
        {
            Object classAccess = this.getClassBundleService(null, packageName + FAKE_CLASSNAME, versionRange, null, 0);
            if (classAccess != null)
            {
            	classLoader = classAccess.getClass().getClassLoader();
            }
        }
        else
        {
        	Bundle bundle = this.findBundle(resource, bundleContext, packageName, versionRange);
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
     * @param interfaceClassName The class name (that has the package that the object was registered under)
     * @param serviceClassName The service (or activator) class name.
     * @param versionRange Version range
     * @param filter Other filters to use to find the service
     * @param secsToWait Time to wait for service to start (0=don't wait) WARNING: This may take a while, so don't run this in your main thread.
     * @return The service
     */
    public Object getClassBundleService(String interfaceClassName, String serviceClassName, String versionRange, Dictionary<String, Object> filter, int secsToWait)
    {
        ServiceReference serviceReference = null;
        
        if (interfaceClassName != null)
        	serviceReference = getClassServiceReference(bundleContext, interfaceClassName, versionRange, filter);
        if (serviceReference == null)
        	if (serviceClassName != null)
    	{
    		serviceReference = getClassServiceReference(bundleContext, serviceClassName, versionRange, filter);
    		if (serviceReference == null)
                serviceReference = getClassServiceReference(bundleContext, ServiceTracker.class.getName(), versionRange, ClassServiceUtility.addToFilter(filter, BundleConstants.SERVICE_CLASS, serviceClassName, true));
            if (serviceReference == null)
                serviceReference = getClassServiceReference(bundleContext, ServiceTracker.class.getName(), versionRange, ClassServiceUtility.addToFilter(filter, BundleConstants.ACTIVATOR, serviceClassName, true));
            // If you pass an activator class name, the service class may be different
    	}
        if (serviceReference == null)
            serviceReference = getClassServiceReference(bundleContext, ServiceTracker.class.getName(), versionRange, ClassServiceUtility.addToFilter(filter, BundleConstants.PACKAGE, ClassFinderActivator.getPackageName((interfaceClassName == null) ? serviceClassName : interfaceClassName, false), true));
        if (serviceReference == null)
            serviceReference = getClassServiceReference(bundleContext, ServiceTracker.class.getName(), versionRange, ClassServiceUtility.addToFilter(filter, BundleConstants.SERVICE_PID, ClassFinderActivator.getPackageName((interfaceClassName == null) ? serviceClassName : interfaceClassName, false), true));
        serviceReference = checkService(serviceReference, interfaceClassName);
        
        if ((serviceReference != null) && ((serviceReference.getBundle().getState() & Bundle.ACTIVE) != 0))
            return bundleContext.getService(serviceReference);

        if (secsToWait != 0)
            return ClassFinderActivator.waitForServiceStartup(bundleContext, interfaceClassName, serviceClassName, versionRange, filter, secsToWait);

        return null;
    }
    /**
     * Make sure this service reference is the correct interface/class
     * @param serviceReference
     * @param interfaceClassName
     * @return
     */
    private ServiceReference checkService(ServiceReference serviceReference, String interfaceClassName)
    {
        if (serviceReference != null)
        {
            Object service = bundleContext.getService(serviceReference);
            if (service != null)
            {
                try {
                    if (interfaceClassName != null)
                        if (!service.getClass().isAssignableFrom(Class.forName(interfaceClassName)))
                            serviceReference = null;
                } catch (ClassNotFoundException e) {
                    // Ignore this error
                }
            }
        }
        return serviceReference;
    }
    /**
     * Find this class's class access registered class access service in the current workspace.
     * @param context
     * @param interfaceClassName
     * @param versionRange
     * @param filter
     * @return
     */
    public static ServiceReference getClassServiceReference(BundleContext context, String interfaceClassName, String versionRange, Dictionary<String, Object> filter)
    {
        try {
            String serviceFilter = null;//ClassServiceUtility.addToFilter((String)null, BundleActivatorModel.PACKAGE_NAME, ClassFinderActivator.getPackageName(className, true));
            String interfaceName = null;
            if (filter != null)
            {
                interfaceName = filter.get(BundleConstants.INTERFACE) == null ? null : filter.get(BundleConstants.INTERFACE).toString();

                Enumeration<String> keys = filter.keys();
                while (keys.hasMoreElements())
                {
                    String key = keys.nextElement();
                    if (key.equals(BundleConstants.INTERFACE))
                        continue;
                    serviceFilter = ClassServiceUtility.addToFilter(serviceFilter, key, filter.get(key));
                }
            }
            if (interfaceName == null)
                interfaceName = interfaceClassName;
            serviceFilter = ClassFinderActivator.addVersionFilter(serviceFilter, versionRange);
            ServiceReference[] refs = null;
            if (context != null)
                refs = context.getServiceReferences(interfaceName, serviceFilter);

            if ((refs != null) && (refs.length > 0))
                return refs[0];
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * Find this class's bundle in the repository
     * @param className
     * @param versionRange version
     * @return
     */
    private Class<?> getClassFromBundle(Object resource, String className, String versionRange)
    {
        Class<?> c = null;
        if (resource == null)
        {
            Object classAccess = this.getClassBundleService(null, className, versionRange, null, 0);
            if (classAccess != null)
            {
            	try {
                	c = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        else
        {
        	Bundle bundle = this.findBundle(resource, bundleContext, ClassFinderActivator.getPackageName(className, false), versionRange);
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
     * @param resourcePath
     * @param versionRange version
     * 
     * @return
     */
    private URL getResourceFromBundle(Object resource, String resourcePath, String versionRange)
    {
        URL url = null;
        if (resource == null)
        {
            Object classAccess = this.getClassBundleService(null, ClassFinderActivator.getPackageName(resourcePath, true) + FAKE_CLASSNAME, versionRange, null, 0);
            if (classAccess != null)
                url = classAccess.getClass().getClassLoader().getResource(resourcePath);
        }
        else
        {
        	Bundle bundle = this.findBundle(resource, bundleContext, ClassFinderActivator.getPackageName(resourcePath, true), versionRange);
            url = bundle.getEntry(resourcePath);
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
    private ResourceBundle getResourceBundleFromBundle(Object resource, String baseName, Locale locale, String versionRange)
    {
    	ResourceBundle resourceBundle = null;
        if (resource == null)
        {
            Object classAccess = this.getClassBundleService(null, baseName, versionRange, null, 0);
            if (classAccess != null)
            {
                if ((classAccess != null) && (USE_NO_RESOURCE_HACK))
                {
	                try {
						URL url = classAccess.getClass().getClassLoader().getResource(baseName);
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
        	Bundle bundle = this.findBundle(resource, bundleContext, ClassFinderActivator.getPackageName(baseName, true), versionRange);
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
     * @param packageName
     * @param versionRange
     * @return
     */
    public abstract Object deployThisResource(String packageName, String versionRange, boolean start);

    /**
     * Start up a basebundle service.
     * Note: You will probably want to call this from a thread and attach a service
     * listener since this may take some time.
     * @param versionRange version
     * @param secsToWait Time to wait for startup 0=0, -1=default
     * @param dependentServiceClassName
     * @return true If I'm up already
     * @return false If I had a problem.
     */
    public boolean startBaseBundle(BundleContext context, String interfaceClassName, String dependentServiceClassName, String versionRange, Dictionary<String, Object> filter, int secsToWait)
    {
        ServiceReference ServiceReference = getClassServiceReference((bundleContext != null) ? bundleContext : context, interfaceClassName, versionRange, filter);
        
        if ((ServiceReference != null) && ((ServiceReference.getBundle().getState() & Bundle.ACTIVE) != 0))
            return true;    // Already up!

        // If the repository is not up, but the bundle is deployed, this will find it
    	return (ClassFinderActivator.waitForServiceStartup(context, interfaceClassName, dependentServiceClassName, versionRange, null, secsToWait) != null);
    }
    /**
     * Shutdown the bundle for this service.
     * @param service The interface the service is registered under.
     * @param service The service or the package name of the service (service pid) that I'm looking for.
     * @return True if successful
     */
    public boolean shutdownService(String serviceClass, Object service)
    {
        if (service == null)
            return false;
        if (bundleContext == null)
            return false;
        String filter = null;
        if (serviceClass == null)
            if (!(service instanceof String))
                serviceClass = service.getClass().getName();
        if (service instanceof String)
            filter = ClassServiceUtility.addToFilter("", BundleConstants.SERVICE_PID, (String)service);
        ServiceReference[] refs;
        try {
            refs = bundleContext.getServiceReferences(serviceClass, filter);

            if ((refs == null) || (refs.length == 0))
                return false;
            for (ServiceReference reference : refs)
            {
                if ((bundleContext.getService(reference) == service) || (service instanceof String))
                {
                    if (refs.length == 1)
                    {    // Last/only one, shut down the service
                        // Lame code
                        String dependentBaseBundleClassName = service.getClass().getName();
                        String packageName = ClassFinderActivator.getPackageName(dependentBaseBundleClassName, false);
                        if (service instanceof String)
                            packageName = (String)service;
                        Bundle bundle = this.findBundle(null, bundleContext, packageName, null);
                        if (bundle != null)
                            if ((bundle.getState() & Bundle.ACTIVE) != 0)
                        {
                            try {
                                bundle.stop();
                            } catch (BundleException e) {
                                e.printStackTrace();
                            }
                        }
                        return true;
                    }
                }
            }
        } catch (InvalidSyntaxException e1) {
            e1.printStackTrace();
        }
        return false;    // Not found?
    }
    
    /**
     * Resource cache code.
     * TODO(don) - Need to listen for uninstalled bundles.
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
     * @param bundleContext
     * @param objResource
     * @return
     */
    public Bundle findBundle(Object objResource, Object bundleContext, String packageName, String versionRange)
    {
        if (bundleContext == null)
            bundleContext = this.bundleContext;
        if (bundleContext == null)
            return null;
        if (objResource == null)
            return BaseClassFinderService.findBundle((BundleContext)bundleContext, packageName, versionRange);
        Bundle[] bundles = ((BundleContext)bundleContext).getBundles();
        for (Bundle bundle : bundles)
        {
            if (objResource != null)
            {
                if (this.isResourceBundleMatch(objResource, bundle))
                    return bundle;               
            }
        }
        return null;
    }
    /**
     * Find the currently installed bundle that exports this package.
     * @param context
     * @param packageName
     * @return
     */
    public static Bundle findBundle(BundleContext context, String packageName, String versionRange)
    {
        if (context == null)
            return null;
        Bundle[] bundles = context.getBundles();
        Bundle bestBundle = null;
        for (Bundle bundle : bundles)
        {
            if (packageName != null)
            {
                Dictionary<?, ?> dictionary = bundle.getHeaders();
                String packages = (String)dictionary.get(Constants.EXPORT_PACKAGE);
                Version bundleVersion = new Version((String)dictionary.get(Constants.BUNDLE_VERSION));

                StringBuilder sb = new StringBuilder(packages == null ? "" : packages);
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
                        if (isValidVersion(bundleVersion, versionRange))
                            bestBundle = bundle;
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

    /**
     * Start this bundle.
     * @param bundle
     */
    public void startBundle(Bundle bundle)
    {
        if (bundle != null)
            if ((bundle.getState() != Bundle.ACTIVE) && (bundle.getState() != Bundle.STARTING))
        {
            try {
                bundle.start();
            } catch (BundleException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Log this message.
     * @param context
     * @param level
     * @param message
     */
    public boolean log(Object context, int level, String message)
    {
        if (!(context instanceof BundleContext))
            return false;
        BundleContext bundleContext = (BundleContext)context;
        ServiceReference reference = bundleContext.getServiceReference(LogService.class.getName());
        if (reference != null)
        {
            LogService logging = (LogService)bundleContext.getService(reference);
            if (logging != null)
            {
                logging.log(level, message);
                return true;
            }
        }
        return false;
    }
    
    /**
     * TODO - Need to make logging work under OSGi.
     * @param name
     * @param resourceBundleName
     * @return
     */
    public Logger getOsgiLogger(String name, String resourceBundleName)
    {
        return new OsgiLogger(name, resourceBundleName);
    }
    
    class OsgiLogger extends Logger
    {

        protected OsgiLogger(String name, String resourceBundleName) {
            super(name, resourceBundleName);
        }
        
        public void log(Level level, String message) {
            int osgiLevel = LogService.LOG_INFO;
            if (level == Level.INFO)
                osgiLevel = LogService.LOG_INFO;
            if (level == Level.WARNING)
                osgiLevel = LogService.LOG_WARNING;
            if (level == Level.SEVERE)
                osgiLevel = LogService.LOG_ERROR;
            if (level == Level.CONFIG)
                osgiLevel = LogService.LOG_DEBUG;
            BaseClassFinderService.this.log(null, osgiLevel, message);
        }
    }
    /**
     * See if this version falls within this version range.
     * @param version
     * @param versionRange
     * @return True if it is a valid version
     */
    public static final char SPACE = ' ';
    public static boolean isValidVersion(Version version, String versionRange)
    {
        if (versionRange == null)
            return true;
       
        if ((versionRange.contains(",")) || (versionRange.contains("[")) || (versionRange.contains("]")) || (versionRange.contains("(")) || (versionRange.contains(")")))
        {
            // There has to be better code in the osgi framework that will parse ranges like [1.2.3)
            try {
                boolean valid = true;
                StringTokenizer st = new StringTokenizer(versionRange, ",");
                while (st.hasMoreTokens())
                {
                    String range = st.nextToken();
                    char start = SPACE, end = SPACE;
                    if ((range.startsWith("[")) || (range.startsWith("(")))
                        {start = range.charAt(0);range = range.substring(1);}
                    if ((range.endsWith("]")) || (range.endsWith(")")))
                        {end = range.charAt(range.length() - 1);range = range.substring(0, range.length() - 1);}

                    Version rangeVersion = new Version(range);
                    if ((start == '[') && (version.compareTo(rangeVersion)) < 0)
                        valid = false;
                    if ((start == '(') && (version.compareTo(rangeVersion)) <= 0)
                        valid = false;
                    if ((end == ']') && (version.compareTo(rangeVersion)) >= 0)
                        valid = false;
                    if ((end == ')') && (version.compareTo(rangeVersion)) > 0)
                        valid = false;
                }
                return valid;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return true;    // Weird version = okay?
            }
        }
        else
        {
            try {
                Version rangeVersion = new Version(versionRange);
                if (rangeVersion.equals(version))
                    return true;
                // HACK HACK HACK - This code is for sloppy bundle definitions (standard practice okays major and minor match)
                if (version.getMajor() == rangeVersion.getMajor())
                    if (version.getMinor() >= rangeVersion.getMinor())
                        return true;
                return false;
            } catch (IllegalArgumentException e) {
                return true;    // Weird version = okay?
            }
        }
    }
    /**
     * Get the configuration properties for this Pid.
     * @param servicePid The service Pid
     * @return The properties or null if they don't exist.
     */
    @SuppressWarnings("unchecked")
    @Override
    public Dictionary<String, Object> getProperties(String servicePid)
    {
        Dictionary<String, Object> properties = null;
         try {
             if (servicePid != null)
             {
                 ServiceReference caRef = bundleContext.getServiceReference(ConfigurationAdmin.class.getName());
                 if (caRef != null)
                 {
                     ConfigurationAdmin configAdmin = (ConfigurationAdmin)bundleContext.getService(caRef);
                     Configuration config = configAdmin.getConfiguration(servicePid);
    
                     properties = config.getProperties();
                 }
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
         return properties;
    }
    /**
     * Set the configuration properties for this Pid.
     * @param servicePid The service Pid
     * @param properties The properties to save.
     * @return True if successful
     */
     @Override
     public boolean saveProperties(String servicePid, Dictionary<String, Object> properties)
     {
         try {
             if (servicePid != null)
             {
                 ServiceReference caRef = bundleContext.getServiceReference(ConfigurationAdmin.class.getName());
                 if (caRef != null)
                 {
                     ConfigurationAdmin configAdmin = (ConfigurationAdmin)bundleContext.getService(caRef);
                     Configuration config = configAdmin.getConfiguration(servicePid);
    
                     config.update(properties);
                     return true;
                 }
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
         return false;
     }
}
