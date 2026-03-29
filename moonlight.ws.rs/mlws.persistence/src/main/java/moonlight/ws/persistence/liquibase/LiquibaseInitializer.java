package moonlight.ws.persistence.liquibase;

import java.sql.Connection;

import javax.sql.DataSource;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.RequestScoped;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

@RequestScoped
public class LiquibaseInitializer implements Runnable {

    private static final Contexts NO_CONTEXTS = null;

    @Resource(lookup = "java:jboss/datasources/MoonlightDS")
    private DataSource dataSource;

    @Override
    public void run() {
        try {
            applyChangeLog();
        } catch (Exception x) {
            throw new RuntimeException(x);
        }
    }

    private void applyChangeLog() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            try (Liquibase liquibase = new Liquibase("liquibase/changelog_master.xml", new ClassLoaderResourceAccessor(getClass().getClassLoader()), database)) {
                liquibase.update(NO_CONTEXTS);
            }
        }
    }
}
