package com.novamens.content.web.admin.markup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.model.Model;

import com.novamens.beans.BeansService;
import com.novamens.kbee.wicket.markup.html.areainfo.AreaInfoPanel;
import com.novamens.kbee.wicket.markup.html.areainfo.GridInfoPanel;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.util.NumberFormatter;
import kbee.util.PropertiesFactory;
import kbee.util.Tuple;

public class SystemInfoDatabasePanel extends AbstractSystemInfoPanel {

    private static final long serialVersionUID = 1L;

    static private Logger logger = LogManager.getLogger(SystemInfoDatabasePanel.class.getName());

    public SystemInfoDatabasePanel() {
        this("info-panel");
    }

    public SystemInfoDatabasePanel(String id) {
        super(id);
    }

    /**
     */
    public void onInitialize() {
        super.onInitialize();

        AreaInfoPanel area = new AreaInfoPanel("info");
        add(area);

        area.addPanel(new GridInfoPanel("element", this.dbInfo(), getLabel("info"), true));
        area.addPanel(new GridInfoPanel("element", this.dbSizeInfo(), getLabel("size"), true));

        List<String> list = new ArrayList<String>();
        list.add("Context");
        list.add("Unit");
        list.add("Settings");
        list.add("Boot_Val");
        list.add("Reset_Val");

        List<String> relevant = new ArrayList<String>();
        relevant.add("checkpoint_segments");
        relevant.add("effective_cache_size");
        relevant.add("listen_addresses");
        relevant.add("max_connections");
        relevant.add("shared_buffers");
        relevant.add("wal_buffers");
        relevant.add("work_mem");

        relevant.add("maintenance_work_mem");
        relevant.add("checkpoint_completion_target");
        relevant.add("default_statistics_target");
        relevant.add("random_page_cost");
        relevant.add("min_wal_size");
        relevant.add("max_wal_size");
        relevant.add("max_worker_processes");
        relevant.add("max_parallel_workers_per_gather");
        relevant.add("max_parallel_workers");

        List<Tuple> dbs = dbSettingsInfo(relevant);

        if (dbs.size() > 0)
            area.addPanel(new GridInfoPanel("element", dbs, getLabel("configuration"), list, true));

        List<Tuple> dbsa = dbSettingsInfo();
        if (dbsa.size() > 0)
            area.addPanel(new GridInfoPanel("element", dbsa, getLabel("configuration_all"), list, true));
    }

