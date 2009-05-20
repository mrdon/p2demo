package com.atlassian.labs.accesslog;

import com.atlassian.labs.activeobjects.external.EntityManagerProvider;
import com.atlassian.sal.api.user.UserManager;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.SQLException;

import net.java.ao.EntityManager;

public class AccessLogFilter implements Filter
{
    private final EntityManager entityManager;
    private final UserManager userManager;

    public AccessLogFilter(EntityManagerProvider provider, UserManager userManager)
    {
        this.userManager = userManager;
        this.entityManager = provider.createEntityManager("accesslog");
    }

    public void init(FilterConfig filterConfig) throws ServletException
    {
        try
        {
            entityManager.migrate(LogEntry.class);
        }
        catch (SQLException e)
        {
            throw new ServletException(e);
        }
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
    {
        String user = userManager.getRemoteUsername((HttpServletRequest) servletRequest);
        String url = ((HttpServletRequest)servletRequest).getRequestURI();
        try
        {
            LogEntry entry = entityManager.create(LogEntry.class);
            entry.setUser(user);
            entry.setUrl(url);
            entry.save();
            System.out.println("log has " + entityManager.count(LogEntry.class) + " entries");
        }
        catch (SQLException e)
        {
            throw new ServletException(e);
        }
        filterChain.doFilter(servletRequest, servletResponse);

    }

    public void destroy()
    {
    }
}
