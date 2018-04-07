import com.merzadyan.crawler.CrawlerManager;
import com.merzadyan.crawler.CrawlerTerminationListener;

public class TestCrawlerTerminationListener {
    /**
     * IMPORTANT NOTE: Due to its concurrent nature, this test requires manual effort. Watch for "Listener has been called"
     * in the logs.
     */
    public static void hasListenerBeenCalled() {
        CrawlerTerminationListener listener = soiScoreMap -> {
            System.out.println("*************************");
            System.out.println("*************************");
            System.out.println("Listener has been called.");
            System.out.println("*************************");
            System.out.println("*************************");
        };
        
        CrawlerManager crawlerManager = new CrawlerManager(listener);
        crawlerManager.setNumberOfCrawlers(1);
        crawlerManager.setTest(true);
        crawlerManager.setTestMode(CrawlerManager.MODE.TEST_MODE_SIMPLE);
        
        try {
            crawlerManager.startNonBlockingCrawl();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        TestCrawlerTerminationListener.hasListenerBeenCalled();
    }
}
