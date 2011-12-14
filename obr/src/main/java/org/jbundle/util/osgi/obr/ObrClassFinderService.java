/*
 * Copyright © 2011 jbundle.org. All rights reserved.
 */
package org.jbundle.util.osgi.obr;

import org.apache.felix.bundlerepository.DataModelHelper;
import org.apache.felix.bundlerepository.Reason;
import org.apache.felix.bundlerepository.Repository;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.felix.bundlerepository.Requirement;
import org.apache.felix.bundlerepository.Resolver;
import org.apache.felix.bundlerepository.Resource;
import org.jbundle.util.osgi.finder.BaseClassFinderService;
import org.jbundle.util.osgi.finder.ClassFinderActivator;
import org.jbundle.util.osgi.finder.ClassServiceUtility;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.service.log.LogService;

/**
 * ClassServiceImpl - Find bundles (and classes) in the obr repository.
 *
 * @author don
 * 
 */
public class ObrClassFinderService extends BaseClassFinderService
	implements BundleActivator
{
    protected RepositoryAdmin repositoryAdmin = null;

    /**
     * Be sure to synchronize on this when you change it.
     */
    public static Boolean waitingForRepositoryAdmin = false;
    public static Boolean waitingForClassService = false;

    /**
     * Bundle starting.
     * If the service listener is up, register me, else wait.
     */
    public void start(BundleContext context) throws Exception
    {
        super.start(context);
        repositoryAdmin = this.getRepositoryAdmin(context, this);
        
        if (repositoryAdmin != null)
        {   // The repository is up, I can get to work (otherwise, I'll be waiting)
        	this.addBootstrapRepository(repositoryAdmin, context);
            this.registerClassFinder(context);
        }
    }
    /**
     * Get the repository admin service.
     * @param context
     * @return
     * @throws InvalidSyntaxException
     */
    public RepositoryAdmin getRepositoryAdmin(BundleContext context, ObrClassFinderService autoStartNotify) throws InvalidSyntaxException
    {
    	RepositoryAdmin admin = null;
    	
        ServiceReference[] ref = context.getServiceReferences(RepositoryAdmin.class.getName(), null);

        if ((ref != null) && (ref.length > 0))
        	admin =  (RepositoryAdmin) context.getService(ref[0]);
        
        if (admin == null)
        	if (autoStartNotify != null)
                if (waitingForRepositoryAdmin == false)
        {   // Wait until the repository service is up until I start servicing clients
            context.addServiceListener(new RepositoryAdminServiceListener(autoStartNotify, context), "(" + Constants.OBJECTCLASS + "=" + RepositoryAdmin.class.getName() + ")");
            waitingForRepositoryAdmin = true;
        }

        return admin;
    }
    /**
     * Add the standard obr repository, so I can get the bundles that I need.
     * @param repositoryAdmin
     * @param context
     */
    public void addBootstrapRepository(RepositoryAdmin repositoryAdmin, BundleContext context)
    {
        if (repositoryAdmin == null)
            return;
        String repository = context.getProperty("jbundle.repository.url");
        if (repository != null)
        	if (repository.length() > 0)
        		this.addRepository(repositoryAdmin, repository);

        //repository = "file:" + System.getProperty("user.home") + File.separator + ".m2" + File.separator  + "full-repository.xml";
        //this.addRepository(repositoryAdmin, repository);        
    }
    /**
     * Add this repository to my available repositories.
     * @param repositoryAdmin
     * @param repository
     */
    public void addRepository(RepositoryAdmin repositoryAdmin, String repository)
    {
        try {
            if (repository != null)
            {
                boolean duplicate = false;
                for (Repository repo : repositoryAdmin.listRepositories())
                {
                    if (repository.equalsIgnoreCase(repo.getURI()))
                        duplicate = true;
                }
                if (!duplicate)
                {
                    Repository repo = repositoryAdmin.addRepository(repository);
                    if (repo == null)
                        repositoryAdmin.removeRepository(repository);   // Ignore repos not found
                }
            }
        } catch (Exception e) {
            // Ignore exception e.printStackTrace();
        }
    }
    /**
     * Called when this service is active.
     * Override this to register your service if you need a service.
     * I Don't register this bootstrap for two reasons:
     * 1. I Don't need this object
     * 2. This object was usually instansiated by bootstrap code copied to the calling classes' jar.
     */
    public void registerClassFinder(BundleContext context)
    {
        waitingForRepositoryAdmin = false;

        this.startClassFinderActivator(context);    // Now that I have the repo, start the ClassService

        ClassServiceUtility.log(context, LogService.LOG_INFO, "ObrClassFinderService is up");
    }
    /**
     * Bundle shutting down.
     */
    public void stop(BundleContext context) throws Exception {
        ClassServiceUtility.log(context, LogService.LOG_INFO, "Stopping ObrClassFinderImpl");

        super.stop(context);
        repositoryAdmin = null;
        waitingForRepositoryAdmin = false;
        waitingForClassService = false;
        ClassFinderActivator.setClassFinder(null);
    }
    /**
     * Convenience method to start the class finder utility service.
     * If admin service is not up yet, this starts it.
     * @param className
     * @return true If I'm up already
     * @return false If I had a problem.
     */
    public boolean startClassFinderActivator(BundleContext context)
    {
    	if (ClassFinderActivator.getClassFinder(context, 0) == this)
    		return true;	// Already up!
        // If the repository is not up, but the bundle is deployed, this will find it
    	String packageName = ClassFinderActivator.getPackageName(ClassFinderActivator.class.getName(), false);
        Bundle bundle = this.findBundle(null, context, packageName, null);
        if (bundle == null)
        {
            Resource resource = (Resource)this.deployThisResource(packageName, null, false);  // Get the bundle info from the repos
            bundle = this.findBundle(resource, context, packageName, null);
        }
        
        if (bundle != null)
        {
            if (((bundle.getState() & Bundle.ACTIVE) == 0)
            		&& ((bundle.getState() & Bundle.STARTING) == 0))
            {
                try {
                    bundle.start();
                } catch (BundleException e) {
                    e.printStackTrace();
                }
            }
            ClassFinderActivator.setClassFinder(this);
            return true;	// Success
        }
        return false;	// Error! Where is my bundle?
    }
    /**
     * Find this resource in the repository, then deploy and optionally start it.
     * @param className
     * @param options 
     * @return
     */
    public Object deployThisResource(String packageName, String version, boolean start)
    {
    	int options = 0;
    	//?if (start)
    	//?	options = Resolver.START;
    	if (repositoryAdmin == null)
    		return null;
        DataModelHelper helper = repositoryAdmin.getHelper();
        if (this.getResourceFromCache(packageName) != null)
        	return this.getResourceFromCache(packageName);
        String filter = "(package=" + packageName + ")";
        filter = ClassFinderActivator.addVersionFilter(filter, version, false);
        Requirement requirement = helper.requirement("package", filter);
        Requirement[] requirements = { requirement };// repositoryAdmin
        Resource[] resources = repositoryAdmin.discoverResources(requirements);
        if ((resources != null) && (resources.length > 0))
        {
            this.deployResources(resources, options);
            Bundle bundle = this.findBundle(resources[0], bundleContext, packageName, version);
            if (start)
                if (bundle != null)
                    if ((bundle.getState() != Bundle.ACTIVE) && (bundle.getState() != Bundle.STARTING))
            {
                try {
                    bundle.start();
                } catch (BundleException e) {
                    e.printStackTrace();
                }
            }
        	this.addResourceToCache(packageName, resources[0]);
        	return resources[0];
        }
        else
        	return null;
    }
    /**
     * Deploy this list of resources.
     * @param resources
     * @param options
     */
    public void deployResources(Resource[] resources, int options)
    {
        if (resources != null)
        {
        	for (Resource resource : resources)
        	{
        		this.deployResource(resource, options);
        	}
        }
    }

    /**
     * Deploy this resource.
     * @param resource
     * @param options
     */
    public void deployResource(Resource resource, int options)
    {
        Resolver resolver = repositoryAdmin.resolver();
        resolver.add(resource);
        if (resolver.resolve(options))
        {
            resolver.deploy(options);
        } else {
            Reason[] reqs = resolver.getUnsatisfiedRequirements();
            for (int i = 0; i < reqs.length; i++) {
                ClassServiceUtility.log(bundleContext, LogService.LOG_ERROR, "Unable to resolve: " + reqs[i]);
            }
        }
    }
    /**
     * Does this resource match this bundle?
     * @param resource
     * @param context
     * @return
     */
    public boolean isResourceBundleMatch(Object objResource, Bundle bundle)
    {
    	Resource resource = (Resource)objResource;
    	return ((bundle.getSymbolicName().equals(resource.getSymbolicName())) && (compareVersion(bundle, resource)));
    }
    /**
     * Does this bundle's version match this resource's version?
     * @param bundle
     * @param resource
     * @return
     */
    public boolean compareVersion(Bundle bundle, Resource resource)
    {
    	if (bundle.getVersion().equals(resource.getVersion()))
    		return true;
    	Version bundleVersion = bundle.getVersion();
    	Version resourceVersion = resource.getVersion();
    	if (bundleVersion.getMajor() == resourceVersion.getMajor())
        	if (bundleVersion.getMinor() == resourceVersion.getMinor())
        		return true;
    	return false;
    }
}

