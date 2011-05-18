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

	// Set this param to change root URL
	public static final String WEB_CONTEXT = "org.jbundle.web.webcontext";
	
	String webContextPath = null;
	
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
        	HttpContext httpContext = null;	// new MyHttpContext(context.getBundle());
        	String fullPath = "/jnlp";
        	Servlet servlet = new JnlpServlet(context);
            Dictionary<String,String> dictionary = new Hashtable<String,String>();
	        httpContext = new JnlpHttpContext(context.getBundle());
	        httpService.registerServlet(fullPath, servlet, dictionary, httpContext);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return httpService;
    }
    
    /**
     * Http Service is down, remove my servlets.
     */
    public void removedService(ServiceReference reference, Object service) {
        HttpService httpService = (HttpService) service;
        String fullPath = "/jnlp";
        httpService.unregister(fullPath);
        super.removedService(reference, service);
    }
    
}