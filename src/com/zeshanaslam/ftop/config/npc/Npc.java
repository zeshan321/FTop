package com.zeshanaslam.ftop.config.npc;

import java.util.Objects;
import java.util.UUID;

public class Npc {
    public int rank;
    public SafeLocation safeLocation;
    public UUID npcUUID;

    public Npc(int rank, SafeLocation safeLocation, UUID npcUUID) {
        this.rank = rank;
        this.safeLocation = safeLocation;
        this.npcUUID = npcUUID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Npc npc = (Npc) o;
        return Objects.equals(npcUUID, npc.npcUUID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(npcUUID);
    }
}
