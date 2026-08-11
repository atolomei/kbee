package kbee.objectstorage.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.novamens.content.command.CommandState;
import com.novamens.content.service.kbfs.KBFSResourceService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.dom.KBFSStorageType;
import com.novamens.kbee.content.resource.KBFileImpl;

import com.novamens.kbee.kbfs.KbeeShardedMinioFileServer;
import com.novamens.kbee.kbfs.KbeeShardedOdilonFileServer;
import com.novamens.kbfs.FileServerMinio;
import com.novamens.kbfs.FileServerOdilon;
import com.novamens.kbfs.FileServerS3;
import com.novamens.kbfs.KBFSService;
import com.novamens.kbfs.v1.FileServerV1;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import com.novamens.transaction.TransactionService;
import com.novamens.util.KbeeRuntimeException;

/**
 * <p>
 * This is a Async Command. It does not use Scheduler's Trx <br />
 * For this reason it must Open its own Hibernate Session and it doesn't need to
 * propagate SQL Exceptions
 * </p>
 *
 */
public class ObjectStorageDomainMoveCommand extends ObjectStorageCommand {

    private static kbee.util.logging.Logger logger = kbee.util.logging.Logger
            .getLogger(ObjectStorageDomainMoveCommand.class.getName());

    static final int BUFFER_SIZE = 8192;

    static final int DEFAULT_MAX_TO_MIGRATE = 10000;

    static long KB = 1024;
    static long MB = 1000 * KB;
    static long GB = 1000 * MB;

    private SessionFactory sf;

    private int total_items = 0;

    private int total_scanned = 0;
    private int files_touched = 0;
    private int files_not_found = 0;
    private int file_db_errors = 0;

    private List<Serializable> list_ids;
    private StringBuilder relevant_errors = new StringBuilder();

    private boolean aborted = false;

    private Domain domain;

    private KBFSStorageType src_kbfs = KBFSStorageType.Minio;
    private KBFSStorageType dest_kbfs = KBFSStorageType.Minio;

    private KBFSService source = null; // FileServerMinio,   AmazonS3, Odilon
    private KBFSService destination = null;; // FileServerMinio,   AmazonS3, Odilon
    private Integer maxhd = null;
    private Integer source_shard = null;
    private Integer destination_shard = null;

    long total_hd = 0; // bytes

    /**
     * 
     * 
     */
    public ObjectStorageDomainMoveCommand() {
        setName(this.getClass().getSimpleName());
        setDescription("This command moves files between Object Storages");
    }

    /**
     * 
     * 
     */
    @Override
    public void stop() {
        super.stop();
        aborted = true;
    }

    /**
     * 
     * 
     */
    @Override
    protected void initCommand() {
        super.initCommand();

        this.domain = null;

        this.total_items = 0;
        this.total_scanned = 0;
        this.files_touched = 0;
        this.files_not_found = 0;
        this.file_db_errors = 0;
        this.aborted = false;
        this.relevant_errors = new StringBuilder();
        this.list_ids = null;

        this.source = null;
        this.destination = null;
        this.maxhd = null;
        this.source_shard = null;
        this.destination_shard = null;
        this.total_hd = 0;
    }

