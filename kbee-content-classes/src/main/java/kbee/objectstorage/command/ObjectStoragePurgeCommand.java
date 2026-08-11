package kbee.objectstorage.command;

import java.io.IOException;
import java.math.RoundingMode;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import org.hibernate.SessionFactory;
import org.xmlpull.v1.XmlPullParserException;

import com.novamens.content.command.CommandState;
import com.novamens.content.resource.KBFile;
import com.novamens.kbee.kbfs.KbeeAmazonS3FileServer;
import com.novamens.kbee.kbfs.KbeeMinioFileServer;
import com.novamens.kbee.kbfs.KbeeOdilonFileServer;
import com.novamens.kbee.kbfs.KbeeShardedMinioFileServer;
import com.novamens.kbee.kbfs.KbeeShardedOdilonFileServer;
import com.novamens.kbfs.FileServerException;
import com.novamens.kbfs.FileServerMinio;
import com.novamens.kbfs.FileServerOdilon;
import com.novamens.kbfs.FileServerS3;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.util.KeyValue;

import io.minio.Result;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.NoResponseException;
import io.minio.messages.Item;
import io.odilon.model.ObjectMetadata;
import kbee.util.NumberFormatter;
import kbee.util.PropertiesFactory;
import software.amazon.awssdk.services.s3.model.S3Object;

public class ObjectStoragePurgeCommand extends ObjectStorageCommand {

    static private final Properties props = PropertiesFactory.getInstance("kbee").getProperties();

