package org.jbundle.util.osgi.finder;

import java.io.File;

import org.jbundle.util.osgi.ClassFinder;
import org.jbundle.util.osgi.bundle.BaseBundleService;
import org.jbundle.util.osgi.bundle.BundleServiceDependentListener;
import org.jbundle.util.osgi.bundle.BundleStarter;
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
public final class ClassFinderActivator extends BaseBundleService
	implements BundleActivator
{
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
        bundleContext = context;

        super.start(context);
    }
    /**
     * Bundle shutting down.
     */
    public void stop(BundleContext context) throws Exception {
        // I'm unregistered automatically
        
        super.stop(context);

        bundleContext = null;
    }
    
    /**
     * Find this class's class access service in the current workspace.
     * @param waitForStart TODO
     * @param className
     * @return
     */
    public static void setClassFinder(ClassFinder classFinder)
    {
        ClassFinderActivator.classFinder = classFinder;
    }
    /**
     * Find this class's class access service in the current workspace.
     * @param waitForStart TODO
     * @param className
     * @return
     */
    public static ClassFinder getClassFinder(Object context, boolean waitForStart)
    {
    	if ((bundleContext == null) && (context != null))
    	{	// This bundle was never started, that's okay. Use this bundle context
    		bundleContext = (BundleContext)context;
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
				if (bundleContext != null)
					classFinder = (ClassFinder)ClassFinderActivator.waitForBundleStartup(bundleContext, ClassFinder.class.getName());

		if (classFinder == null)
			if (waitForStart)
			{	// Start up the 'no persistent storage' service.
				classFinder = new NoClassFinderService();
				try {	// Note: this does not start the service, but it does registers it as a service.
					((NoClassFinderService)classFinder).start(bundleContext);
				} catch (Exception e) {
					e.printStackTrace();	// Never
				}
			}

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

		// Wait a 15 seconds for the ClassService to come up while the activator starts this service
		synchronized (thread)
		{
			try {
				thread.wait(15000);
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
