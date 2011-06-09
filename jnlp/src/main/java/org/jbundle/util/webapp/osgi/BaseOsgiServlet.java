package org.jbundle.util.webapp.osgi;

import java.util.Dictionary;

import javax.servlet.http.HttpServlet;


/**
 * Base OSGi Servlet.
 * Note: Even though this is called OsgiServlet, is must be able to run in a non-osgi environment,
 * so don't have any osgi imports.
 * Note: This is designed to override the JnlpDownloadServlet. I just a little 
 * apprehensive about the licensing if I wrap the (sun) code in an OSGi wrapper. 
 * @author don
 *
 */
public abstract class BaseOsgiServlet extends HttpServlet/*JnlpDownloadServlet*/ {
	private static final long serialVersionUID = 1L;
    
    private Object context = null;
    
    public static final String CONTEXT_PATH = "org.jbundle.util.osgi.contextpath";  // In Config Service

    /**
     * Constructor.
     * @param context
     */
    public BaseOsgiServlet() {
    	super();
    }
    
    /**
     * Constructor.
     * @param context
     */
    public BaseOsgiServlet(Object context) {
    	this();
    	init(context);
    }
    
    /**
     * Constructor.
     * @param context
     */
    public void init(Object context) {
    	this.context = context;
    }
     
    /**
     * Free my resources.
     */
    public void free()
    {
    }
    /**
     * Get the properties for this OSGi service.
     * @return The properties.
     */
    public Dictionary<String,String> getDictionary()
    {
        return null;
    }
    /**
     * Get the Servlet context for this servlet.
     * Override if different from default context.
     * @return The httpcontext.
     */
    public Object getHttpContext()
    {
        return null;    // Override this
    }
    /**
     * Convenience method.
     * Note: You will have to cast the class or override this in your actual OSGi servlet.
     */
    public Object getBundleContext()
    {
        return context;
    }
}