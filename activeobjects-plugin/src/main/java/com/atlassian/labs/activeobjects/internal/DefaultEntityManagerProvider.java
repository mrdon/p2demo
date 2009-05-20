package com.atlassian.labs.activeobjects.internal;

import com.atlassian.labs.activeobjects.external.EntityManagerProvider;
import com.atlassian.sal.api.ApplicationProperties;
import net.java.ao.EntityManager;
import net.java.ao.RawEntity;
import net.java.ao.schema.CamelCaseTableNameConverter;

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.DisposableBean;

/**
 *
 */
public class DefaultEntityManagerProvider implements EntityManagerProvider, DisposableBean
{
    private final File databaseBaseDirectory;
    private final Map<String,EntityManager> entityManagers;

    public DefaultEntityManagerProvider(ApplicationProperties appProps)
    {
        File home = appProps.getHomeDirectory();
        if (home == null || !home.exists())
        {
            throw new RuntimeException("This plugin requires a home directory");
        }
        File data = new File(home, "data");
        File plugins = new File(data, "plugins");
        databaseBaseDirectory = new File(plugins, "activeobjects");
        if (!databaseBaseDirectory.exists())
        {
            databaseBaseDirectory.mkdirs();
        }
        entityManagers = new HashMap<String,EntityManager>();
    }

    public synchronized EntityManager createEntityManager(final String pluginKey)
    {
        EntityManager mgr = entityManagers.get(pluginKey);
        if (mgr == null)
        {
            mgr = new EntityManager(
                "jdbc:hsqldb:file:" + databaseBaseDirectory.getAbsolutePath() + "/db", "sa", "");

            mgr.setTableNameConverter(new CamelCaseTableNameConverter()
            {
                @Override
                protected String convertName(Class<? extends RawEntity<?>> type)
                {
                    return pluginKey + "_" + super.convertName(type);
                }
            });
            entityManagers.put(pluginKey, mgr);
        }

        return mgr;
    }

    public synchronized void destroy() throws Exception
    {
        for (EntityManager mgr : entityManagers.values())
        {
            mgr.getProvider().dispose();
        }
    }
}
