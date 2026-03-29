package moonlight.ws.persistence.init;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

@RequestScoped
public class DataInitializer implements Runnable {

    @Inject
    private UserInitializer userInitializer;

    @Override
    public void run() {
        userInitializer.run();
    }
}
