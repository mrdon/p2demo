package com.atlassian.labs.activeobjects.internal;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import com.atlassian.plugin.test.PluginTestUtils;
import com.atlassian.sal.api.ApplicationProperties;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import net.java.ao.Entity;
import net.java.ao.EntityManager;

/**
 *
 */
public class TestDefaultEntityManagerProvider extends TestCase
{
    public void testGetEntityManager() throws IOException, SQLException
    {
        File dbdir = PluginTestUtils.createTempDirectory(getClass());
        dbdir.mkdir();

        ApplicationProperties appProps = mock(ApplicationProperties.class);
        when(appProps.getHomeDirectory()).thenReturn(dbdir);

        DefaultEntityManagerProvider prov = new DefaultEntityManagerProvider(appProps);

        EntityManager mgr = prov.createEntityManager("foo");
        mgr.migrate(Person.class);

        Person bob = mgr.create(Person.class);
        bob.setName("bob");
        bob.save();

        mgr = prov.createEntityManager("foo");
        bob = mgr.find(Person.class)[0];
        assertEquals("bob", bob.getName());

    }

    public interface Person extends Entity
    {
        public String getName();
        public void setName(String name);
    }
}
