package de.interaapps.punyshort.controller.workspaces;

import de.interaapps.punyshort.controller.HttpController;
import de.interaapps.punyshort.exceptions.NotFoundException;
import de.interaapps.punyshort.exceptions.PermissionsDeniedException;
import de.interaapps.punyshort.exceptions.WorkspaceSlugInvalidException;
import de.interaapps.punyshort.exceptions.WorkspaceTakenException;
import de.interaapps.punyshort.helper.RequestHelper;
import de.interaapps.punyshort.model.database.AccessToken;
import de.interaapps.punyshort.model.database.ShortenLink;
import de.interaapps.punyshort.model.database.User;
import de.interaapps.punyshort.model.database.domains.Domain;
import de.interaapps.punyshort.model.database.workspaces.Workspace;
import de.interaapps.punyshort.model.database.workspaces.WorkspaceDomain;
import de.interaapps.punyshort.model.database.workspaces.WorkspaceUser;
import de.interaapps.punyshort.model.requests.workspaces.AddDomainRequest;
import de.interaapps.punyshort.model.requests.workspaces.CreateWorkspaceInvitationRequest;
import de.interaapps.punyshort.model.requests.workspaces.CreateWorkspaceRequest;
import de.interaapps.punyshort.model.responses.ActionResponse;
import de.interaapps.punyshort.model.responses.PaginatedResponse;
import de.interaapps.punyshort.model.responses.PaginationData;
import de.interaapps.punyshort.model.responses.domains.DomainResponse;
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

@PathPrefix("/v1/workspaces/{id}/domains")
public class WorkspaceDomainsController extends HttpController {
    @Get
    @With("auth")
    public PaginatedResponse<DomainResponse> getAll(Exchange exchange, @Path("id") String id, @Attrib("user") User user, @Attrib("token") AccessToken accessToken) {
        accessToken.checkPermission("workspaces.domains:read");

        Workspace workspace = Workspace.getById(id);

        if (workspace == null) throw new NotFoundException();

        WorkspaceUser workspaceUser = workspace.getUser(user.id);

        if (workspaceUser == null || workspaceUser.role != WorkspaceUser.Role.ADMIN)
            throw new PermissionsDeniedException();

        Query<Domain> workspaceDomainsQuery = Domain.getByWorkspace(workspace.id, user);

        RequestHelper.defaultNavigation(exchange, workspaceDomainsQuery);
        RequestHelper.orderBy(workspaceDomainsQuery, exchange, "created_at", false);

        PaginationData pagination = RequestHelper.pagination(workspaceDomainsQuery, exchange);
        return new PaginatedResponse<>(workspaceDomainsQuery.all().stream().map(d -> new DomainResponse(d, false)).collect(Collectors.toList()), pagination);
    }

    @Post
    @With("auth")
    public DomainResponse add(@Body AddDomainRequest request, @Path("id") String id, @Attrib("user") User user, @Attrib("token") AccessToken accessToken) {
        accessToken.checkPermission("workspaces.domains:write");

        Workspace workspace = Workspace.getById(id);
        if (workspace == null) throw new NotFoundException();

        WorkspaceUser workspaceUser = workspace.getUser(user.id);

        if (workspaceUser == null || workspaceUser.role != WorkspaceUser.Role.ADMIN)
            throw new PermissionsDeniedException();

        Domain domain = Domain.get(request.domainId);
        if (domain == null) throw new NotFoundException();

        if (!domain.isPublic && domain.getUser(user.id) == null)
            throw new PermissionsDeniedException();

        workspace.addDomain(domain);

        return new DomainResponse(domain, false);
    }

    @Delete("/{domainId}")
    @With("auth")
    public ActionResponse delete(@Path("id") String id, @Path("domainId") String domainId, @Attrib("token") AccessToken accessToken, @Attrib("user") User user) {
        accessToken.checkPermission("workspaces.domains:delete");

        Workspace workspace = Workspace.getById(id);
        if (workspace == null) throw new NotFoundException();

        WorkspaceUser workspaceUser = workspace.getUser(user.id);

        if (workspaceUser == null || workspaceUser.role != WorkspaceUser.Role.ADMIN)
            throw new PermissionsDeniedException();

        Domain domain = Domain.get(domainId);
        if (domain == null) throw new NotFoundException();

        workspace.removeDomain(domain);

        return new ActionResponse(true);
    }
}
