package com.merzadyan.ui;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import org.apache.log4j.Logger;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

public class TextAreaAppender extends WriterAppender {
    private static final Logger LOGGER = Logger.getLogger(TextAreaAppender.class.getName());
    
    private static volatile TextArea textArea;
    protected static final int LINE_LIMIT = 10000;
    private int lines = 0;
    
    /**
     * Set the target TextArea for the logging data to be redirected to.
     */
    public static void setTextArea(final TextArea textArea) {
        TextAreaAppender.textArea = textArea;
    }
    
    /**
     * Format and append the event to the stored TextArea.
     */
    @Override
    public void append(LoggingEvent event) {
        final String message = this.layout.format(event);
        
        try {
            Platform.runLater(() -> {
                try {
                    if (textArea != null) {
                        if (textArea.getText().length() == 0) {
                            textArea.setText(message);
                        } else {
                            // Flush the text if the limit is reached.
                            if (lines == LINE_LIMIT) {
                                textArea.clear();
                            }
                            
                            textArea.selectEnd();
                            textArea.insertText(textArea.getText().length(), message);
                            lines = lines++;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            // Ignore this case - platform has not yet been initialised.
        }
    }
}