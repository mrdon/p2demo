package com.atlassian.labs.accesslog;

import net.java.ao.EntityManager;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import com.atlassian.sal.api.user.UserManager;
import com.atlassian.labs.activeobjects.external.EntityManagerProvider;

public class AccessLogDisplayServlet extends HttpServlet
{
    private final EntityManager entityManager;
    private final UserManager userManager;

    public AccessLogDisplayServlet(EntityManagerProvider provider, UserManager userManager)
    {
        this.userManager = userManager;
        this.entityManager = provider.createEntityManager("accesslog");
    }
    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException
    {
        String user = userManager.getRemoteUsername(httpServletRequest);
        if (user == null || !userManager.isSystemAdmin(user))
        {
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Only accessible via system admins");
            return;
        }
        httpServletResponse.setContentType("text/html");
        PrintWriter out = httpServletResponse.getWriter();
        out.write("<html><head>");
        out.write("<meta name=\"decorator\" content=\"atl.admin\">");
        out.write("</head><body>");
        out.write("<h2> Access Log </h2>");
        out.write("<table><tr><th>User</th><th>URL</th></tr>");
        try
        {
            for (LogEntry entry : entityManager.find(LogEntry.class))
            {
                out.write("<tr><td>");
                out.write(entry.getUser() == null ? "Anonymous" : entry.getUser());
                out.write("</td><td>");
                out.write(entry.getUrl());
                out.write("</td></tr>");
            }
        }
        catch (SQLException e)
        {
            throw new ServletException(e);
        }
        out.write("</table></body></html>");
        out.close();   
    }
}
