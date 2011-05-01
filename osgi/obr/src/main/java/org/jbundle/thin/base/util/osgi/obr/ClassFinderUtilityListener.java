package org.jbundle.thin.base.util.osgi.obr;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

/**
 * Listen for the class service to come up and notify this waiting thread when it does.
 */
public class ClassFinderUtilityListener implements ServiceListener
{
    BundleContext context = null;

    private Thread thread = null;
    
    public ClassFinderUtilityListener(Thread thread, BundleContext context)
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
        {   // Repository admin came up 
        	thread.notify();
        }
        if (event.getType() == ServiceEvent.UNREGISTERING)
        {
            // What do I do?
        }
    }
}
