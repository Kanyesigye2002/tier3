package com.tier3.tier3graphql.config.security;

import graphql.ErrorClassification;
import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.language.SourceLocation;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class BadCredentialsException extends RuntimeException implements GraphQLError {
    private static final long serialVersionUID = 4129146858129498534L;

    @Override
    public String getMessage() {
        return "Email or password didn''t match";
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
