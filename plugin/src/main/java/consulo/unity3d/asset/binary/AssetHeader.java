/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package consulo.unity3d.asset.binary;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 * @unity SerializedFileHeader
 */
public class AssetHeader {

    // size of the structure data
    private long metadataSize;

    // size of the whole asset file
    private long fileSize;

    // offset to the serialized data
    private long dataOffset;

    // byte order of the serialized data?
    private byte endianness;

    // unused
    private final byte[] reserved = new byte[3];

    private int assetVersion;

    public void read(ByteBuffer in) throws IOException {
        metadataSize = in.getInt();
        fileSize = in.getInt();
        assetVersion = in.getInt();
        dataOffset = in.getInt();

        if (assetVersion >= 9) {
            endianness = in.get();
            in.get(reserved);
        }
        else {
            endianness = in.get();
        }

        if (assetVersion >= 22) {
            metadataSize = in.getInt();
            fileSize = in.getLong();
            dataOffset = in.getLong();
            in.getLong(); // unknown
        }
    }

    public long metadataSize() {
        return metadataSize;
    }

    public void metadataSize(long metadataSize) {
        this.metadataSize = metadataSize;
    }

    public long fileSize() {
        return fileSize;
    }

    public void fileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int version() {
        return assetVersion;
    }

    public void version(int version) {
        assetVersion = version;
    }

    public long dataOffset() {
        return dataOffset;
    }

    public void dataOffset(long dataOffset) {
        this.dataOffset = dataOffset;
    }

    public byte endianness() {
        return endianness;
    }

    public void endianness(byte endianness) {
        this.endianness = endianness;
    }

    public ByteOrder order() {
        // older formats use big endian
        return assetVersion > 5 ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
    }
}
