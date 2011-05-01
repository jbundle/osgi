package org.jbundle.thin.base.util.osgi.obr;

import java.io.File;
import java.util.Dictionary;

import org.apache.felix.bundlerepository.DataModelHelper;
import org.apache.felix.bundlerepository.Reason;
import org.apache.felix.bundlerepository.Repository;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.felix.bundlerepository.Requirement;
import org.apache.felix.bundlerepository.Resolver;
import org.apache.felix.bundlerepository.Resource;
import org.jbundle.thin.base.util.osgi.finder.BaseClassFinder;
import org.jbundle.thin.base.util.osgi.finder.ClassFinderUtility;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

/**
 * ClassServiceImpl - Find bundles (and classes) in the obr repository.
 *
 * @author don
 * 
 */
public class ObrClassFinderImpl extends BaseClassFinder
	implements BundleActivator
{
	private ClassFinderUtility cachedClassFinderUtility = null;
	
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
        System.out.println("Starting ObrClassFinderImpl");

        super.start(context);
        repositoryAdmin = this.getRepositoryAdmin(context, this);
        
        if (repositoryAdmin != null)
        {   // If the repository is up, I can get to work (otherwise, I'll be waiting)
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
    public RepositoryAdmin getRepositoryAdmin(BundleContext context, ObrClassFinderImpl autoStartNotify) throws InvalidSyntaxException
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

        repository = "file:" + System.getProperty("user.home") + File.separator + ".m2" + File.separator  + "full-repository.xml";
        this.addRepository(repositoryAdmin, repository);        
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

        System.out.println("ObrClassFinderImpl is up");

        this.startClassFinderUtility(context);    // Now that I have the repo, start the ClassService
    }
    /**
     * Bundle shutting down.
     */
    public void stop(BundleContext context) throws Exception {
        System.out.println("Stopping ObrClassFinderImpl");

        super.stop(context);
        repositoryAdmin = null;
        waitingForRepositoryAdmin = false;
        waitingForClassService = false;
        cachedClassFinderUtility = null;
    }
    /**
     * Get the class service.
     * This call should activate this bundle and start the ClassService.
     * @return
     */
	public ClassFinderUtility getClassFinderUtility()
	{
		if (cachedClassFinderUtility != null)
			return cachedClassFinderUtility;

		// First time or not running, try to find the class service
		cachedClassFinderUtility = findClassFinderUtility(true);
		
		return cachedClassFinderUtility;
	}
	/**
	 * Get the class service.
	 * @param waitForStart TODO
	 * @return The class service or null if it doesn't exist.
	 */
	public ClassFinderUtility findClassFinderUtility(boolean waitForStart)
	{
        if (bundleContext == null)
		{
			System.out.println("Error: ObrClassFinder was never started\n" + 
					"Add it as your bundle activator");
			return null;
		}

        ClassFinderUtility classFinderUtility = null;
    	
		try {
            ServiceReference[] ref = bundleContext.getServiceReferences(ClassFinderUtility.class.getName(), null);
		
			if ((ref != null) && (ref.length > 0))
				classFinderUtility =  (ClassFinderUtility)bundleContext.getService(ref[0]);
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}

		if (classFinderUtility == null)
			if (waitForStart)
				if (waitingForClassService == false)
		{
			waitingForClassService = true;
			// TODO Minor synchronization issue here
			Thread thread = Thread.currentThread();
			ClassFinderUtilityListener classFinderListener = null;
			try {
				bundleContext.addServiceListener(classFinderListener = new ClassFinderUtilityListener(thread, bundleContext), "(" + Constants.OBJECTCLASS + "=" + ClassFinderUtility.class.getName() + ")");
			} catch (InvalidSyntaxException e) {
				e.printStackTrace();
			}

			// Wait a minute for the ClassService to come up while the activator starts this service
			synchronized (thread)
			{
				try {
					thread.wait(60000);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
			bundleContext.removeServiceListener(classFinderListener);
			waitingForClassService = false;
			
			try {
				ServiceReference[] ref = bundleContext.getServiceReferences(ClassFinderUtility.class.getName(), null);
			
				if ((ref != null) && (ref.length > 0))
					classFinderUtility =  (ClassFinderUtility)bundleContext.getService(ref[0]);
			} catch (InvalidSyntaxException e) {
				e.printStackTrace();
			}

			if (classFinderUtility == null)
				System.out.println("The ClassService never started - \n" +
					"Include the bootstrap code in your bundle and make sure it is listed as an activator!");
		}

		return classFinderUtility;
	}
    /**
     * Convenience method to start the class finder utility service.
     * If admin service is not up yet, this starts it.
     * @param className
     * @return true If I'm up already
     * @return false If I had a problem.
     */
    public boolean startClassFinderUtility(BundleContext context)
    {
    	if (cachedClassFinderUtility != null)
    		return true;	// Never
    	cachedClassFinderUtility = findClassFinderUtility(false);	// See if someone else started it up
    	if (cachedClassFinderUtility != null)
    		return true;	// Already up!
        // If the repository is not up, but the bundle is deployed, this will find it
        Resource resource = (Resource)this.deployThisResource(ClassFinderUtility.class.getName(), false, false);  // Get the bundle info from the repos
        
        String packageName = ClassFinderUtility.getPackageName(ClassFinderUtility.class.getName(), false);
        Bundle bundle = this.getBundleFromResource(resource, context, packageName);
        
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
            cachedClassFinderUtility = getClassFinderUtility();	// This will wait until it is active to return
            return (cachedClassFinderUtility != null);	// Success
        }
        return false;	// Error! Where is my bundle?
    }
    /**
     * Find this resource in the repository, then deploy and optionally start it.
     * @param className
     * @param options 
     * @return
     */
    public Object deployThisResource(String className, boolean start, boolean resourceType)
    {
    	int options = 0;
    	if (start)
    		options = Resolver.START;
    	if (repositoryAdmin == null)
    		return null;
        DataModelHelper helper = repositoryAdmin.getHelper();
        String packageName = ClassFinderUtility.getPackageName(className, resourceType);
        String filter2 = "(package=" + packageName + ")"; // + "(version=xxx)"
        Requirement requirement = helper.requirement("package", filter2);
        Requirement[] requirements = { requirement };// repositoryAdmin
        Resource[] resources = repositoryAdmin.discoverResources(requirements);
        this.deployResources(resources, options);
        if ((resources != null) && (resources.length > 0))
        	return resources[0];
        else
        	return null;
    }
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
                System.out.println("Unable to resolve: " + reqs[i]);
            }
        }
    }
    /**
     * Find the currently installed bundle that exports this package.
     * NOTE: This is stupid, there has to be a way to do this.
     * @param resource
     * @param context
     * @return
     */
    public Bundle getBundleFromResource(Object objResource, BundleContext context, String packageName)
    {
    	Resource resource = (Resource)objResource;
        if (context == null)
            return null;
        Bundle[] bundles = context.getBundles();
        Bundle bestBundle = null;
        String bestVersion = null;
        for (Bundle bundle : bundles)
        {
            if (resource != null)
            {
                if ((bundle.getSymbolicName().equals(resource.getSymbolicName())) && (compareVersion(bundle, resource)))
                    return bundle;               
            }
            else if (packageName != null)
            {
                Dictionary<?, ?> dictionary = bundle.getHeaders();
                String packages = (String)dictionary.get("Export-Package");
                String version = (String)dictionary.get("Bundle-Version");
                if (packages != null)
                {
                    StringBuilder sb = new StringBuilder(packages);
                    while (true)
                    {
                        int start = sb.indexOf("=\"");
                        if (start == -1)
                            break;
                        int end = sb.indexOf("\"", start + 2);
                        if ((start > -1) && (end > -1))
                            sb.delete(start, end + 1);
                        else
                            break;  // never
                    }
                    while (true)
                    {
                        int semi = sb.indexOf(";");
                        if (semi == -1)
                            break;
                        int comma = sb.indexOf(",", semi);
                        if (comma == -1)
                            comma = sb.length();
                        else if (sb.charAt(comma + 1) == ' ')
                            comma++;
                        if ((semi > -1) && (comma > -1))
                            sb.delete(semi, comma);
                        else
                            break;  // never
                    }
                    
                    String[] packs = sb.toString().split(",");
                    for (String pack : packs)
                    {
                        if (packageName.equals(pack))
                        {
                        	if ((bestVersion == null)
                        		|| (bestVersion.compareTo(version) < 0))
                        	{
                        		bestBundle = bundle;	// TODO Newest version NO NO NO - Should get requested versions ie., 'x.x.)'
                        		bestVersion = version;
                        	}
                        }
                    }
                }
            }
        }
        
        return bestBundle;
    }
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

