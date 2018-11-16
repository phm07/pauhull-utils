package de.pauhull.utils.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.pauhull.utils.scheduler.Scheduler;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * Fetches UUIDs/Names with Mojang-API
 *
 * @author pauhull
 * @version 1.0
 */
public class AsynchronousUUIDFetcher {

    //TODO add cache

    private ExecutorService executor;

    /**
     * Create new AsynchronousUUIDFetcher with given ExecutorService
     *
     * @param executor The ExecutorService
     */
    public AsynchronousUUIDFetcher(ExecutorService executor) {
        this.executor = executor;
    }

    /**
     * Create new AsynchronousUUIDFetcher and create new ExecutorService with given number of Threads
     *
     * @param threads Number of threads
     * @deprecated Use {@link #AsynchronousUUIDFetcher(ExecutorService)} instead
     */
    @Deprecated
    public AsynchronousUUIDFetcher(int threads) {
        this(Scheduler.createExecutorService());
    }

    /**
     * Creates UUID from String
     *
     * @param uuidAsString The UUID as String
     * @return The UUID
     */
    public static UUID parseUUIDFromString(String uuidAsString) {
        String[] parts = {
                "0x" + uuidAsString.substring(0, 8),
                "0x" + uuidAsString.substring(8, 12),
                "0x" + uuidAsString.substring(12, 16),
                "0x" + uuidAsString.substring(16, 20),
                "0x" + uuidAsString.substring(20, 32)
        };

        long mostSigBits = Long.decode(parts[0]).longValue();
        mostSigBits <<= 16;
        mostSigBits |= Long.decode(parts[1]).longValue();
        mostSigBits <<= 16;
        mostSigBits |= Long.decode(parts[2]).longValue();

        long leastSigBits = Long.decode(parts[3]).longValue();
        leastSigBits <<= 48;
        leastSigBits |= Long.decode(parts[4]).longValue();

        return new UUID(mostSigBits, leastSigBits);
    }

    /**
     * Fetches UUID from player name
     *
     * @param playerName The player name
     * @return The UUID
     * @deprecated Use {@link #fetchNameAsync(UUID, Consumer)} instead
     */
    @Deprecated
    public UUID fetchUUIDSync(String playerName) {
        // Get response from Mojang API
        InputStream inputStream;
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() == 400) {
                Bukkit.getLogger().severe("There is no player with the name \"" + playerName + "\"!");
                return null;
            }

            inputStream = connection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        // Parse JSON response and get UUID
        JsonElement element = new JsonParser().parse(bufferedReader);
        JsonObject object = element.getAsJsonObject();
        String uuidAsString = object.get("id").getAsString();

        // Return UUID
        return parseUUIDFromString(uuidAsString);
    }

    /**
     * Fetches name from UUID
     *
     * @param uuid The UUID
     * @return the player name
     * @deprecated Use {@link #fetchNameAsync(UUID, Consumer)} instead
     */
    @Deprecated
    public String fetchNameSync(UUID uuid) {
        // Get response from Mojang API
        InputStream inputStream;
        try {
            URL url = new URL("https://api.mojang.com/user/profiles/" + uuid.toString().replace("-", "") + "/names");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() == 400) {
                Bukkit.getLogger().severe("There is no player with the UUID \"" + uuid.toString() + "\"!");
                return null;
            }

            inputStream = connection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        // Parse JSON response and return name
        JsonElement element = new JsonParser().parse(bufferedReader);
        JsonArray array = element.getAsJsonArray();
        JsonObject object = array.get(array.size()-1).getAsJsonObject();
        return object.get("name").getAsString();

    }

    /**
     * Fetches UUID from player name asynchronously
     *
     * @param playerName The player name
     * @param consumer   Returns the UUID
     */
    public void fetchUUIDAsync(String playerName, Consumer<UUID> consumer) {
        executor.execute(() -> {

            try {
                // Get response from Mojang API
                URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() == 400) {
                    Bukkit.getLogger().severe("There is no player with the name \"" + playerName + "\"!");
                    return;
                }

                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                // Parse JSON response and get UUID
                JsonElement element = new JsonParser().parse(bufferedReader);
                JsonObject object = element.getAsJsonObject();
                String uuidAsString = object.get("id").getAsString();

                inputStream.close();
                bufferedReader.close();

                // Return UUID
                consumer.accept(parseUUIDFromString(uuidAsString));
            } catch (IOException e) {
                Bukkit.getLogger().severe("Couldn't connect to URL.");
                e.printStackTrace();
            }
        });
    }

    /**
     * Fetches player name from UUID asynchronously
     *
     * @param uuid     The UUID
     * @param consumer Returns player name
     */
    public void fetchNameAsync(UUID uuid, Consumer<String> consumer) {
        executor.execute(() -> {
            try {
                // Get response from Mojang API
                URL url = new URL(String.format("https://api.mojang.com/user/profiles/%s/names", uuid.toString().replace("-", "")));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() == 400) {
                    Bukkit.getLogger().severe("There is no player with the UUID \"" + uuid.toString() + "\"!");
                    return;
                }

                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                // Parse JSON response and return name
                JsonElement element = new JsonParser().parse(bufferedReader);
                JsonArray array = element.getAsJsonArray();
                JsonObject object = array.get(array.size()-1).getAsJsonObject();

                bufferedReader.close();
                inputStream.close();

                consumer.accept(object.get("name").getAsString());
            } catch (IOException e) {
                Bukkit.getLogger().severe("Couldn't connect to URL \"https://api.mojang.com/\". Is there an internet connection?");
                e.printStackTrace();
            }
        });
    }

    /**
     * Shuts down executor.
     * Only needs to be done after {@link #AsynchronousUUIDFetcher(int)} is used.
     *
     * @deprecated Create your own ExecutorService with {@link Scheduler} and shut it down yourself.
     */
    @Deprecated
    public void shutdown() {
        executor.shutdown();
    }

}