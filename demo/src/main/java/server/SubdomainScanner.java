package server;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

public class SubdomainScanner {
    private final List<String> subdomains;
    private static final int THREAD_POOL_SIZE = 50;

    public SubdomainScanner() {
        subdomains = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream("src/main/resources/subdomains.txt")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                subdomains.add(line.trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String scanSubdomains(String domain) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        List<Future<String>> futures = new ArrayList<>();

        for (String sub : subdomains) {
            String fullSubdomain = sub + "." + domain;
            futures.add(executor.submit(() -> checkSubdomain(fullSubdomain)));
        }

        StringBuilder result = new StringBuilder();
        for (Future<String> future : futures) {
            try {
                String validSub = future.get();
                if (validSub != null) {
                    result.append(validSub).append("\n");
                }
            } catch (Exception ignored) {
            }
        }

        executor.shutdown();

        return result.length() > 0 ? result.toString() : "Không tìm thấy subdomain";
    }

    private String checkSubdomain(String fullSubdomain) {
        try {
            String url = "https://" + fullSubdomain;
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                return url;
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
