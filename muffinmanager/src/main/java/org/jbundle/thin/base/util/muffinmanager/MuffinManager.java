/*
 * MuffinManager.java
 *
 * Created on January 30, 2001, 12:14 AM
 */
 
package org.jbundle.thin.base.util.muffinmanager;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.jnlp.BasicService;
import javax.jnlp.FileContents;
import javax.jnlp.PersistenceService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;

/** 
 * MuffinManager - This code handles the muffins for the java web start program.
 * @author  Administrator
 * @version 1.0.0
 */
public class MuffinManager extends Object
{
    /**
     * The basic jnlp services.
     */
    protected BasicService m_bs = null;
    /**
     * The jnlp persistence service.
     */
    protected PersistenceService m_ps = null;
    /**
     * The codebase to prefix muffins.
     */
    protected String m_strCodeBase = null;
    /**
     * The default encoding for muffins.
     */
    public static final String ENCODING = "UTF-8";

    /**
     * Creates new MuffinManager.
     */
    public MuffinManager()
    {
        super();
    }
    /**
     * Creates new MuffinManager .
     * @param applet The parent object (ignored).
     */
    public MuffinManager(Object applet)
    {
        this();
        this.init(applet);
    }
    /**
     * Creates new MuffinManager.
     * @param applet The parent object (ignored).
     */
    public void init(Object applet)
    {
        try {
            m_ps = (PersistenceService)ServiceManager.lookup("javax.jnlp.PersistenceService");
            m_bs = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService");
            m_strCodeBase = m_bs.getCodeBase().toString();
        } catch (UnavailableServiceException e) {
            m_ps = null;
            m_bs = null;
        }
    }
    /**
     * Is the muffin manager service available?
     * @return true if available.
     */
    public boolean isServiceAvailable()
    {
        return (m_ps != null);
    }
    /**
     * Get the current value for this muffin.
     * @param strParam The key for this muffin parameter.
     * @return The value for this muffin (null if none).
     */
    public String getMuffin(String strParam)
    {
        try   {
            URL url = new URL(m_strCodeBase + strParam);
            FileContents fc = m_ps.get(url);
            if (fc == null)
                return null;
            // read in the contents of a muffin
            byte[] buf = new byte[(int)fc.getLength()];
            InputStream is = fc.getInputStream();
            int pos = 0;
            while((pos = is.read(buf, pos, buf.length - pos)) > 0) {
                // just loop
            }
            is.close();
            String strValue = new String(buf, ENCODING);
            return strValue;
        } catch (Exception ex)  {
            // Return null for any exception
        }
        return null;
    }
    /**
     * Set the current value for this muffin.
     * @param strParam The key for this muffin parameter.
     * @param strValue The value for this muffin.
     */
    public void setMuffin(String strParam, String strValue)
    {
        FileContents fc = null;
        URL url = null;
        try   {
            url = new URL(m_strCodeBase + strParam);
        } catch (Exception ex)  {
            return;
        }
        try   {
            fc = m_ps.get(url);
            fc.getMaxLength(); // This will throw an exception if there is no muffin yet.
        } catch (Exception ex)  {
            fc = null;
        }
        try   {
            if (fc == null)
            {
                m_ps.create(url, 100);
                fc = m_ps.get(url);
            }          // don't append
            if (strValue != null)
            {
                OutputStream os = fc.getOutputStream(false);
                byte[] buf = strValue.getBytes(ENCODING);
                os.write(buf);
                os.close();
                m_ps.setTag(url, PersistenceService.DIRTY);
            }
            else
                m_ps.delete(url);
        } catch (Exception ex)  {
            ex.printStackTrace(); // Return null for any exception
        }
    }
    /**
     * Get the Java WebStart codebase.
     * @return The codebase.
     */
    public URL getCodeBase()
    {
        if (m_bs != null)
            return m_bs.getCodeBase();
        return null;
    }
    /**
     * Display this URL in a web browser.
     * @param url The URL to display.
     * @return True if successfully displayed.
     */
    public boolean showTheDocument(URL url)
    {
        if (m_bs != null)
            return m_bs.showDocument(url);
        return false;
    }
}
