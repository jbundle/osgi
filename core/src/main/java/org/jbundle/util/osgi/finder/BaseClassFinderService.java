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
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            Object resource = this.deployThisResource(ClassFinderActivator.getPackageName(className, false), versionRange, true);
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
     * @param className
     * @return The class definition or null if not found.
     */
    public URL findResourceURL(String className, String versionRange)
    {
        //if (ClassServiceBootstrap.repositoryAdmin == null)
        //    return null;

        URL url = this.getResourceFromBundle(null, className, versionRange);

        if (url == null) {
            Object resource = this.deployThisResource(ClassFinderActivator.getPackageName(className, true), versionRange, true);
            if (resource != null)
            	url = this.getResourceFromBundle(resource, className, versionRange);
        }

        return url;
    }
    /**
     * Find, resolve, and return this ResourceBundle.
     * @param className
     * @return The class definition or null if not found.
     * TODO: Need to figure out how to get the bundle's class loader, so I can set up the resource chain
     */
    public ResourceBundle findResourceBundle(String className, Locale locale, String versionRange)
    {
        //if (ClassServiceBootstrap.repositoryAdmin == null)
        //    return null;

        ResourceBundle resourceBundle = this.getResourceBundleFromBundle(null, className, locale, versionRange);

        if (resourceBundle == null) {
            Object resource = this.deployThisResource(ClassFinderActivator.getPackageName(className, true), versionRange, true);
            if (resource != null)
            {
            	resourceBundle = this.getResourceBundleFromBundle(resource, className, locale, versionRange);
            	if (resourceBundle == null)
            	{
            		Class<?> c = this.getClassFromBundle(resource, className, versionRange);
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
            Object resource = this.deployThisResource(ClassFinderActivator.getPackageName(className, false), versionRange, true);
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
     * TODO This is expensive, I need to synchronize and use a static writer.
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
                BundleService classAccess = this.getClassBundleService(className, versionRange, null, 0);
                if (classAccess != null)
                	object = classAccess.convertStringToObject(string);
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
    public ClassLoader findBundleClassLoader(String packageName, String versionRange)
    {
    	ClassLoader classLoader = this.getClassLoaderFromBundle(null, packageName, versionRange);

        if (classLoader == null) {
            Object resource = this.deployThisResource(packageName, versionRange, true);
            if (resource != null)
            	classLoader = this.getClassLoaderFromBundle(resource, packageName, versionRange);
        }

        return classLoader;    	
    }
    /**
     * Find this class's bundle in the repository
     * @param versionRange version
     * @param className
     * @return
     */
    private ClassLoader getClassLoaderFromBundle(Object resource, String packageName, String versionRange)
    {
    	String className = packageName + FAKE_CLASSNAME;
    	ClassLoader classLoader = null;
        if (resource == null)
        {
            BundleService classAccess = this.getClassBundleService(className, versionRange, null, 0);
            if (classAccess != null)
            {
            	classLoader = classAccess.getClass().getClassLoader();
            }
        }
        else
        {
        	Bundle bundle = this.findBundle(resource, bundleContext, ClassFinderActivator.getPackageName(className, false), versionRange);
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
    public BundleService getClassBundleService(String className, String versionRange, Dictionary<String, String> filter, int secsToWait)
    {
        ServiceReference ServiceReference = getClassServiceReference(bundleContext, className, versionRange, filter);
        
        if ((ServiceReference != null) && ((ServiceReference.getBundle().getState() & Bundle.ACTIVE) != 0))
            return (BundleService)bundleContext.getService(ServiceReference);

        if (secsToWait != 0)
            return (BundleService)ClassFinderActivator.waitForServiceStartup(bundleContext, className, versionRange, filter, secsToWait);

        return null;
    }
    /**
     * Find this class's class access registered class access service in the current workspace.
     * @param className
     * @return
     */
    public static ServiceReference getClassServiceReference(BundleContext context, String className, String versionRange, Dictionary<String, String> filter)
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
                interfaceName = BundleService.class.getName();  // Never
            serviceFilter = ClassFinderActivator.addVersionFilter(serviceFilter, versionRange);
            ServiceReference[] refs = context.getServiceReferences(interfaceName, serviceFilter);

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
            BundleService classAccess = this.getClassBundleService(className, versionRange, null, 0);
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
     * @param className
     * @param versionRange version
     * 
     * @return
     */
    private URL getResourceFromBundle(Object resource, String className, String versionRange)
    {
        URL url = null;
        if (resource == null)
        {
            BundleService classAccess = this.getClassBundleService(className, versionRange, null, 0);
            if (classAccess != null)
                url = classAccess.getResource(className);
        }
        else
        {
        	Bundle bundle = this.findBundle(resource, bundleContext, ClassFinderActivator.getPackageName(className, true), versionRange);
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
    private ResourceBundle getResourceBundleFromBundle(Object resource, String baseName, Locale locale, String versionRange)
    {
    	ResourceBundle resourceBundle = null;
        if (resource == null)
        {
            BundleService classAccess = this.getClassBundleService(baseName, versionRange, null, 0);
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
     * @param className
     * @param options 
     * @return
     */
    public abstract Object deployThisResource(String packageName, String versionRange, boolean start);

    /**
     * Start up a basebundle service.
     * Note: You will probably want to call this from a thread and attach a service
     * listener since this may take some time.
     * @param versionRange version
     * @param secsToWait Time to wait for startup 0=0, -1=default
     * @param className
     * @return true If I'm up already
     * @return false If I had a problem.
     */
    public boolean startBaseBundle(BundleContext context, String dependentBaseBundleClassName, String versionRange, int secsToWait)
    {
        ServiceReference ServiceReference = getClassServiceReference(bundleContext, dependentBaseBundleClassName, versionRange, null);
        
        if ((ServiceReference != null) && ((ServiceReference.getBundle().getState() & Bundle.ACTIVE) != 0))
            return true;    // Already up!

        // If the repository is not up, but the bundle is deployed, this will find it
    	return (ClassFinderActivator.waitForServiceStartup(context, dependentBaseBundleClassName, versionRange, null, secsToWait) != null);
    }
    /**
     * Shutdown the bundle for this service.
     * @param service The service object
     */
    public void shutdownService(Object service)
    {
    	// Lame code
    	String dependentBaseBundleClassName = service.getClass().getName();
    	String packageName = ClassFinderActivator.getPackageName(dependentBaseBundleClassName, false);
        Bundle bundle = this.findBundle(null, bundleContext, packageName, null);
        if (bundle == null) {
            Object resource = this.deployThisResource(packageName, null, false);  // Get the bundle info from the repos
            bundle = this.findBundle(resource, bundleContext, packageName, null);
        }
    	if (bundle != null)
    		if ((bundle.getState() & Bundle.ACTIVE) != 0)
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
     * Find, resolve, and return this bundle.
     * @param packageName
     */
    public Object findBundle(String packageName, String versionRange)
    {
        Bundle bundle = this.findBundle(null, bundleContext, packageName, versionRange);

        if (bundle == null) {
            Object resource = this.deployThisResource(packageName, versionRange, true);
            if (resource != null)
                bundle = this.findBundle(resource, bundleContext, packageName, versionRange);
        }
        return bundle;
    }
    /**
     * Find the currently installed bundle that exports this package.
     * @param bundleContext
     * @param resource
     * @return
     */
    public Bundle findBundle(Object objResource, Object bundleContext, String packageName, String versionRange)
    {
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
     * @param resource
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
     * Log this message.
     * @param bundleContext
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
}
