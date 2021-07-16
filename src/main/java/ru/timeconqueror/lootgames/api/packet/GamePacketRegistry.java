package ru.timeconqueror.lootgames.api.packet;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.Objects;

public abstract class GamePacketRegistry {
    private static final Storage<IClientGamePacket> CLIENT_STORAGE = new Storage<>();
    private static final Storage<IServerGamePacket> SERVER_STORAGE = new Storage<>();

    public static Storage<IClientGamePacket> clientStorage() {
        return CLIENT_STORAGE;
    }

    public static Storage<IServerGamePacket> serverStorage() {
        return SERVER_STORAGE;
    }

    public static class Storage<T> {
        private final BiMap<Key, Class<? extends T>> packets = HashBiMap.create();

        public Class<? extends T> getPacketClass(Key key) {
            Class<? extends T> packet = packets.get(key);
            if (packet == null) {
                throw new NullPointerException(String.format("The packet with key %s doesn't exist.", key));
            }

            return packet;
        }

        public Key getKey(Class<? extends T> packetClass) {
            Key info = packets.inverse().get(packetClass);
            if (info == null) {
                throw new NullPointerException(String.format("The packet %s wasn't registered.", packetClass));
            }

            return info;
        }

        public void regPacket(String modId, int packetId, Class<? extends T> packetClass) {
            Key key = new Key(modId, packetId);

            try {
                if (packets.put(key, packetClass) != null) {
                    throw new RuntimeException(String.format("Id %s has been already registered.", key));
                }
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(String.format("Packet %s has been already registered.", packetClass.getName()));
            }
        }
    }

    public static class Key {
        private final String modId;
        private final int packetId;

        public Key(String modId, int packetId) {
            this.modId = modId;
            this.packetId = packetId;
        }

        public String getModId() {
            return modId;
        }

        public int getPacketId() {
            return packetId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;
            Key that = (Key) o;
            return packetId == that.packetId &&
                    modId.equals(that.modId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(modId, packetId);
        }

        @Override
        public String toString() {
            return modId + ":" + packetId;
        }
    }
}
