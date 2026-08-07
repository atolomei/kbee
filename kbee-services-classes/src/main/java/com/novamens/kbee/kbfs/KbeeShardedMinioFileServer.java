package com.novamens.kbee.kbfs;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.dom.KBFSStorageType;
import com.novamens.kbfs.FileServerException;
import com.novamens.kbfs.FileServerMinio;

import com.novamens.kbfs.LocalFileServerCache;
import com.novamens.service.ServiceLocator;

import io.odilon.util.FileNameNormalizer;
import kbee.util.PropertiesFactory;

/**
 * If file does not have fsid, we use file shard, if not we infer based on
 * bucket, object.
 * 
 * SHARD 1 ------- kbfs2.endpoint kbfs2.accesskey kbfs2.secretkey
 * kbfs2.probability kbfs2.fsid kbfs2.type [kbfs2. Amazon S3]
 * 
 * OR
 * 
 * kbfs2.shard1.endpoint kbfs2.shard1.accesskey kbfs2.shard1.secretkey
 * kbfs2.shard1.probability kbfs2.shard1.fsid
 * 
 * 
 * SHARD 2 ------- kbfs2.shard2.endpoint kbfs2.shard2.accesskey
 * kbfs2.shard2.secretkey kbfs2.shard2.probability
 * 
 * 
 * SHARD 3 ------- kbfs2.shard3.endpoint kbfs2.shard3.accesskey
 * kbfs2.shard3.secretkey kbfs2.shard3.probability
 * 
 * 
 * SHARD 4 ...
 *
 * 
 */
public class KbeeShardedMinioFileServer implements FileServerMinio {

    static private Logger logger = LogManager.getLogger(KbeeShardedMinioFileServer.class.getName());
    static private Logger startupLogger = LogManager.getLogger("StartupLogger");

    static final double MB = 1000000;
    static final int MAX_SHARD = 200;

    static final int BUFFER_SIZE = 8192;

    private int total_shards = 1;

    private LocalFileServerCache fslocalcache;

    private List<Double> shard_probability = new ArrayList<Double>();

    private Map<Integer, FileServerMinio> f2_minio_shard = new ConcurrentHashMap<Integer, FileServerMinio>();

    private Map<String, Integer> f2_minio_fsid_shard = new ConcurrentHashMap<String, Integer>();

    private boolean _initialized = false;

    private final Integer ONE = Integer.valueOf(1);

    private int var_shard_counter = 0;
    private Integer var_first_writable_shard = Integer.valueOf(1); // Shards start from 1

    private boolean minor = false;

    public KbeeShardedMinioFileServer() throws FileServerException {
        startupLogger.info("Minio Shard Manager");
        startupLogger.info(" Starting Sharded Minio File Server");
    }

    @Override
    public String getDisplayName() {
        return this.getClass().getSimpleName();
    }

    
	@Override
	public String normalize(String name) {
		return FileNameNormalizer.normalize(name);
	}
    
    @Override
    public KBFSStorageType getKBFSStorageType() {
        return KBFSStorageType.Minio;
    }

    public Map<Integer, FileServerMinio> getShards() {
        if (!isInitialized()) {
            try {
                init();
            } catch (FileServerException e) {
                logger.error("init", e);
            }
        }
        return f2_minio_shard;
    }

    public Map<String, Integer> getShardIds() {
        if (!isInitialized()) {
            try {
                init();
            } catch (FileServerException e) {
                logger.error("init", e);
            }
        }
        return f2_minio_fsid_shard;
    }

    @Override
    public void putObject(Integer shard, String bucketName, String objectName, String filename, InputStream stream, long size,
            String contentType) throws FileServerException {
        getShards().get(shard).putObject(bucketName, objectName, filename, stream, size, contentType);
    }

    /**
     * shard is a f(bucketName, objectName)
     */
    @Override
    public void putObject(String bucketName, String objectName, String filename, InputStream stream, long size, String contentType)
            throws FileServerException {
        getShards().get(getShard(bucketName, objectName)).putObject(bucketName, objectName, filename, stream, size, contentType);
    }

