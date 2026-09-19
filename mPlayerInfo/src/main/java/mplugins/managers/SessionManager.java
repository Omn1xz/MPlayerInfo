package mplugins.managers;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private final Map<UUID, Long> joinTimes = new ConcurrentHashMap<>();

    public void markJoin(UUID uuid, long millis) {
        joinTimes.put(uuid, millis);
    }

    public void markJoin(UUID uuid) {
        markJoin(uuid, System.currentTimeMillis());
    }

    public void markQuit(UUID uuid) {
        joinTimes.remove(uuid);
    }

    public long getSessionMillis(UUID uuid) {
        Long join = joinTimes.get(uuid);
        if (join == null) return 0L;
        return Math.max(0L, System.currentTimeMillis() - join);
    }

    public boolean hasSession(UUID uuid) {
        return joinTimes.containsKey(uuid);
    }
}
