package com.merzadyan.ui;

import com.merzadyan.Common;
import com.merzadyan.SOIRegistry;
import com.merzadyan.SeedUrl;
import com.merzadyan.SeedUrlRegistry;
import com.merzadyan.Stock;
import com.merzadyan.TextAreaAppender;
import com.merzadyan.crawler.CrawlerManager;
import com.merzadyan.crawler.CrawlerTerminationListener;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class MainWindow extends Application {
    private static final Logger LOGGER = Logger.getLogger(MainWindow.class.getName());
    
    private CrawlerManager crawlerManager;
    private ResultsCallback resultsCallback;
    
    /**
     * Indicates the current state of the crawlers. True if crawling is currently being performed.
     * False if crawling has not yet been started or the #onTermination callback has not been called.
     */
    private boolean currentlyCrawling;
    
    /*
     * Main tab.
     */
    @FXML
    private TextArea consoleTextArea;
    @FXML
    private Label hhmmssLbl;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> timerHandler;
    
    /*
     * Seed URLs tab.
     */
    @FXML
    private TableView<SeedUrl> seedUrlTableView;
    @FXML
    private TableColumn<SeedUrl, String> urlTblCol,
            typeTblCol;
    
    @FXML
    private ObservableList<SeedUrl> seedUrlObservableList;
    @FXML
    private ComboBox seedUrlOptionsComboBox;
    @FXML
    private TextField seedUrlTextField;
    @FXML
    private Button addSeedUrlBtn,
            removeSeedUrlBtn;
    
    /*
     * SOI Registry tab.
     */
    @FXML
    private Slider preselectedStockSlider;
    @FXML
    private ComboBox preselectedStocksComboBox;
    @FXML
    private TextField companyNameTextField,
            tickerSymbolTextField,
            stockExchangeTextField;
    @FXML
    private Button addSoiBtn,
            removeSoiBtn;
    @FXML
    private ListView soiRegistryListView;
    private ObservableList<Stock> soiObservableList;
    
    /*
     * Config tab.
     */
    @FXML
    private TextField userAgentNameTextField,
            dataDumpTextField;
    @FXML
    private Slider numberOfCrawlersSlider,
            maxDepthOfCrawlingSlider,
            politenessDelaySlider,
            includeHTTPSPagesSlider,
            includeBinaryContentCrawlingSlider,
            resumableCrawlingSlider,
            testSlider;
    @FXML
    private ComboBox testModeComboBox;
    
    private class ResultsCallback implements CrawlerTerminationListener {
        @Override
        public void onTermination(HashMap<Stock, ArrayList<Integer>> soiScoreMap) {
            LOGGER.debug("#onTermination");
            
            stopTimer();
            
            currentlyCrawling = false;
            
            if (soiScoreMap == null) {
                return;
            }
            
            try {
                soiScoreMap.forEach((stock, scores) -> {
                    String out = ("Stock: " + stock.getCompany()) +
                            " Symbol: " + stock.getSymbol() +
                            " Stock Exchange: " + stock.getStockExchange() +
                            " Sentiment Score: " + stock.getLatestSentimentScore();
                    LOGGER.debug("result: " + out);
                });
            } catch (Exception e) {
                LOGGER.fatal(e);
            }
        }
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        setUserAgentStylesheet(STYLESHEET_MODENA);
        
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource("main.fxml")));
        primaryStage.setScene(scene);
        // Disable resizing ability of the window.
        primaryStage.setResizable(false);
        primaryStage.show();
    }
    
    /**
     * Called after @FXML annotated members have been injected.
     */
    @FXML
    public void initialize() {
        // Use on, off values instead of 0, 1 in sliders.
        StringConverter<Double> binaryLabelFormat = (new StringConverter<Double>() {
            @Override
            public String toString(Double v) {
                if (v == 0) {
                    return "off";
                }
                return "on";
            }
            
            @Override
            public Double fromString(String v) {
                if (v.equals("off")) {
                    return 0d;
                }
                return 1d;
            }
        });
        
        /*
         * Main tab.
         */
        TextAreaAppender.setTextArea(consoleTextArea);
        consoleTextArea.appendText("Started application.\n");
        
        resultsCallback = new ResultsCallback();
        crawlerManager = new CrawlerManager(resultsCallback);
        
        /*
         * Seed URLs tab.
         */
        seedUrlObservableList = FXCollections.observableArrayList();
        seedUrlObservableList.addAll(SeedUrlRegistry.getInstance().getUrlSet());
        urlTblCol.setCellValueFactory(new PropertyValueFactory<>("url"));
        typeTblCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        seedUrlTableView.setItems(seedUrlObservableList);
        
        seedUrlOptionsComboBox.getItems().clear();
        seedUrlOptionsComboBox.getItems().addAll(
                "Use default seed URLs only.",
                "Use both default and custom seed URLs.",
                "Use custom seed URLs only."
        );
        
        /*
         * SOI Registry tab.
         */
        soiObservableList = FXCollections.observableList(new ArrayList<>());
        soiObservableList.addAll(SOIRegistry.getInstance().getStockSet());
        
        // Customise list view cells.
        Callback stockCellCallback = new Callback() {
            @Override
            public Object call(Object param) {
                return new ListCell<Stock>() {
                    @Override
                    protected void updateItem(Stock item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (empty) {
                            setText(null);
                        } else {
                            setText(item.getCompany() + " <<<" + item.getSymbol() +
                                    ">>> [" + item.getStockExchange() + "]");
                        }
                    }
                };
            }
        };
        
        // FIXME: first item in the list view cannot be selected.
        soiRegistryListView.setCellFactory(stockCellCallback);
        // Sort stocks alphabetically.
        soiObservableList.sort((o1, o2) -> o1.getCompany().compareToIgnoreCase(o2.getCompany()));
        soiRegistryListView.setItems(soiObservableList);
        
        preselectedStockSlider.setLabelFormatter(binaryLabelFormat);
        togglePreselectedStock();
        preselectedStockSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            togglePreselectedStock();
        });
        
        preselectedStocksComboBox.getItems().clear();
        preselectedStocksComboBox.setCellFactory(stockCellCallback);
        preselectedStocksComboBox.getItems().addAll(SOIRegistry.getInstance().getDefaultStockSet());
        
        /*
         * Config tab.
         */
        userAgentNameTextField.setText(crawlerManager.getUserAgentString());
        dataDumpTextField.setText(crawlerManager.getCrawlStorageFolder());
        
        includeHTTPSPagesSlider.setLabelFormatter(binaryLabelFormat);
        includeBinaryContentCrawlingSlider.setLabelFormatter(binaryLabelFormat);
        resumableCrawlingSlider.setLabelFormatter(binaryLabelFormat);
        
        testModeComboBox.getItems().clear();
        testModeComboBox.getItems().addAll(
                CrawlerManager.MODE.TEST_MODE_ONE,
                CrawlerManager.MODE.TEST_MODE_TWO,
                CrawlerManager.MODE.TEST_MODE_THREE
        );
        
        testSlider.setLabelFormatter(binaryLabelFormat);
        toggleTest();
        testSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            toggleTest();
        });
    }
    
    public void startCrawlers() {
        LOGGER.debug("startCrawlers");
        
        if (currentlyCrawling) {
            return;
        }
        
        try {
            // TODO: TextArea cannot handle vast amount of input text from log4j and becomes unresponsive.
            // See https://stackoverflow.com/questions/33078241/javafx-application-freeze-when-i-append-log4j-to-textarea
            // TODO: different approach - instead of reducing output (muting crawler4j/Stanford logs) OR increasing
            // buffer size of GUI console: keep one non-static instance of the GUI console and retrieve my logs i.e.
            // relevant tracing info and analysis results?
            if (crawlerManager == null) {
                LOGGER.fatal("CrawlerManager: null");
                return;
            }
            
            startTimer();
            crawlerManager.startNonBlockingCrawl();
        } catch (Exception ex) {
            LOGGER.debug(ex);
        }
    }
    
    public void stopCrawlers() {
        LOGGER.debug("stopCrawlers");
        
        if (!currentlyCrawling) {
            return;
        }
        
        if (crawlerManager != null) {
            crawlerManager.stopCrawl();
        }
    }
    
    public void addSeedUrl() {
        // TODO: take the values from the seed url options combo box.
        String url = seedUrlTextField.getText().trim().toLowerCase();
        SeedUrl seedUrl = new SeedUrl(url, SeedUrl.Type.USER_DEFINED);
        // Prevent duplicate entries.
        if (!seedUrlObservableList.contains(seedUrl)) {
            seedUrlObservableList.add(seedUrl);
        }
    }
    
    public void removeSeedUrl() {
        SeedUrl seedUrl = seedUrlTableView.getSelectionModel().getSelectedItem();
        if (seedUrl != null) {
            SeedUrlRegistry.getInstance().remove(seedUrl);
            seedUrlObservableList.remove(seedUrl);
        }
    }
    
    public void saveConfigs() {
        // Guard against null strings.
        String userAgentName = userAgentNameTextField.getText().trim();
        String dataDump = userAgentNameTextField.getText().trim();
        if (!Common.isNullOrEmptyString(userAgentName)) {
            crawlerManager.setUserAgentString(userAgentNameTextField.getText().trim());
        }
        if (!Common.isNullOrEmptyString(dataDump)) {
            crawlerManager.setCrawlStorageFolder(dataDumpTextField.getText().trim());
        }
        
        crawlerManager.setNumberOfCrawlers((int) numberOfCrawlersSlider.getValue());
        crawlerManager.setMaxDepthOfCrawling((int) maxDepthOfCrawlingSlider.getValue());
        crawlerManager.setPolitenessDelay((int) politenessDelaySlider.getValue());
        crawlerManager.setIncludeHttpsPages(adapt(includeHTTPSPagesSlider.getValue()));
        crawlerManager.setIncludeBinaryContentInCrawling(adapt(includeBinaryContentCrawlingSlider.getValue()));
        crawlerManager.setResumableCrawling(adapt(resumableCrawlingSlider.getValue()));
        
        crawlerManager.setTest(adapt(testSlider.getValue()));
        crawlerManager.setTestMode((String) testModeComboBox.getValue());
        
        // Provide visual feedback for the saving-configs action.
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText(null);
        alert.setContentText("Saved configs!");
        alert.showAndWait();
    }
    
    /**
     * Resets the UI controls in the configs tab.
     */
    public void resetConfigs() {
        userAgentNameTextField.setText(CrawlerManager.DEFAULT.DEFAULT_USER_AGENT_STRING);
        dataDumpTextField.setText(CrawlerManager.DEFAULT.DEFAULT_CRAWL_STORAGE_FOLDER);
        numberOfCrawlersSlider.setValue(CrawlerManager.DEFAULT.DEFAULT_NUMBER_OF_CRAWLERS);
        maxDepthOfCrawlingSlider.setValue(CrawlerManager.DEFAULT.DEFAULT_MAX_DEPTH_OF_CRAWLING);
        politenessDelaySlider.setValue(CrawlerManager.DEFAULT.DEFAULT_POLITENESS_DELAY);
        includeHTTPSPagesSlider.setValue(adapt(CrawlerManager.DEFAULT.DEFAULT_INCLUDE_HTTPS_PAGES));
        includeBinaryContentCrawlingSlider.setValue(adapt(CrawlerManager.DEFAULT.DEFAULT_INCLUDE_BINARY_CONTENT_IN_CRAWLING));
        resumableCrawlingSlider.setValue(adapt(CrawlerManager.DEFAULT.DEFAULT_RESUMABLE_CRAWLING));
        testSlider.setValue(adapt(CrawlerManager.DEFAULT.DEFAULT_TEST));
        testModeComboBox.setValue(CrawlerManager.DEFAULT.DEFAULT_TEST_MODE);
    }
    
    /**
     * Controls and toggles the states (enable/disable) of testModeComboBox on the current value
     * of the testSlider.
     */
    private void toggleTest() {
        testModeComboBox.setDisable(!adapt(testSlider.getValue()));
    }
    
    /**
     * Controls and toggles the states (enable/disable) of UI controls on the current value
     * of the preselectedStockSlider.
     */
    private void togglePreselectedStock() {
        if (adapt(preselectedStockSlider.getValue())) {
            preselectedStocksComboBox.setDisable(false);
            companyNameTextField.setDisable(true);
            tickerSymbolTextField.setDisable(true);
            stockExchangeTextField.setDisable(true);
        } else {
            preselectedStocksComboBox.setDisable(true);
            companyNameTextField.setDisable(false);
            tickerSymbolTextField.setDisable(false);
            stockExchangeTextField.setDisable(false);
        }
    }
    
    public void addSoi() {
        boolean isPreselectedStock = adapt(preselectedStockSlider.getValue());
        Stock stock;
        if (isPreselectedStock) {
            stock = (Stock) preselectedStocksComboBox.getValue();
        } else {
            String company = companyNameTextField.getText().trim().toLowerCase();
            String symbol = tickerSymbolTextField.getText().trim().toLowerCase();
            String stockExchange = stockExchangeTextField.getText().trim().toLowerCase();
            
            if (Common.isNullOrEmptyString(company) || Common.isNullOrEmptyString(symbol) ||
                    Common.isNullOrEmptyString(stockExchange)) {
                return;
            }
            
            stock = new Stock(company, symbol, stockExchange);
        }
        // Prevent duplicate entries.
        if (!soiObservableList.contains(stock)) {
            soiObservableList.add(stock);
        }
    }
    
    public void removeSoi() {
        Stock stock = (Stock) soiRegistryListView.getSelectionModel().getSelectedItem();
        if (stock != null) {
            SOIRegistry.getInstance().remove(stock);
            soiObservableList.remove(stock);
        }
    }
    
    /**
     * Converts boolean value to corresponding double value.
     *
     * @param bool
     * @return
     */
    private double adapt(boolean bool) {
        return bool ? 1d : 0d;
    }
    
    /**
     * Converts double value to corresponding boolean value.
     *
     * @param dbl
     * @return
     */
    private boolean adapt(double dbl) {
        return dbl == 1d;
    }
    
    /**
     * Starts internal timer used to calculate the elapsed time for the crawling process.
     */
    private void startTimer() {
        // IMPORTANT: #currentTimeMillis returns one hour before - add one hour to correct this.
        final long startTime = System.currentTimeMillis() + (60 * 60 * 1000);
        final SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        
        final Runnable updateElapsedTimeRunnable = () -> {
            // Avoid throwing IllegalStateException by running from a non-JavaFX thread.
            Platform.runLater(
                    () -> {
                        long elapsedTime = System.currentTimeMillis() - startTime;
                        hhmmssLbl.setText(formatter.format(new Date(elapsedTime)));
                    }
            );
        };
        // Schedule automatically cancelling/nullifying the timer if not cancelled already by then.
        int autoCancelBy = 4,
                // Schedule task to run every x milliseconds.
                scheduleEvery = 1000;
        timerHandler =
                scheduler.scheduleAtFixedRate(updateElapsedTimeRunnable, 0, scheduleEvery, MILLISECONDS);
        scheduler.schedule(() -> {
            timerHandler.cancel(true);
        }, autoCancelBy, HOURS);
    }
    
    /**
     * Stops internal timer used to calculate the elapsed time for the crawling process.
     */
    private void stopTimer() {
        if (timerHandler != null) {
            scheduler.schedule(() -> {
                timerHandler.cancel(true);
            }, 0, MILLISECONDS);
        }
    }
    
}
