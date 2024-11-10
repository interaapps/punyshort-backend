package de.interaapps.punyshort.controller.workspaces;

import de.interaapps.punyshort.controller.HttpController;
import de.interaapps.punyshort.exceptions.*;
import de.interaapps.punyshort.helper.RequestHelper;
import de.interaapps.punyshort.model.database.AccessToken;
import de.interaapps.punyshort.model.database.ShortenLink;
import de.interaapps.punyshort.model.database.User;
import de.interaapps.punyshort.model.database.workspaces.Workspace;
import de.interaapps.punyshort.model.database.workspaces.WorkspaceDomain;
import de.interaapps.punyshort.model.database.workspaces.WorkspaceUser;
import de.interaapps.punyshort.model.requests.workspaces.CreateWorkspaceInvitationRequest;
import de.interaapps.punyshort.model.requests.workspaces.CreateWorkspaceRequest;
import de.interaapps.punyshort.model.responses.ActionResponse;
import de.interaapps.punyshort.model.responses.PaginatedResponse;
import de.interaapps.punyshort.model.responses.PaginationData;
import de.interaapps.punyshort.model.responses.workspaces.WorkspaceResponse;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.router.annotation.PathPrefix;
import org.javawebstack.httpserver.router.annotation.With;
import org.javawebstack.httpserver.router.annotation.params.Attrib;
import org.javawebstack.httpserver.router.annotation.params.Body;
import org.javawebstack.httpserver.router.annotation.params.Path;
import org.javawebstack.httpserver.router.annotation.verbs.Delete;
import org.javawebstack.httpserver.router.annotation.verbs.Get;
import org.javawebstack.httpserver.router.annotation.verbs.Post;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.query.Query;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

@PathPrefix("/v1/workspaces")
public class WorkspacesController extends HttpController {
    @Get
    @With("auth")
    public PaginatedResponse<WorkspaceResponse> getAll(Exchange exchange, @Attrib("user") User user, @org.javawebstack.httpserver.router.annotation.params.Query("invitations") String queryInvitations, @Attrib("token") AccessToken accessToken) {
        accessToken.checkPermission("workspaces:read");
        Query<Workspace> workspaceQuery = Repo.get(Workspace.class)
            .query();
        workspaceQuery.whereExists(WorkspaceUser.class, u ->
            u.where(WorkspaceUser.class, "workspaceId", "=", Workspace.class, "id")
                .where("userId", user.id)
                .where("state", queryInvitations != null && queryInvitations.equals("true") ? WorkspaceUser.State.INVITED : WorkspaceUser.State.ACCEPTED)
        );

        RequestHelper.defaultNavigation(exchange, workspaceQuery);
        RequestHelper.orderBy(workspaceQuery, exchange, "created_at", false);

        PaginationData pagination = RequestHelper.pagination(workspaceQuery, exchange);
        return new PaginatedResponse<>(workspaceQuery.all().stream().map(WorkspaceResponse::new).collect(Collectors.toList()), pagination);
    }

    @Post
    @With("auth")
    public WorkspaceResponse create(@Body CreateWorkspaceRequest request, @Attrib("user") User user, @Attrib("token") AccessToken accessToken) {
        accessToken.checkPermission("workspaces:write");

        request.slug = request.slug.toLowerCase();
        if (!Pattern.matches("^[A-Za-z0-9._-]*$", request.slug))
            throw new WorkspaceSlugInvalidException();

        if (Workspace.bySlug(request.name) != null)
            throw new WorkspaceTakenException();

        Workspace workspace = new Workspace();

        workspace.name = request.name;
        workspace.slug = request.slug;

        workspace.save();
        workspace.addUser(user, WorkspaceUser.Role.ADMIN, WorkspaceUser.State.ACCEPTED);

        return new WorkspaceResponse(workspace);
    }

    @Get("/{id}")
    public WorkspaceResponse get(@Path("id") String id, @Attrib("token") AccessToken accessToken, @Attrib("user") User user) {
        Workspace workspace = Workspace.getById(id);
        if (workspace == null)
            workspace = Workspace.bySlug(id);

        if (workspace == null)
            throw new NotFoundException();

        if (workspace.getUser(user.id) == null)
            throw new PermissionsDeniedException();

        accessToken.checkPermission("workspaces:read");

        return new WorkspaceResponse(workspace);
    }


    @Delete("/{id}")
    @With("auth")
    public ActionResponse delete(@Path("id") String id, @Attrib("token") AccessToken accessToken, @Attrib("user") User user) {
        Workspace workspace = Workspace.getByIdOrFail(id);

        accessToken.checkPermission("workspaces:delete");

        WorkspaceUser workspaceUser = workspace.getUserOrFail(user.id);

        if (workspaceUser.role != WorkspaceUser.Role.ADMIN)
            throw new PermissionsDeniedException();

        workspace.delete();

        Repo.get(ShortenLink.class).where("workspaceId", workspace.id).get().forEach(s -> {
            s.workspaceId = null;
            s.save();
        });
        Repo.get(WorkspaceUser.class).where("workspaceId", workspace.id).delete();
        Repo.get(WorkspaceDomain.class).where("workspaceId", workspace.id).delete();

        return new ActionResponse(true);
    }
}
