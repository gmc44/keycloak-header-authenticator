package com.alliander.keycloak.authenticator;

import java.util.ArrayList;
import java.util.Arrays;

import javax.ws.rs.core.Response;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.services.ServicesLogger;

/**
 * Created by joris on 25/11/2016.
 * Updated by gmc44 on 15/06/2021 : get username from http header
 * Updated by gmc44 on 13/05/2022 : check context user VS header user
 */

public class KeycloakHeaderAuthenticator implements Authenticator {

    // logger using keyloak.services.ServicesLogger
    private static final ServicesLogger logger = ServicesLogger.LOGGER;

    String Module = "HeaderAuth : ";

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
            logger.debug(Module+"HeaderName = " + headerName);

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
                logger.debug(Module+"User found from Header = " + headerusername);

                //Set HeaderUserModel from HeaderUserName
                UserModel headerusermodel = KeycloakModelUtils.findUserByNameOrEmail(context.getSession(), context.getRealm(), headerusername);
                if(headerusermodel == null) {
                    logger.warn(Module+"Failed to get ldap user from header = " + headerusername);
                    accessDenied(context, AuthenticationFlowError.UNKNOWN_USER, "Failed to get ldap user from header (" + headerusername + ")");
                } else {
                    logger.debug(Module+"Keycloak User found from Header = " + headerusername);

                    //Check if there is already a user in context : Try to get Current User in KEYCLOAK_IDENTITY Cookie
                    String currentusername = "nocurrentuser";
                    UserModel currentusermodel = context.getUser();
                    try {
                        currentusername = currentusermodel.getUsername();
                    } catch (NullPointerException npe) {
                        logger.debug(Module+"No Current Keycloak User found");
                    }
                    
                    logger.debug(Module+"Current Keycloak User = "+currentusername);
                    
                    // Check if current user is the same as header user
                    if (currentusername.equals("nocurrentuser") || currentusername.equalsIgnoreCase(headerusername)) {

                        
                        // Set User
                        if (currentusername.equals("nocurrentuser")) {
                            context.setUser(headerusermodel);
                        } else {
                            context.setUser(currentusermodel);
                        }


                        // Copy Headers to UserSessionNotes
                        if (headersToNotes != null) {
                            for (String headerToNoteName : new ArrayList<String>(Arrays.asList(headersToNotes.split("##")))) {
                                String headerToNoteValue = context.getHttpRequest().getHttpHeaders().getHeaderString(headerToNoteName);
                                context.getAuthenticationSession().setUserSessionNote(headerToNoteName,headerToNoteValue);
                                logger.debug(Module+"Header " + headerToNoteName + " = " + headerToNoteValue);
                            }
                        }
                        
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
                            logger.debug(Module+msg);
                        } else {
                            logger.warn(Module+msg+"Failed to logout User");
                        }
                        accessDenied(context, AuthenticationFlowError.USER_CONFLICT, msg);
                    }
                }
            }
        }        
    }

    private void accessDenied(final AuthenticationFlowContext context, AuthenticationFlowError autherror, String reason) {
        logger.warn(Module+"Access denied : " + reason);
        context.failure(autherror, errorResponse(context, autherror));
    }

    private Response errorResponse(AuthenticationFlowContext context, AuthenticationFlowError errormsg) {
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        return context.form()
            .setError(errormsg.toString(), authSession.getClient().getClientId())
            .createForm("error.ftl");
    }

    public void action(AuthenticationFlowContext context) {
        //nothing here   
    }

    public boolean requiresUser() {
        // We don't need a User to check if an HTTP header is passed or not
        return false;
    }

    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        // This method should not be called (because requiresUser returns false).
        return true;
    }

    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        logger.debug(Module+"setRequiredActions called ... session=" + session + ", realm=" + realm + ", user=" + user);
    }

    public void close() {
        logger.debug(Module+"close called ...");
    }


}