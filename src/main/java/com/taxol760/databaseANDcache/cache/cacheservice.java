package com.taxol760.databaseANDcache.cache;

import com.taxol760.service.auth.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class cacheservice {
    private final String Redis_Geo ="Geo";
    private final String OCCUPIED_DRIVERS_SET = "occupied-drivers";
    private static final String DRIVER_INFO_KEY_PREFIX = "live-driver:";
    private static final Duration LIVE_DRIVER_TTL = Duration.ofMinutes(2);

    private final RedisTemplate<String, Object> redisTemplate;
    private final CurrentUserService currentUserService;

    public void addDriver(int id) {
        redisTemplate.opsForGeo().add(Redis_Geo,new Point(70,67),id);
    }

    public void addDriver(CachedDriverInfo driverInfo, double lon, double lat) {
        redisTemplate.opsForGeo().add(Redis_Geo, new Point(lon, lat), driverInfo.id().intValue());
        setDriverInfo(driverInfo);
    }

    public void delDriver(int id) {
        redisTemplate.opsForGeo().remove(Redis_Geo,id);
        redisTemplate.delete(driverInfoKey(id));
    }

    public List<Integer> suggestDriverIds(double lon, double lat) {
        Circle searchArea = new Circle(new Point(lon, lat), new Distance(15, Metrics.KILOMETERS));

        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance()
                .sortAscending()
                .limit(20);

        var geoResults = redisTemplate.opsForGeo().radius(Redis_Geo, searchArea, args);

        if (geoResults == null) return List.of();

        return geoResults.getContent().stream()
                .map(result -> ((Number) result.getContent().getName()).intValue())
                .filter(id -> !isDriverOccupied(id))
                .limit(4)
                .toList();
    }

    public List<CachedDriverInfo> suggestDrivers(double lon, double lat) {
        return suggestDriverIds(lon, lat)
                .stream()
                .map(this::getLiveDriverInfo)
                .filter(Objects::nonNull)
                .toList();
    }


    public void setDriverOccupied(int driverId) {
        redisTemplate.opsForSet().add(OCCUPIED_DRIVERS_SET, String.valueOf(driverId));
    }

    public void setDriverFree(int driverId) {
        redisTemplate.opsForSet().remove(OCCUPIED_DRIVERS_SET, String.valueOf(driverId));
    }

    public boolean isDriverOccupied(int driverId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(OCCUPIED_DRIVERS_SET, String.valueOf(driverId)));
    }

    public void updateDriver(CachedDriverInfo driverInfo, double lon, double lat) {
        redisTemplate.opsForGeo().add(Redis_Geo, new Point(lon, lat), driverInfo.id().intValue());
        setDriverInfo(driverInfo);
    }

    public void refreshDriverInfo(CachedDriverInfo driverInfo) {
        String key = driverInfoKey(driverInfo.id());
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            setDriverInfo(driverInfo);
        }
    }

    public CachedDriverInfo getCachedDriverInfo(int id) {
        Object value = redisTemplate.opsForValue().get(driverInfoKey(id));

        if (value instanceof CachedDriverInfo driverInfo) {
            return driverInfo;
        }

        return null;
    }

    private void setDriverInfo(CachedDriverInfo driverInfo) {
        redisTemplate.opsForValue().set(driverInfoKey(driverInfo.id()), driverInfo, LIVE_DRIVER_TTL);
    }

    private CachedDriverInfo getLiveDriverInfo(int id) {
        CachedDriverInfo driverInfo = getCachedDriverInfo(id);
        if (driverInfo != null) {
            return driverInfo;
        }

        redisTemplate.opsForGeo().remove(Redis_Geo, id);
        return null;
    }

    private String driverInfoKey(long id) {
        return DRIVER_INFO_KEY_PREFIX + id;
    }
}