    @Override
    public void putObject(Integer shard, String bucketName, String objectName, String filename, InputStream stream,
            String contentType) throws FileServerException {
        getShards().get(shard).putObject(bucketName, objectName, filename, stream, contentType);
    }

    /**
     * shard is a f(bucketName, objectName)
     */
    @Override
    public void putObject(String bucketName, String objectName, String filename, InputStream stream, String contentType)
            throws FileServerException {
        getShards().get(getShard(bucketName, objectName)).putObject(bucketName, objectName, filename, stream, contentType);
    }

    @Override
    public void putObject(Integer shard, String bucketName, String objectName, String filename, InputStream stream)
            throws FileServerException {
        getShards().get(shard).putObject(bucketName, objectName, filename, stream);
    }

    /**
     * shard is a f(bucketName, objectName)
     */
    @Override
    public void putObject(String bucketName, String objectName, String filename, InputStream stream) throws FileServerException {
        getShards().get(getShard(bucketName, objectName)).putObject(bucketName, objectName, filename, stream);
    }

    @Override
    public void putObject(Integer shard, String bucketName, String objectName, String fileName) throws FileServerException {
        getShards().get(shard).putObject(bucketName, objectName, fileName);
    }

    /**
     * shard is a f(bucketName, objectName)
     */
    @Override
    public void putObject(String bucketName, String objectName, String fileName) throws FileServerException {
        getShards().get(getShard(bucketName, objectName)).putObject(bucketName, objectName, fileName);
    }

    @Override
    public String presignedGetObject(Integer shard, String bucketName, String objectName) throws FileServerException {
        return getShards().get(shard).presignedGetObject(bucketName, objectName);
    }

    /**
     * shard is a f(bucketName, objectName)
     */
    @Override
    public String presignedGetObject(String bucketName, String objectName) throws FileServerException {
        return getShards().get(getShard(bucketName, objectName)).presignedGetObject(bucketName, objectName);
    }

    @Override
    public String presignedGetObject(Integer shard, String bucketName, String objectName, int expires_seconds)
            throws FileServerException {
        return getShards().get(shard).presignedGetObject(bucketName, objectName, expires_seconds);
    }

    /**
     * shard is a f(bucketName, objectName)
     */
    @Override
    public String presignedGetObject(String bucketName, String objectName, int expires_seconds) throws FileServerException {
        return getShards().get(getShard(bucketName, objectName)).presignedGetObject(bucketName, objectName, expires_seconds);
    }

    @Override
    public InputStream getObject(Integer shard, String bucketName, String objectName) throws FileServerException {
        return getShards().get(shard).getObject(bucketName, objectName);
    }

    /**
     * shard is a f(bucketName, objectName)
     */
    @Override
    public InputStream getObject(String bucketName, String objectName) throws FileServerException {
        return getShards().get(getShard(bucketName, objectName)).getObject(bucketName, objectName);
    }

    @Override
    public InputStream getObject(String fsid, String bucketName, String objectName) throws FileServerException {
        if (getShardIds().containsKey(fsid))
            return getObject(getShardIds().get(fsid), bucketName, objectName);
        throw new FileServerException("Invalid fsid " + fsid);
    }

    @Override
    public File getDownloadedFile(Integer shard, String bucketName, String objectName, String fileName) throws FileServerException {
        return getShards().get(shard).getDownloadedFile(bucketName, objectName, fileName);
    }

    @Override
    public File getDownloadedFile(String bucketName, String objectName, String fileName) throws FileServerException {
        return getShards().get(getShard(bucketName, objectName)).getDownloadedFile(bucketName, objectName, fileName);
    }

    @Override
    public void removeObject(Integer shard, String bucketName, String objectName) throws FileServerException {
        getShards().get(shard).removeObject(bucketName, objectName);
    }

    @Override
    public void removeObject(String bucketName, String objectName) throws FileServerException {
        this.getShards().get(getShard(bucketName, objectName)).removeObject(bucketName, objectName);
    }

