# keycloak-header-authenticator

To install the HTTP Header Authenticator one has to:

* Add the jar to the Keycloak server:
`cp target/keycloak-header-authenticator.jar _KEYCLOAK_HOME_/providers/`

* Add the template to the Keycloak server:
`cp templates/hdr-validation.ftl _KEYCLOAK_HOME_/themes/base/login/`

Configure your REALM to use the HTTP Header Authentication.
First create a new REALM (or select a previously created REALM).

Under Authentication > Flows:
* Copy 'Browse' flow to 'Browser with Header check' flow
* Click on 'Actions > Add execution on the 'Browser with Header check' line and add the 'HTTP Header Authentication'
* Set 'HTTP Header Authentication' to 'REQUIRED' or 'ALTERNATIVE'

Under Authentication > Bindings:
* Select 'Browser with Header check' as the 'Browser Flow' for the REALM.
