/*
 * Copyright © 2012 jbundle.org. All rights reserved.
 */
package org.jbundle.util.osgi.bundle;

import org.jbundle.util.osgi.finder.ClassServiceUtility;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

/**
 * RepositoryAdminServiceListener - Notify me when this dependent service is up.
 * 
 * @author don
 * 
 */
public class DependentServiceRegisteredListener implements ServiceListener
{
	BaseBundleActivator bundleService = null;
	
    BundleContext context = null;
    
    public DependentServiceRegisteredListener(BaseBundleActivator bundleService, BundleContext context)
    {
        this.context = context;
        this.bundleService = bundleService;
    }
    /**
     * 
     * @param event
     */
    @Override
    public void serviceChanged(ServiceEvent event)
    {
        if (event.getType() == ServiceEvent.REGISTERED)
        {   // Repository admin came up 
            ServiceReference serviceReference = event.getServiceReference();
            Bundle bundle = serviceReference.getBundle();
            BundleContext context = bundle.getBundleContext();
            if ((bundle.getState() & Bundle.STARTING) != 0)
                context.addBundleListener(new DependentBundleStartupListener(bundleService, context, bundle));  // Still starting, wait 'til it starts
            if ((bundle.getState() & Bundle.ACTIVE) != 0)
            {
                Object service = bundleService.startupService(context);  // Good, it is started. Call the startup service
                bundleService.setService(service);
            }
            else if ((bundle.getState() & Bundle.STARTING) == 0)  // What?
                ClassServiceUtility.log(context, LogService.LOG_ERROR, "BundleService never started: " + bundleService.getClass().getName());
        }
        if (event.getType() == ServiceEvent.UNREGISTERING)
        {
            bundleService.registerService(null);
            context.removeServiceListener(this);
        }
    }
}
