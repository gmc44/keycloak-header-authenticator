package com.alliander.keycloak.authenticator;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
// import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;

/**
 * Created by joris on 25/11/2016.
 * Updated by gmc44 on 15/06/2021 : get username from http header
 */

public class KeycloakHeaderAuthenticator implements Authenticator {

    private static Logger logger = Logger.getLogger(KeycloakHeaderAuthenticator.class);

    public static final String CREDENTIAL_TYPE = "http-header_validation";

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        logger.debug("authenticate called ... context = " + context);

        //Get HeaderName
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        String headerName = config.getConfig().get(HDRAuthenticatorContstants.CONF_PRP_HEADER_NAME);
        if(headerName == null) {
            accessDenied(context, "Failed to get header config, empty ?");
            return;
        }
        logger.debug("HeaderName = " + headerName);

        //Get User from Header
        String username = null;
        try {
            username = context.getHttpRequest().getHttpHeaders().getHeaderString(headerName);
        } catch (NullPointerException npe) {
            accessDenied(context, "Failed to read header");
            return;
        }
        logger.debug("User found from Header = " + username);

        //Set User in Keycloak Context
        UserModel user = KeycloakModelUtils.findUserByNameOrEmail(context.getSession(), context.getRealm(), username);
        if(user == null) {
            accessDenied(context, "Failed to get ldap user from header (" + username + ")");
            logger.warn("Failed to get ldap user from header = " + username);
            return;
        } else {
            logger.debug("Keycloak User found from Header = " + username);
            context.setUser(user);
            context.success();
        }
    }

    private void accessDenied(AuthenticationFlowContext context, String reason) {
        logger.warn("Access denied : " + reason);
        context.failure(AuthenticationFlowError.UNKNOWN_USER);
        context.clearUser();
    }

    public void action(AuthenticationFlowContext context) {
        // Nothing here
    }

    public boolean requiresUser() {
        // We don't need a User to check if an HTTP header is passed or not
        // return false;
        return false;
    }

    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        // This method should not be called (because requiresUser returns false).
        return true;
    }

    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        logger.debug("setRequiredActions called ... session=" + session + ", realm=" + realm + ", user=" + user);
    }

    public void close() {
        logger.debug("close called ...");
    }


}