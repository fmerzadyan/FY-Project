package com.merzadyan.ui;

import com.merzadyan.FileOp;
import com.merzadyan.crawler.CrawlerManager;
import com.merzadyan.crawler.CrawlerTerminationListener;
import com.merzadyan.seed.SeedUrl;
import com.merzadyan.seed.SeedUrlRegistry;
import com.merzadyan.stock.DateCategoriser;
import com.merzadyan.stock.History;
import com.merzadyan.stock.SOIRegistry;
import com.merzadyan.stock.Stock;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
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
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

@SuppressWarnings("unchecked")
public class MainWindow extends Application {
    private static final Logger LOGGER = Logger.getLogger(MainWindow.class.getName());
    
    private CrawlerManager crawlerManager;
    private CountDownLatch countDownLatch;
    private HashMap<String, ArrayList<Stock>> stocksAsTimeProgresses;
    // finalStockResultList is the result from the last crawl-process which process for one date interval.
    private ArrayList<Stock> finalStockResultList;
    private static final String IMMEDIATE_DIR = "src/main/resources/ser";
    private static final String SERIALISED_FILE_PATH = IMMEDIATE_DIR + "/history.ser";
    
    /**
     * Indicates the current state of the crawlers. True if crawling is currently being performed.
     * False if crawling has not yet been started or the #onTermination callback has not been called.
     */
    private boolean currentlyCrawling;
    
    /*
     * Main tab.
     */
    @FXML
    private Button deleteCrawlDataFileBtn,
            deleteConsoleTextBtn,
            startBtn,
            stopBtn;
    
    @FXML
    private ImageView deleteCrawlDataFileImgView,
            deleteConsoleTextImgView,
            startBtnImgView,
            stopBtnImgView;
    
    
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
    private static final String USE_DEFAULT_SEED_URL_ONLY = "USE_DEFAULT_SEED_ONLY",
                                USE_CUSTOM_SEED_URL_ONLY = "USE_CUSTOM_SEED_URL_ONLY",
                                USE_BOTH = "USE_BOTH";
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
    private ComboBox processIntervalComboBox;
    
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
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        setUserAgentStylesheet(STYLESHEET_MODENA);
        
        // Parent root = FXMLLoader.load(getClass().getResource("/com/merzadyan/ui/main.fxml"));
        Parent root = FXMLLoader.load(getClass().getResource("/layout/main.fxml"));
        
        primaryStage.setScene(new Scene(root));
        
        // Disable resizing ability of the window.
        primaryStage.setResizable(false);
        primaryStage.show();
        
