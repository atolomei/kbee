package kbee.objectstorage.command;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.command.CommandState;
import com.novamens.content.service.kbfs.KBFSResourceService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;

/**
 * 
 * Info
 * 
 * Status (ongoing)
 * 
 * 
 * 
 * Done so far Partial metrics Currently doing Progress ETC
 * 
 * 
 * 
 * 
 * Started Terminated Duration Results Log Comments Errors
 *
 */
public class ObjectStorageDomainDeleteCommand extends ObjectStorageCommand {

    private static kbee.util.logging.Logger logger = kbee.util.logging.Logger
            .getLogger(ObjectStorageDomainDeleteCommand.class.getName());

    private SessionFactory sf = null;

    private long total_files_to_process = 0;
    private int total_items = 0;

    private int total_scanned = 0;
    private int files_touched = 0;
    private int files_ok = 0;
    private int files_not_found = 0;
    private int file_errors = 0;

    private boolean aborted = false;

    private List<Serializable> list_ids;
    private StringBuilder relevant_errors = new StringBuilder();

    public ObjectStorageDomainDeleteCommand() {
        setName("ObjectStorageDeleteCommand");
        setDescription("Permanently delete Object Storage resources.");
    }

    @Override
    protected void initCommand() {
        super.initCommand();

        total_files_to_process = 0;
        total_scanned = 0;
        files_touched = 0;
        files_ok = 0;
        files_not_found = 0;
        file_errors = 0;

        super.setDateStarted(OffsetDateTime.now());
        super.setResult(null);
        super.setResultComments(null);

        list_ids = null;

    }

    @Override
    public long getTotalItems() {
        return this.total_items;
    }

    @Override
    public long getTotalItemsProcessed() {
        return this.total_scanned;
    }

    @Override
    public void stop() {
        super.stop();
        aborted = true;
    }

    /**
     * 
     * Remove Files Mark as as
     * 
     */
    @SuppressWarnings("rawtypes")
    @Override
    protected void executeAsync() {

        try {

            initCommand();

            setProgress(0);
            setDateStarted(OffsetDateTime.now());

            // open Hibernate Session
            // -------------------------------------------------------------------
            //
            //
            this.sf = com.novamens.hibernate.session.Session.open();

            if (this.sf == null) {
                setState(CommandState.ERROR);
                throw new IllegalArgumentException("com.novamens.hibernate.session.Session.open() is null");
            }

            if (getTargetDomain() == null) {
                setState(CommandState.ERROR);
                throw new IllegalArgumentException("parameter 'domain' is null");
            }

            if (getTargetDomain().getState() == ObjectState.ENABLED) {
                setState(CommandState.ERROR);
                throw new IllegalArgumentException("Domain is can not be in state ENABLED, it must be DELETED or ARCHIVED.");
            }

            // Authenticate
            // -------------------------------------------------------------------
            //
            //
            ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");

            Query query = null;

            this.setStatusInfo("Calculating size");

            query = sf.getCurrentSession()
                    .createQuery("from KBFileImpl K where K.domain.id=" + String.valueOf(getTargetDomain().getId())
                            + " and K.exists_in_object_storage=true order by K.lastModifiedDate ");

            logger.debug("query -> " + query.getQueryString());

            this.total_files_to_process = ((Long) (sf.getCurrentSession()
                    .createQuery("select count(*) from KBFileImpl K where  K.exists_in_object_storage=true and K.domain.id="
                            + String.valueOf(getTargetDomain().getId()))
                    .uniqueResult())).longValue();

            this.setStatusInfo("Executing");

            if (getMaxToProcess() > 0) {
                query.setMaxResults(getMaxToProcess());
                if (this.total_files_to_process > getMaxToProcess())
                    this.total_files_to_process = getMaxToProcess();
            }

            this.total_items = Math.toIntExact(this.total_files_to_process);

            // this works form small lists,
            // for large lists we have to use a SolR query and ResultSet
            //
            List<?> srclist = query.list();

            this.setStatusInfo("Starting processing " + String.valueOf(this.total_files_to_process) + " files");
            logger.debug("Starting processing " + String.valueOf(this.total_files_to_process) + " files");

            list_ids = new ArrayList<Serializable>();

            for (Object kfile : srclist)
                list_ids.add(((KBFileImpl) kfile).getId());

            srclist = null;

            for (Serializable kfile_id : list_ids) {

                try {
                    if (isStopped() || aborted)
                        break;

                    this.total_scanned++;

                    KBFileImpl object1 = (KBFileImpl) getContentDao().findResourceById(KBFileImpl.class, kfile_id);

                    deleteFile(object1);

                    this.setStatusInfo("Scanned " + String.valueOf(this.total_scanned) + " / "
                            + String.valueOf(this.total_files_to_process) + ". Deleted: " + String.valueOf(this.files_touched));
                    logger.debug("Scanned " + String.valueOf(this.total_scanned) + " / "
                            + String.valueOf(this.total_files_to_process) + ". Deleted: " + String.valueOf(this.files_touched)
                            + "[ " + String.valueOf(getProgress()) + "% ]");

                    if (this.file_errors > 10)
                        aborted = true;

                } catch (Exception e) {
                    logger.error(e);
                }
            }

            if (!aborted && !isStopped()) {
                setProgress(100.0);
                setResult("OK");
                setState(CommandState.COMPLETED);
                setDateTerminated(OffsetDateTime.now());

                logger.debug("Total        : " + String.valueOf(this.total_files_to_process) + " " + "| Processed  : "
                        + String.valueOf(this.total_scanned) + " " + "| Deleted    : " + String.valueOf(this.files_touched) + " "
                        + "| Not Found  : " + String.valueOf(this.files_not_found) + " " + "| Errors     : "
                        + String.valueOf(file_errors));

                setResultComments("Total        : " + String.valueOf(this.total_files_to_process) + " " + "| Processed  : "
                        + String.valueOf(this.total_scanned) + " " + "| Deleted    : " + String.valueOf(this.files_touched) + " "
                        + "| Not Found  : " + String.valueOf(this.files_not_found) + " " + "| Errors     : "
                        + String.valueOf(file_errors));
            } else {

                if (aborted) {
                    setResult("Error");
                    setState(CommandState.ERROR);
                    setDateTerminated(OffsetDateTime.now());
                    setResultComments("Fixed " + String.valueOf(this.files_touched) + " Objects. Total Files to process: "
                            + String.valueOf(this.total_files_to_process) + " OK Files: " + String.valueOf(this.files_ok)
                            + ". Errors: " + String.valueOf(file_errors));

                } else {
                    setResult("Canceled by user");
                    setState(CommandState.CANCELED);
                    setDateTerminated(OffsetDateTime.now());
                    setResultComments("Fixed " + String.valueOf(this.files_touched) + " Objects. Total Files to process: "
                            + String.valueOf(this.total_files_to_process) + " OK Files: " + String.valueOf(this.files_ok)
                            + ". Errors: " + String.valueOf(file_errors));
                }
            }

            logger.debug("Ending Command execution " + getName());

        } catch (Throwable e) {
            logger.error(e);

            setResult("ERROR -> " + e.getClass().getName());
            setResultDetails(e.getMessage());
            setResultComments("Deleted " + String.valueOf(this.files_touched) + " Objects | Total Files to process: "
                    + String.valueOf(this.total_files_to_process) + " | Deleted: " + String.valueOf(this.files_ok) + " | Errors: "
                    + String.valueOf(file_errors));
            setState(CommandState.ERROR);

        } finally {

            if (sf != null)
                com.novamens.hibernate.session.Session.close();

            setStatusInfo("DB Session closed.");

            logger.debug("done");
            logger.debug("-----------------------------------------------------------");
            setDateTerminated(OffsetDateTime.now());
        }
    }

