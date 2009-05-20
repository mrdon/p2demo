package com.atlassian.labs.activeobjects.external;

import net.java.ao.EntityManager;

/**
 *
 */
public interface EntityManagerProvider
{
    EntityManager createEntityManager(String pluginKey);
}
