package org.jbundle.util.osgi.jnlp;

import org.osgi.framework.BundleContext;



/**
 * Start up the web service listener.
 * @author don
 */
public class HttpServiceActivator extends org.jbundle.util.webapp.osgi.HttpServiceActivator
{
    public String getServicePid()
    {
        return OsgiJnlpServlet.SERVICE_PID;
    }
    public String getServletClass()
    {
        return OsgiJnlpServlet.class.getName();
    }
    public String getDefaultSystemContextPath(BundleContext context)
    {
        return context.getProperty(OsgiJnlpServlet.CONTEXT_PATH);
    }
}