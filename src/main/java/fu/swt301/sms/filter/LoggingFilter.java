/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fu.swt301.sms.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.logging.Logger;

/**
 *
 * @author admin
 */
@WebFilter("/*")
public class LoggingFilter implements Filter {

    private static final Logger LOGGER
            = Logger.getLogger(LoggingFilter.class.getName());

    @Override
    public void doFilter(ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        LOGGER.info(httpRequest.getMethod()
                + " "
                + httpRequest.getRequestURI());

        chain.doFilter(request, response);

    }
}
