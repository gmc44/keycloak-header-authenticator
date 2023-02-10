<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
<#if section = "form">
  <script>
    window.onload = function() {
      /* onload redirect on "reset-login" href */
      const resetLogin = document.getElementById("reset-login");
      if ( resetLogin ) {
        /* prevent loop */
        const currentURL = new URL(window.location.href); /* https://keycloak.your.domain/auth/realms/yourrealm/protocol/openid-connect/auth */
        const currentURLPath = currentURL.pathname.split("/"); /* (auth,realms,yourrealm,protocol,openid-connect,auth) */
        currentURLPath.pop();                                  /* (auth,realms,yourrealm,protocol,openid-connect) */
        const currentURLshortPath = currentURLPath.join("/");  /* /auth/realms/yourrealm/protocol/openid-connect */

        const restartLoginURL = new URL(resetLogin); /* https://keycloak.your.domain/auth/realms/yourrealm/login-actions/authenticate */
        const restartLoginURLPath = restartLoginURL.pathname.split("/"); /* (auth,realms,yourrealm,login-actions,authenticate) */
        restartLoginURLPath.pop();                                       /* (auth,realms,yourrealm,login-actions) */
        const restartLoginURLshortPath = restartLoginURLPath.join("/");  /* /auth/realms/yourrealm/login-actions */

        if ( restartLoginURL && currentURLshortPath != restartLoginURLshortPath ) {
          /* redirect */
          window.location = restartLoginURL;
        }
      }
    }
  </script>
  </#if>
</@layout.registrationLayout>