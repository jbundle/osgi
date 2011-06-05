package org.jbundle.util.osgi.p2;

import org.jbundle.util.osgi.finder.BaseClassFinderService;
import org.osgi.framework.BundleActivator;

/**
 * ClassServiceImpl - Find bundles (and classes) in the obr repository.
 *
 * @author don
 * 
 */
public class P2ClassFinderService extends BaseClassFinderService
	implements BundleActivator
{
    /**
     * Find this resource in the repository, then deploy and optionally start it.
     * @param className
     * @param options 
     * @return
     */
    public Object deployThisResource(String className, boolean start, boolean resourceType)
    {
        	return null;
    }
}

