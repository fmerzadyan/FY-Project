package com.merzadyan.ui;

import com.merzadyan.CrawlerManager;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.log4j.Logger;

public class MainWindow extends Application {
    private static final Logger LOGGER = Logger.getLogger(MainWindow.class.getName());
    
    private CrawlerManager crawlerManager;
    
    @FXML
    private ProgressIndicator crawlingProgInd;
    
    @FXML
    private Slider includeHTTPSPagesSlider;
    @FXML
    private Slider includeBinaryContentCrawlingSlider;
    @FXML
    private Slider resumableCrawlingSlider;
    
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
     *
     * @throws Exception
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
        
        includeHTTPSPagesSlider.setLabelFormatter(binaryLabelFormat);
        includeBinaryContentCrawlingSlider.setLabelFormatter(binaryLabelFormat);
        resumableCrawlingSlider.setLabelFormatter(binaryLabelFormat);
    }
    
    public void startCrawlers() {
        LOGGER.debug("startCrawlers");
        if (crawlingProgInd != null) {
            crawlingProgInd.setDisable(false);
            crawlingProgInd.setVisible(true);
        }
        
        if (crawlerManager == null) {
            crawlerManager = new CrawlerManager();
        }
        
        try {
            // crawlerManager.startNonBlockingCrawl();
        } catch (Exception ex) {
            LOGGER.debug(ex);
        }
    }
    
    public void stopCrawlers() {
        LOGGER.debug("stopCrawlers");
        // TODO: FIXME: stopping crawlers makes program unresponsive.
        if (crawlingProgInd != null && crawlerManager != null) {
            crawlerManager.stopCrawl();
            crawlingProgInd.setDisable(true);
            crawlingProgInd.setVisible(false);
        }
    }
}