    private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ObjectStoragePurgeCommand.class.getName());

    static long KB = 1024;
    static long MB = 1000 * KB;
    static long GB = 1000 * MB;

    private static String executing_thread;
    private static AtomicBoolean is_executing = new AtomicBoolean(false);

    private final NumberFormat nf_dec = NumberFormat.getInstance(Locale.getDefault());
    private final NumberFormat nf_int = NumberFormat.getInstance(Locale.getDefault());

    private SessionFactory sf = null;

    private int total_items = 0;
    private int files_removed = 0;
    private long total_hardDisk = 0;

    private int files_errors = 0;
    private long sections = 1;
    private boolean aborted = false;

    private StringBuilder relevant_errors = new StringBuilder();

    private Boolean isDelete = Boolean.valueOf(false);
    private long hardDisk = 0;

    /**
     * <p>
     * We do not remove files from S3 yet.<br/>
     * In order to remove files from Minio<br/>
     * <br/>
     * SystemParameter -> </b>ObjectStoragePurgeCommand.delete</b> must be true
     * <br/>
     * and the user must select "delete" in {@link ObjectStoragePurgeDomainObjects}
     * </p>
     */
    public ObjectStoragePurgeCommand() {
        setName("ObjectStoragePurgeCommand");
        setDescription(
                "This command purges Object Storage removing binary objects that have no KBFile that contains them ('ghosts files').");

        nf_dec.setMinimumFractionDigits(4);
        nf_dec.setMaximumFractionDigits(4);
        nf_dec.setRoundingMode(RoundingMode.HALF_UP);

        nf_int.setMinimumFractionDigits(0);
        nf_int.setMaximumFractionDigits(0);
        nf_int.setRoundingMode(RoundingMode.HALF_UP);

    }

    @Override
    public long getTotalItems() {
        return this.total_items;
    }

    @Override
    public long getTotalItemsProcessed() {
        return this.total_items;
    }

    @Override
    public void stop() {
        super.stop();
        aborted = true;
    }

    @Override
    protected void initCommand() {
        super.initCommand();

        this.total_items = 0;
        this.files_removed = 0;
        this.files_errors = 0;
        this.aborted = false;
        this.relevant_errors = new StringBuilder();
        this.sections = 1;
        this.total_hardDisk = 0;
        this.isDelete = Boolean.valueOf(false);
    }

    @Override
    protected void executeAsync() {
        try {

            if (is_executing.get()) {
                relevant_errors.append("Can not execute this Command while another instance is under execution");
                throw new KbeeRuntimeException("Can not execute this Command while another instance is under execution");
            }

            is_executing.set(true);
            executing_thread = super.getId().toString();

            initCommand();

            setDateStarted(OffsetDateTime.now());
            setState(CommandState.RUNNING);
            setProgress(0.0);

            if (getParameters() != null && getParameters().get("removefiles") != null
                    && getParameters().get("removefiles").toString().trim().equals("true"))
                this.isDelete = Boolean.valueOf(true);
            else {
                this.isDelete = Boolean.valueOf(false);
            }

            /**
             * NO PODEMOS PROCESAR S3 HASTA TENER SEPARADOS LOS ENTORNOS DE PRODUCCION Y
             * DESARROLLO EN LAS CREDENCIALES AMAZON
             * 
             */
            FileServerMinio file_server_minio = ServiceLocator.getService(FileServerMinio.class);
            FileServerOdilon file_server_odilon = ServiceLocator.getService(FileServerOdilon.class);
            FileServerS3 s3 = ServiceLocator.getService(FileServerS3.class);

            try {
                logger.debug("Counting Storages");
                this.setStatusInfo("Counting Storages");

                if (s3 != null && s3.isEnabled())
                    sections = 1;

                if (file_server_minio != null) {
                    Map<Integer, FileServerMinio> map = ((KbeeShardedMinioFileServer) file_server_minio).getShards();

                    if (file_server_minio instanceof KbeeShardedMinioFileServer) {
                        sections = sections + map.entrySet().size();
                    } else
                        sections += 1;
                }
                if (file_server_odilon != null) {
                    Map<Integer, FileServerOdilon> map = ((KbeeShardedOdilonFileServer) file_server_odilon).getShards();

                    if (file_server_odilon instanceof KbeeShardedOdilonFileServer) {
                        sections = sections + map.entrySet().size();
                    } else
                        sections += 1;
                }

            } catch (Exception e) {
                throw e;
            }

            if (sections == 0)
                throw new KbeeRuntimeException("There are no Object Storage available");

            logger.debug("Total Storages -> " + String.valueOf(sections));
            this.setStatusInfo("Total Storages -> " + String.valueOf(sections));

            if (file_server_minio instanceof KbeeShardedMinioFileServer) {
                Map<Integer, FileServerMinio> map = ((KbeeShardedMinioFileServer) file_server_minio).getShards();
                for (Entry<Integer, FileServerMinio> entry : map.entrySet()) {
                    FileServerMinio file_server_minio_shard = entry.getValue();
                    processMinio(file_server_minio_shard);
                }
            } else {
                processMinio(file_server_minio);
            }
            logger.debug("done Minio");

            if (file_server_odilon instanceof KbeeShardedOdilonFileServer) {
                Map<Integer, FileServerOdilon> map = ((KbeeShardedOdilonFileServer) file_server_odilon).getShards();
                for (Entry<Integer, FileServerOdilon> entry : map.entrySet()) {
                    FileServerOdilon file_server_odilon_shard = entry.getValue();
                    processOdilon(file_server_odilon_shard);
                }
            } else {
                processOdilon(file_server_odilon);
            }
            logger.debug("done Odilon");

            // if (s3 != null && s3.isEnabled()) {
            // processS3(s3);
            // logger.debug("done S3");
            // }

            try {

                if (!aborted && !isStopped()) {
                    setProgress(100);
                    setResult("OK");
                    setState(CommandState.COMPLETED);
                    setDateTerminated(OffsetDateTime.now());
                    setResultDetails("Removed / listed: " + String.valueOf(this.files_removed) + " Objects | Total Files scanned: "
                            + String.valueOf(this.total_items) + " | Hard Disk: " + formatFileSize(this.total_hardDisk)
                            + (this.files_errors > 0 ? "  File errors  : " + String.valueOf(this.files_errors) : ""));
                } else {

                    if (aborted) {
                        setResult("Canceled");
                        setState(CommandState.ERROR);
                        setDateTerminated(OffsetDateTime.now());
                        setResultDetails(
                                "Removed / listed: " + String.valueOf(this.files_removed) + " Objects | Total Files scanned: "
                                        + String.valueOf(this.total_items) + " | Hard Disk: " + formatFileSize(this.total_hardDisk)
                                        + (this.files_errors > 0 ? "  File errors  : " + String.valueOf(this.files_errors) : ""));

                    } else {
                        setResult("Canceled");
                        setState(CommandState.CANCELED);
                        setDateTerminated(OffsetDateTime.now());
                        setResultDetails("Removed / listed: " + String.valueOf(this.files_removed) + " Objects | Total Files: "
                                + String.valueOf(this.total_items) + " | Hard Disk:  " + formatFileSize(this.total_hardDisk)
                                + (this.files_errors > 0 ? "  File errors  : " + String.valueOf(this.files_errors) : ""));
                    }
                }
                logger.debug("Ending Command execution -> " + getName());
            } finally {
                logger.debug("Command execution done");
            }
        } catch (Throwable e) {
            logger.error("executeAsync");
            logger.error(e);
            setResult(e.getClass().getSimpleName() + " | " + e.getMessage());
            setResultDetails("Removed / listed: " + String.valueOf(this.files_removed) + " Objects | Total Files scanned: "
                    + String.valueOf(this.total_items) + " | Hard Disk: " + formatFileSize(this.total_hardDisk)
                    + (this.files_errors > 0 ? "  File errors  : " + String.valueOf(this.files_errors) : ""));
            setState(CommandState.ERROR);
            setDateTerminated(OffsetDateTime.now());

        } finally {

            try {

                logAudit();

                setResultComments(relevant_errors.toString());
                setDateTerminated(OffsetDateTime.now());

            } catch (Exception e) {
                logger.error(e);
            }
            if (is_executing.get() && executing_thread != null && executing_thread.equals(super.getId().toString())) {

                is_executing.set(false);
                executing_thread = null;
            }
        }
    }

    private String formatFileSize(long size) {
        return nf_dec.format((double) size / (double) MB).trim() + " MB";
    }

    private void logAudit() {

        logger.debug("Started -> " + getDateStarted().toString());
        logger.debug("End -> " + OffsetDateTime.now().toString());
        logger.debug("Total files deleted -> " + NumberFormatter.formatNumber(this.files_removed));
        logger.debug("Total Hard Disk -> " + formatFileSize(this.total_hardDisk));

        if (getParameters() == null)
            return;

        logger.debug("Server  -> " + ((getParameters().get("server") != null) ? getParameters().get("server").toString() : ""));
        logger.debug("Database -> " + props.getProperty("jdbc.url", "").trim());
        logger.debug("Client -> " + ((getParameters().get("client") != null) ? getParameters().get("client").toString() : ""));

    }

    /**
     * @param s3
     */
    
    @SuppressWarnings("unused")
    private void processS3(FileServerS3 s3) {

        try {

            this.sf = com.novamens.hibernate.session.Session.open();

            if (this.sf == null) {
                setState(CommandState.ERROR);
                throw new IllegalArgumentException("com.novamens.hibernate.session.Session.open() is null");
            }

            ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");
            logger.debug("Authenticated ->  root@kbee");

            List<KeyValue<String>> listRemoval = new ArrayList<KeyValue<String>>();

            hardDisk = 0;

            logger.debug("Starting to process Amazon S3");
            this.setStatusInfo("Starting to process Amazon S3");

            List<String> buckets = s3.listBuckets();

            for (String bucketName : buckets) {

                if (aborted)
                    break;

                if (bucketName.startsWith("kbee")) {

                    logger.debug(bucketName);

                    ListIterator<S3Object> iterator = ((KbeeAmazonS3FileServer) s3).listObjects(bucketName);

                    while (iterator.hasNext() && !aborted) {
                        try {
                            S3Object res = iterator.next();
                            String objectName = res.key();
                            KBFile kbfile = getContentDao().findKBFileByObjectName(bucketName, objectName);
                            if (kbfile == null) {
                                listRemoval.add(new KeyValue<String>(bucketName, objectName));
                                hardDisk += res.size();
                                total_items++;
                            }

                        } catch (Exception e) {
                            logger.error(e);
                        }
                    }
                }
            }

            try {

                if (aborted)
                    return;

                logger.debug("----------------------------------------------------");
                logger.debug(" Total to remove from  Amazon S3 " + s3.getEndPoint() + " -> " + listRemoval.size()
                        + " files | HardDisk -> " + String.valueOf((double) hardDisk / (double) MB) + " MB");
                logger.debug("----------------------------------------------------");

                Thread.sleep(1000);

                int counter = 0;

                double factor = 100.0 / (double) sections;

                this.setStatusInfo(" Total to remove from " + s3.getEndPoint() + " -> " + listRemoval.size()
                        + " files | HardDisk -> " + String.valueOf((double) hardDisk / (double) MB) + " MB");

                for (KeyValue<String> kv : listRemoval) {

                    if (aborted)
                        break;

                    try {

                        logger.debug("Would be Deleted : b -> " + kv.getKey().toString() + "   | o -> " + kv.getValue());

                        // -------------------------------------------------------------
                        // WE DO NOT DELETE FILES FROM S3 YET
                        //
                        // if (this.deleteFiles)
                        // s3.removeObject(kv.getKey().toString(), kv.getValue());
                        //
                        // -------------------------------------------------------------

                        counter++;
                        files_removed++;

                        if (files_removed >= getMaxToProcess())
                            break;

                    } catch (Exception e) {
                        logger.error(e);
                        files_errors++;
                    }

                    setProgress(factor * (double) counter / (double) listRemoval.size());

                }

                Thread.sleep(1000);
                this.total_hardDisk += hardDisk;

            } catch (Exception e) {
                logger.error("processS3");
                logger.error(e);
            }

        } catch (FileServerException e) {
            logger.error("processS3");
            logger.error(e);
        }

        finally {

            if (sf != null) {
                com.novamens.hibernate.session.Session.close();
                this.setStatusInfo("DB Session closed");
            }
            logger.debug("done S3");
        }

    }

    /**
     * Minio specific
     * 
     * @param file_server_minio
     * 
     *                          read 100 -> open DB -> process -> close DB
     * 
     */

    /**
     * 
     * ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");
     **/

    private void processMinio(FileServerMinio file_server_minio) {

        logger.debug("---------------------------------------");
        logger.debug("Starting to process Minio");

        logger.debug(file_server_minio.getEndPoint());
        logger.debug(file_server_minio.getAccessKey());
        logger.debug(file_server_minio.getSecretKey());
        logger.debug(file_server_minio.getFSId());
        logger.debug(file_server_minio.getShard());

        logger.debug("Starting to process Minio -> " + file_server_minio.getEndPoint());
        this.setStatusInfo("Starting to process Minio -> " + file_server_minio.getEndPoint());

        if (files_removed >= getMaxToProcess()) {
            this.setStatusInfo("Files removed / listed -> " + String.valueOf(getMaxToProcess()));
            return;
        }

        try {

            for (String bucketName : file_server_minio.listBuckets()) {

                logger.debug("bucketName -> " + bucketName);

                if (aborted)
                    return;

                Iterable<Result<Item>> it = ((KbeeMinioFileServer) file_server_minio).listObjects(bucketName);
                Iterator<Result<Item>> iterator = it.iterator();

                List<Item> listCandidates = new ArrayList<Item>();

                while (iterator.hasNext() && !aborted) {

                    Result<Item> res = iterator.next();
                    Item item = res.get();
                    listCandidates.add(item);

                    if (listCandidates.size() == 1000) {
                        processListItems(file_server_minio, bucketName, listCandidates);
                        listCandidates = new ArrayList<Item>();
                    }

                    if (files_removed >= getMaxToProcess())
                        break;
                }

                processListItems(file_server_minio, bucketName, listCandidates);
            }

        } catch (InvalidKeyException | FileServerException | InvalidBucketNameException | NoSuchAlgorithmException
                | InsufficientDataException | NoResponseException | ErrorResponseException | InternalException | IOException
                | XmlPullParserException e) {

            throw new KbeeRuntimeException(e);

        } finally {
            logger.debug("done minio -> " + file_server_minio.getEndPoint());
        }
    }

    private void processOdilon(FileServerOdilon file_server_odilon) {

        logger.debug("---------------------------------------");
        logger.debug("Starting to process Odilon");

        logger.debug(file_server_odilon.getEndPoint());
        logger.debug(file_server_odilon.getAccessKey());
        logger.debug(file_server_odilon.getSecretKey());
        logger.debug(file_server_odilon.getFSId());
        logger.debug(file_server_odilon.getShard());

        logger.debug("Starting to process Odilon -> " + file_server_odilon.getEndPoint());
        this.setStatusInfo("Starting to process Odilon -> " + file_server_odilon.getEndPoint());

        if (files_removed >= getMaxToProcess()) {
            this.setStatusInfo("Files removed / listed -> " + String.valueOf(getMaxToProcess()));
            return;
        }

        try {

            for (String bucketName : file_server_odilon.listBuckets()) {

                logger.debug("bucketName -> " + bucketName);

                if (aborted)
                    return;

                Iterator<io.odilon.model.list.Item<ObjectMetadata>> it = ((KbeeOdilonFileServer) file_server_odilon)
                        .listObjects(bucketName);

                List<ObjectMetadata> listCandidates = new ArrayList<ObjectMetadata>();

                while (it.hasNext() && !aborted) {

                    io.odilon.model.list.Item<ObjectMetadata> res = it.next();
                    if (res.isOk()) {
                        ObjectMetadata item = res.getObject();
                        listCandidates.add(item);
                    }

                    if (listCandidates.size() == 1000) {
                        processListItems(file_server_odilon, bucketName, listCandidates);
                        listCandidates = new ArrayList<ObjectMetadata>();
                    }

                    if (files_removed >= getMaxToProcess())
                        break;

                }

                processListItems(file_server_odilon, bucketName, listCandidates);

            }

        } catch (Exception e) {
            throw new KbeeRuntimeException(e);

        } finally {
            logger.debug("done odilon -> " + file_server_odilon.getEndPoint());
        }
    }

    /**
     * @param file_server_minio
     * @param bucketName
     * @param listCandidates
     */
    private void processListItems(FileServerMinio file_server_minio, String bucketName, List<Item> listCandidates) {

        if (files_removed >= getMaxToProcess())
            return;

        try {

            this.sf = com.novamens.hibernate.session.Session.open();
            logger.debug("DB Session opened");

            ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");
            logger.debug("Authenticated ->  root@kbee");

            if (this.sf == null) {
                setState(CommandState.ERROR);
                throw new IllegalArgumentException("com.novamens.hibernate.session.Session.open() is null");
            }

            for (Item item : listCandidates) {

                if (aborted)
                    return;

                this.total_items++;

                String objectName = item.objectName();
                KBFile kbfile = getContentDao().findKBFileByObjectName(bucketName, objectName);

                if (kbfile == null) {

                    if (this.isDelete) {

                        logger.debug("Delete : b -> " + bucketName + "   | o -> " + objectName);

                        try {
                            file_server_minio.removeObject(bucketName, objectName);

                            this.total_hardDisk += item.objectSize();
                            this.files_removed++;

                        } catch (FileServerException e) {
                            logger.error(e);
                        }
                    } else {
                        logger.debug("Would be Deleted : b -> " + bucketName + "   | o -> " + objectName);

                        this.total_hardDisk += item.objectSize();
                        this.files_removed++;
                    }

                    if (files_removed >= getMaxToProcess())
                        break;
                } else {
                    logger.debug("ok: b -> " + bucketName + "   | o -> " + objectName);
                }
            }
        } catch (Exception e) {
            logger.error("original exception -----------------------------------------------> ");
            logger.error(e);
            throw new KbeeRuntimeException(e);

        } finally {
            if (sf != null) {
                com.novamens.hibernate.session.Session.close();

                logger.debug("removed  -> " + String.valueOf(this.files_removed));
                logger.debug("processed  -> " + String.valueOf(this.total_items));

                logger.debug("DB Session closed");
                this.setStatusInfo("DB Session closed");
            }
        }
    }

    /**
     * @param file_server_minio
     * @param bucketName
     * @param listCandidates
     */
    private void processListItems(FileServerOdilon file_server_odilon, String bucketName, List<ObjectMetadata> listCandidates) {

        if (files_removed >= getMaxToProcess())
            return;

        try {

            this.sf = com.novamens.hibernate.session.Session.open();

            logger.debug("DB Session opened");

            ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");
            logger.debug("Authenticated ->  root@kbee");

            if (this.sf == null) {
                setState(CommandState.ERROR);
                throw new IllegalArgumentException("com.novamens.hibernate.session.Session.open() is null");
            }

            for (ObjectMetadata item : listCandidates) {

                if (aborted)
                    return;

                this.total_items++;

                String objectName = item.getObjectName();
                KBFile kbfile = getContentDao().findKBFileByObjectName(bucketName, objectName);

                if (kbfile == null) {

                    if (this.isDelete) {

                        logger.debug("Delete : b -> " + bucketName + "   | o -> " + objectName);

                        try {

                            file_server_odilon.removeObject(bucketName, objectName);

                            this.total_hardDisk += item.getLength();
                            this.files_removed++;

                        } catch (FileServerException e) {
                            logger.error(e);
                        }
                    } else {
                        logger.debug("Would be Deleted : b -> " + bucketName + "   | o -> " + objectName);

                        this.total_hardDisk += item.getLength();
                        this.files_removed++;
                    }

                    if (files_removed >= getMaxToProcess())
                        break;
                } else {
                    logger.debug("ok: b -> " + bucketName + "   | o -> " + objectName);
                }
            }
        } catch (Exception e) {
            logger.error("original exception -----------------------------------------------> ");
            logger.error(e);
            throw new KbeeRuntimeException(e);

        } finally {

            if (sf != null) {

                com.novamens.hibernate.session.Session.close();

                logger.debug("removed  -> " + String.valueOf(this.files_removed));
                logger.debug("processed  -> " + String.valueOf(this.total_items));

                logger.debug("DB Session closed");
                this.setStatusInfo("DB Session closed");
            }
        }
    }
}
