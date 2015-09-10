package nu.jixa.its.web.endpoint;

import java.net.URI;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import nu.jixa.its.model.Team;
import nu.jixa.its.service.ITSRepository;
import nu.jixa.its.service.ITSRepositoryException;
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

  @Autowired
  private ITSRepository itsRepository;

  @Context
  private UriInfo uriInfo;

  //TEAM

  //✓Team       | Uppdatera ett team
  //✓Team       | Ta bort* ett team
  //✓Team       | Hämta alla team
  //✓UserTeam   | Lägga till en User till ett team

  @GET
  public Response

  @GET
  @Path("{teamNumber}")
  public Response getUser(@PathParam("teamNumber") final long teamNumber) {
    try {
      Team team = itsRepository.getTeam(teamNumber);
      return Response.ok(team).build();
    } catch (ITSRepositoryException e) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(NO_TEAM_WITH_TEAM_NUMBER + teamNumber).build();
    }
  }

  //✓Team       | Skapa ett team
  @POST
  public Response createTeam(final Team team) {
    if (team == null) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(BAD_REQUEST_NULL_OR_INVALID).build();
    }

    try {
      itsRepository.addTeam(team);
    } catch (ITSRepositoryException e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(BAD_REQUEST_NULL_OR_INVALID).build();
    }

    final URI location = uriInfo.getAbsolutePathBuilder().path(team.getNumber().toString()).build();
    return Response.created(location).build();
  }


}
