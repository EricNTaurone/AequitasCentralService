package com.aequitas.aequitascentralservice.tenancy.datasource;

import com.aequitas.aequitascentralservice.domain.value.CurrentUser;
import com.aequitas.aequitascentralservice.tenancy.TenantContextHolder;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.AbstractDataSource;

/**
 * Wraps the primary {@link DataSource} to set tenant-specific PostgreSQL GUCs on checkout and reset
 * them on close.
 */
public class TenantAwareDataSource extends AbstractDataSource {

    private final DataSource delegate;

    public TenantAwareDataSource(final DataSource delegate) {
        this.delegate = delegate;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return wrap(delegate.getConnection());
    }

    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        return wrap(delegate.getConnection(username, password));
    }

    private Connection wrap(final Connection connection) throws SQLException {
        final CurrentUser user = TenantContextHolder.getCurrentUser();
        if (user == null) {
            return connection;
        }
        try {
            setSessionVariables(connection, user);
            return (Connection)
                    Proxy.newProxyInstance(
                            connection.getClass().getClassLoader(),
                            new Class<?>[] {Connection.class},
                            new ResetOnCloseHandler(connection));
        } catch (SQLException ex) {
            connection.close();
            throw ex;
        }
    }

    private void setSessionVariables(final Connection connection, final CurrentUser user)
            throws SQLException {
        try (var statement = connection.prepareStatement("SELECT set_config(?, ?, false)")) {
            statement.setString(1, "app.current_firm_id");
            statement.setString(2, user.firmId().toString());
            statement.execute();

            statement.setString(1, "app.current_user_id");
            statement.setString(2, user.userId().toString());
            statement.execute();

            statement.setString(1, "app.current_role");
            statement.setString(2, user.role().name());
            statement.execute();
        }
    }

    private static final class ResetOnCloseHandler implements InvocationHandler {

        private final Connection target;

        private ResetOnCloseHandler(final Connection target) {
            this.target = target;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args)
                throws Throwable {
            if ("close".equals(method.getName())) {
                try {
                    resetSessionVariables();
                } catch (SQLException ex) {
                    try {
                        method.invoke(target, args);
                    } catch (Throwable t) {
                        ex.addSuppressed(t);
                    }
                    throw ex;
                }
            }
            return method.invoke(target, args);
        }

        private void resetSessionVariables() throws SQLException {
            try (var statement = target.createStatement()) {
                statement.execute("RESET app.current_firm_id");
                statement.execute("RESET app.current_user_id");
                statement.execute("RESET app.current_role");
            }
        }
    }
}
