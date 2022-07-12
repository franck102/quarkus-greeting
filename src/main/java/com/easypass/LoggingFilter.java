package com.easypass;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.arc.Unremovable;
import io.vertx.core.http.HttpServerRequest;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;
import org.jboss.resteasy.reactive.server.ServerResponseFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.UriInfo;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Singleton
@Unremovable
public class LoggingFilter {
    private static final Logger _log = LoggerFactory.getLogger(LoggingFilter.class);
    @Inject
    ObjectMapper _mapper;
    AtomicInteger _requestId = new AtomicInteger();

    @ServerRequestFilter(priority = 0)
    public void getFilter(UriInfo info, HttpServerRequest request, ContainerRequestContext ctx) {
        int requestId = _requestId.incrementAndGet();
        ctx.setProperty("log_id", requestId);
        String body = new BufferedReader(new InputStreamReader(ctx.getEntityStream())).lines().collect(Collectors.joining("\n"));
        ctx.setEntityStream(new ByteArrayInputStream(body.getBytes()));
        _log.info("[" + request + "] " + ctx.getMethod() + " " + info.getPath() + " " + body);
    }

    @ServerResponseFilter
    public void getResponseFilter(UriInfo info, ContainerRequestContext request, ContainerResponseContext response)
            throws JsonProcessingException {
        _log.info("[" + request.getProperty("log_id") + "] " + response.getStatus() + " "+request.getMethod() + " " + info.getPath() + " " +
                        (response.getEntity() == null ? "" :
                _mapper.writeValueAsString(response.getEntity().toString())));
    }
}