        // primaryStage.setAlwaysOnTop(true);
        primaryStage.setOnCloseRequest((event -> {
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
        // Ensure default configs on initialisation.
        resetConfigs();
        
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
        deleteCrawlDataFileBtn.setPickOnBounds(false);
        deleteConsoleTextBtn.setPickOnBounds(false);
        // Disable click-ability of unintended areas around button.
        startBtn.setPickOnBounds(false);
        stopBtn.setPickOnBounds(false);
        
        deleteCrawlDataFileImgView.setFitWidth(50);
        deleteConsoleTextImgView.setFitWidth(50);
        startBtnImgView.setFitWidth(50);
        stopBtnImgView.setFitWidth(50);
        deleteCrawlDataFileImgView.setPreserveRatio(true);
        deleteConsoleTextImgView.setPreserveRatio(true);
        startBtnImgView.setPreserveRatio(true);
        stopBtnImgView.setPreserveRatio(true);
        
        
        TextAreaAppender.setTextArea(consoleTextArea);
        consoleTextArea.appendText("Started application.\n");
        
        ResultsCallback resultsCallback = new ResultsCallback();
        crawlerManager = new CrawlerManager(resultsCallback);
        stocksAsTimeProgresses = new HashMap<>();
        finalStockResultList = new ArrayList<>();
        // Restore last saved state.
        stocksAsTimeProgresses = deserialise();
        if (stocksAsTimeProgresses == null) {
            stocksAsTimeProgresses = new HashMap<>();
        }
        
        /*
         * Seed URLs tab.
         */
        seedUrlObservableList = FXCollections.observableArrayList();
        if (SeedUrlRegistry.getInstance() != null && SeedUrlRegistry.getInstance().getUrlSet() != null &&
                SeedUrlRegistry.getInstance().getUrlSet().size() != 0) {
            seedUrlObservableList.addAll(SeedUrlRegistry.getInstance().getUrlSet());
        } else {
            LOGGER.fatal("seedUrlObservableList.addAll(SeedUrlRegistry.getInstance().getUrlSet() is null or size is 0.");
        }
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
                                        if (!url.getType().equals(SeedUrl.Type.DEFAULT)) {
                                            SeedUrlRegistry.getInstance().remove(url);
                                            seedUrlObservableList.remove(url);
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
        // TODO: consider using these options or seed url options combo box.
        seedUrlOptionsComboBox.getItems().addAll(
                USE_DEFAULT_SEED_URL_ONLY,
                USE_CUSTOM_SEED_URL_ONLY,
                USE_BOTH
        );
        
        /*
         * SOI Registry tab.
         */
        soiObservableList = FXCollections.observableArrayList();
        
        if (SOIRegistry.getInstance().getSoiSet() != null && SOIRegistry.getInstance().getSoiSet().size() != 0) {
            soiObservableList.addAll(SOIRegistry.getInstance().getSoiSet());
        } else {
            LOGGER.fatal("SOIRegistry.getInstance().getSoiSet() is null or size is 0.");
        }
        
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
                                        try {
                                            FXMLLoader fxmlLoader = new FXMLLoader();
                                            fxmlLoader.setLocation(getClass().getResource("/layout/chart.fxml"));
                                            
                                            Stage stage = new Stage();
                                            stage.setScene(new Scene(fxmlLoader.load()));
                                            
                                            ChartWindow controller = fxmlLoader.getController();
                                            
                                            // Iterates through finalStockResultList, assumes only one entry
                                            // for a given company. If the company name matches then retrieve the data.
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
                                            
                                            controller.initData(stocksAsTimeProgresses.get(stock.getCompany()));
                                            
                                            stage.show();
                                        } catch (Exception e) {
                                            e.printStackTrace();
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
        preselectedStockSlider.valueProperty().addListener((observable, oldValue, newValue) -> togglePreselectedStock());
        
        preselectedStocksComboBox.getItems().clear();
        preselectedStocksComboBox.setCellFactory(stockCellCallback);
        if (SOIRegistry.getInstance() != null && SOIRegistry.getInstance().getFtse100Set() != null &&
                SOIRegistry.getInstance().getFtse100Set().size() != 0) {
            preselectedStocksComboBox.getItems().addAll(SOIRegistry.getInstance().getFtse100Set());
        } else {
            LOGGER.fatal("SOIRegistry.getInstance().getFtse100Set() is null or size is 0.");
        }
        
        /*
         * Config tab.
         */
        processIntervalComboBox.getItems().clear();
        
        String[] intervals = new DateCategoriser(null).extractIntervals();
        if (intervals != null && intervals.length != 0) {
            processIntervalComboBox.getItems().addAll(
                    new DateCategoriser(null).extractIntervals()
            );
        } else {
            LOGGER.fatal("new DateCategoriser(null).extractIntervals() is null or length is 0.");
        }
        
        userAgentNameTextField.setText(crawlerManager.getUserAgentString());
        dataDumpTextField.setText(crawlerManager.getCrawlStorageFolder());
        
        includeHTTPSPagesSlider.setLabelFormatter(binaryLabelFormat);
        includeBinaryContentCrawlingSlider.setLabelFormatter(binaryLabelFormat);
        resumableCrawlingSlider.setLabelFormatter(binaryLabelFormat);
        
        testModeComboBox.getItems().clear();
        testModeComboBox.getItems().addAll(
                CrawlerManager.MODE.TEST_MODE_COMPLEX
        );
        
        testSlider.setLabelFormatter(binaryLabelFormat);
        toggleTest();
        testSlider.valueProperty().addListener((observable, oldValue, newValue) -> toggleTest());
    }
    
    public void startCrawlers() {
        if (currentlyCrawling) {
            return;
        }
        
        try {
            if (crawlerManager == null) {
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
                    
                    // Get the date interval.
                    Stock dummy = null;
                    for (Stock stock : finalStockResultList) {
                        // Only used to collect the date interval for later use.
                        // To be extra cautious: null checks - should not be the case when these fields are ever null.
                        if (dummy == null && !FileOp.isNullOrEmpty(stock.getCompany()) &&
                                stock.getStartDate() != null && stock.getEndDate() != null) {
                            dummy = stock;
                            dummy.setCompany(dummy.getCompany().toLowerCase());
                        }
                        LOGGER.debug("#non-UI thread: all crawlers are terminated.");
                        String out = ("Stock: " + stock.getCompany()) +
                                " Sentiment Score: " + stock.getLatestSentimentScore() +
                                " Histogram: " + Arrays.toString(stock.getHistogram()) +
                                " Start Date: " + stock.getStartDate() +
                                " End Date: " + stock.getEndDate();
                        LOGGER.debug("result: " + out);
                    }
                    
                    if (dummy != null) {
                        // CountdownLatch ensures to execute after all the crawler threads have finished.
                        // finalStockResultList holds a batch of stocks as a result of processing in a specified date interval,
                        // therefore the date interval for all the stocks should be the same.
                        try {
                            // list represents the different intervals for a stock.
                            ArrayList<Stock> list = stocksAsTimeProgresses.get(dummy.getCompany());
                            
                            if (list != null) {
                                for (Stock stock : list) {
                                    // Check if there is there a result produced from this interval.
                                    // If not then add and exit.
                                    if (!stock.getStartDate().equals(dummy.getStartDate()) &&
                                            !stock.getStartDate().equals(dummy.getStartDate())) {
                                        list.add(dummy);
                                        
                                        stocksAsTimeProgresses.put(dummy.getCompany(), list);
                                        // add only once.
                                        break;
                                    }
                                }
                            } else {
                                list = new ArrayList<>();
                                list.add(dummy);
                                stocksAsTimeProgresses.put(dummy.getCompany(), list);
                            }
                            
                            serialise();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            
            // Get value of seed url options combo box.
            String option = (String) seedUrlOptionsComboBox.getValue();
            if (option == null || option.isEmpty()) {
                option = USE_DEFAULT_SEED_URL_ONLY;
            }
            switch (option) {
                case USE_DEFAULT_SEED_URL_ONLY:
                    crawlerManager.setSeedUrlOption(SeedUrl.Option.DEFAULT_ONLY);
                    break;
                case USE_CUSTOM_SEED_URL_ONLY:
                    crawlerManager.setSeedUrlOption(SeedUrl.Option.CUSTOM_ONLY);
                    break;
                case USE_BOTH:
                    crawlerManager.setSeedUrlOption(SeedUrl.Option.BOTH);
                    break;
                default:
                    crawlerManager.setSeedUrlOption(SeedUrl.Option.DEFAULT_ONLY);
                    break;
            }
            
            startTimer();
            crawlerManager.startNonBlockingCrawl();
            currentlyCrawling = true;
            startBtn.setDisable(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void stopCrawlers() {
        if (!currentlyCrawling) {
            return;
        }
        
        if (crawlerManager != null) {
            crawlerManager.stopCrawl();
        }
    }
    
    /**
     * Deletes the data dump produced from crawling.
     */
    public void deleteCrawlDataFile() {
        String filePath = crawlerManager.getCrawlStorageFolder();
        
        try {
            Files.walk(Paths.get(filePath))
                    .map(Path::toFile)
                    .sorted((o1, o2) -> -o1.compareTo(o2))
                    .forEach(File::delete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Clears the console.
     */
    public void deleteConsoleText() {
        consoleTextArea.clear();
    }
    
    public void addSeedUrl() {
        String url = seedUrlTextField.getText().trim().toLowerCase();
        
        if (FileOp.isNullOrEmpty(url)) {
            return;
        }
        
        SeedUrl seedUrl = new SeedUrl(url, SeedUrl.Type.USER_DEFINED);
        // Prevent duplicate entries.
        // Despite the fact that same approach works for adding/removing SOI.
        if (!SeedUrlRegistry.getInstance().getUrlSet().contains(seedUrl)) {
            SeedUrlRegistry.getInstance().add(seedUrl);
            seedUrlObservableList.add(seedUrl);
        }
    }
    
    public void saveConfigs() {
        String interval = (String) processIntervalComboBox.getValue();
        crawlerManager.setInterval(interval);
        
        // Guard against null strings.
        String userAgentName = userAgentNameTextField.getText().trim();
        String dataDump = userAgentNameTextField.getText().trim();
        if (!FileOp.isNullOrEmpty(userAgentName)) {
            crawlerManager.setUserAgentString(userAgentNameTextField.getText().trim());
        }
        if (!FileOp.isNullOrEmpty(dataDump)) {
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
        seedUrlOptionsComboBox.setValue(USE_DEFAULT_SEED_URL_ONLY);
        processIntervalComboBox.setValue(CrawlerManager.DEFAULT.DEFAULT_INTERVAL);
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
            
            if (FileOp.isNullOrEmpty(company) || FileOp.isNullOrEmpty(symbol) ||
                    FileOp.isNullOrEmpty(stockExchange)) {
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
    
    private HashMap<String, ArrayList<Stock>> deserialise() {
        return deserialise(SERIALISED_FILE_PATH);
    }
    
    // Method is static and dependency injectable to be reusable in the JUnit test.
    public static HashMap<String, ArrayList<Stock>> deserialise(final String SERIALISED_FILE_PATH) {
        if (!FileOp.isFile(SERIALISED_FILE_PATH)) {
            return null;
        }
        
        if (FileOp.isEmptyFile(SERIALISED_FILE_PATH)) {
            return null;
        }
        
        FileInputStream fileInputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            fileInputStream = new FileInputStream(SERIALISED_FILE_PATH);
            objectInputStream = new ObjectInputStream(fileInputStream);
            History history = (History) objectInputStream.readObject();
            if (history != null && history.getLastSaved() != null && history.getLastSaved().size() > 0) {
                LOGGER.debug("#deserialise: read object out.");
                return history.getLastSaved();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    
    private void serialise() {
        serialise(stocksAsTimeProgresses, SERIALISED_FILE_PATH);
    }
    
    // Method is static and dependency injectable to be reusable in the JUnit test.
    public static void serialise(HashMap<String, ArrayList<Stock>> stocksAsTimeProgresses,
                                 final String SERIALISED_FILE_PATH) {
        // No need to serialise if the stocksAsTimeProgresses is null or empty.
        if (stocksAsTimeProgresses == null || stocksAsTimeProgresses.size() == 0) {
            return;
        }
        
        File immediateDirs = new File(IMMEDIATE_DIR);
        immediateDirs.mkdirs();
        
        FileOutputStream fileOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        
        try {
            fileOutputStream = new FileOutputStream(SERIALISED_FILE_PATH);
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            History history = new History();
            history.setLastSaved(stocksAsTimeProgresses);
            objectOutputStream.writeObject(history);
            LOGGER.debug("serialise: wrote object in.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
