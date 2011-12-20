/*
 * Copyright © 2011 jbundle.org. All rights reserved.
 */
package org.jbundle.util.osgi.bundle;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

/**
 * RepositoryAdminServiceListener - Notify me when this dependent service is up.
 * 
 * @author don
 * 
 */
public class DependentServiceRegisteredListener implements ServiceListener
{
	BaseBundleService bundleService = null;
	
    BundleContext context = null;
    
    public DependentServiceRegisteredListener(BaseBundleService bundleService, BundleContext context)
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
//x            Object service = context.getService(serviceReference);
//xq            if (bundleService == null)
//x            	if (service instanceof BaseBundleService)
//x            		bundleService = (BaseBundleService)service;
//x            context.removeServiceListener(this);	// Don't need this anymore
            bundleService.startupThisService(context);
            
            context.removeServiceListener(this);
        }
        if (event.getType() == ServiceEvent.UNREGISTERING)
        {
            // Never
        }
    }
}
