/*
 * Copyright © 2012 jbundle.org. All rights reserved.
 */
package org.jbundle.util.osgi.bundle;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import org.jbundle.util.osgi.BundleConstants;
import org.jbundle.util.osgi.finder.BaseClassFinderService;
import org.jbundle.util.osgi.finder.ClassFinderActivator;
import org.jbundle.util.osgi.finder.ClassServiceUtility;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.log.LogService;

/**
 * A base activator for a service.
 * Extend this class with your service activator.
 * This is just a convenience class to avoid having to use a declarative service model.
 * This makes it easy to programmatically register a service and require dependent bundles to start.
 * Typically you only need to override the startupService, shutdownService, and getInterface class methods.
 * @author don
 *
 */
public class BaseBundleActivator extends Object
	implements BundleActivator, ServiceListener
{
	/**
	 * Constructor.
	 */
	public BaseBundleActivator()
	{
		super();
	}

	/**
	 * This is not necessary. I save it for debugging.
	 */
	protected Dictionary<String,String> properties = null;
	/**
	 * My service registration.
	 */
	protected ServiceRegistration serviceRegistration = null;
    /**
     * The bundle context.
     */
    protected BundleContext context = null;
    /**
     * The (optional) service that was started by this activator.
     */
    protected Object service = null;
    
	/**
	 * Setup the application properties.
	 * Override this to set the properties.
	 * @param bundleContext BundleContext
	 */
	public void init()
	{
		Dictionary<String, String> properties = getConfigurationProperties(this.getProperties(), false);
		this.setProperties(properties);
        this.setProperty(BundleConstants.SERVICE_PID, getServicePid());
        this.setProperty(BundleConstants.SERVICE_CLASS, getServiceClassName());		
	}
    /**
     * Get the (persistent) configuration dictionary from the service manager.
     * Note: Properties are stored under the activator's package name.
     * @return The properties
     */
    @SuppressWarnings("unchecked")
    public Dictionary<String, String> getConfigurationProperties(Dictionary<String, String> dictionary, boolean returnCopy)
    {
        if (returnCopy)
            dictionary = putAll(dictionary, null);
        if (dictionary == null)
            dictionary = new Hashtable<String, String>();
        try {
            String servicePid = this.getServicePid();
            if (servicePid != null)
            {
                ServiceReference caRef = context.getServiceReference(ConfigurationAdmin.class.getName());
                if (caRef != null)
                {
                    ConfigurationAdmin configAdmin = (ConfigurationAdmin)context.getService(caRef);
                    Configuration config = configAdmin.getConfiguration(servicePid);

                    Dictionary<String, String> configProperties = config.getProperties();
                    dictionary = putAll(configProperties, dictionary);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dictionary;
    }

    /**
     * Bundle starting up.
     * Don't override this, override startupService.
     */
    public void start(BundleContext context) throws Exception {
        ClassServiceUtility.log(context, LogService.LOG_INFO, "Starting " + this.getClass().getName() + " Bundle");
        
        this.context = context;
        this.init();	// Setup the properties
        
		String interfaceClassName = getInterfaceClassName();
		this.setProperty(BundleConstants.ACTIVATOR, this.getClass().getName());	// In case I have to find this service by activator class

        try {
			context.addServiceListener(this, ClassServiceUtility.addToFilter((String)null, Constants.OBJECTCLASS, interfaceClassName));
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
		
        if (service == null)
        {
            boolean allStarted = this.checkDependentServices(context);
            if (allStarted)
            {
            	service = this.startupService(context);
            	this.registerService(service);
            }
        }
    }
    /**
     * Bundle stopping.
     * Don't override this, override shutdownService.
     */
    public void stop(BundleContext context) throws Exception {
        ClassServiceUtility.log(context, LogService.LOG_INFO, "Stopping " + this.getClass().getName() + " Bundle");
    	if (this.shutdownService(service, context))
    		service = null;
        // Unregisters automatically
        this.context = null;
    }
    /**
     * Listen for register/unregister events.
     */
    @Override
    public void serviceChanged(ServiceEvent event) {
        if (event.getType() == ServiceEvent.REGISTERED)
        {
        }
        if (event.getType() == ServiceEvent.UNREGISTERING)
        {
            service = null;
        }
    }

    /**
     * Make sure the dependent services are up, then call startupService.
     * @param versionRange Bundle version
     * @param baseBundleServiceClassName
     * @return false if I'm waiting for the service to startup.
     */
    public boolean checkDependentServices(BundleContext bundleContext)
    {
    	return true;	// Override this to add/startup dependent services
    }
    /**
     * Make sure the dependent services are up, then call startupService.
     * @param interfaceClassName The service interface class
     * @param defaultServiceClassName The default service to start
     * @param versionRange Bundle version
     * @param properties Properties to pass when/if starting the service
     * @param baseBundleServiceClassName
     * @return false if I'm waiting for the service to startup.
     */
    public boolean addDependentService(BundleContext bundleContext, String interfaceClassName, String defaultServiceClassName, String versionRange, Dictionary<String, String> properties)
    {
    	if (interfaceClassName == null)
    		interfaceClassName = defaultServiceClassName;
        ServiceReference serviceReference = BaseClassFinderService.getClassServiceReference(bundleContext, interfaceClassName, versionRange, null);
        Bundle bundle = BaseClassFinderService.findBundle(bundleContext, ClassFinderActivator.getPackageName((defaultServiceClassName != null) ? defaultServiceClassName : interfaceClassName, false), versionRange);
        if (bundle == null)
        	if (serviceReference != null)
        		bundle = serviceReference.getBundle();

		try {
	        bundleContext.addServiceListener(new DependentServiceRegisteredListener(this, bundleContext), /*"(&" +*/ "(" + Constants.OBJECTCLASS + "=" + interfaceClassName + ")");	// This will call startupThisService once the service is up
	
	        if ((serviceReference != null) && ((bundle.getState() & Bundle.ACTIVE) != 0))
	        {    // Good, dependent service is already up; now I can start up.
	            return true;
	        }
	    	// Dependent service has not started, so I need to start it and then listen
		    if (serviceReference != null)	// Rare - Service is probably is 'starting' mode.
                bundleContext.addBundleListener(new DependentBundleStartupListener(this, bundleContext, bundle)); // This will call startupThisService once the service is up
	    	new BundleStarter(this, bundleContext, interfaceClassName, defaultServiceClassName, versionRange, properties).start();
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
    	return false;
    }
    /**
     * Start this service.
     * Override this to do all the startup.
     * @param context bundle context
     * @return true if successful.
     */
    public Object startupService(BundleContext bundleContext)
    {
        return this;	// By default, this is the service
    }
    /**
     * Stop this service.
     * Override this to do all the startup.
     * @param bundleService
     * @param context bundle context
     * @return true if successful.
     */
    public boolean shutdownService(Object service, BundleContext context)
    {
        return true;
    }
    /**
     * Get the service for this implementation class.
     * @param interfaceClassName
     * @return
     */
    public void setService(Object service)
    {
    	this.service = service;
    }
    /**
     * Convenience method to get the service for this implementation class.
     * Note: You typically override this and cast the service to the correct class.
     * @param interfaceClassName
     * @return
     */
    public Object getService()
    {
        return service;       // Get the service object for this activator
    }
    /**
     * Get the service for this implementation class.
     * @param interfaceClassName
     * @return
     */
    public void registerService(Object service)
    {
    	this.setService(service);
    	String serviceClass = getInterfaceClassName(); 

    	if (service != null)
    		serviceRegistration = context.registerService(serviceClass, this.service, properties);
    }
    /**
     * Convenience method to get the service for this implementation class.
     * @param interfaceClassName
     * @return
     */
    public Object getService(Class<?> interfaceClass)
    {
    	return this.getService(interfaceClass.getName());		// Always (hopefully)
    }
    /**
     * Convenience method to get the service for this implementation class.
     * @param interfaceClassName
     * @return
     */
    public Object getService(String interfaceClassName)
    {
    	return this.getService(interfaceClassName, null, null, null);
    }
    /**
     * Convenience method to get the service for this implementation class.
     * @param interfaceClassName
     * @return
     */
    public Object getService(String interfaceClassName, String serviceClassName, String versionRange, Dictionary<String,String> filter)
    {
        return ClassServiceUtility.getClassService().getClassFinder(context).getClassBundleService(interfaceClassName, serviceClassName, versionRange, filter, -1);
    }
    
    /**
     * The service key in the config admin system.
     * @return By default the package name, else override this.
     */
    public String getServicePid()
    {
        String servicePid = context.getProperty(BundleConstants.SERVICE_PID);
        if (servicePid != null)
            return servicePid;
        servicePid = this.getServiceClassName();
        if (servicePid == null)
            servicePid = this.getClass().getName();
        return ClassFinderActivator.getPackageName(servicePid, false);
    }
    /**
     * Get the interface/service class name.
     * @return
     */
    public Class<?> getInterfaceClass()
    {
		return null;
    }
    /**
     * Get the interface/service class name.
     * @return
     */
    public String getInterfaceClassName()
    {
    	Class<?> interfaceClass = getInterfaceClass();
    	if (interfaceClass != null)
    		return interfaceClass.getName();
		String interfaceClassName = this.getProperty(BundleConstants.INTERFACE);
		if (interfaceClassName == null)
		{
			if (service != null)
				interfaceClassName = service.getClass().getName();	// Default - register under class name
			else
				interfaceClassName = this.getClass().getName();	// Default - register under class name
		}
		return interfaceClassName;
    }
    /**
     * Get the interface/service class name.
     * @return
     */
    public Class<?> getServiceClass()
    {
		return null;
    }
    /**
     * Get the interface/service class name.
     * @return
     */
    public String getServiceClassName()
    {
    	Class<?> serviceClass = getServiceClass();
    	if (serviceClass != null)
    		return serviceClass.getName();
		String serviceClassName = context.getProperty(BundleConstants.SERVICE_CLASS);	// Don't use getProperty - endless loop
		if (serviceClassName == null)
		{
			if (service != null)
				serviceClassName = service.getClass().getName();
			else
				serviceClassName = this.getClass().getName();
		}
		return serviceClassName;
    }
    /**
     * Get the properties.
     * @return the properties.
     */
    public void setProperties(Dictionary<String,String> properties)
    {
        this.properties = properties;
    }
    /**
     * Get the properties.
     * @return the properties.
     */
    public Dictionary<String,String> getProperties()
    {
        return properties;
    }
	/**
	 * Get the properties.
	 * @return the properties.
	 */
	public void setProperty(String key, String value)
	{
		if (properties == null)
			properties = new Hashtable<String, String>();
		properties.put(key, value);
	}
	/**
	 * Get the properties.
	 * @return the properties.
	 */
	public String getProperty(String key)
	{
		String servicePid = this.getServicePid();
		String value = null;
		if (!key.contains("."))
		{
			value = context.getProperty(servicePid + '.' + key);
	        if ((value == null) && (properties != null))
	            value = properties.get(servicePid + '.' + key);
		}
        if (value == null)
			value = context.getProperty(key);
        if ((value == null) && (properties != null))
            value = properties.get(key);
		return value;
	}
    /**
     * Copy all the values from one dictionary to another.
     * @param sourceDictionary
     * @param destDictionary
     * @return
     */
    public static Dictionary<String, String> putAll(Dictionary<String, String> sourceDictionary, Dictionary<String, String> destDictionary)
    {
        if (destDictionary == null)
            destDictionary = new Hashtable<String, String>();
        if (sourceDictionary != null)
        {
            Enumeration<String> keys = sourceDictionary.keys();
            while (keys.hasMoreElements())
            {
                String key = keys.nextElement();
                destDictionary.put(key, sourceDictionary.get(key));
            }
        }
        return destDictionary;
    }
}
