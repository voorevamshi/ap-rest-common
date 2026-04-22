package com.optum.ap.services.rest.common.security.filters;

import java.io.IOException;
import java.time.Instant;

import org.apache.log4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.tpa.ap.common.service.util.StringUtil;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;

@Component
@WebFilter(urlPatterns = "/ap/rest/data/*")
@Order(Integer.MIN_VALUE)
public class LoggingFilter implements Filter{
	 private static final Logger logger = Logger.getLogger(LoggingFilter.class);
	    
	 @Override
	    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
	    		throws IOException, ServletException {
		 HttpServletRequest httpRequest = (HttpServletRequest) request;
	   	 Instant startTime = Instant.now();
	   	String stargateRequestTrackingHeader = httpRequest.getHeader("optum-cid-ext");
	   	 if( !StringUtil.isEmptyString(stargateRequestTrackingHeader)) {
	   	 try {
	   		 chain.doFilter(request, response);
	   	 }finally {
	   		 Instant endTime = Instant.now();
	   		 long duration = endTime.toEpochMilli() - startTime.toEpochMilli();
	   		 if(duration>=5000) {
	   			 logger.error("Response time exceeds beyond 5(secs), time taken for request: " + duration + " ms ");
	   		 }
	   	 }
	   	 }else {
	   		 try {
	   		 chain.doFilter(request, response);
	   	 }catch(Exception e) {
	   		 logger.error("Error in First Filter -> "+e.getMessage());
	   	 }
	   	 }
	    }
  
     @Override
     public void init(FilterConfig filterConfig) throws ServletException {
     	// TODO Auto-generated method stub
     }
    
     @Override
     public void destroy() {
     	// TODO Auto-generated method stub
     }

}


