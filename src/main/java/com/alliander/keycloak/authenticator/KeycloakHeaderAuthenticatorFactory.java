package com.alliander.keycloak.authenticator;

import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * Created by joris on 25/11/2016.
 */
public class KeycloakHeaderAuthenticatorFactory implements AuthenticatorFactory {

    public static final String PROVIDER_ID = "hdr-authentication";

    private static Logger logger = Logger.getLogger(KeycloakHeaderAuthenticatorFactory.class);
    private static final KeycloakHeaderAuthenticator SINGLETON = new KeycloakHeaderAuthenticator();


    public static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.ALTERNATIVE,
            AuthenticationExecutionModel.Requirement.DISABLED};

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(HDRAuthenticatorContstants.CONF_PRP_HEADER_NAME);
        property.setLabel("HTTP header to check");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("This HTTP HEADER must be present with a specified value, otherwise the flow will terminate.");
        configProperties.add(property);
        property = new ProviderConfigProperty();
        property.setName(HDRAuthenticatorContstants.CONF_PRP_HEADER_COPYHEADERSTONOTES);
        property.setLabel("Headers to copy to UserSessionNotes");
        property.setType(ProviderConfigProperty.MULTIVALUED_STRING_TYPE);
        property.setHelpText("List of headers that will be copied to UserSessionNotes and can be used in client scope mapper");
        configProperties.add(property);
    }

    public String getId() {
        logger.debug("getId called ... returning " + PROVIDER_ID);
        return PROVIDER_ID;
    }

    public Authenticator create(KeycloakSession session) {
        logger.debug("create called ... returning " + SINGLETON);
        return SINGLETON;
    }

    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        logger.debug("getRequirementChoices called ... returning " + REQUIREMENT_CHOICES);
        return REQUIREMENT_CHOICES;
    }

    public boolean isUserSetupAllowed() {
        logger.debug("isUserSetupAllowed called ... returning false");
        return false;
    }

    public boolean isConfigurable() {
        boolean result = true;
        logger.debug("isConfigurable called ... returning " + result);
        return result;
    }

    public String getHelpText() {
        logger.debug("getHelpText called ...");
        return "Validates if a required HTTP HEADER has the correct value.";
    }

    public String getDisplayType() {
        String result = "HTTP Header Authentication";
        logger.debug("getDisplayType called ... returning " + result);
        return result;
    }

    public String getReferenceCategory() {
        logger.debug("getReferenceCategory called ... returning http-header");
        return "http-header";
    }

    public List<ProviderConfigProperty> getConfigProperties() {
        logger.debug("getConfigProperties called ... returning " + configProperties);
        return configProperties;
    }

    public void init(Config.Scope config) {
        logger.debug("init called ... config.scope = " + config);
    }

    public void postInit(KeycloakSessionFactory factory) {
        logger.debug("postInit called ... factory = " + factory);
    }

    public void close() {
        logger.debug("close called ...");
    }
}