package de.interaapps.punyshort.model.requests.workspaces;

import org.javawebstack.validator.Rule;

public class CreateWorkspaceRequest {
    @Rule({"string(2)", "required"})
    public String name;

    @Rule({"string(2)", "required"})
    public String slug;
}
