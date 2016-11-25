package com.alliander.keycloak.authenticator;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import javax.ws.rs.core.Response;

/**
 * Created by joris on 25/11/2016.
 */
public class KeycloakHeaderAuthenticator implements Authenticator {

    private static Logger logger = Logger.getLogger(KeycloakHeaderAuthenticator.class);

    public static final String CREDENTIAL_TYPE = "http-header_validation";

    public void authenticate(AuthenticationFlowContext context) {
        logger.info("authenticate called ... context = " + context);

        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        String headerName = config.getConfig().get(HDRAuthenticatorContstants.CONF_PRP_HEADER_NAME);
        String requiredValue = config.getConfig().get(HDRAuthenticatorContstants.CONF_PRP_HEADER_REQ_VALUE);

        if(headerName == null || requiredValue == null) {
            Response challenge =  context.form()
                    .setError("HTTP Header validator is not configured")
                    .createForm("hdr-validation.ftl");
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
            return;
        }

        String headerValue = null;
        try {
            headerValue = context.getHttpRequest().getHttpHeaders().getHeaderString(headerName);
        } catch (NullPointerException npe) {
            // ignore
        }

        if(headerValue == null || ! headerValue.equals(requiredValue)) {
            Response challenge =  context.form()
                    .setError("Missing required HTTP HEADER")
                    .createForm("hdr-validation.ftl");
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
            return;
        } else {
            context.success();
        }
    }


    public void action(AuthenticationFlowContext context) {
        logger.info("action called ... context = " + context);

    }



    public boolean requiresUser() {
        logger.info("requiresUser called ... returning false");
        return false;
    }

    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        logger.info("configuredFor called ... session=" + session + ", realm=" + realm + ", user=" + user);
        boolean result = true;
        logger.info("... returning "  +result);
        return result;
    }

    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        logger.info("setRequiredActions called ... session=" + session + ", realm=" + realm + ", user=" + user);
    }

    public void close() {
        logger.info("close called ...");
    }


}