    /**
     *
     * 
     */
    @Override
    protected void executeAsync() {

        try {

            initCommand();

            // open Hibernate Session
            // -----------------------------------------------------------------------
            this.sf = com.novamens.hibernate.session.Session.open();

            
            setDateStarted(OffsetDateTime.now());
            super.setState(CommandState.RUNNING);
            setProgress(0);

            // Authenticate
            // ---------------------------------------------------------------------------------
            ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");

            logger.debug("------------------------------------------------");
            logger.debug(getParameters().toString());
            logger.debug("domain", getDomain() != null ? getDomain().getOrganization() : "null");
            logger.debug("hd", String.valueOf(this.getMaxHD()));
            logger.debug("Source", getSource() != null ? getSource().getDisplayName() : "null");
            logger.debug("Destination", getDestination() != null ? getDestination().getDisplayName() : "null");
            logger.debug("------------------------------------------------");

            if (getTargetDomain() == null) {
                super.setState(CommandState.ERROR);
                relevant_errors.append("Domain is null");
                throw new IllegalArgumentException("Domain is null");
            }

            if (getSource() == null || getDestination() == null) {
                this.relevant_errors.append((relevant_errors.length() < 2000) ? "Source and Destination must be defined" : "");
                throw new IllegalArgumentException("Source and Destination must be defined");
            }

            Query<?> query = null;

            String storage_q;

           // if (getSource() instanceof FileServerV1) {
           //     storage_q = "K.storageType=" + String.valueOf(KBFSStorageType.KB FS1.getId()) + " and K.shard=1";
           // }

            if (getSource() instanceof FileServerS3) {
                storage_q = "K.storageType=" + String.valueOf(KBFSStorageType.AmazonS3.getId());
            }

            else if (getSource() instanceof FileServerMinio) {
                storage_q = "K.storageType=" + String.valueOf(KBFSStorageType.Minio.getId()) + " and K.shard="
                        + String.valueOf(getSourceShard());
            }

            else if (getSource() instanceof FileServerOdilon) {
                storage_q = "K.storageType=" + String.valueOf(KBFSStorageType.Odilon.getId()) + " and K.shard="
                        + String.valueOf(getSourceShard());
            } else {
                logger.error("getSource() not supported !");
                throw new KbeeRuntimeException("getSource() not supported !");
            }

            String qe = "from KBFileImpl K where K.domain.id=" + String.valueOf(getDomain().getId()) + " and " + storage_q
                    + " order by K.id desc";

            logger.debug(qe);

            query = sf.getCurrentSession().createQuery(qe);

            query.setMaxResults(10000);
            List<?> srclist = query.list();
            list_ids = new ArrayList<Serializable>();
            for (Object kfile : srclist) {
                list_ids.add(((KBFileImpl) kfile).getId());
            }
            srclist = null;

            total_hd = 0;

            this.setStatusInfo("Starting processing HD -> " + this.getMaxHD().toString());
            logger.debug("Starting processing HD -> " + this.getMaxHD().toString());

            int errors = 0;

            long MAX_HD_BYTES = getMaxHD() * GB;

            for (Serializable kfile_id : list_ids) {

                try {

                    if (isStopped() || aborted)
                        break;

                    if (total_hd > MAX_HD_BYTES)
                        break;

                    KBFileImpl file = (KBFileImpl) getContentDao().findResourceById(KBFileImpl.class, kfile_id);

                    this.total_scanned++;

                    moveFile(file);

                    setProgress(MAX_HD_BYTES > 0 ? (100 * (double) this.total_hd / (double) MAX_HD_BYTES) : 100);

                    String ps = "   Files moved   -> <b> " + String.valueOf(this.files_touched) + " </b> / <b> "
                            + ServiceLocator.getService(DateTimeService.class).formatFileSize(total_hd) + " </b> | Files Scanned -> <b>"
                            + String.valueOf(this.total_scanned) + " </b>";

                    this.setStatusInfo(ps);
                    logger.debug(ps);

                    if (this.file_db_errors > 5)
                        aborted = true;

                } catch (Exception e) {
                    logger.error(e);
                    if (errors++ > 100)
                        aborted = true;
                }
            }

            setResultDetails("  HD moved    : <b> "
                    + String.valueOf(ServiceLocator.getService(DateTimeService.class).formatFileSize(total_hd)) + " </b>"
                    + "| Processed   : <b> " + String.valueOf(this.total_scanned) + " </b>" + "| Files Moved : <b> "
                    + String.valueOf(this.files_touched) + " </b>" + "| Files error : <b> " + String.valueOf(this.files_not_found)
                    + " </b>" + "| DB Errors   : <b> " + String.valueOf(file_db_errors));

            if (!aborted && !isStopped()) {
                setProgress(100);
                setResult("OK");
                setState(CommandState.COMPLETED);
            } else {

                if (aborted) {
                    setResult("Error");
                    setState(CommandState.ERROR);
                } else {
                    setResult("Canceled by user");
                    setState(CommandState.CANCELED);
                }
            }

            logger.debug("Ending Command execution " + getName());

        } catch (Throwable e) {
            logger.error(e);
            setResult(e.getClass().getSimpleName());
            setResultDetails(e.getMessage());
            setState(CommandState.ERROR);

        } finally {

            setResultComments(relevant_errors.toString());
            setStatusInfo("DB Session closed.");
            setDateTerminated(OffsetDateTime.now());

            com.novamens.hibernate.session.Session.close();
        }
    }

    @Override
    public long getTotalItems() {
        return this.total_items;
    }

    @Override
    public long getTotalItemsProcessed() {
        return this.total_scanned;
    }

    public KBFSStorageType getSrcStorageType() {
        return src_kbfs;
    }

    public KBFSStorageType getDestStorageType() {
        return dest_kbfs;
    }

