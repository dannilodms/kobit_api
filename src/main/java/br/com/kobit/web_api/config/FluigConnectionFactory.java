package br.com.kobit.web_api.config;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public final class FluigConnectionFactory {
	private FluigConnectionFactory() {
	}

	public static Connection getConnection() throws SQLException, NamingException {
		Context initialContext = new InitialContext();
		DataSource dataSource = (DataSource) initialContext.lookup("java:/jdbc/AppDS");
		return dataSource.getConnection();
	}
}