    @Override
    public String ping(Integer shard) {

        if (!isInitialized()) {
            try {
                init();
            } catch (FileServerException e) {
                logger.error("ping", e);
            }
        }
        return getShards().get(shard).ping();
    }

    @Override
    public String ping() {
        if (!isInitialized()) {
            try {
                init();
            } catch (FileServerException e) {
                logger.error("ping", e);
            }
        }
        return getShards().get(Integer.valueOf(1)).ping();

    }

    @Override
    public boolean isObject(Integer shard, String bucketName, String objectName) throws FileServerException {
        return getShards().get(shard).isObject(bucketName, objectName);
    }

    @Override
    public boolean isObject(String bucketName, String objectName) throws FileServerException {
        return getShards().get(getShard(bucketName, objectName)).isObject(bucketName, objectName);
    }

    @Override
    public String getEndPoint(Integer shard) {
        return getShards().get(shard).getEndPoint();
    }

    @Override
    public String getEndPoint() {
        return getShards().get(Integer.valueOf(1)).getEndPoint();
    }

    @Override
    public String getAccessKey(Integer shard) {
        return getShards().get(shard).getAccessKey();
    }

    @Override
    public String getAccessKey() {
        return getShards().get(Integer.valueOf(1)).getAccessKey();
    }

    @Override
    public String getSecretKey(Integer shard) {
        return getShards().get(shard).getSecretKey();
    }

    @Override
    public String getSecretKey() {
        return getShards().get(Integer.valueOf(1)).getSecretKey();
    }

    @Override
    public String reconnect(Integer shard, String url, String accessKey, String secretKey) throws FileServerException {
        return getShards().get(shard).reconnect(url, accessKey, secretKey);
    }

    @Override
    public String reconnect(String url, String accessKey, String secretKey) throws FileServerException {
        return getShards().get(Integer.valueOf(1)).reconnect(url, accessKey, secretKey);
    }

    @Override
    public String getFSId() {
        return "minio-shard-id";
    }

    @Override
    public Integer getShard(String fsid) {
        if (getShardIds().containsKey(fsid))
            return getShardIds().get(fsid);
        return null;
    }

    @Override
    public String getFSId(Integer shard) {
        return getShards().get(Integer.valueOf(shard)).getFSId();
    }

    public int getCacheSize() {
        return getLocalFileServerCache().getTotalItems();
    }

    public long getCacheUsage() {
        return getLocalFileServerCache().getTotalDisk();
    }

    private LocalFileServerCache getLocalFileServerCache() {
        if (fslocalcache == null)
            fslocalcache = ServiceLocator.getService(LocalFileServerCache.class);
        return fslocalcache;
    }

    public List<String> listBuckets() throws FileServerException {
        return null;
    }

    /**
     * 1,2,3,4..total shards
     */
    @Override
    public Integer getShard(String bucketName, String objectName) {

        if (this.total_shards == 1 || this.shard_probability.get(0) > 0.99) {
            logger.debug(bucketName + "-" + objectName + " . Shard: 1");
            return ONE;
        }

        int val = Math.abs((bucketName + "-" + objectName).hashCode() % 100);
        Double dval = Double.valueOf(val) / 100.0;
        Integer n = Integer.valueOf(1);
        double floor = 0.0;

        for (Double shard : shard_probability) {
            if (shard > 0.0 && dval >= floor && dval < (floor + shard)) {
                logger.debug(bucketName + "-" + objectName + " . Shard: " + n);
                return n;
            }
            n++;
            floor += shard;
        }

        logger.debug(bucketName + "-" + objectName + " . Shard (default): " + var_first_writable_shard);
        return var_first_writable_shard;
    }

    @Override
    public Integer getShard() {
        return ONE;
    }

    @Override
    public double getProbability() {
        return 1.0;
    }

    @Override
    public void setProbability(double d) {
    }

