package org.jbundle.util.osgi.jnlp;

import org.jbundle.util.osgi.BundleService;
import org.jbundle.util.osgi.bundle.BaseBundleService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Start up the web service listener.
 * @author don
 */
public class HttpServiceActivator extends BaseBundleService
{
    ServiceTracker httpServiceTracker;
    
    @Override
    public void serviceChanged(ServiceEvent event) {
        if (event.getType() == ServiceEvent.REGISTERED)
        { // Osgi Service is up, Okay to start the server
            System.out.println("Starting Http Service tracker");
    		if (httpServiceTracker == null)
    		{
    			BundleContext context = event.getServiceReference().getBundle().getBundleContext();
    			this.startupThisService(this);
    			//x this.checkDependentServicesAndStartup(context, EnvironmentActivator.class.getName());
    		}
        }
        if (event.getType() == ServiceEvent.UNREGISTERING)
        {
            System.out.println("Stopping http service tracker");
            httpServiceTracker.close();
            httpServiceTracker = null;
        }        
    }
    /**
     * Start this service.
     * Override this to do all the startup.
     * @return true if successful.
     */
    public boolean startupThisService(BundleService bundleService)
    {
        httpServiceTracker = new HttpServiceTracker(context);
        httpServiceTracker.open();
        
        return true;
    }
}