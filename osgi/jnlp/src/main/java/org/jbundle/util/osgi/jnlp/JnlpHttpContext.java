package org.jbundle.util.osgi.jnlp;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;

/**
 * Jnlp http context.
 * @author don
 *
 */
public class JnlpHttpContext implements HttpContext {

    private Bundle bundle;

    public JnlpHttpContext(Bundle bundle)
    {
        this.bundle = bundle;
    }

	@Override
	public boolean handleSecurity(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		return true;
	}

	@Override
    public URL getResource(String name)
    {
        if (name.startsWith("/")) {
            name = name.substring(1);
        }
        return this.bundle.getResource(name);
    }

	@Override
	public String getMimeType(String name) {
		return "application/x-java-jnlp-file";
	}

}