    @Override
    public String presignedGetObject(String fsid, String bucketName, String objectName) throws FileServerException {
        Integer sh = getShard(fsid);
        if (sh == null || !getShards().containsKey(sh))
            throw new FileServerException("Invalid fsid: " + fsid);
        return getShards().get(sh).presignedGetObject(bucketName, objectName);
    }

    @Override
    public String presignedGetObject(String fsid, String bucketName, String objectName, int expires_seconds)
            throws FileServerException {
        Integer sh = getShard(fsid);
        if (sh == null || !getShards().containsKey(sh))
            throw new FileServerException("Invalid fsid: " + fsid);
        return getShards().get(sh).presignedGetObject(bucketName, objectName, expires_seconds);

    }

    @Override
    public File getDownloadedFile(String fsid, String bn, String on, String fileName) throws FileServerException {
        Integer sh = getShard(fsid);
        if (sh == null || !getShards().containsKey(sh))
            throw new FileServerException("Invalid fsid: " + fsid);
        return getShards().get(sh).getDownloadedFile(bn, on, fileName);
    }

    @Override
    public void removeObject(String fsid, String bucketName, String objectName) throws FileServerException {
        Integer sh = getShard(fsid);
        if (sh == null || !getShards().containsKey(sh))
            throw new FileServerException("Invalid fsid: " + fsid);
        removeObject(sh, bucketName, objectName);
    }

    @Override
    public boolean isObject(String fsid, String bucketName, String objectName) throws FileServerException {
        Integer sh = getShard(fsid);
        if (sh == null || !getShards().containsKey(sh))
            throw new FileServerException("Invalid fsid: " + fsid);
        return isObject(sh, bucketName, objectName);
    }

    private boolean isReadOnly = false;

    public void setReadOnly(boolean b) {
        this.isReadOnly = b;
    }

    @Override
    public boolean isReadOnly() {
        return isReadOnly;
    }

    /**
     * 
     * Minimum 1 MinioFileServer Max 6 MinioServers. Each of them can be Stand alone
     * or Distributed
     * 
     * @throws FileServerException
     * 
     */
    private synchronized void init() throws FileServerException {

        if (this._initialized)
            return;

        synchronized (this) {

            if (this._initialized)
                return;

            String URL = PropertiesFactory.getInstance("kbee").getProperties()
                    .getProperty("kbfs2.endpoint", "http://localhost:9000").trim();
            String ACCESSKEY = PropertiesFactory.getInstance("kbee").getProperties()
                    .getProperty("kbfs2.accesskey", "2H5GJ4KJ7JRUILRTRSLG").trim();
            String READONLY = PropertiesFactory.getInstance("kbee").getProperties().getProperty("kbfs2.readonly", "no")
                    .toLowerCase().trim();
            String SECRETKEY = PropertiesFactory.getInstance("kbee").getProperties()
                    .getProperty("kbfs2.secretkey", "1Hgja+uhRdRTm5N8lPn4wNCSHKDJV7yfVwzPPsU0").trim();
            String FSID = PropertiesFactory.getInstance("kbee").getProperties().getProperty("kbfs2.fsid", "fs-dev1").trim();
            String TYPE = PropertiesFactory.getInstance("kbee").getProperties().getProperty("kbfs2.type", "minio").trim();

            String URL1 = PropertiesFactory.getInstance("kbee").getProperties().getProperty("kbfs2.shard1.endpoint", URL).trim();
            String ACCESSKEY1 = PropertiesFactory.getInstance("kbee").getProperties()
                    .getProperty("kbfs2.shard1.accesskey", ACCESSKEY).trim();
            String SECRETKEY1 = PropertiesFactory.getInstance("kbee").getProperties()
                    .getProperty("kbfs2.shard1.secretkey", SECRETKEY).trim();
            String READONLY1 = PropertiesFactory.getInstance("kbee").getProperties().getProperty("kbfs2.shard1.readonly", READONLY)
                    .toLowerCase().trim();
            String FSID1 = PropertiesFactory.getInstance("kbee").getProperties().getProperty("kbfs2.shard1.fsid", FSID).trim();
            String TYPE1 = PropertiesFactory.getInstance("kbee").getProperties().getProperty("kbfs2.type", TYPE).toLowerCase()
                    .trim();

            try {

                if (TYPE1.equals("minio")) {

                    KbeeMinioFileServer kbfs = new KbeeMinioFileServer(URL1, ACCESSKEY1, SECRETKEY1, FSID1,
                            Integer.valueOf(++var_shard_counter), 1.0);
                    kbfs.setReadOnly(READONLY1.equals("yes") || READONLY1.equals("true"));
                    this.f2_minio_shard.put(Integer.valueOf(var_shard_counter), kbfs);
                    this.f2_minio_fsid_shard.put(kbfs.getFSId(), Integer.valueOf(var_shard_counter));
                }

                for (int mfs = 2; mfs < MAX_SHARD; mfs++)
                    add(mfs);

                setProbabilities();
            } finally {
                this._initialized = true;
            }
        }
    }