    protected com.novamens.transaction.Transaction beginTransaction() {
        return ServiceLocator.getService(TransactionService.class).beginTransaction(false);
    }

    /**
     * 
     * put pdf on destination update kbflie remove pdf
     * 
     * @param file
     */
    private void moveFile(KBFileImpl file) {

        logger.debug(file.getDisplayName() + " | " + file.getUrl() + " | " + String.valueOf((double) file.getSize() / (double) MB)
                + " MB");

        String oldUrl = file.getUrl();
        String b_name = file.getBucketName();
        String o_name = file.getObjectName();

        String new_b_name = null;
        String new_o_name = null;
        // String new_url = null;

        boolean o_is = false;

        try {

            KBFSResourceService service;

            try {

                o_is = file.getService(KBFSResourceService.class).isObject();

            } catch (Exception e2) {

                if (relevant_errors.length() < 2000)
                    relevant_errors.append((relevant_errors.length() > 0 ? " | " : "") + file.getDisplayName());

                logger.error("isobject error");
                files_not_found++;
                return;
            }

            if (!o_is) {
                logger.error("Resource does not exist in -> "+ file.getService(KBFSResourceService.class).getKBFSService().getDisplayName());
                files_not_found++;
                return;
            }

            try (InputStream is = file.getService(KBFSResourceService.class).getObject()) {
                // ------------------------------------------------------------------------------------
                // save in new storage
                // ------------------------------------------------------------------------------------

                service = file.getService(KBFSResourceService.class);

                // force Type and Shard
                service.setDefaultKBFSStorageType(getDestination().getKBFSStorageType());

                if (getDestinationShard() != null)
                    service.setPreassignedShard(getDestinationShard());

                // save new File
                service.putObject(file.getFileName(), is);

                new_b_name = file.getBucketName();
                new_o_name = file.getObjectName();

                logger.debug("new resource done -> " + service.getKBFSService().getDisplayName());

            } catch (Throwable e) {
                logger.error(e);
                files_not_found++;
                return;
            }

            // ------------------------------------------------------------------------------------
            // save KBFile with archive in new storage
            // ------------------------------------------------------------------------------------

            try {
                // save new KBFile with 
                //
                getContentDao().saveTX(file);
                logger.debug("KBFile saved ok -> " + file.getDisplayName());

            } catch (Throwable e) {

                /**
                 * ---- if the KBFile can not be saved, it keeps the original reference to the
                 * src resource the new encrypted archive is headless and has to be
                 * removed
                 */
                logger.error(e);
                file_db_errors++;

                /**
                if (!new_o_name.equals(o_name)) {
                    try {

                        file.getService(KBFSResourceService.class).getKBFSService().removeObject(new_b_name, new_o_name);

                    } catch (Exception e1) {
                        if (relevant_errors.length() < 2000)
                            relevant_errors.append((relevant_errors.length() > 0 ? " | " : "") + " FATAL ERROR can not remove -> "
                                    + new_b_name + "/" + new_o_name);
                        logger.error(e1);
                        files_not_found++;
                        this.aborted = true;
                    }
                }
                **/
                relevant_errors.append((relevant_errors.length() > 0 ? " | " : "") + file.getName());
                return;
            }

            // ------------------------------------------------------------------------------------
            // remove original archive
            // ------------------------------------------------------------------------------------
            try {
                if (new_o_name != null && !new_o_name.equals(o_name)) {
                    getSource().removeObject(b_name, o_name);
                    logger.debug("old resource removed  -> " + getSource().getDisplayName() + " -> " + b_name + "/" + o_name);
                }

                this.files_touched++;
                this.total_hd += file.getSize();

            } catch (Throwable e) {
                files_not_found++;
                this.aborted = true;
                logger.error(e);
                if (relevant_errors.length() < 2000)
                    relevant_errors.append((relevant_errors.length() > 0 ? " | " : "")
                            + " FATAL ERROR can not remove original resource -> " + b_name + "/" + o_name);
            }

        } finally {

            try {
                logger.debug("-----------------------------");
                logger.debug(
                        "before -> " + file.getDisplayName() + " | " + oldUrl + " | " + b_name + " | " + o_name + " | " + o_is);
                logger.debug("after  -> " + file.getDisplayName() + " | " + file.getUrl() + " | " + file.getBucketName() + " | "
                        + file.getObjectName() + " | " + file.getService(KBFSResourceService.class).isObject());
                logger.debug("-----------------------------");

            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    /**
     * @return
     */
    public Integer getMaxHD() {

        if (this.maxhd != null)
            return this.maxhd;

        String ds = (String) getParameters().get("hd");

        if (ds != null) {
            try {
                maxhd = Integer.valueOf(ds);
            } catch (Exception e) {
                maxhd = Integer.valueOf(100);
            }
        }

        if (maxhd == null)
            maxhd = Integer.valueOf(100);

        return maxhd;
    }

    /**
     * 
     */
    public Domain getDomain() {

        if (this.domain != null)
            return this.domain;

        String ds = (String) getParameters().get("domain");
        if (ds != null)
            this.domain = getContentDao().findDomainById(Long.valueOf(ds));

        return this.domain;
    }

    /**
     * 
     * @return
     */
    public KBFSService getSource() {

        if (source != null)
            return source;

        if (getParameters() == null)
            return null;

        if (getParameters().get("source") != null) {
            String src = (String) getParameters().get("source");

            if (src.equals(FileServerV1.KEY)) {
                source = ServiceLocator.getService(FileServerV1.class);
                return source;
            } else if (src.equals(FileServerS3.KEY)) {
                source = ServiceLocator.getService(FileServerS3.class);
                return source;
            }

            else if (src.startsWith("Minio_")) {
                String s = (String) getParameters().get("source_shard");
                if (s == null) {
                    source = ServiceLocator.getService(FileServerMinio.class);
                    return source;
                }

                KBFSService service = ServiceLocator.getService(FileServerMinio.class);

                if (service instanceof KbeeShardedMinioFileServer)
                    source = ((KbeeShardedMinioFileServer) service).getShards().get(Integer.valueOf(s));
                else
                    source = ServiceLocator.getService(FileServerMinio.class);

                source_shard = Integer.valueOf(Integer.valueOf(s));
            }

            else if (src.startsWith("Odilon_")) {
                String s = (String) getParameters().get("destination_shard");

                if (s == null) {
                    source = ServiceLocator.getService(FileServerOdilon.class);
                    return source;
                }

                KBFSService service = ServiceLocator.getService(FileServerOdilon.class);

                if (service instanceof KbeeShardedOdilonFileServer)
                    source = ((KbeeShardedOdilonFileServer) service).getShards().get(Integer.valueOf(s));
                else
                    source = ServiceLocator.getService(FileServerOdilon.class);

                source_shard = Integer.valueOf(Integer.valueOf(s));
            }
        }
        return source;
    }

    /**
     * @return
     */
    public KBFSService getDestination() {

        if (destination != null)
            return destination;

        if (getParameters() == null)
            return null;

        if (getParameters().get("destination") != null) {

            String des = (String) getParameters().get("destination");

            if (des.equals(FileServerV1.KEY)) {
                destination = ServiceLocator.getService(FileServerV1.class);
                return destination;

            } else if (des.equals(FileServerS3.KEY)) {
                destination = ServiceLocator.getService(FileServerS3.class);
                return destination;

            } else if (des.toLowerCase().startsWith("minio_")) {
                String s = (String) getParameters().get("destination_shard");

                if (s == null) {
                    destination = ServiceLocator.getService(FileServerMinio.class);
                    return destination;
                }

                KBFSService service = ServiceLocator.getService(FileServerMinio.class);

                if (service instanceof KbeeShardedMinioFileServer)
                    destination = ((KbeeShardedMinioFileServer) service).getShards().get(Integer.valueOf(s));
                else
                    destination = ServiceLocator.getService(FileServerMinio.class);

                destination_shard = Integer.valueOf(Integer.valueOf(s));
            }

            else if (des.toLowerCase().startsWith("odilon_")) {
                String s = (String) getParameters().get("destination_shard");

                if (s == null) {
                    destination = ServiceLocator.getService(FileServerOdilon.class);
                    return destination;
                }

                KBFSService service = ServiceLocator.getService(FileServerOdilon.class);

                if (service instanceof KbeeShardedOdilonFileServer)
                    destination = ((KbeeShardedOdilonFileServer) service).getShards().get(Integer.valueOf(s));
                else
                    destination = ServiceLocator.getService(FileServerOdilon.class);

                destination_shard = Integer.valueOf(Integer.valueOf(s));
            }
        }

        return destination;
    }

    /**
     * @return
     */
    public Integer getSourceShard() {
        if (getSource() != null)
            return source_shard;
        return 0;
    }

    public Integer getDestinationShard() {
        if (getDestination() != null)
            return destination_shard;
        return 0;
    }

}
