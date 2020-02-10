package io.neow3j.transaction;

import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.IOUtils;
import io.neow3j.io.NeoSerializable;
import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.model.types.TransactionAttributeUsageType;
import io.neow3j.utils.Numeric;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class TransactionAttribute extends NeoSerializable {

    public TransactionAttributeUsageType usage;

    public byte[] data;

    public TransactionAttribute() {
    }

    public TransactionAttribute(TransactionAttributeUsageType usage, byte[] data) {
        this.usage = usage;
        this.data = data;
        if (usage.fixedDataLength() != null && data.length != usage.fixedDataLength()) {
            throw new IllegalArgumentException("The data has different length than the length " +
                    "required by the attribute usage type.");
        }
        if (usage.maxDataLength() != null && data.length > usage.maxDataLength()) {
            throw new IllegalArgumentException("The data is longer then the maximum length " +
                    "allowed by the attribute usage type.");
        }
    }

    public TransactionAttribute(TransactionAttributeUsageType usage, String data) {
        this(usage, (data != null ? Numeric.hexStringToByteArray(data) : null));
    }

    public TransactionAttributeUsageType getUsage() {
        return usage;
    }

    public byte[] getDataAsBytes() {
        return this.data;
    }

    public String getData() {
        return this.data != null ? Numeric.toHexString(data) : null;
    }


    @Override
    public int getSize() {
        int size = 1 // Type byte
                + this.data.length; // Attribute data size
        if (this.usage.fixedDataLength() == null) {
            size += IOUtils.getVarSize(this.data.length); // Data size integer
        }
        return size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TransactionAttribute)) {
            return false;
        }
        TransactionAttribute that = (TransactionAttribute) o;
        return getUsage() == that.getUsage() &&
                Arrays.equals(getDataAsBytes(), that.getDataAsBytes());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getUsage());
        result = 31 * result + Arrays.hashCode(getDataAsBytes());
        return result;
    }

    @Override
    public String toString() {
        return "TransactionAttribute{" +
                "usage=" + usage +
                ", data=" + (data != null ? Numeric.toHexStringNoPrefix(data) : "null") +
                '}';
    }

    @Override
    public void deserialize(BinaryReader reader) throws DeserializationException {
        try {
            this.usage = TransactionAttributeUsageType.valueOf(reader.readByte());
            if (usage.fixedDataLength() != null) {
                this.data = reader.readBytes(usage.fixedDataLength());
            } else if (usage.maxDataLength() != null) {
                this.data = reader.readVarBytes(usage.maxDataLength());
            } else {
                this.data = reader.readVarBytes();
            }
        } catch (IOException e) {
            throw new DeserializationException(e);
        }
    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        writer.writeByte(usage.byteValue());
        if (usage.fixedDataLength() != null) {
            writer.write(data);
        } else {
            writer.writeVarBytes(data);
        }
    }
}