    /**
     * 
     * 
     */
    private void setProbabilities() {

        Double total_rw = Double.valueOf(0.0);
        boolean found = false;
        for (Entry<Integer, FileServerMinio> entry : this.f2_minio_shard.entrySet()) {
            if (!entry.getValue().isReadOnly()) {
                total_rw += 1.0;
                if (!found) {
                    var_first_writable_shard = entry.getValue().getShard();
                    found = true;
                }
            }
        }

        if (total_rw < 1.0)
            total_rw = 1.0;

        Double pbase = Double.valueOf(1.00 / total_rw);
        Double ptotal = Double.valueOf(0.0);

        for (int t = 0; t < this.f2_minio_shard.size(); t++) {
            String pbs;

            if (t == 0) {
                pbs = PropertiesFactory.getInstance("kbee").getProperties().getProperty("kbfs2.probability", "null").trim();
                if (pbs.equals("null"))
                    pbs = PropertiesFactory.getInstance("kbee").getProperties()
                            .getProperty("kbfs2.shard" + String.valueOf(t + 1).trim() + ".probability", String.valueOf(pbase))
                            .trim();
            } else
                pbs = PropertiesFactory.getInstance("kbee").getProperties()
                        .getProperty("kbfs2.shard" + String.valueOf(t + 1).trim() + ".probability", String.valueOf(pbase)).trim();

            Double pb;
            try {

                if (this.f2_minio_shard.containsKey(Integer.valueOf(t + 1)) && this.f2_minio_shard.get(t + 1).isReadOnly())
                    pb = Double.valueOf(0.0);
                else
                    pb = Double.valueOf(pbs);

            } catch (Exception e) {
                pb = Double.valueOf(pbase);
            }

            this.shard_probability.add(pb);
            if (this.f2_minio_shard.containsKey(Integer.valueOf(t + 1))) {
                this.f2_minio_shard.get(Integer.valueOf(t + 1)).setProbability(pb);
            }
            ptotal += pb;
        }

        this.total_shards = this.shard_probability.size();

        if (ptotal > 1.00 || ptotal < 0.99) {
            if (ptotal < 0.1) {
                this.shard_probability = new ArrayList<Double>();
                this.shard_probability.set(0, Double.valueOf(1.0));
                this.f2_minio_shard.get(1).setProbability(1.0);
            } else if (ptotal > 1.00) {
                if (total_rw > 0) {
                    Double normalize_factor = Double.valueOf(1.0 / ptotal);
                    for (int n = 0; n < this.shard_probability.size(); n++) {
                        this.shard_probability.set(n, this.shard_probability.get(n) * normalize_factor);
                        if (this.f2_minio_shard.containsKey(Integer.valueOf(n + 1))) {
                            this.f2_minio_shard.get(Integer.valueOf(n + 1)).setProbability(this.shard_probability.get(n));
                        }
                    }
                }
            } else {
                if (total_rw > 0) {
                    double to_add = (1.0 - ptotal) / total_rw;
                    for (int n = 0; n < this.shard_probability.size(); n++) {
                        if (this.f2_minio_shard.containsKey(Integer.valueOf(n + 1))
                                && !(this.f2_minio_shard.get(Integer.valueOf(n + 1)).isReadOnly())) {
                            this.shard_probability.set(n, this.shard_probability.get(n) + to_add);
                            this.f2_minio_shard.get(Integer.valueOf(n + 1)).setProbability(this.shard_probability.get(n));

                        }
                    }
                }
            }
        }

        startupLogger.info("Minio_Shard_Manager");
        startupLogger.info("Total Minio Servers ->" + String.valueOf(total_shards));

        int k = 1;
        for (Double d : this.shard_probability) {
            startupLogger.info("Minio_Shard_Manager. Shard_" + String.valueOf(k) + ". Probability  -> " + String.valueOf(d));
            k++;
        }
    }

