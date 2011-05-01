package org.jbundle.thin.base.util.osgi.finder;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

public class ClassFinderListener  implements ServiceListener
{
    BundleContext context = null;

    private Thread thread = null;
    
    public ClassFinderListener(Thread thread, BundleContext context)
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
        	thread.notify();
        }
        if (event.getType() == ServiceEvent.UNREGISTERING)
        {
            // What do I do?
        }
    }
}
