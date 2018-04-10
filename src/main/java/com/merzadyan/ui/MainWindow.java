package com.merzadyan.ui;

import com.merzadyan.Common;
import com.merzadyan.History;
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
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Slider;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class MainWindow extends Application {
    private static final Logger LOGGER = Logger.getLogger(MainWindow.class.getName());
    
    private CrawlerManager crawlerManager;
    private ResultsCallback resultsCallback;
    private CountDownLatch countDownLatch;
    private ArrayList<Stock> finalStockResultList;
    private static final String SERIALISED_FILE_PATH = "src\\main\\resources\\History.ser";
    
    /**
     * Indicates the current state of the crawlers. True if crawling is currently being performed.
     * False if crawling has not yet been started or the #onTermination callback has not been called.
     */
    private boolean currentlyCrawling;
    
    /*
     * Main tab.
     */
    @FXML
    private Button startBtn,
            stopBtn;
    
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
            typeTblCol,
            seedActionTblCol;
    @FXML
    private ObservableList<SeedUrl> seedUrlObservableList;
    
    @FXML
    private ComboBox seedUrlOptionsComboBox;
    @FXML
    private TextField seedUrlTextField;
    
    /*
     * SOI Registry tab.
     */
    @FXML
    private TableView<Stock> soiRegistryTableView;
    @FXML
    private TableColumn<Stock, String> companyTblCol,
            symbolTblCol,
            stockExchangeTblCol,
            forecastTblCol,
            soiActionTblCol;
    private ObservableList<Stock> soiObservableList;
    
    @FXML
    private Slider preselectedStockSlider;
    @FXML
    private ComboBox preselectedStocksComboBox;
    @FXML
    private TextField companyNameTextField,
            tickerSymbolTextField,
            stockExchangeTextField;
    @FXML
    private Button addSoiBtn;
    
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
            LOGGER.debug("#onTermination.");
            
            countDownLatch.countDown();
            
            if (soiScoreMap == null) {
                return;
            }
            
            try {
                soiScoreMap.forEach((stock, scores) -> {
                    synchronized (finalStockResultList) {
                        finalStockResultList.add(stock);
                    }
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
        
        // primaryStage.setAlwaysOnTop(true);
        primaryStage.setOnCloseRequest((event -> {
            // Serialise the data in the event that the window was closed.
            if (finalStockResultList != null && finalStockResultList.size() > 0) {
                serialise();
            }
            
            // Force stop all threads.
            Platform.exit();
            System.exit(0);
        }));
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
        
        /*
         * Main tab.
         */
        // Disable click-ability of unintended areas around button.
        startBtn.setPickOnBounds(false);
        stopBtn.setPickOnBounds(false);
        
        TextAreaAppender.setTextArea(consoleTextArea);
        consoleTextArea.appendText("Started application.\n");
        
        resultsCallback = new ResultsCallback();
        crawlerManager = new CrawlerManager(resultsCallback);
        finalStockResultList = new ArrayList<>();
        // Restore last saved state.
        deserialise();
        
        /*
         * Seed URLs tab.
         */
        seedUrlObservableList = FXCollections.observableArrayList();
        seedUrlObservableList.addAll(SeedUrlRegistry.getInstance().getUrlSet());
        urlTblCol.setCellValueFactory(new PropertyValueFactory<>("url"));
        typeTblCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        
        // Custom cell factory is required to add button to table view cell.
        Callback<TableColumn<SeedUrl, String>, TableCell<SeedUrl, String>> seedActionCellFactory =
                new Callback<TableColumn<SeedUrl, String>, TableCell<SeedUrl, String>>() {
                    @Override
                    public TableCell call(final TableColumn<SeedUrl, String> param) {
                        final TableCell<SeedUrl, String> cell = new TableCell<SeedUrl, String>() {
                            final Button btn = new Button("Remove");
                            
                            @Override
                            public void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                
                                if (empty) {
                                    setGraphic(null);
                                    setText(null);
                                } else {
                                    btn.setOnAction(event -> {
                                        SeedUrl url = getTableView().getItems().get(getIndex());
                                        LOGGER.debug(url.getUrl());
                                        
                                        if (!url.getType().equals(SeedUrl.Type.DEFAULT)) {
                                            SeedUrlRegistry.getInstance().remove(url);
                                            seedUrlObservableList.remove(url);
                                        } else {
                                            LOGGER.debug("Will not remove DEFAULT type url.");
                                        }
                                    });
                                    setGraphic(btn);
                                    setText(null);
                                }
                            }
                        };
                        
                        // Centre the contents of the cell (includes the button).
                        cell.setAlignment(Pos.CENTER);
                        return cell;
                    }
                };
        seedActionTblCol.setCellFactory(seedActionCellFactory);
        
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
        soiObservableList = FXCollections.observableArrayList();
        soiObservableList.addAll(SOIRegistry.getInstance().getStockSet());
        companyTblCol.setCellValueFactory(new PropertyValueFactory<>("company"));
        symbolTblCol.setCellValueFactory(new PropertyValueFactory<>("symbol"));
        stockExchangeTblCol.setCellValueFactory(new PropertyValueFactory<>("stockExchange"));
        
        // Custom cell factory is required to add button to table view cell.
        Callback<TableColumn<Stock, String>, TableCell<Stock, String>> forecastCellFactory =
                new Callback<TableColumn<Stock, String>, TableCell<Stock, String>>() {
                    @Override
                    public TableCell call(final TableColumn<Stock, String> param) {
                        final TableCell<Stock, String> cell = new TableCell<Stock, String>() {
                            final Button btn = new Button("Result");
                            
                            @Override
                            public void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                
                                if (empty) {
                                    setGraphic(null);
                                    setText(null);
                                } else {
                                    btn.setOnAction(event -> {
                                        Stock stock = getTableView().getItems().get(getIndex());
                                        LOGGER.debug(stock.getCompany());
                                        
                                        try {
                                            FXMLLoader loader = new FXMLLoader(getClass().getResource("chart.fxml"));
                                            
                                            Stage stage = new Stage();
                                            stage.setScene(new Scene(loader.load()));
                                            
                                            // TODO: enable persistent store of sentiment scores.
                                            ChartWindow controller = loader.getController();
                                            
                                            for (Stock result : finalStockResultList) {
                                                if (result.getCompany().trim().toLowerCase().equals(stock.getCompany()
                                                        .trim().toLowerCase())) {
                                                    // Sync the data.
                                                    result.setSymbol(stock.getSymbol());
                                                    result.setStockExchange(stock.getStockExchange());
                                                    stock.setLatestSentimentScore(result.getLatestSentimentScore());
                                                    stock.setHistogram(result.getHistogram());
                                                    stock.setStartDate(result.getStartDate());
                                                    stock.setEndDate(result.getEndDate());
                                                }
                                            }
                                            
                                            // Serialise the data.
                                            if (finalStockResultList != null && finalStockResultList.size() > 0) {
                                                serialise();
                                            }
                                            
                                            controller.initData(stock);
                                            
                                            stage.show();
                                        } catch (Exception e) {
                                            LOGGER.error(e);
                                        }
                                    });
                                    setGraphic(btn);
                                    setText(null);
                                }
                            }
                        };
                        
                        // Centre the contents of the cell (includes the button).
                        cell.setAlignment(Pos.CENTER);
                        return cell;
                    }
                };
        // Not required.
        // forecastTblCol.setCellValueFactory(new PropertyValueFactory<>("NotRequired"));
        forecastTblCol.setCellFactory(forecastCellFactory);
        
        // Custom cell factory is required to add button to table view cell.
        Callback<TableColumn<Stock, String>, TableCell<Stock, String>> soiActionCellFactory =
                new Callback<TableColumn<Stock, String>, TableCell<Stock, String>>() {
                    @Override
                    public TableCell call(final TableColumn<Stock, String> param) {
                        final TableCell<Stock, String> cell = new TableCell<Stock, String>() {
                            final Button btn = new Button("Remove");
                            
                            @Override
                            public void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                
                                if (empty) {
                                    setGraphic(null);
                                    setText(null);
                                } else {
                                    btn.setOnAction(event -> {
                                        Stock stock = getTableView().getItems().get(getIndex());
                                        LOGGER.debug(stock.getCompany());
                                        
                                        SOIRegistry.getInstance().remove(stock);
                                        soiObservableList.remove(stock);
                                    });
                                    setGraphic(btn);
                                    setText(null);
                                }
                            }
                        };
                        
                        // Centre the contents of the cell (includes the button).
                        cell.setAlignment(Pos.CENTER);
                        return cell;
                    }
                };
        soiActionTblCol.setCellFactory(soiActionCellFactory);
        
        soiRegistryTableView.setItems(soiObservableList);
        
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
                CrawlerManager.MODE.TEST_MODE_SIMPLE,
                CrawlerManager.MODE.TEST_MODE_COMPLEX
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
            
            countDownLatch = new CountDownLatch((int) numberOfCrawlersSlider.getValue());
            new Thread(() -> {
                try {
                    countDownLatch.await();
                    
                    stopTimer();
                    currentlyCrawling = false;
                    startBtn.setDisable(false);
                    
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
                        LOGGER.debug("result: " + out);
                    }
                } catch (Exception e) {
                    LOGGER.fatal(e);
                }
            }).start();
            
            startTimer();
            crawlerManager.startNonBlockingCrawl();
            currentlyCrawling = true;
            startBtn.setDisable(true);
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
        
        if (Common.isNullOrEmptyString(url)) {
            return;
        }
        
        SeedUrl seedUrl = new SeedUrl(url, SeedUrl.Type.USER_DEFINED);
        // Prevent duplicate entries.
        // IMPORTANT NOTE: using seedUrlObservableList#contains does not prevent duplicate entries.
        // Despite the fact that same approach works for adding/removing SOI.
        if (!SeedUrlRegistry.getInstance().getUrlSet().contains(seedUrl)) {
            SeedUrlRegistry.getInstance().add(seedUrl);
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
            SOIRegistry.getInstance().add(stock);
            soiObservableList.add(stock);
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
    
    public void deserialise() {
        LOGGER.debug("deserialise.");
        
        if (!Common.isFile(SERIALISED_FILE_PATH)) {
            return;
        }
        
        if (Common.isEmptyFile(SERIALISED_FILE_PATH)) {
            return;
        }
        
        if (finalStockResultList == null) {
            finalStockResultList = new ArrayList<>();
        }
        
        FileInputStream fileInputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            fileInputStream = new FileInputStream(SERIALISED_FILE_PATH);
            objectInputStream = new ObjectInputStream(fileInputStream);
            History history = (History) objectInputStream.readObject();
            if (history != null && history.getLastSaved() != null && history.getLastSaved().size() > 0) {
                finalStockResultList = history.getLastSaved();
            }
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    LOGGER.error(e);
                }
            }
            
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    LOGGER.error(e);
                }
            }
        }
    }
    
    public void serialise() {
        LOGGER.debug("serialise.");
        
        // No need to serialise if the finalStockResultList is null or empty.
        if (finalStockResultList == null || finalStockResultList == null || finalStockResultList.size() == 0) {
            return;
        }
        
        FileOutputStream fileOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        
        try {
            fileOutputStream = new FileOutputStream(SERIALISED_FILE_PATH);
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            History history = new History();
            history.setLastSaved(finalStockResultList);
            objectOutputStream.writeObject(history);
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    LOGGER.error(e);
                }
            }
            
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    LOGGER.error(e);
                }
            }
        }
    }
}