    protected Object reload(Object object) {
        return getContentDao().reload(object);
    }

    /**
     * 
     */
    /**
     * 
     * @param file
     */
    private void deleteFile(KBFileImpl file) {

        Transaction transaction = null;
        boolean is_ok = false;

        try {
            transaction = beginTransaction();
            file.setisExistInObjectStorage(false);
            file.setLastModifiedUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
            file.setLastModifiedOffsetDateTime(OffsetDateTime.now());

            if (file.getTitle() != null) {
                if (!file.getTitle().endsWith("[DELETED]"))
                    file.setTitle(file.getTitle() + " [DELETED]");
            } else
                file.setTitle(file.getId().toString() + " [DELETED]");

            file.setDescription(file.getDescription() + " | Deleted from Object Storage by "
                    + ServiceLocator.getService(SecurityService.class).getSessionUser().getFirstLastName());
            getContentDao().save(file);
            is_ok = true;
            logger.debug("marked " + file.getTitle());
            ;

        } catch (Exception e) {
            logger.error(e);
            this.file_errors++;
            logger.error(e);
            throw new ContentMgmtException(e);

        } finally {
            if (transaction != null) {
                if (is_ok)
                    transaction.commit();
                else {
                    transaction.rollback();
                    return;
                }
            }
        }

        // remove from Object Storage
        //
        if (is_ok) {
            try {
                KBFSResourceService service = file.getService(KBFSResourceService.class);
                service.removeObject();
                logger.debug("removed from Object Storage  " + file.getTitle());
                this.files_touched++;
                setProgress(this.total_files_to_process > 0 ? ((int) 100 * this.total_scanned / (int) this.total_files_to_process)
                        : 100);

            } catch (Throwable e) {
                this.file_errors++;
                logger.error(e);
                throw new ContentMgmtException(e);
            }
        }
    }

}
