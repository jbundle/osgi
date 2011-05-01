package org.jbundle.thin.base.util.osgi.obr;

import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

/**
 * RepositoryAdminServiceListener - Notify me when the repository admin is up.
 * 
 * @author don
 * 
 */
public class RepositoryAdminServiceListener implements ServiceListener
{
    BundleContext context = null;
    
    ObrClassFinderImpl classServiceBootstrap = null;
    
    public RepositoryAdminServiceListener(ObrClassFinderImpl classServiceBootstrap, BundleContext context)
    {
        this.context = context;
        this.classServiceBootstrap = classServiceBootstrap;
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
            Object service = context.getService(serviceReference);
            RepositoryAdmin repositoryAdmin = null;
            if (service instanceof RepositoryAdmin)
                repositoryAdmin = (RepositoryAdmin)service; // Always
            classServiceBootstrap.addBootstrapRepository(repositoryAdmin, context);
            classServiceBootstrap.registerClassFinder(context); // Now that the repository started, you can register my started service for others to use
        }
        if (event.getType() == ServiceEvent.UNREGISTERING)
        {
            // What do I do?
        }
    }
}
