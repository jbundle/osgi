/*
 * Copyright © 2012 jbundle.org. All rights reserved.
 */
package org.jbundle.util.osgi.finder;

import java.io.File;
import java.util.Dictionary;
import java.util.StringTokenizer;

import org.jbundle.util.osgi.BundleService;
import org.jbundle.util.osgi.ClassFinder;
import org.jbundle.util.osgi.bundle.BaseBundleService;
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
public final class ClassFinderActivator extends BaseBundleService
	implements BundleActivator
{
    public static final int DEFAULT_SERVICE_WAIT_SECS = 60;
    
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

        super.stop(context);    // I'm unregistered automatically

        bundleContext = null;
    }
    
    /**
     * Find this class's class access service in the current workspace.
     * @param waitForStart
     * @param className
     * @return
     */
    public static void setClassFinder(ClassFinder classFinder)
    {
        ClassFinderActivator.classFinder = classFinder;
        ClassServiceUtility classService = (ClassServiceUtility)ClassServiceUtility.getClassService();
        classService.setClassFinder(classFinder);
    }
    /**
     * Find this class's class access service in the current workspace.
     * @param secsToWait 
     * @param className
     * @return
     */
    public static ClassFinder getClassFinder(Object context, int secsToWait)
    {
    	if ((bundleContext == null) && (context != null))
    	{	// This bundle was never started, that's okay. Use this bundle context
    		bundleContext = (BundleContext)context;
    	}
    	else if ((classFinder == null) && (secsToWait == -1))
    	    secsToWait = 0;    // If I was properly started, then use No until obr service starts
    	
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
			if (secsToWait != 0)
				if (bundleContext != null)
					classFinder = (ClassFinder)ClassFinderActivator.waitForServiceStartup(bundleContext, ClassFinder.class.getName(), null, null, secsToWait);

		if (classFinder == null)
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
     * Wait for bundle class name.
     * @param context
     * @param bundleClassName
     * @return
     */
    public static boolean waitForBundleStartup(BundleContext context, Bundle bundle, int secsToWait)
    {
        if ((bundle.getState() & Bundle.ACTIVE) == 0)
        { // Wait for it to start up!
            if (((bundle.getState() & Bundle.RESOLVED) != 0) || ((bundle.getState() & Bundle.INSTALLED) != 0))
            {
                try {
                    bundle.start();
                } catch (BundleException e) {
                    e.printStackTrace();
                }
            }
            if ((bundle.getState() & Bundle.ACTIVE) == 0)
            { // Wait for it to start up!
                Thread thread = Thread.currentThread();
                BundleStartupListener bundleStartupListener = null;
                context.addBundleListener(bundleStartupListener = new BundleStartupListener(thread, bundleContext, bundle));
                // Wait 15 seconds for the ClassService to come up while the activator starts this service
                synchronized (thread) {
                    try {
                        thread.wait((secsToWait == -1) ? DEFAULT_SERVICE_WAIT_SECS * 1000 : secsToWait * 1000);    // Will notify me when it is up
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                context.removeBundleListener(bundleStartupListener);
            }
        }
        
        return ((bundle.getState() & Bundle.ACTIVE) != 0);
    }
    /**
     * Wait for bundle class name.
     * @param context
     * @param bundleClassName
     * @return
     */
    public static BundleActivator waitForServiceStartup(BundleContext context, String className, String versionRange, Dictionary<String, String> filter, int secsToWait)
    {
        ServiceReference ref = BaseClassFinderService.getClassServiceReference(context, className, versionRange, filter);
        if (ref != null)
        {   // Good, it's registered - make sure it's started, or just start it!
            if (ClassFinderActivator.waitForBundleStartup(context, ref.getBundle(), secsToWait))
                return (BundleActivator)context.getService(ref);
            else
                return null;    // Never
        }
        
        Bundle bundle = BaseClassFinderService.findBundle(context, ClassFinderActivator.getPackageName(className, false), versionRange);
        if (bundle == null)
            if (!className.equals(ClassFinder.class.getName())) // Never. This code is in the ClassFinder bundle.
        {
            ClassFinder classFinder = getClassFinder(context, secsToWait);        
            String packageName = ClassFinderActivator.getPackageName(className, false);
            Object resource = classFinder.deployThisResource(packageName, versionRange, true);  // Get the bundle info from the repos
            bundle = (Bundle)classFinder.findBundle(resource, context, packageName, versionRange);
        }
        if (bundle == null)
            return null;    // Error, can't find bundle
        
		Thread thread = Thread.currentThread();
		ServiceRegisteredListener classFinderListener = null;
		try {
			context.addServiceListener(classFinderListener = new ServiceRegisteredListener(thread, bundleContext), "(" + Constants.OBJECTCLASS + "=" + className + ")");
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
        if (!ClassFinderActivator.waitForBundleStartup(context, bundle, secsToWait))
            return null;
		// Double-check to make sure it didn't startup while I was doing all this.
		BundleService bundleService = getClassFinder(context, secsToWait).getClassBundleService(className, versionRange, filter, 0);
		if (bundleService == null)
		{ // Wait 15 seconds for the ClassService to come up while the activator starts this service
    		synchronized (thread) {
    			try {
    				thread.wait((secsToWait == -1) ? DEFAULT_SERVICE_WAIT_SECS * 1000 : secsToWait * 1000);    // Will notify me when it is up
    			} catch (InterruptedException ex) {
    				ex.printStackTrace();
    			}
    		}
		}
		context.removeServiceListener(classFinderListener);
		
		ref = BaseClassFinderService.getClassServiceReference(context, className, versionRange, filter);
		if (ref == null)
		{
            ClassServiceUtility.log(context, LogService.LOG_WARNING, "The " + className + " was never registered - make sure you start it!");
		    return null;
		}
		return (BundleActivator)context.getService(ref);
    }
    /**
     * 
     * ie., (&(package=org.jibx.runtime)(version>=1.2.0)(!(version>=2.0.0)))
     * @param versionRange
     * @return
     */
    public static String addVersionFilter(String currentFilter, String versionRange)
    {
        if (currentFilter == null)
            return null;
    	StringBuilder sb = new StringBuilder(currentFilter);
    	if (versionRange != null)
    		if (versionRange.length() > 0)
		{
			sb.insert(0, "(&");
			if ((versionRange.contains(",")) || (versionRange.contains("[")) || (versionRange.contains("]")) || (versionRange.contains("(")) || (versionRange.contains(")")))
	        {
                // There has to be better code in the osgi framework that will parse ranges like [1.2.3)
			    StringTokenizer st = new StringTokenizer(versionRange, ",");
			    while (st.hasMoreTokens())
			    {
			        String range = st.nextToken();
                    if (range.startsWith("["))
                        sb.append("(version>=").append(range.substring(1) + ")");
                    if (range.startsWith("("))
                        sb.append("(version>").append(range.substring(1) + ")");
                    if (range.endsWith("]"))
                        sb.append("(version<=").append(range.substring(0, range.length() - 1) + ")");
                    if (range.endsWith(")"))
                        sb.append("(version<=").append(range.substring(0, range.length() - 1) + ")");
			    }
	        }
	        else
	        {
                if (versionRange.indexOf('.') == -1)
                    sb.append("(version=" + versionRange + ")");
                else
                {
    	            try {  // Standard practice is to match major from minor
    	                Version v = new Version(versionRange);
    	                sb.append("(version>=").append(v.getMajor()).append('.').append(v.getMinor()).append(".0)");
    	                sb.append("(version<").append(v.getMajor()+1).append(")");
    	            } catch (IllegalArgumentException e) {
    	                e.printStackTrace();
    	            }
    	        }
	    	}
			sb.append(')');
		}
    	return sb.toString();
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
}
