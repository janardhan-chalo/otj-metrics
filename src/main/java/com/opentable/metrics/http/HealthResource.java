package com.opentable.metrics.http;

import java.util.Map;

import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.health.HealthCheck.Result;

import org.apache.commons.lang3.tuple.Pair;

@Named
@Produces(MediaType.APPLICATION_JSON)
@Path("/health")
public class HealthResource {
    private final HealthController controller;

    HealthResource(HealthController controller) {
        this.controller = controller;
    }

    @GET
    @Path("/")
    public Response getHealth() {
        final Pair<Map<String, Result>, Boolean> result = controller.runHealthChecks();
        final Boolean succeeded = result.getRight();
        final int status = succeeded ? 200 : 500;
        return Response.status(status).entity(result.getLeft()).build();
    }

    @GET
    @Path("/group/{group}")
    public Response getHealthGroup(@PathParam("group") String group) {
        final Pair<Map<String, Result>, Boolean> result = controller.runHealthChecks(group);
        if (result == null) {
            return Response.status(404).build();
        }
        final Boolean succeeded = result.getRight();
        final int status = succeeded ? 200 : 500;
        return Response.status(status).entity(result.getLeft()).build();
    }
}
