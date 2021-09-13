import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class PacketDeserializationTest {

    interface TestPacket extends Serializable {
    }

    static class PacketOne implements TestPacket {
        private static final long serialVersionUID = 1L;
        public double data = 0;

        public PacketOne() {
        }
    }

    static class PacketTwo implements TestPacket {
        private static final long serialVersionUID = 1L;
        public double data = 0;

        public PacketTwo() {
        }
    }

    private TestPacket packetFromByteArray(byte[] array) {
        ByteArrayInputStream bis = new ByteArrayInputStream(array);
        try (ObjectInputStream in = new ObjectInputStream(bis)) {
            return (TestPacket) in.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            fail();
            return null;
        }
    }

    private byte[] packetToByteArray(TestPacket testPacket) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(testPacket);
            out.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            fail();
            return null;
        }
    }

    @Test
    public void TestDifferentPacketDeserialization() {
        PacketOne p1 = new PacketOne();
        PacketTwo p2 = new PacketTwo();
        TestPacket result = packetFromByteArray(packetToByteArray(p1));
        assertTrue(result instanceof PacketOne);
        assertFalse(result instanceof PacketTwo);
    }
}
