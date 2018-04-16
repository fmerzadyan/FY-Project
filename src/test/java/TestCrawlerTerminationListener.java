import com.merzadyan.crawler.CrawlerManager;
import com.merzadyan.crawler.CrawlerTerminationListener;

public class TestCrawlerTerminationListener {
    /**
     * IMPORTANT NOTE: Due to its concurrent nature, this test requires manual effort. Watch for "Listener has been called"
     * in the logs.
     */
    public static void listenerShouldBeCalledOnCrawlerTermination() {
        final String message = "CrawlerTerminationListener callback has been called.";
        CrawlerTerminationListener listener = map -> System.out.println(message);
        
        CrawlerManager crawlerManager = new CrawlerManager(listener);
        // Setting the max crawler depth to 1 means the crawling will stop after the first url visit.
        // The aim of this test is to test whether the message is outputted to the console output.
        // thereby confirming that the listener callback has been called and listener has worked.
        crawlerManager.setMaxDepthOfCrawling(1);
        crawlerManager.setNumberOfCrawlers(1);
        crawlerManager.setResumableCrawling(false);
        crawlerManager.setIncludeHttpsPages(false);
        crawlerManager.setPolitenessDelay(200);
        crawlerManager.setTest(true);
        crawlerManager.setTestMode(CrawlerManager.MODE.TEST_MODE_COMPLEX);
        
        try {
            crawlerManager.startNonBlockingCrawl();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        TestCrawlerTerminationListener.listenerShouldBeCalledOnCrawlerTermination();
    }
}
