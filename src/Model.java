import org.tartarus.snowball.ext.GermanStemmer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Model {
    public static final String SENTENCES_TABLE = "SENTENCES",
            STOPWORDS_TABLE = "STOPWORDS",
            WORDS_TABLE = "words",
            WORDS_INVERTED_TABLE = "words_inverted",
            WORDS_STEMMED_TABLE = "words_stemmed",
            WORDS_POSITIONS = "words_positions";

    private static final ArrayList<Character> DELIMITERS = new ArrayList<>(Arrays.asList(' ', '.', '?', '!', ',', '-', ';', '\n', ':', '«', '»', '–', ':', '\r'));

    private static final ArrayList<Character> DELIMITERS_SENTENCE = new ArrayList<>(Arrays.asList('.', '?', '!', ':', '\r', '\n', (char)13));
    private int wordCount = 1, sentenceCount = 1;

    private DBConnector dbc;
    private File textFile, stopFile;

    public Model(String[] args, DBConnector dbc) {
        this.dbc = dbc;
        this.textFile = new File(args[0]);
        this.stopFile = new File(args[1]);
        makeStopWordList(stopFile);
    }

    public int getWordCount()
    {
        return wordCount;
    }

    public int getSentenceCount()
    {
        return sentenceCount;
    }

    public void tokenize() {
        StringBuilder sb = new StringBuilder();
        StringBuilder sentence = new StringBuilder();
        try (FileInputStream fs = new FileInputStream(textFile);
             InputStreamReader fr = new InputStreamReader(fs)) {
            int nextChar;
            while ((nextChar = fr.read()) != -1) {
                if (!DELIMITERS.contains((char) nextChar)) {
                    sb.append((char) nextChar);
                    sentence.append((char) nextChar);
                } else {
                    if (sb.toString().isEmpty())
                        continue;
                    addWordToDatabase(sb.toString());
                    wordCount++;
                    sb.delete(0, sb.length());

                    sentence.append((char) nextChar);

                    if (DELIMITERS_SENTENCE.contains((char) nextChar)) {
                        sentenceCount++;
                        dbc.insertInDB(Model.SENTENCES_TABLE, sentence.toString());
                        sentence.delete(0, sentence.length());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void makeStopWordList(File stopFile) {
        try (Scanner scanner = new Scanner(stopFile)) {
            String word;
            while (scanner.hasNext()) {
                word = scanner.next();
                if (!dbc.isInTable(STOPWORDS_TABLE, "sword", word)) {
                    dbc.insertInDB(STOPWORDS_TABLE, word);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void addWordToDatabase(String word) {
        if(!dbc.isInTable(STOPWORDS_TABLE, "sword", word))
        {
            dbc.insertInDB(WORDS_TABLE, word);
            dbc.insertInDB(WORDS_STEMMED_TABLE, stem(word));
        }
    }

    public static String stem(String string) {
        GermanStemmer stemmer = new GermanStemmer();
        stemmer.setCurrent(string);
        stemmer.stem();
        return stemmer.getCurrent();
    }

    public String search(String searchTerm) {
        String result = new String();

        try
        {
            ResultSet rs = dbc.queryDB("Select posId from " + Model.WORDS_INVERTED_TABLE + " where WORD=\"" + searchTerm + "\"");
            if(rs.next())
            {
                int posId = rs.getInt("posId");
                rs = dbc.queryDB("Select positions from " + Model.WORDS_POSITIONS + " where POSID=\"" + posId + "\"");
                if(rs.next())
                {
                    result = rs.getString("positions");
                }
                else
                {
                    System.err.println("Error finding positions linked to found posId");
                }
            }
            else
            {
                result = "'" + searchTerm + "' could not be found";
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return result;
    }
}
