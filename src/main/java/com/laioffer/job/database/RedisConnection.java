package com.laioffer.job.database;

import redis.clients.jedis.Jedis;

public class RedisConnection {
    private static final String INSTANCE = "18.217.195.158";
    private static final int PORT = 6379;
    private static final String PASSWORD = "12345678";
    private static final String SEARCH_KEY_TEMPLATE = "search:lat=%s&lon=%s&keyword=%s";
    private static final String FAVORITE_KEY_TEMPLATE = "history:userId=%s";

    //Add methods to help you create and close connections to Redis
    private Jedis jedis;

    public RedisConnection() {
        jedis = new Jedis(INSTANCE, PORT);
        jedis.auth(PASSWORD);
    }

    public void close() {
        jedis.close();
    }

    //use Redis to temporarily cache the job information returned by GitHub, so if
    //users keep searching for jobs in a given location, instead of calling GitHub
    //API every time, we can simply read the data from Redis
    public String getSearchResult(double lat, double lon, String keyword) {
        if (jedis == null) {
            return null;
        }
        String key = String.format(SEARCH_KEY_TEMPLATE, lat, lon, keyword);
        return jedis.get(key);
    }

    public void setSearchResult(double lat, double lon, String keyword, String value) {
        if (jedis == null) {
            return;
        }
        String key = String.format(SEARCH_KEY_TEMPLATE, lat, lon, keyword);
        jedis.set(key, value);
        jedis.expire(key, 10);   //key的有效期10 second
    }


    //Similar to search we can use Redis to cache favorite history data to do
    //get, set and delete favorite
    public String getFavoriteResult(String userId) {
        if (jedis == null) {
            return null;
        }
        String key = String.format(FAVORITE_KEY_TEMPLATE, userId);
        return jedis.get(key);
    }

    public void setFavoriteResult(String userId, String value) {
        if (jedis == null) {
            return;
        }
        String key = String.format(FAVORITE_KEY_TEMPLATE, userId);
        jedis.set(key, value);
        jedis.expire(key, 10);
    }

    public void deleteFavoriteResult(String userId) {
        if (jedis == null) {
            return;
        }
        String key = String.format(FAVORITE_KEY_TEMPLATE, userId);
        jedis.del(key);
    }
}
