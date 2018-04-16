package com.merzadyan.stock;

import com.merzadyan.FileOp;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.TreeSet;

/**
 * SOIRegistry (Stocks of Interest Registry) contains a list of stocks to watch during crawling process.
 */
public class SOIRegistry {
    private static final Logger LOGGER = Logger.getLogger(SOIRegistry.class.getName());
    
    private static final SOIRegistry instance = new SOIRegistry();
    
    /**
     * References the location of the file containing the built-in stocks of interest.
     * The file holds data such as company name and its corresponding ticker symbol in the same row
     * wherein each field is separated by a % symbol.
     */
    public static final String FTSE_100_FILE_PATH =
            "src/main/resources/dictionary/ftse-100.txt";
    public static final String SERIALISED_DIR = "src/main/resources/ser";
    /**
     * References the location of the serialised file containing user-defined stocks of interest.
     * The file holds Stock objects.
     */
    public static final String SERIALISED_SOI_FILE_PATH = SERIALISED_DIR + "/box.ser";
    
    private Box box;
    
    private TreeSet<Stock> ftse100Set;
    
    private SOIRegistry() {
        try {
            ftse100Set = extractFtse100();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        box = new Box();
        
        if (!new File(SERIALISED_DIR).isDirectory()) {
            File intermediateDirs = new File(SERIALISED_DIR);
            intermediateDirs.mkdirs();
        }
        
        try {
            deserialise();
        } catch (Exception e) {
            LOGGER.debug(e);
        }
    }
    
    public static SOIRegistry getInstance() {
        return instance;
    }
    
    public void createAndPopulateFTSE100File() {
        File intermediateDirs = new File("src/main/resources/dictionary");
        // Create intermediate directories that are parent directories to the FTSE-100 file.
        intermediateDirs.mkdirs();
        
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(FTSE_100_FILE_PATH), "utf-8"))) {
            LOGGER.debug("Creating and populating file: " + FTSE_100_FILE_PATH);
            // Create and populate the file with FTSE-100 companies.
            writer.write("3i % III\n" +
                    "Admiral Group % ADM\n" +
                    "Anglo American plc % AAL\n" +
                    "Antofagasta % ANTO\n" +
                    "Ashtead Group % AHT\n" +
                    "Associated British Foods % ABF\n" +
                    "AstraZeneca % AZN\n" +
                    "Aviva % AV.\n" +
                    "BAE Systems  % BA.\n" +
                    "Barclays % BARC\n" +
                    "Barratt Developments % BDEV\n" +
                    "Berkeley Group Holdings % BKG\n" +
                    "BHP % BLT\n" +
                    "BP % BP.\n" +
                    "British American Tobacco % BATS\n" +
                    "British Land % BLND\n" +
                    "BT Group % BT.A\n" +
                    "Bunzl % BNZL\n" +
                    "Burberry % BRBY\n" +
                    "Carnival Corporation & plc % CCL\n" +
                    "Centrica % CNA\n" +
                    "Coca-Cola HBC AG % CCH\n" +
                    "Compass Group % CPG\n" +
                    "CRH plc % CRH\n" +
                    "Croda International % CRDA\n" +
                    "DCC plc % DCC\n" +
                    "Diageo % DGE\n" +
                    "Direct Line Group % DLG\n" +
                    "easyJet % EZJ\n" +
                    "Evraz % EVR\n" +
                    "Experian % EXPN\n" +
                    "Ferguson plc % FERG\n" +
                    "Fresnillo plc % FRES\n" +
                    "G4S % GFS\n" +
                    "GKN % GKN\n" +
                    "GlaxoSmithKline % GSK\n" +
                    "Glencore % GLEN\n" +
                    "Halma % HLMA\n" +
                    "Hammerson % HMSO\n" +
                    "Hargreaves Lansdown % HL.\n" +
                    "HSBC % HSBA\n" +
                    "Imperial Brands % IMB\n" +
                    "Informa % INF\n" +
                    "InterContinental Hotels Group % IHG\n" +
                    "International Airlines Group % IAG\n" +
                    "Intertek % ITRK\n" +
                    "ITV plc % ITV\n" +
                    "Johnson Matthey % JMAT\n" +
                    "Just Eat % JE.\n" +
                    "Kingfisher plc % KGF\n" +
                    "Land Securities % LAND\n" +
                    "Legal & General % LGEN\n" +
                    "Lloyds Banking Group % LLOY\n" +
                    "London Stock Exchange Group % LSE\n" +
                    "Marks & Spencer % MKS\n" +
                    "Mediclinic International % MDC\n" +
                    "Micro Focus % MCRO\n" +
                    "Mondi % MNDI\n" +
                    "Morrisons % MRW\n" +
                    "National Grid plc % NG.\n" +
                    "Next plc % NXT\n" +
                    "NMC Health % NMC\n" +
                    "Old Mutual % OML\n" +
                    "Paddy Power Betfair % PPB\n" +
                    "Pearson PLC % PSON\n" +
                    "Persimmon plc % PSN\n" +
                    "Prudential plc % PRU\n" +
                    "Randgold Resources % RRS\n" +
                    "Reckitt Benckiser % RB.\n" +
                    "RELX Group % REL\n" +
                    "Rentokil Initial % RTO\n" +
                    "Rio Tinto Group % RIO\n" +
                    "Rolls-Royce Holdings % RR.\n" +
                    "The Royal Bank of Scotland Group % RBS\n" +
                    "Royal Dutch Shell % RDSA\n" +
                    "RSA Insurance Group % RSA\n" +
                    "Sage Group % SGE\n" +
                    "Sainsbury's % SBRY\n" +
                    "Schroders % SDR\n" +
                    "Scottish Mortgage Investment Trust % SMT\n" +
                    "Segro % SGRO\n" +
                    "Severn Trent % SVT\n" +
                    "Shire plc % SHP\n" +
                    "Sky plc % SKY\n" +
                    "Smith & Nephew % SN.\n" +
                    "Smith, D.S. % SMDS\n" +
                    "Smiths Group % SMIN\n" +
                    "Smurfit Kappa % SKG\n" +
                    "SSE plc % SSE\n" +
                    "Standard Chartered % STAN\n" +
                    "Standard Life Aberdeen % SLA\n" +
                    "St. James's Place plc % STJ\n" +
                    "Taylor Wimpey % TW.\n" +
                    "Tesco % TSCO\n" +
                    "TUI Group % TUI\n" +
                    "Unilever % ULVR\n" +
                    "United Utilities % UU.\n" +
                    "Vodafone Group % VOD\n" +
                    "Whitbread % WTB\n" +
                    "WPP plc % WPP");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public TreeSet<Stock> extractFtse100() throws IOException {
        if (!FileOp.isFile(FTSE_100_FILE_PATH) || FileOp.isEmptyFile(FTSE_100_FILE_PATH)) {
            createAndPopulateFTSE100File();
        }
        
        return adapt(FTSE_100_FILE_PATH);
    }
    
    private TreeSet<Stock> adapt(String path) throws IOException {
        TreeSet<Stock> set = new TreeSet<>();
        FileReader fileReader = new FileReader(path);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        
        while ((line = bufferedReader.readLine()) != null) {
            String[] pair = line.split("%");
            if (pair[0] == null || pair[1] == null) {
                // continue to next iteration if format is broken.
                continue;
            }
            String company = pair[0].trim().toLowerCase();
            String symbol = pair[1].trim().toLowerCase();
            // NOTE: since only FTSE-100 companies are listed in soi.txt then all are on LSE.
            Stock stock = new Stock(company, symbol, "LSE");
            // Duplicate entries are disallowed in tree sets by default.
            set.add(stock);
        }
        return set;
    }
    
    /**
     * IMPORTANT NOTE: for each time when stocks are added via the GUI, call this method keep
     * the the registry in sync with the UI. Adds the specified element to this set if it is not already present.
     *
     * @param stock
     */
    public synchronized void add(Stock stock) {
        boolean isAdded = box.set.add(stock);
        if (isAdded) {
            serialise(box.set);
        }
    }
    
    /**
     * IMPORTANT NOTE: for each time when stocks are removed via the GUI, call this method keep
     * * the the registry in sync with the UI. Removes the specified element from this set if it is present.
     *
     * @param stock
     */
    public synchronized void remove(Stock stock) {
        boolean isRemoved = box.set.remove(stock);
        if (isRemoved) {
            serialise(box.set);
        }
    }
    
    public TreeSet<Stock> getSoiSet() {
        return box.set;
    }
    
    public TreeSet<Stock> getFtse100Set() {
        return ftse100Set;
    }
    
    private void serialise(TreeSet<Stock> set) {
        serialise(set, SERIALISED_SOI_FILE_PATH);
    }
    
    public static void serialise(TreeSet<Stock> set, final String SERIALISED_SOI_FILE_PATH) {
        FileOutputStream fileOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        
        try {
            fileOutputStream = new FileOutputStream(SERIALISED_SOI_FILE_PATH);
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            Box box = new Box();
            box.set.addAll(set);
            objectOutputStream.writeObject(box);
            LOGGER.debug("Wrote object in.");
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
    
    private void deserialise() {
        box = deserialise(SERIALISED_SOI_FILE_PATH);
    }
    
    public static Box deserialise(final String SERIALISED_SOI_FILE_PATH) {
        FileInputStream fileInputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            fileInputStream = new FileInputStream(SERIALISED_SOI_FILE_PATH);
            objectInputStream = new ObjectInputStream(fileInputStream);
            Box box = (Box) objectInputStream.readObject();
            LOGGER.debug("Read object out.");
            return box;
        } catch (Exception e) {
            // e.printStackTrace();
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
}
