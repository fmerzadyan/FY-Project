package com.merzadyan.ui;

import com.merzadyan.*;
import com.merzadyan.crawler.CrawlerManager;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.log4j.Logger;

import java.util.ArrayList;

public class MainWindow extends Application {
    private static final Logger LOGGER = Logger.getLogger(MainWindow.class.getName());
    
    private CrawlerManager crawlerManager;
    
    @FXML
    private TextArea consoleTextArea;
    
    @FXML
    private TextField userAgentNameTextField,
            dataDumpTextField;
    @FXML
    private Slider numberOfCrawlersSlider,
            maxDepthOfCrawlingSlider,
            politenessDelaySlider,
            includeHTTPSPagesSlider,
            includeBinaryContentCrawlingSlider,
            resumableCrawlingSlider;
    
    @FXML
    private TextField companyNameTextField,
            tickerSymbolTextField,
            stockExchangeTextField;
    
    @FXML
    private ListView soiRegistryListView;
    
    private ObservableList<Stock> soiObservableList;
    
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
        TextAreaAppender.setTextArea(consoleTextArea);
        consoleTextArea.appendText("Started application.\n");
        
        crawlerManager = new CrawlerManager();
        userAgentNameTextField.setText(crawlerManager.getUserAgentString());
        dataDumpTextField.setText(crawlerManager.getCrawlStorageFolder());
        
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
        
        includeHTTPSPagesSlider.setLabelFormatter(binaryLabelFormat);
        includeBinaryContentCrawlingSlider.setLabelFormatter(binaryLabelFormat);
        resumableCrawlingSlider.setLabelFormatter(binaryLabelFormat);
        
        // Retrieve the dictionary of stocks from SOIRegistry.
        SOIRegistry soiRegistry = SOIRegistry.getInstance();
        // Adapt the hash set to an array list.
        ArrayList<Stock> list = new ArrayList<>(soiRegistry.getStockSet());
        soiObservableList = FXCollections.observableList(list);
        // Customise list view cells.
        soiRegistryListView.setCellFactory(param -> new ListCell<Stock>() {
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
        });
        // Sort stocks alphabetically.
        soiObservableList.sort((o1, o2) -> o1.getCompany().compareToIgnoreCase(o2.getCompany()));
        soiRegistryListView.setItems(soiObservableList);
    }
    
    public void startCrawlers() {
        LOGGER.debug("startCrawlers");
        try {
            // TODO: TextArea cannot handle vast amount of input text from log4j and becomes unresponsive.
            // See https://stackoverflow.com/questions/33078241/javafx-application-freeze-when-i-append-log4j-to-textarea
            // TODO: different approach - instead of reducing output (muting crawler4j/Stanford logs) OR increasing
            // buffer size of GUI console: keep one non-static instance of the GUI console and retrieve my logs i.e.
            // relevant tracing info and analysis results?
            if (crawlerManager != null) {
                 crawlerManager.startNonBlockingCrawl();
            }
        } catch (Exception ex) {
            LOGGER.debug(ex);
        }
    }
    
    public void stopCrawlers() {
        LOGGER.debug("stopCrawlers");
        if (crawlerManager != null) {
            crawlerManager.stopCrawl();
        }
    }
    
    public void saveConfigs() {
        // Guard against null strings.
        String userAgentName = userAgentNameTextField.getText().trim();
        String dataDump = userAgentNameTextField.getText().trim();
        if (CommonOp.isNotNullAndNotEmpty(userAgentName)) {
            crawlerManager.setUserAgentString(userAgentNameTextField.getText().trim());
        }
        if (CommonOp.isNotNullAndNotEmpty(dataDump)) {
            crawlerManager.setCrawlStorageFolder(dataDumpTextField.getText().trim());
        }
        
        crawlerManager.setNumberOfCrawlers((int) numberOfCrawlersSlider.getValue());
        crawlerManager.setMaxDepthOfCrawling((int) maxDepthOfCrawlingSlider.getValue());
        crawlerManager.setPolitenessDelay((int) politenessDelaySlider.getValue());
        crawlerManager.setIncludeHttpsPages(includeHTTPSPagesSlider.getValue() == 1d);
        crawlerManager.setIncludeBinaryContentInCrawling(includeBinaryContentCrawlingSlider.getValue() == 1d);
        crawlerManager.setResumableCrawling(resumableCrawlingSlider.getValue() == 1d);
        
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
    }
    
    /**
     * Converts boolean value to corresponding double value.
     * @param bool
     * @return
     */
    private double adapt(boolean bool) {
        return bool ? 1d : 0d;
    }
}
