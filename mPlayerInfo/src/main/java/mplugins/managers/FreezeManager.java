package mplugins.managers;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FreezeManager {

    private final Set<UUID> frozen = ConcurrentHashMap.newKeySet();

    public boolean isFrozen(UUID uuid) {
        return frozen.contains(uuid);
    }

    public boolean toggle(UUID uuid) {
        if (frozen.contains(uuid)) {
            frozen.remove(uuid);
            return false;
        }
        frozen.add(uuid);
        return true;
    }

    public void setFrozen(UUID uuid, boolean value) {
        if (value) frozen.add(uuid);
        else frozen.remove(uuid);
    }
}
