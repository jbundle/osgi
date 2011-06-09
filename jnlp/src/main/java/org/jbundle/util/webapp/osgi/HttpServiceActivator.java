package org.jbundle.util.webapp.osgi;

import java.util.Dictionary;
import java.util.Hashtable;

import org.jbundle.util.osgi.BundleService;
import org.jbundle.util.osgi.bundle.BaseBundleService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Start up the web service listener.
 * @author don
 */
public class HttpServiceActivator extends BaseBundleService
{
    public static final String SERVICE_PID = "service.pid"; // This must be somewhere
    ServiceTracker httpServiceTracker;
    private ServiceRegistration ppcService;

    /**
     * Start or stop the http service tracker on bundle start/stop.
     * @param event The service event.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
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
            
            String pid = this.getServicePid();
            if (pid != null)
            {
                Dictionary props = new Hashtable();
                props.put(SERVICE_PID, pid);
                ppcService = context.registerService(ManagedService.class.getName(), new HttpConfigurator(context), props);
            }

            if (httpServiceTracker == null)
    		    this.startupThisService(null, context);
        }
        if (event.getType() == ServiceEvent.UNREGISTERING)
        {
            log(context, LogService.LOG_INFO, "Stopping the WebStart http service tracker");
            if (ppcService != null) {
                ppcService.unregister();
                ppcService = null;
            }
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
        httpServiceTracker = new HttpServiceTracker(context, getServicePid(), getServletClass(), getDefaultSystemContextPath(context));
        httpServiceTracker.open();
        context.registerService(ServiceTracker.class.getName(), httpServiceTracker, null);    // Why isn't this done automatically?

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
        ServiceReference reference = context.getServiceReference(LogService.class.getName());
        if (reference != null)
        {
            LogService logging = (LogService)context.getService(reference);
            if (logging != null)
                logging.log(level, message);
        }
    }
    
    public String getServicePid()
    {
        return null;    // Override this to enable config admin.
    }
    public String getServletClass()
    {
        return null;    // Override this to enable config admin.
    }
    public String getDefaultSystemContextPath(BundleContext context)
    {
        return null;    // Override with: context.getProperty(OsgiJnlpServlet.CONTEXT_PATH);
    }
}