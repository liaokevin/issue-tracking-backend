package nu.jixa.its.web.endpoint;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import nu.jixa.its.model.Team;
import nu.jixa.its.model.User;
import nu.jixa.its.model.WorkItem;
import nu.jixa.its.service.ITSRepository;
import nu.jixa.its.service.exception.ITSRepositoryException;
import nu.jixa.its.web.JixaAuthenticator;
import nu.jixa.its.web.Values;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/teams")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TeamsEndpoint {

  private static final String BAD_REQUEST_NULL_OR_INVALID =
      "Null or Invalid JSON Data in Request Body";
  private static final String NO_TEAM_WITH_TEAM_NUMBER = "No team with Team Number: ";
  private static final String BAD_REQUEST_MISMATCH_BETWEEN_PATH_AND_TEAM =
      "Usernumber mismatch between path and new user info";

  @Autowired
  private ITSRepository itsRepository;

  @Autowired
  JixaAuthenticator authenticator;

  @Context
  private UriInfo uriInfo;

  @GET
  public Response getTeams() {
    Iterable<Team> teams = itsRepository.getAllTeams();
    Collection<Team> teamsCollection = new ArrayList<>();
    for (Team team : teams) {
      teamsCollection.add(team);
    }

    return Response.ok(teamsCollection).build();
  }

  @GET
  @Path("{teamNumber}")
  public Response getTeam(
      @Context HttpHeaders httpHeaders,
      @PathParam("teamNumber") final long teamNumber) {
    authenticator.enforceTeamAccessRights(httpHeaders,teamNumber);

    try {
      Team team = itsRepository.getTeam(teamNumber);
      return Response.ok(team).build();
    } catch (ITSRepositoryException e) {
      return Response.status(Status.NOT_FOUND)
          .entity(NO_TEAM_WITH_TEAM_NUMBER + teamNumber).build();
    }
  }

  @POST
  public Response createTeam(final Team team) {
    if (team == null) {
      return Util.badRequestResponse(Values.BAD_REQUEST_NULL_OR_INVALID_MESSAGE);
    }
    try {
      itsRepository.addTeam(team);
    } catch (ITSRepositoryException e) {
      return Util.badRequestResponse(e);
    }
    final URI location = uriInfo.getAbsolutePathBuilder().path(team.getNumber().toString()).build();
    return Response.created(location).build();
  }

  @PUT
  @Path("{teamNumber}")
  public Response updateTeam(@Context HttpHeaders httpHeaders,
      @PathParam("teamNumber") final long teamNumber,
      final Team updatedTeam) {
    authenticator.enforceTeamAccessRights(httpHeaders, teamNumber);

    if (updatedTeam == null || updatedTeam.getNumber() == null) {
      return Response.status(Status.BAD_REQUEST)
          .entity(BAD_REQUEST_NULL_OR_INVALID).build();
    }
    try {
      if (teamNumber == updatedTeam.getNumber()) {
        itsRepository.updateTeam(updatedTeam);
        return Response.status(Status.NO_CONTENT).build();
      } else {
        return Response.status(Status.BAD_REQUEST)
            .entity(BAD_REQUEST_MISMATCH_BETWEEN_PATH_AND_TEAM).build();
      }
    } catch (ITSRepositoryException e) {
      return Response.status(Status.NOT_FOUND)
          .entity(e.getMessage()).build();
    }
  }

  @DELETE
  @Path("{teamNumber}")
  public Response deleteTeam(@Context HttpHeaders httpHeaders,
      @PathParam("teamNumber") final long teamNumber) {
    authenticator.enforceTeamAccessRights(httpHeaders,teamNumber);

    try {
      itsRepository.deleteTeam(teamNumber);
    } catch (ITSRepositoryException e) {
      return Response.status(Status.NOT_FOUND)
          .entity(e.getMessage()).build();
    }
    return Response.noContent().build();
  }

  @GET
  @Path("{teamNumber}/users")
  public Response getAllUsersInTeam(@Context HttpHeaders httpHeaders,
      @PathParam("teamNumber") final long teamNumber) {

    authenticator.enforceTeamAccessRights(httpHeaders,teamNumber);

    if (teamNumber == 0) {
      return Response.status(Status.BAD_REQUEST).entity(BAD_REQUEST_NULL_OR_INVALID).build();
    }
    try {
      Iterable<User> usersByTeam = itsRepository.getUsersByTeam(teamNumber);

      if (usersByTeam.iterator().hasNext()) {
        return Response.ok(usersByTeam).build();
      } else {
        return Response.noContent().build();
      }
    } catch (ITSRepositoryException e) {
      return Response.status(Status.BAD_REQUEST).build();
    }
  }

  @GET
  @Path("{teamNumber}/work-items")
  public Response getAllWorkItemsForTeam(@Context HttpHeaders httpHeaders,
      @PathParam("teamNumber") final long teamNumber) {
    authenticator.enforceTeamAccessRights(httpHeaders,teamNumber);

    if (teamNumber == 0) {
      return Response.status(Status.BAD_REQUEST).entity(BAD_REQUEST_NULL_OR_INVALID).build();
    }
    try {
      Iterable<WorkItem> usersByTeam = itsRepository.getWorkItemsByTeam(teamNumber);

      if (usersByTeam.iterator().hasNext()) {
        return Response.ok(usersByTeam).build();
      } else {
        return Response.noContent().build();
      }
    } catch (ITSRepositoryException e) {
      return Response.status(Status.BAD_REQUEST).build();
    }
  }

  @PUT
  @Path("{teamNumber}/users")
  public Response addUserToTeam(@Context HttpHeaders httpHeaders,
      @PathParam("teamNumber") final long teamNumber,
      final User userToAdd) {
    authenticator.enforceTeamAccessRights(httpHeaders,teamNumber);

    if (userToAdd == null) {
      return Response.status(Status.BAD_REQUEST)
          .entity(BAD_REQUEST_NULL_OR_INVALID).build();
    }
    try {
      itsRepository.addUserToTeamWithNumber(userToAdd.getNumber(), teamNumber);

      final URI location =
          uriInfo.getAbsolutePathBuilder().path(String.valueOf(teamNumber)).build();
      return Response.created(location).build();
    } catch (ITSRepositoryException e) {
      return Response.status(Status.NOT_FOUND).entity(e.getMessage()).build();
    }
  }
}
