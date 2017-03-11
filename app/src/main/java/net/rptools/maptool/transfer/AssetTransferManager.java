package net.rptools.maptool.transfer;

import com.bitfighters.maptool.maptoolinput.MyData;

import net.rptools.maptool.model.Asset;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin on 10.03.2017.
 */


public class AssetTransferManager {
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    private Map<Serializable, AssetConsumer> consumerMap = new HashMap<Serializable, AssetConsumer>();

    private static AssetTransferManager manager;

    public static AssetTransferManager getInstance(){
        if(manager == null)
            manager = new AssetTransferManager();
        return manager;
    }

    public synchronized void flush() {
        consumerMap.clear();
    }

    /**
     * Update the appropriate asset.  To be notified when the asset is complete add a ConsumerListener.
     * When the asset is complete it will be removed from the internal map automatically
     * @throws IOException
     */
    public synchronized void update(AssetChunk chunk) throws IOException {
        AssetConsumer consumer = consumerMap.get(chunk.getId());
        if (consumer == null) {
            throw new IllegalArgumentException("Not expecting chunk: " + chunk.getId());
        }
        consumer.update(chunk);
        if (consumer.isComplete()) {
            consumerMap.remove(consumer.getId());
            assetComplete(consumer.getId(), consumer.getName(), consumer.getFilename());
        }
    }

    public synchronized void addConsumer(AssetConsumer consumer) {
        if (consumerMap.get(consumer.getId()) != null) {
            throw new IllegalArgumentException("Asset is already being downloaded: " + consumer.getId());
        }
        consumerMap.put(consumer.getId(), consumer);
    }

    public void assetComplete(Serializable id, String name, File data) {
        byte[] assetData = null;
        try {
            assetData = readFileToByteArray(data);
        } catch (IOException ioe) {
            System.err.println("Error loading composed asset file: " + id);
            return;
        }
        Asset asset = new Asset(name, assetData);
        // Install it into our system
        MyData.instance.putAsset(asset);

        // Remove the temp file
        data.delete();
    }

    public static byte[] readFileToByteArray(File file) throws IOException {
        InputStream in = null;
        try {
            in = openInputStream(file);
            return toByteArray(in);
        } finally {
            closeQuietly(in);
        }
    }

    public static FileInputStream openInputStream(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (file.canRead() == false) {
                throw new IOException("File '" + file + "' cannot be read");
            }
        } else {
            throw new FileNotFoundException("File '" + file + "' does not exist");
        }
        return new FileInputStream(file);
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    public static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    public static long copyLarge(InputStream input, OutputStream output)
            throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
    public static void closeQuietly(InputStream input) {
        closeQuietly((Closeable)input);
    }
    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }
}

