import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebCrawler {
    private static int MAX_DEPTH;
    private static String USER_AGENT;
    private static int DELAY_BETWEEN_REQUESTS;
    private static String LOG_FILE_PATH;
    private static String CRAWLED_URLS_OUTPUT_PATH;
    private static final String QUEUE_STATE_PATH = "output/queue_state.txt";
    private static final String VISITED_URLS_STATE_PATH = "output/visited_urls_state.txt";

    private Set<String> visitedUrls = new HashSet<>();
    private Queue<UrlDepthPair> urlQueue = new LinkedList<>();
    private static final Logger logger = Logger.getLogger(WebCrawler.class.getName());

    public static void main(String[] args) {
        Config config = new Config();
        MAX_DEPTH = Integer.parseInt(config.getProperty("crawler.maxDepth"));
        USER_AGENT = config.getProperty("crawler.userAgent");
        DELAY_BETWEEN_REQUESTS = Integer.parseInt(config.getProperty("crawler.delayBetweenRequests"));
        LOG_FILE_PATH = config.getProperty("logging.file");
        CRAWLED_URLS_OUTPUT_PATH = config.getProperty("crawled.urls.output.path");

        setupLogger();

        WebCrawler crawler = new WebCrawler();
        crawler.loadState();
        crawler.startCrawling("http://example.com");
    }

    private static void setupLogger() {
        try {
            File logFile = new File(LOG_FILE_PATH);
            logFile.getParentFile().mkdirs(); // Create directories if they do not exist
            FileHandler fileHandler = new FileHandler(LOG_FILE_PATH, true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setLevel(Level.parse(System.getProperty("logging.level", "INFO")));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception occurred", e);
        }
    }

    public void startCrawling(String startUrl) {
        if (urlQueue.isEmpty()) {
            urlQueue.add(new UrlDepthPair(startUrl, 0));
        }

        while (!urlQueue.isEmpty()) {
            UrlDepthPair currentUrlDepthPair = urlQueue.poll();
            String currentUrl = currentUrlDepthPair.getUrl();
            int currentDepth = currentUrlDepthPair.getDepth();

            if (currentDepth > MAX_DEPTH || visitedUrls.contains(currentUrl)) {
                continue;
            }

            visitedUrls.add(currentUrl);
            logger.log(Level.INFO, "Crawled URL: {0}", currentUrl);
            logCrawledUrl(currentUrl);

            try {
                Document document = Jsoup.connect(currentUrl).userAgent(USER_AGENT).get();

                Elements links = document.select("a[href]");
                for (Element link : links) {
                    String absUrl = link.absUrl("href");
                    if (!visitedUrls.contains(absUrl) && !urlQueueContains(absUrl)) {
                        urlQueue.add(new UrlDepthPair(absUrl, currentDepth + 1));
                    }
                }

                Thread.sleep(DELAY_BETWEEN_REQUESTS);
            } catch (IOException | InterruptedException e) {
                logger.log(Level.SEVERE, "Exception occurred while crawling", e);
            }

            saveState();
        }
    }

    private boolean urlQueueContains(String url) {
        for (UrlDepthPair pair : urlQueue) {
            if (pair.getUrl().equals(url)) {
                return true;
            }
        }
        return false;
    }

    private void logCrawledUrl(String url) {
        try {
            File file = new File(CRAWLED_URLS_OUTPUT_PATH);
            file.getParentFile().mkdirs(); // Create directories if they do not exist
            try (FileWriter writer = new FileWriter(file, true)) {
                writer.write(url + System.lineSeparator());
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception occurred while saving state", e);
        }
    }

    private void saveState() {
        try (ObjectOutputStream queueOut = new ObjectOutputStream(new FileOutputStream(QUEUE_STATE_PATH));
             ObjectOutputStream visitedUrlsOut = new ObjectOutputStream(new FileOutputStream(VISITED_URLS_STATE_PATH))) {
            queueOut.writeObject(new LinkedList<>(urlQueue));
            visitedUrlsOut.writeObject(new HashSet<>(visitedUrls));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception occurred while saving state", e);
        }
    }

    private void loadState() {
        try (ObjectInputStream queueIn = new ObjectInputStream(new FileInputStream(QUEUE_STATE_PATH));
             ObjectInputStream visitedUrlsIn = new ObjectInputStream(new FileInputStream(VISITED_URLS_STATE_PATH))) {
            urlQueue = (Queue<UrlDepthPair>) queueIn.readObject();
            visitedUrls = (Set<String>) visitedUrlsIn.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // If the files do not exist or cannot be read, start with empty state
            urlQueue = new LinkedList<>();
            visitedUrls = new HashSet<>();
        }
    }

    private static class UrlDepthPair implements Serializable {
        private final String url;
        private final int depth;

        public UrlDepthPair(String url, int depth) {
            this.url = url;
            this.depth = depth;
        }

        public String getUrl() {
            return url;
        }

        public int getDepth() {
            return depth;
        }
    }
}