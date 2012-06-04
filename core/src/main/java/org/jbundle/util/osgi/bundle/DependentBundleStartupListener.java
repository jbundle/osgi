/*
 * Copyright © 2012 jbundle.org. All rights reserved.
 */
package org.jbundle.util.osgi.bundle;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

/**
 * RepositoryAdminServiceListener - Notify me when this dependent service is up.
 * 
 * @author don
 * 
 */
public class DependentBundleStartupListener implements BundleListener
{
	BaseBundleActivator bundleService = null;
	
    BundleContext context = null;
    
    Bundle bundle = null;
    
    public DependentBundleStartupListener(BaseBundleActivator bundleService, BundleContext context, Bundle bundle)
    {
        this.context = context;
        this.bundleService = bundleService;
        this.bundle = bundle;
    }
    @Override
    public void bundleChanged(BundleEvent event) {
        if (event.getBundle() == bundle)
            if (event.getType() == BundleEvent.STARTED)
        {   // Class came up
            Object service = bundleService.startupService(context);
            bundleService.registerService(service);
            
            context.removeBundleListener(this);
        }
        
    }
}
