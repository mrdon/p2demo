package it.com.atlassian.labs.activeobjects;

import junit.framework.TestCase;

import java.io.File;

import com.atlassian.plugin.JarPluginArtifact;
import com.atlassian.plugin.osgi.hostcomponents.HostComponentProvider;
import com.atlassian.plugin.osgi.hostcomponents.ComponentRegistrar;
import com.atlassian.plugin.test.PluginJarBuilder;
import com.atlassian.plugin.test.PluginTestUtils;
import com.atlassian.sal.api.ApplicationProperties;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
public class TestIntegrations extends PluginInContainerTestBase
{
    public void testBasic() throws Exception
    {
        final ApplicationProperties props = mock(ApplicationProperties.class);
        when(props.getHomeDirectory()).thenReturn(PluginTestUtils.createTempDirectory(getClass()));
        initPluginManager(new HostComponentProvider()
        {
            public void provide(ComponentRegistrar componentRegistrar)
            {
                componentRegistrar.register(ApplicationProperties.class).forInstance(props);
            }
        });
        pluginManager.installPlugin(new JarPluginArtifact(new File(System.getProperty("plugin.jar"))));
        assertTrue(pluginManager.isPluginEnabled("com.atlassian.labs.activeobjects"));

        File plugin = new PluginJarBuilder()
                .addFormattedJava("my.Foo",
                        "package my;",
                        "public interface Foo extends net.java.ao.Entity {",
                        " public String getName();",
                        " public void setName(String name);",
                        "}")
                .addFormattedJava("my.FooComponent",
                        "package my;",
                        "public class FooComponent {",
                        "  public FooComponent(com.atlassian.labs.activeobjects.external.EntityManagerProvider prov) throws Exception {",
                        "    net.java.ao.EntityManager mgr = prov.createEntityManager('bob');",
                        "    mgr.migrate(new Class[]{my.Foo.class});",
                        "    Foo foo = (Foo) mgr.create(my.Foo.class, new net.java.ao.DBParam[0]);",
                        "    foo.setName('bob');",
                        "    foo.save();",
                        "    foo = (Foo) mgr.find(Foo.class)[0];",
                        "    if (!foo.getName().equals('bob')) throw new RuntimeException('not saved');",
                        "  }",
                        "}")
                .addFormattedResource("atlassian-plugin.xml",
                        "<atlassian-plugin name='Test' key='test.ao.plugin' pluginsVersion='2'>",
                        "    <plugin-info>",
                        "        <version>1.0</version>",
                        "    </plugin-info>",
                        "    <component key='obj' class='my.FooComponent'/>",
                        "    <component-import key='emp' interface='com.atlassian.labs.activeobjects.external.EntityManagerProvider' />",
                        "</atlassian-plugin>")
                .build();

        pluginManager.installPlugin(new JarPluginArtifact(plugin));
        assertTrue(pluginManager.isPluginEnabled("test.ao.plugin"));
        
        assertNotNull(System.getProperty("plugin.jar"));
    }
}
