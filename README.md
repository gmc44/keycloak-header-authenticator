# keycloak-header-authenticator

To install the HTTP Header Authenticator one has to:

* Git Clone
`git clone https://github.com/gcorgne/keycloak-header-authenticator.git`

* Maven package
`cd keycloak-header-authenticator;mvn package`

* Add the jar to the keycloak docker image volumes :
`- /apps/keycloak/themes/hdr-validation.ftl:/opt/jboss/keycloak/themes/base/login/hdr-validation.ftl`

* Add the template to the Keycloak docker image volumes :
`- /apps/keycloak/providers:/opt/jboss/keycloak/providers`

Configure your REALM to use the HTTP Header Authentication.
First create a new REALM (or select a previously created REALM).

Under Authentication > Flows:
* Copy 'Browse' flow to 'Browser with Header check' flow
* Click on 'Actions > Add execution on the 'Browser with Header check' line and add the 'HTTP Header Authentication'
* Set 'HTTP Header Authentication' to 'REQUIRED' or 'ALTERNATIVE'

Under Authentication > Bindings:
* Select 'Browser with Header check' as the 'Browser Flow' for the REALM.
