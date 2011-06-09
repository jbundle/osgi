package org.jbundle.util.webapp.osgi;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.jbundle.util.osgi.finder.ClassServiceUtility;
import org.jbundle.util.osgi.jnlp.JnlpHttpContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;

/**
 * HttpServiceTracker - Wait for the http service to come up to add servlets.
 * 
 * @author don
 *
 */
public class HttpServiceTracker extends ServiceTracker {

    public static final String DEFAULT_CONTEXT_PATH = "/webstart";
    
	String contextPath = null;
	
	HttpServlet servlet = null;
	String servicePid = null;
	String servletClassName = null;
	String defaultSystemContextPath = null;

    /**
	 * Constructor - Listen for HttpService.
	 * @param context
	 */
    public HttpServiceTracker(BundleContext context, String servicePid, String servletClassName, String defaultSystemContextPath) {
        super(context, HttpService.class.getName(), null);
        this.servicePid = servicePid;
        this.servletClassName = servletClassName;
        this.defaultSystemContextPath = defaultSystemContextPath;
    }
    
    /**
     * Http Service is up, add my servlets.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Object addingService(ServiceReference reference) {
        HttpService httpService = (HttpService) context.getService(reference);
        
        try {
            if (contextPath == null)
                contextPath = defaultSystemContextPath;
            if (contextPath == null)
                if (servicePid != null)
            {
                ServiceReference caRef = context.getServiceReference(ConfigurationAdmin.class.getName());
                if (caRef != null)
                {
                    ConfigurationAdmin configAdmin = (ConfigurationAdmin)context.getService(caRef);
                    Configuration config = configAdmin.getConfiguration(servicePid);
                 
                    Dictionary properties = config.getProperties();
                    if (properties == null)
                    {
                       properties = new Hashtable();
                    }
                    else
                    {
                        contextPath = (String)properties.get(BaseOsgiServlet.CONTEXT_PATH);
                    }
                    if (contextPath == null)
                        contextPath = DEFAULT_CONTEXT_PATH;
                    // configure the Dictionary
                    properties.put(BaseOsgiServlet.CONTEXT_PATH, contextPath);                 
                    //push the configuration dictionary to the ConfigAdminService
                    config.update(properties);
                }            
                if (contextPath == null)
                    contextPath = DEFAULT_CONTEXT_PATH;
            }
            
            servlet = (HttpServlet)ClassServiceUtility.getClassService().makeObjectFromClassName(servletClassName);
            Dictionary<String,String> dictionary = null;
            HttpContext httpContext = null;
            if (servlet instanceof BaseOsgiServlet)
            {
                ((BaseOsgiServlet)servlet).init(context);
                dictionary = ((BaseOsgiServlet)servlet).getDictionary();
                httpContext = (HttpContext)((BaseOsgiServlet)servlet).getHttpContext();
            }
            if (dictionary == null)
                dictionary = new Hashtable<String,String>();
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
        if (servlet instanceof BaseOsgiServlet)
            ((BaseOsgiServlet)servlet).free();
        super.removedService(reference, service);
    }
    
    /**
     * Change the contextPath.
     * @param contextPath
     */
    public void setContextPath(String contextPath)
    {
        if (contextPath.equals(this.contextPath))
            return;
        ServiceReference reference = context.getServiceReference(HttpService.class.getName());
        if (reference == null)
            return;
        HttpService httpService = (HttpService) context.getService(reference);
        httpService.unregister(this.contextPath);
        this.contextPath = contextPath;
        Dictionary<String,String> dictionary = new Hashtable<String,String>();
        JnlpHttpContext httpContext = new JnlpHttpContext(context.getBundle());
        try {
            httpService.registerServlet(contextPath, servlet, dictionary, httpContext);
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (NamespaceException e) {
            e.printStackTrace();
        }
    }
    
}