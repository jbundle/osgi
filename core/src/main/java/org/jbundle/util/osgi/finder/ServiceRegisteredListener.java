/*
 * Copyright © 2011 jbundle.org. All rights reserved.
 */
package org.jbundle.util.osgi.finder;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

public class ServiceRegisteredListener implements ServiceListener
{
    BundleContext context = null;

    private Thread thread = null;
    
    public ServiceRegisteredListener(Thread thread, BundleContext context)
    {
        super();
        this.thread = thread;
        this.context = context;
    }
    /**
     * 
     * @param event
     */
    @Override
    public void serviceChanged(ServiceEvent event)
    {
        if (event.getType() == ServiceEvent.REGISTERED)
        {   // Class finder came up
        	synchronized (thread)
        	{
        		thread.notify();
        	}
        }
        if (event.getType() == ServiceEvent.UNREGISTERING)
        {
            // What do I do?
        }
    }
}
