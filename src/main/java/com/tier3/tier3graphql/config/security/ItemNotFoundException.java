package com.tier3.tier3graphql.config.security;

import graphql.ErrorClassification;
import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.language.SourceLocation;

import java.util.List;

public class ItemNotFoundException extends RuntimeException implements GraphQLError {
    private static final long serialVersionUID = 158136221282852553L;

    @Override
    public String getMessage() {
        return "Item not found";
    }

    @Override
    public List<SourceLocation> getLocations() {
        return null;
    }

    @Override
    public ErrorClassification getErrorType() {
        return ErrorType.ValidationError;
    }
}
