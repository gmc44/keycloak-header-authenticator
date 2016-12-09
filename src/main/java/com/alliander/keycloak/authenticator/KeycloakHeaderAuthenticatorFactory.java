package com.alliander.keycloak.authenticator;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.ConfigurableAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joris on 25/11/2016.
 */
public class KeycloakHeaderAuthenticatorFactory implements AuthenticatorFactory, ConfigurableAuthenticatorFactory {

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
        property.setName(HDRAuthenticatorContstants.CONF_PRP_HEADER_REQ_VALUE);
        property.setLabel("Required value of the HTTP header");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Only when the value of the specified HTTP HEADER matches the value, the flow will continue.");
        configProperties.add(property);
    }

    public String getId() {
        logger.info("getId called ... returning " + PROVIDER_ID);
        return PROVIDER_ID;
    }

    public Authenticator create(KeycloakSession session) {
        logger.info("create called ... returning " + SINGLETON);
        return SINGLETON;
    }

    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        logger.info("getRequirementChoices called ... returning " + REQUIREMENT_CHOICES);
        return REQUIREMENT_CHOICES;
    }

    public boolean isUserSetupAllowed() {
        logger.info("isUserSetupAllowed called ... returning false");
        return false;
    }

    public boolean isConfigurable() {
        boolean result = true;
        logger.info("isConfigurable called ... returning " + result);
        return result;
    }

    public String getHelpText() {
        logger.info("getHelpText called ...");
        return "Validates if a required HTTP HEADER has the correct value.";
    }

    public String getDisplayType() {
        String result = "HTTP Header Authentication";
        logger.info("getDisplayType called ... returning " + result);
        return result;
    }

    public String getReferenceCategory() {
        logger.info("getReferenceCategory called ... returning http-header");
        return "http-header";
    }

    public List<ProviderConfigProperty> getConfigProperties() {
        logger.info("getConfigProperties called ... returning " + configProperties);
        return configProperties;
    }

    public void init(Config.Scope config) {
        logger.info("init called ... config.scope = " + config);
    }

    public void postInit(KeycloakSessionFactory factory) {
        logger.info("postInit called ... factory = " + factory);
    }

    public void close() {
        logger.info("close called ...");
    }
}
