import com.merzadyan.stock.Stock;
import com.merzadyan.crawler.CrawlerManager;
import org.apache.commons.lang.SerializationException;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class TestAppWithoutUI {
    private static final Logger l = Logger.getLogger(TestAppWithoutUI.class.getName());
    
    public static void main(String[] args) {
        
        int maxCrawlers = 7;
        
        final List<Stock> finalStockResultList = new ArrayList<>();
        final CountDownLatch cl = new CountDownLatch(maxCrawlers);
        CrawlerManager cm = new CrawlerManager((soiScoreMap -> {
            if (soiScoreMap == null) {
                return;
            }
            cl.countDown();
            
            try {
                soiScoreMap.forEach((stock, scores) -> {
                    synchronized (finalStockResultList) {
                        finalStockResultList.add(stock);
                    }
                });
            } catch (Exception e) {
                l.fatal(e);
            }
        }));
        
        cm.setInterval("2018-03-08 to 2018-03-15");
        cm.setTest(true);
        cm.setTestMode(CrawlerManager.MODE.TEST_MODE_COMPLEX);
        cm.setNumberOfCrawlers(maxCrawlers);
        cm.setPolitenessDelay(200);
        cm.setResumableCrawling(false);
        
        try {
            cm.startNonBlockingCrawl();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
        new Thread(() -> {
            try {
                cl.await();
                
                for (int i = 0; i < finalStockResultList.size(); i++) {
                    int nextIndex = i + 1;
                    if (nextIndex >= finalStockResultList.size()) {
                        break;
                    }
                    
                    Stock current = finalStockResultList.get(i),
                            next = finalStockResultList.get(nextIndex);
                    if (current.getCompany().trim().toLowerCase().equals(next.getCompany().trim().toLowerCase())) {
                        int length = current.getHistogram().length;
                        int[] histogram = new int[length];
                        
                        // Sum elements in histograms.
                        for (int j = 0; j < length; j++) {
                            histogram[j] = current.getHistogram()[j] + next.getHistogram()[j];
                        }
                        
                        next.setHistogram(histogram);
                        finalStockResultList.remove(current);
                    }
                }
                
                for (Stock stock : finalStockResultList) {
                    String out = ("Stock: " + stock.getCompany()) +
                            " Symbol: " + stock.getSymbol() +
                            " Stock Exchange: " + stock.getStockExchange() +
                            " Sentiment Score: " + stock.getLatestSentimentScore() +
                            " Histogram: " + Arrays.toString(stock.getHistogram()) +
                            " Start Date: " + stock.getStartDate() +
                            " End Date: " + stock.getEndDate();
                    l.debug("result: " + out);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    static class TestData implements Serializable {
        LocalDate localDate = LocalDate.parse("2018-03-01");
    }
    
    public static void testSerialisation() {
        try {
            serialise();
            
            deserialise();
        } catch (SerializationException se) {
            l.debug("SE: " + se);
        } catch (Exception e) {
            l.debug("E: " + e);
        }
    }
    
    static String SERIALISED_FILE_PATH = "src\\main\\resources\\Test.ser";
    
    public static void deserialise() {
        FileInputStream fileInputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            fileInputStream = new FileInputStream(SERIALISED_FILE_PATH);
            objectInputStream = new ObjectInputStream(fileInputStream);
            TestData testData = (TestData) objectInputStream.readObject();
            l.debug(testData.localDate.toString());
        } catch (Exception e) {
            l.error(e);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    l.error(e);
                }
            }
            
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    l.error(e);
                }
            }
        }
    }
    
    public static void serialise() {
        FileOutputStream fileOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        
        try {
            fileOutputStream = new FileOutputStream(SERIALISED_FILE_PATH);
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            TestData testData = new TestData();
            testData.localDate = LocalDate.parse("2018-04-04");
            objectOutputStream.writeObject(testData);
        } catch (Exception e) {
            l.error(e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    l.error(e);
                }
            }
            
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    l.error(e);
                }
            }
        }
    }
}

