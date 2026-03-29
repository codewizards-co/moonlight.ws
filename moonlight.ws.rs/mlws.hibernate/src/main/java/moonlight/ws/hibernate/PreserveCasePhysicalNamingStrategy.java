package moonlight.ws.hibernate;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class PreserveCasePhysicalNamingStrategy implements PhysicalNamingStrategy {

	@Override
	public Identifier toPhysicalCatalogName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
		return process(logicalName, jdbcEnvironment);
	}

	@Override
	public Identifier toPhysicalSchemaName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
		return process(logicalName, jdbcEnvironment);
	}

	@Override
	public Identifier toPhysicalTableName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
		return process(logicalName, jdbcEnvironment);
	}

	@Override
	public Identifier toPhysicalSequenceName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
		return process(logicalName, jdbcEnvironment);
	}

	@Override
	public Identifier toPhysicalColumnName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
		return process(logicalName, jdbcEnvironment);
	}

	protected Identifier process(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
		return logicalName == null ? null : Identifier.toIdentifier('"' + logicalName.getText() + '"');
	}
}
