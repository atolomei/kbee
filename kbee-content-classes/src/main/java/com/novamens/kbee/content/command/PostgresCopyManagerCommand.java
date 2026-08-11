package com.novamens.kbee.content.command;

import com.novamens.beans.BeansService;
import com.novamens.content.command.CommandState;
import com.novamens.service.ServiceLocator;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 
 * 
 * parte de la lib del drive JDBC
 * 
 * Exportar tablas a CSV
 * Imporar de CSV a tabla
 * 
 *
 */
public class PostgresCopyManagerCommand extends AsyncCommand implements DBToolCommand {
    private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PostgresCopyManagerCommand.class.getName());

    private Long result = 0l;

    public PostgresCopyManagerCommand() {
        setDescription("Execute CopyManager commands. Parameters are: 'cmd' and 'filein' or 'fileout'.");
        setName("Postgres CopyManager Command");
    }

    @Override
    protected void executeAsync() {
        try {
            validateParameters();
            final SessionFactory open = com.novamens.hibernate.session.Session.open();
            result = 0l;
            logger.info("Executing PostgresCopyManagerCommand.");

            getSession().getTransaction().begin();
            result = getSession().doReturningWork(connection -> {
                try {
                    Long res = null;
                    CopyManager cm = PostgresCopyManagerCommand.this.getCopyManagerInstance(connection);
                    String copyCommand = PostgresCopyManagerCommand.this.getCmd();

                    if (PostgresCopyManagerCommand.this.getFilein() != null) {
                        try (Reader reader = new FileReader(PostgresCopyManagerCommand.this.getFilein())) {
                            res = cm.copyIn(copyCommand, reader);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else if (PostgresCopyManagerCommand.this.getFileout() != null) {
                        try (Writer writer = new FileWriter(PostgresCopyManagerCommand.this.getFileout())) {
                            res = cm.copyOut(copyCommand, writer);
                        }
                    }
                    return res;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            getSession().getTransaction().commit();
            logger.info("Command ended successfully.");
            setState(CommandState.COMPLETED);

        } catch (Exception e) {
            logger.error(e);
            setState(CommandState.ERROR);
            setResultComments(e.getClass().getSimpleName() + "| " + e.getMessage());
        } finally {
            com.novamens.hibernate.session.Session.close();
        }
    }

    @Override
    public long getTotalItemsProcessed() {
        return result;
    }

    @Override
    public long getTotalItems() {
        return result;
    }

    private CopyManager getCopyManagerInstance(Connection connection) throws SQLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return new CopyManager(connection.unwrap(BaseConnection.class));
    }

    private void validateParameters() {
        if (getCmd() == null || getCmd().isEmpty()) {
            throw new RuntimeException("Parameter 'cmd' not set.");
        }
        if ((getFilein() == null || getFilein().isEmpty()) && (getFileout() == null || getFileout().isEmpty())) {
            throw new RuntimeException("Parameter 'filein' or 'fileout' must be set.");
        }

        if (getFilein() != null && getFileout() != null) {
            throw new RuntimeException("Parameter 'filein' and 'fileout' must not be set at the same time.");
        }
    }


    protected Session getSession() {
        BeansService beans = ServiceLocator.getService(BeansService.class);
        SessionFactory sf = (SessionFactory) beans.getBean("sessionFactory");
        return sf.getCurrentSession();
    }

    public String getCmd() {
        return (String) this.getParameters().get("cmd");
    }

    public void setCmd(String cmd) {
        this.getParameters().put("cmd", cmd);
    }

    public String getFilein() {
        return (String) this.getParameters().get("filein");
    }

    public void setFilein(String filein) {
        this.getParameters().put("filein", filein);
    }

    public String getFileout() {
        return (String) this.getParameters().get("fileout");
    }

    public void setFileout(String fileout) {
        this.getParameters().put("fileout", fileout);
    }
}