    /**
     * @return
     */
    private List<Tuple> dbInfo() {

        long start = System.currentTimeMillis();

        List<Tuple> data = new ArrayList<Tuple>();

        try {
            data.add(new Tuple("Database. Version", getContentDao().getDatabaseVersion()));
            data.add(new Tuple("Database. URL", PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.url", "")));

            if (getSessionUser() != null && getSessionUser().getUserName().equals("root@kbee")) {
                data.add(new Tuple("Database. Password <span class=\"only-root\">(root)</span>",
                        PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.password", "")));
            } else {
                data.add(new Tuple("Database. Password <span class=\"only-root\">(root)</span>", "***"));
            }

            data.add(new Tuple("Database. Size", NumberFormatter.formatFileSize(getContentDao().getDatabaseSize())));
            data.add(new Tuple("User", PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.username", "") + " ("
                    + PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.password", "") + ")"));

            DataSource datasource = (DataSource) ServiceLocator.getService(BeansService.class).getBean("dataSource");

            if (datasource != null) {
                if (datasource instanceof com.novamens.kbee.sql.DataSource) {
                    GenericObjectPool<PoolableConnection> pool = ((com.novamens.kbee.sql.DataSource) datasource).getPool();
                    data.add(new Tuple("Connections (Active Idle Waiters)",
                            String.valueOf(pool.getNumActive()) + "<span class=\"separator\">|</span>"
                                    + String.valueOf(pool.getNumIdle()) + "<span class=\"separator\">|</span>"
                                    + String.valueOf(pool.getNumWaiters())));
                }
            }
            data.add(new Tuple("Database. SQL Gateway",
                    "<a class=\"btn-link\"  href=\"/datamanagement/sql-gateway\"target=\"_blank\"> /datamanagement/sql-gateway</a>"));
        } catch (Exception e) {
            logger.error(e);
            data.add(new Tuple("Error", e.getClass().getSimpleName()));
        } finally {
            long end = System.currentTimeMillis();
            if (logger.isDebugEnabled())
                data.add(new Tuple("Render time ", String.valueOf(end - start) + " ms"));
            logger.debug("Render time " + String.valueOf(end - start) + " ms");
        }

        return data;
    }

    /**
     * 
     * @return
     */
    private List<Tuple> dbSizeInfo() {

        long start = System.currentTimeMillis();

        List<Tuple> data = new ArrayList<Tuple>();

        try {
            data.add(new Tuple("Database. Version", getContentDao().getDatabaseVersion()));
            data.add(new Tuple("Database. URL", PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.url", "")));
            data.add(new Tuple("Database. Size", NumberFormatter.formatFileSize(getContentDao().getDatabaseSize())));

        } finally {
            long end = System.currentTimeMillis();
            data.add(new Tuple("Render time ", String.valueOf(end - start) + " ms"));
            logger.debug("Render time " + String.valueOf(end - start) + " ms");
        }
        return data;
    }

    protected BCElement getPageBCElement() {
        return new BCElement(new Model<String>("Database"));
    }

    protected List<Tuple> dbSettingsInfo() {
        return dbSettingsInfo(null);

    }

    /***
     * 
     * 
     */
    protected List<Tuple> dbSettingsInfo(List<String> variables) {

        List<Tuple> data = new ArrayList<Tuple>();
        List<?> list = getContentDao().getDatabaseSettings();

        for (Object object : list) {
            Object[] strarr = (Object[]) object;
            if (variables == null || variables.contains(strarr[0].toString())) {
                String[] s = new String[5];
                s[0] = (strarr[1] == null ? "null " : strarr[1].toString());
                s[1] = (strarr[2] == null ? "null " : strarr[2].toString());
                s[2] = (strarr[3] == null ? "null " : strarr[3].toString());
                s[3] = (strarr[4] == null ? "null " : strarr[4].toString());
                s[4] = (strarr[5] == null ? "null " : strarr[5].toString());
                data.add(new Tuple(strarr[0].toString(), s));
            }
        }
        return data;
    }

    /***
     * 
     * 
     * @return
     */
    private List<Tuple> mem() {

        long start = System.currentTimeMillis();
        List<Tuple> data = new ArrayList<Tuple>();

        try {

            if (isLinux()) {
                for (Entry<String, String> entry : getContentDao().getDBServerMemInfo().entrySet())
                    data.add(new Tuple(entry.getKey(), entry.getValue()));
            } else
                data.add(new Tuple("Warn", "This metrics requires DB on Linux"));

        } catch (Exception e) {
            logger.error(e.getClass().getName(), e);
            data.add(new Tuple(e.getClass().getName(), e.getMessage()));
        } finally {
            long end = System.currentTimeMillis();
            data.add(new Tuple("Render time ", String.valueOf(end - start) + " ms"));
            logger.debug("Render time " + String.valueOf(end - start) + " ms");
        }
        return data;
    }

    /***	 
     *  
     * 
     */
    private List<Tuple> loadAverage() {
        long start = System.currentTimeMillis();
        List<Tuple> data = new ArrayList<Tuple>();
        try {

            if (isLinux()) {
                StringBuilder str = new StringBuilder();
                for (String entry : getContentDao().getDBServerLoadAvg()) {
                    if (str.length() > 0)
                        str.append("  ");
                    str.append(entry);
                }
                data.add(new Tuple("CPU Load Average (1m 5m 15m)", str.toString()));

                for (Entry<String, String> entry : getContentDao().getDBServerMemInfo().entrySet())
                    data.add(new Tuple(entry.getKey(), entry.getValue()));

            } else
                data.add(new Tuple("Warn", "This metrics requires DB on Linux"));

        } catch (Exception e) {
            logger.error(e.getClass().getName(), e);
            data.add(new Tuple(e.getClass().getName(), e.getMessage()));
        } finally {
            long end = System.currentTimeMillis();
            if (logger.isDebugEnabled())
                data.add(new Tuple("Render time ", String.valueOf(end - start) + " ms"));
            logger.debug("Render time " + String.valueOf(end - start) + " ms");
        }
        return data;
    }
}
