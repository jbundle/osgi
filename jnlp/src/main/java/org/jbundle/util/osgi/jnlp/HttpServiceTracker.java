package org.jbundle.util.osgi.jnlp;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.Servlet;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * HttpServiceTracker - Wait for the http service to come up to add servlets.
 * 
 * @author don
 *
 */
public class HttpServiceTracker extends ServiceTracker{

    public static final String DEFAULT_CONTEXT_PATH = "/webstart";
    
	String contextPath = null;
	
	OsgiJnlpServlet servlet = null;

    /**
	 * Constructor - Listen for HttpService.
	 * @param context
	 */
    public HttpServiceTracker(BundleContext context) {
        super(context, HttpService.class.getName(), null);
    }
    
    /**
     * Http Service is up, add my servlets.
     */
    public Object addingService(ServiceReference reference) {
        HttpService httpService = (HttpService) context.getService(reference);
        
        try {
        	contextPath = context.getProperty(OsgiJnlpServlet.CONTEXT_PATH);
        	if (contextPath == null)
        	    contextPath = DEFAULT_CONTEXT_PATH;
        	servlet = new OsgiJnlpServlet(context);
            Dictionary<String,String> dictionary = new Hashtable<String,String>();
            JnlpHttpContext httpContext = new JnlpHttpContext(context.getBundle());
	        httpService.registerServlet(contextPath, servlet, dictionary, httpContext);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return httpService;
    }
    
    /**
     * Http Service is down, remove my servlets.
     */
    public void removedService(ServiceReference reference, Object service) {
        ((HttpService)service).unregister(contextPath);
        if (servlet != null)
            servlet.free();
        super.removedService(reference, service);
    }
    
}