    @Override
    public void setMinor(boolean b) {
        minor = b;
    }

    @Override
    public boolean isMinor() {
        return minor;
    }

    private void add(int order) throws FileServerException {

        final String _URL = PropertiesFactory.getInstance("kbee").getProperties()
                .getProperty("kbfs2.shard" + String.valueOf(order).trim() + ".endpoint", "null").trim();

        if (!_URL.equals("null")) {

            final String _ACCESSKEY = PropertiesFactory.getInstance("kbee").getProperties()
                    .getProperty("kbfs2.shard" + String.valueOf(order).trim() + ".accesskey", "null").trim();
            final String _READONLY = PropertiesFactory.getInstance("kbee").getProperties()
                    .getProperty("kbfs2.shard" + String.valueOf(order).trim() + ".readonly", "no").toLowerCase().trim();
            final String _SECRETKEY = PropertiesFactory.getInstance("kbee").getProperties()
                    .getProperty("kbfs2.shard" + String.valueOf(order).trim() + ".secretkey", "null").trim();
            final String _FSID = PropertiesFactory.getInstance("kbee").getProperties()
                    .getProperty("kbfs2.shard" + String.valueOf(order).trim() + ".fsid", "fs-dev8").trim();
            final String _TYPE = PropertiesFactory.getInstance("kbee").getProperties()
                    .getProperty("kbfs2.shard" + String.valueOf(order).trim() + ".type", "minio").trim();
            final String _MINOR = PropertiesFactory.getInstance("kbee").getProperties()
                    .getProperty("kbfs2.shard" + String.valueOf(order).trim() + ".minor", "no").trim();

            if (_TYPE.equals("minio")) {
                KbeeMinioFileServer fs = new KbeeMinioFileServer(_URL, _ACCESSKEY, _SECRETKEY, _FSID,
                        Integer.valueOf(++var_shard_counter));
                fs.setMinor(_MINOR.equals("yes"));
                String pg = fs.ping();
                if (pg.equals("ok")) {
                    fs.setReadOnly(_READONLY.equals("yes") || _READONLY.equals("true"));
                    this.f2_minio_shard.put(Integer.valueOf(var_shard_counter), fs);
                    this.f2_minio_fsid_shard.put(this.f2_minio_shard.get(Integer.valueOf(var_shard_counter)).getFSId(),
                            Integer.valueOf(var_shard_counter));
                } else {

                    startupLogger.error("Shard " + order + " - " + _URL + " | Ping: " + pg);
                    throw new FileServerException("Shard " + order + " - " + _URL + " | Ping: " + pg);
                }

                startupLogger.info("Minio_Shard_Manager. Added Shard_" + String.valueOf(order) + " -> " + _URL);

            }
        } else {
            final String _ACCESSKEY = PropertiesFactory.getInstance("kbee").getProperties()
                    .getProperty("kbfs2.shard" + String.valueOf(order).trim() + ".accesskey", "").trim();
            if (_ACCESSKEY != null && _ACCESSKEY.length() > 0)
                startupLogger
                        .error("There is an AccessKey without endpoint -> " + _ACCESSKEY + " \n Please check configuration file");
        }
    }

    private boolean isInitialized() {
        return _initialized;
    }

    @Override
    public Map<String, String> getInfo() {
        return new HashMap<String, String>();
    }

}
