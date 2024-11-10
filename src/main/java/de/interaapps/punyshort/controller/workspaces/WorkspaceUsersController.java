package de.interaapps.punyshort.controller.workspaces;

import de.interaapps.punyshort.controller.HttpController;
import de.interaapps.punyshort.exceptions.AnotherAdminNeededException;
import de.interaapps.punyshort.exceptions.NotFoundException;
import de.interaapps.punyshort.exceptions.PermissionsDeniedException;
import de.interaapps.punyshort.helper.RequestHelper;
import de.interaapps.punyshort.model.database.AccessToken;
import de.interaapps.punyshort.model.database.User;
import de.interaapps.punyshort.model.database.domains.Domain;
import de.interaapps.punyshort.model.database.workspaces.Workspace;
import de.interaapps.punyshort.model.database.workspaces.WorkspaceUser;
import de.interaapps.punyshort.model.requests.workspaces.AddDomainRequest;
import de.interaapps.punyshort.model.requests.workspaces.CreateWorkspaceInvitationRequest;
import de.interaapps.punyshort.model.requests.workspaces.EditWorkspaceUserRequest;
import de.interaapps.punyshort.model.responses.ActionResponse;
import de.interaapps.punyshort.model.responses.PaginatedResponse;
import de.interaapps.punyshort.model.responses.PaginationData;
import de.interaapps.punyshort.model.responses.domains.DomainResponse;
import de.interaapps.punyshort.model.responses.workspaces.WorkspaceUserResponse;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.router.annotation.PathPrefix;
import org.javawebstack.httpserver.router.annotation.With;
import org.javawebstack.httpserver.router.annotation.params.Attrib;
import org.javawebstack.httpserver.router.annotation.params.Body;
import org.javawebstack.httpserver.router.annotation.params.Path;
import org.javawebstack.httpserver.router.annotation.verbs.Delete;
import org.javawebstack.httpserver.router.annotation.verbs.Get;
import org.javawebstack.httpserver.router.annotation.verbs.Post;
import org.javawebstack.httpserver.router.annotation.verbs.Put;
import org.javawebstack.orm.query.Query;

import java.util.stream.Collectors;

@PathPrefix("/v1/workspaces/{id}/users")
public class WorkspaceUsersController extends HttpController {
    @Get
    @With("auth")
    public PaginatedResponse<WorkspaceUserResponse> getAll(Exchange exchange, @Path("id") String id, @Attrib("user") User user, @Attrib("token") AccessToken accessToken) {
        accessToken.checkPermission("workspaces.users:read");

        Workspace workspace = Workspace.getByIdOrFail(id);

        WorkspaceUser workspaceUser = workspace.getUserOrFail(user.id);

        if (workspaceUser.role != WorkspaceUser.Role.ADMIN)
            throw new PermissionsDeniedException();

        Query<WorkspaceUser> workspaceDomainsQuery = WorkspaceUser.getByWorkspace(workspace.id);

        RequestHelper.defaultNavigation(exchange, workspaceDomainsQuery);
        RequestHelper.orderBy(workspaceDomainsQuery, exchange, "created_at", false);

        PaginationData pagination = RequestHelper.pagination(workspaceDomainsQuery, exchange);
        return new PaginatedResponse<>(workspaceDomainsQuery.all().stream().map(WorkspaceUserResponse::new).collect(Collectors.toList()), pagination);
    }

    @Post
    @With("auth")
    public ActionResponse inviteUser(@Path("id") String id, @Body CreateWorkspaceInvitationRequest request, @Attrib("user") User user, @Attrib("token") AccessToken accessToken) {
        Workspace workspace = Workspace.getById(id);
        accessToken.checkPermission("workspaces.users:write");

        if (workspace.getUser(user.id).role != WorkspaceUser.Role.ADMIN)
            throw new PermissionsDeniedException();

        User requestUser = User.getByMail(request.email);

        if (requestUser == null)
            return new ActionResponse(true);

        workspace.addUser(requestUser, request.role, WorkspaceUser.State.INVITED);

        return new ActionResponse(true);
    }

    @Put("/{userId}")
    @With("auth")
    public ActionResponse editUser(@Path("id") String id, @Path("userId") String userId, @Body EditWorkspaceUserRequest request, @Attrib("user") User user, @Attrib("token") AccessToken accessToken) {
        accessToken.checkPermission("workspaces.users:write");

        Workspace workspace = Workspace.getByIdOrFail(id);
        WorkspaceUser workspaceUser = workspace.getUserOrFail(user.id);

        if (workspaceUser.role != WorkspaceUser.Role.ADMIN)
            throw new PermissionsDeniedException();

        WorkspaceUser editWorkspaceUser = workspace.getUserOrFail(userId);
        if (request.role != null)
            editWorkspaceUser.role = request.role;

        editWorkspaceUser.save();

        return new ActionResponse(true);
    }

    @Delete("/{userId}")
    @With("auth")
    public ActionResponse removeUser(@Path("id") String id, @Path("userId") String userId, @Attrib("user") User user, @Attrib("token") AccessToken accessToken) {
        accessToken.checkPermission("workspaces.users:write", "workspaces.invitations:write");

        Workspace workspace = Workspace.getByIdOrFail(id);
        User requestUser = User.getByIdOrFail(userId);

        if (workspace.getUserOrFail(user.id, false).role != WorkspaceUser.Role.ADMIN && !requestUser.id.equals(user.id))
            throw new PermissionsDeniedException();

        WorkspaceUser deleteUser = workspace.getUserOrFail(userId, false);

        if (deleteUser.role == WorkspaceUser.Role.ADMIN && workspace.getUsers().stream().filter(u -> u.role == WorkspaceUser.Role.ADMIN).count() == 1) {
            throw new AnotherAdminNeededException();
        }

        workspace.removeUser(userId);

        return new ActionResponse(true);
    }


    @Post("/{userId}/accept")
    @With("auth")
    public ActionResponse acceptInvitation(@Path("id") String id, @Path("userId") String userId, @Attrib("user") User user, @Attrib("token") AccessToken accessToken) {
        accessToken.checkPermission("workspaces.invitations:write");

        Workspace workspace = Workspace.getByIdOrFail(id);
        User requestUser = User.getByIdOrFail(userId);

        if (!requestUser.id.equals(user.id))
            throw new PermissionsDeniedException();

        WorkspaceUser acceptUser = workspace.getUserOrFail(userId, false);
        acceptUser.state = WorkspaceUser.State.ACCEPTED;
        acceptUser.save();

        return new ActionResponse(true);
    }
}
