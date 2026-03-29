package moonlight.ws.business.rest.impl;

import jakarta.ejb.Stateless;
import moonlight.ws.api.WelcomeRest;

@Stateless
public class WelcomeRestImpl implements WelcomeRest {

    @Override
    public String getWelcomePage() {
        return """
                <html>
                    <head>
                        <title>mlws.webservice</title>
                    </head>
                    <body>
                        Welcome to the <b>mlws.webservice</b>!
                    </body>
                </html>
                """;
    }

}
