package com.atlassian.labs.accesslog;

import net.java.ao.Entity;

public interface LogEntry extends Entity
{
    String getUser();
    void setUser(String user);

    String getUrl();
    void setUrl(String url);
}
