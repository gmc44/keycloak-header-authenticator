package com.alliander.keycloak.authenticator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.events.Details;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * Created by joris on 25/11/2016.
 * Updated by gmc44 on 15/06/2021 : get username from http header
 * Updated by gmc44 on 13/05/2022 : check context user VS header user
 */

public class KeycloakHeaderAuthenticator implements Authenticator {

    private static Logger logger = Logger.getLogger(KeycloakHeaderAuthenticator.class);

    public static final String CREDENTIAL_TYPE = "http-header_validation";

    @Override
    public void authenticate(final AuthenticationFlowContext context) {

        //Get HeaderName
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        String headerName = config.getConfig().get(HDRAuthenticatorContstants.CONF_PRP_HEADER_NAME);
        String headersToNotes = config.getConfig().get(HDRAuthenticatorContstants.CONF_PRP_HEADER_COPYHEADERSTONOTES);
        if(headerName == null) {
            accessDenied(context, AuthenticationFlowError.UNKNOWN_USER, "Failed to get header config, empty ?");
        } else {
            logger.debug("HeaderName = " + headerName);

            //Get User from Header
            String headerusername;
            try{
               headerusername = context.getHttpRequest().getHttpHeaders().getHeaderString(headerName);
            } catch (NullPointerException npe) {
               headerusername = null;
            }
            
            if(headerusername == null) {
                accessDenied(context, AuthenticationFlowError.UNKNOWN_USER, "Failed to read header");
            } else {
                logger.debug("User found from Header = " + headerusername);

                //Set HeaderUserModel from HeaderUserName
                UserModel headerusermodel = KeycloakModelUtils.findUserByNameOrEmail(context.getSession(), context.getRealm(), headerusername);
                if(headerusermodel == null) {
                    logger.warn("Failed to get ldap user from header = " + headerusername);
                    accessDenied(context, AuthenticationFlowError.UNKNOWN_USER, "Failed to get ldap user from header (" + headerusername + ")");
                } else {
                    logger.debug("Keycloak User found from Header = " + headerusername);

                    //Check if there is already a user in context : Try to get Current User in KEYCLOAK_IDENTITY Cookie
                    String currentusername = "nocurrentuser";
                    UserModel currentusermodel = context.getUser();
                    try {
                        currentusername = currentusermodel.getUsername();
                    } catch (NullPointerException npe) {
                        logger.debug("No Current Keycloak User found");
                    }
                    
                    logger.debug("Current Keycloak User = "+currentusername);
                    
                    // Check if current user is the same as header user
                    if (currentusername.equals("nocurrentuser") || currentusername.equals(headerusername)) {

                        // Set User
                        context.setUser(headerusermodel);

                        // Copy Headers to UserSessionNotes
                        if (headersToNotes != null) {
                            for (String headerToNoteName : new ArrayList<String>(Arrays.asList(headersToNotes.split("##")))) {
                                String headerToNoteValue = context.getHttpRequest().getHttpHeaders().getHeaderString(headerToNoteName);
                                context.getAuthenticationSession().setUserSessionNote(headerToNoteName,headerToNoteValue);
                                logger.debug("Header " + headerToNoteName + " = " + headerToNoteValue);
                            }
                        }
                        
                        // Remember Me by default
                        context.getAuthenticationSession().setAuthNote(Details.REMEMBER_ME, "true");
	                    context.getEvent().detail(Details.REMEMBER_ME, "true");
                        
                        // Set Success
                        context.success();
                    } else {
                        // Conflict Context User != Header User
                        String msg = "User Cookie ("+ currentusername +") is different from User Header (" + headerusername + "), clearing "+ currentusername +" session... ";
                        KeycloakSession keycloakSession = context.getSession();
                        AuthenticationManager.AuthResult authResult = AuthenticationManager.authenticateIdentityCookie(keycloakSession,context.getRealm(), true);
                        if (authResult != null) {
                            // Removing current user session
                            AuthenticationManager.backchannelLogout(keycloakSession, authResult.getSession(), true);
                            logger.debug(msg);
                        } else {
                            logger.warn(msg+"Failed to logout User");
                        }
                        accessDenied(context, AuthenticationFlowError.USER_CONFLICT, msg);
                        // context.resetFlow();
                    }
                }
            }
        }        
    }

    private void accessDenied(final AuthenticationFlowContext context, AuthenticationFlowError autherror, String reason) {
        logger.warn("Access denied : " + reason);
        context.failure(autherror, errorResponse(context, autherror));
    }

    private Response errorResponse(AuthenticationFlowContext context, AuthenticationFlowError errormsg) {
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        return context.form()
            .setError(errormsg.toString(), authSession.getClient().getClientId())
            .createErrorPage(Response.Status.FORBIDDEN);
    }

    public void action(AuthenticationFlowContext context) {
        //nothing here   
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