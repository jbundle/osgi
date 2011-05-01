package org.jbundle.thin.base.util.osgi.finder;

import java.io.File;

import org.jbundle.thin.base.util.osgi.bundle.BaseBundleService;
import org.jbundle.thin.base.util.osgi.bundle.BundleServiceDependentListener;
import org.jbundle.thin.base.util.osgi.bundle.BundleStarter;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * OsgiClassService - Service to find and load bundle classes and resources.
 * 
 * @author don
 * 
 */
public final class ClassFinderUtility extends BaseBundleService
	implements BundleActivator
{
    /**
     * The byte to char and back encoding that I use. TODO(don) Move this to shared place
     */
    public static final String OBJECT_ENCODING = "ISO-8859-1";

    /**
	 * Good from start to stop.
	 */
    static BundleContext bundleContext = null;

    static ClassFinder classFinder = null;

	/**
     * Bundle starting.
     * If the service listener is up, register me, else wait.
     */
    public void start(BundleContext context) throws Exception
    {
        System.out.println("Starting and registering the ClassFinderUtility");
        
        bundleContext = context;

        super.start(context);
    }
    /**
     * Bundle shutting down.
     */
    public void stop(BundleContext context) throws Exception {
        System.out.println("Stopping ClassFinderUtility bundle");
        // I'm unregistered automatically
        
        super.stop(context);

        bundleContext = null;
    }
    
    /**
     * Find this class's class access registered class access service in the current workspace.
     * @param waitForStart TODO
     * @param className
     * @return
     */
    public static ClassFinder getClassFinder(Object context, boolean waitForStart)
    {
    	if ((bundleContext == null) && (context != null))
    	{	// This bundle was never started, so start it!
    		bundleContext = (BundleContext)context;
    		try {
    			String dependentBaseBundleClassName = ClassFinderUtility.class.getName();
    			bundleContext.addServiceListener(new BundleServiceDependentListener(null, bundleContext), /*"(&" +*/ "(objectClass=" + dependentBaseBundleClassName + ")");	// This will call startupThisService once the service is up
    	    	new BundleStarter(null, bundleContext, dependentBaseBundleClassName).start();
    		} catch (InvalidSyntaxException e) {
    			e.printStackTrace();
    		}
    	}
    	if ((classFinder == null) && (bundleContext != null))
    	{
			try {
				ServiceReference[] ref = bundleContext.getServiceReferences(ClassFinder.class.getName(), null);
			
				if ((ref != null) && (ref.length > 0))
					classFinder =  (ClassFinder)bundleContext.getService(ref[0]);
			} catch (InvalidSyntaxException e) {
				e.printStackTrace();
			}
    	}
		if (classFinder == null)
			if (waitForStart)
				classFinder = (ClassFinder)ClassFinderUtility.waitForBundleStartup(bundleContext, ClassFinder.class.getName());

		return classFinder;
    }
    /**
     * Get the package name of this class name.
     * NOTE: This is exactly the same as Util.getPackageName, move this!
     * @param className
     * @return
     */
    public static String getPackageName(String className, boolean resource)
    {
        String packageName = null;
        if (className != null)
        {
    		if (className.indexOf(File.separator) != -1)
    		{
    			className = className.substring(0, className.lastIndexOf(File.separator));
        		packageName = className.replace(File.separator.charAt(0), '.');
    		}
    		else if (className.indexOf('/') != -1)
    		{
    			className = className.substring(0, className.lastIndexOf('/'));
        		packageName = className.replace('/', '.');
    		}
        	else
        	{
	        	if (resource)
	        		if (className.endsWith(PROPERTIES))
	        			className = className.substring(0, className.length() - PROPERTIES.length());
	            if (className.lastIndexOf('.') != -1)
	                packageName = className.substring(0, className.lastIndexOf('.'));
        	}
        }
        return packageName;
    }
    public static final String PROPERTIES = ".properties";
    /**
     * Wait for bundle class name.
     * @param context
     * @param bundleClassName
     * @return
     */
    static boolean waitingForClassFinder = false;
    public static BundleActivator waitForBundleStartup(BundleContext context, String bundleClassName)
    {
    	BundleActivator bundleActivator = null;
		waitingForClassFinder = true;
		// TODO Minor synchronization issue here
		Thread thread = Thread.currentThread();
		ClassFinderListener classFinderListener = null;
		try {
			context.addServiceListener(classFinderListener = new ClassFinderListener(thread, bundleContext), "(" + Constants.OBJECTCLASS + "=" + bundleClassName + ")");
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}

		// Wait a minute for the ClassService to come up while the activator starts this service
		synchronized (thread)
		{
			try {
				thread.wait(60000);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
		context.removeServiceListener(classFinderListener);
		waitingForClassFinder = false;
		
		try {
			ServiceReference[] ref = context.getServiceReferences(bundleClassName, null);
		
			if ((ref != null) && (ref.length > 0))
				bundleActivator =  (BundleActivator)context.getService(ref[0]);
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}

		if (bundleActivator == null)
			System.out.println("The " + bundleClassName + " never started - make sure you start it!");
		
		return bundleActivator;
    }
}
