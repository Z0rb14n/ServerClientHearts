package net.message;

import java.io.*;

public interface NetworkMessage extends Serializable {
    boolean isValid();

    static NetworkMessage packetFromByteArray(byte[] array) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(array);
        ObjectInputStream in = new ObjectInputStream(bis);
        return (NetworkMessage) in.readObject();
    }

    static byte[] packetToByteArray(NetworkMessage testPacket) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(testPacket);
        out.flush();
        return bos.toByteArray();
    }
}
