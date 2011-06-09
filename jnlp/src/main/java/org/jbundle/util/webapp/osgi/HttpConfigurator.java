package org.jbundle.util.webapp.osgi;

import java.util.Dictionary;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.util.tracker.ServiceTracker;

public class HttpConfigurator implements ManagedService {
    BundleContext context = null;
    
    public HttpConfigurator(BundleContext context)
    {
        super();
        this.context = context;
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public void updated(Dictionary properties) throws ConfigurationException {
        // TODO Stop and start the service with the new context.
        if (properties == null) {
            // no configuration from configuration admin
            // or old configuration has been deleted
        } else {
            // apply configuration from config admin
            String  contextPath = (String)properties.get(BaseOsgiServlet.CONTEXT_PATH);
            if (contextPath != null)
            {
                String filter = null; //??? "(" + Constants.OBJECTCLASS + "=" + HttpServiceTracker.class.getName() + ")"; 
                ServiceReference[] references = null;
                try {
                    references = context.getServiceReferences(ServiceTracker.class.getName(), filter);
                } catch (InvalidSyntaxException e) {
                    e.printStackTrace();
                }
                HttpServiceTracker httpService = null;
                if (references != null)
                {
                    for (ServiceReference reference : references)
                    {
                        if (context.getService(reference) instanceof HttpServiceTracker)
                            httpService = (HttpServiceTracker)context.getService(reference);
                    }
                }
                if (httpService != null)
                    httpService.setContextPath(contextPath);
            }
        }
    }
}