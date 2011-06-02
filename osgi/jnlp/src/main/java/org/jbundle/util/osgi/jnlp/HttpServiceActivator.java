package org.jbundle.util.osgi.jnlp;

import org.jbundle.util.osgi.BundleService;
import org.jbundle.util.osgi.bundle.BaseBundleService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Start up the web service listener.
 * @author don
 */
public class HttpServiceActivator extends BaseBundleService
{
    ServiceTracker httpServiceTracker;

    /**
     * Start or stop the http service tracker on bundle start/stop.
     * @param event The service event.
     */
    @Override
    public void serviceChanged(ServiceEvent event) {
        BundleContext context = null;
        if (event != null)
            if (event.getServiceReference() != null)
                if (event.getServiceReference().getBundle() != null)
                    context = event.getServiceReference().getBundle().getBundleContext();
        if (event.getType() == ServiceEvent.REGISTERED)
        { // Osgi Service is up, Okay to start the server
            log(context, LogService.LOG_INFO, "Starting the WebStart Http Service tracker");
    		if (httpServiceTracker == null)
    		    this.startupThisService(null, context);
        }
        if (event.getType() == ServiceEvent.UNREGISTERING)
        {
            log(context, LogService.LOG_INFO, "Stopping the WebStart http service tracker");
            if (httpServiceTracker != null)
                httpServiceTracker.close();
            httpServiceTracker = null;
        }        
    }
    /**
     * Start this service.
     * Override this to do all the startup.
     * @return true if successful.
     */
    @Override
    public boolean startupThisService(BundleService bundleService, BundleContext context)
    {
        httpServiceTracker = new HttpServiceTracker(context);
        httpServiceTracker.open();

        return true;
    }
    /**
     * Log this message.
     * @param context
     * @param level
     * @param message
     */
    public void log(BundleContext context, int level, String message)
    {
        if (context == null)
            return;
        LogService logging = (LogService)context.getServiceReference(LogService.class.getName());
        if (logging != null)
            logging.log(level, message);
    }
}