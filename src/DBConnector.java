import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class DBConnector {
    Connection con;
    private int wordCount = 1;
    private int sentenceCount = 1;
    private HashMap<String, String> invertedWordList;
    private HashMap<String, Integer> wordList;
    private ArrayList<String> sentenceList;
    private PreparedStatement stopwordSt;

    public DBConnector() {

        String url = "jdbc:mysql://localhost:3306/IRTM?useLegacyDatetimeCode=false&serverTimezone=GMT";
        String user = "root";
        String password = "";
        try {
            con = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        wordList = new HashMap<>();
        invertedWordList = new HashMap<>();
        sentenceList = new ArrayList<>();

        resetDB();
    }

    private void resetDB() {
        String delete = "TRUNCATE TABLE ";

        try (Statement st = con.createStatement()) {
            st.executeUpdate(delete + Model.SENTENCES_TABLE);
            st.executeUpdate(delete + Model.WORDS_TABLE);
            st.executeUpdate(delete + Model.WORDS_INVERTED_TABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ResultSet queryDB(String query) {
        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(query);
            return rs;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void insertInDB(String... args) {//0=tableName, 1=Text, 2=
        switch (args[0]) {
            case Model.STOPWORDS_TABLE:
                insertInStopWords(args[1]);
                break;
            case Model.SENTENCES_TABLE:
                insertInSentences(args[1]);
                break;
            case Model.WORDS_TABLE:
                insertInWords(args[1]);
                break;
            case Model.WORDS_INVERTED_TABLE:
                insertInInvertedList(args[1]);
                break;
            default:
                System.err.println("Non-valid tablename");
        }
    }

    private void insertInInvertedList(String word) {
        if (invertedWordList.containsKey(word)) {
            invertedWordList.put(word, invertedWordList.get(word) + " " + wordCount);
        } else {
            invertedWordList.put(word, "" + wordCount);
        }
    }

    private void UpdateInverted() {
        String insert = "INSERT INTO " + Model.WORDS_INVERTED_TABLE + " (word,positions) VALUES (?,?)";
        try (PreparedStatement invertedStatement = con.prepareStatement(insert)) {
            for (String key : invertedWordList.keySet()) {
                invertedStatement.setString(1, key);
                invertedStatement.setString(2, invertedWordList.get(key));
                invertedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void UpdateWordList() {
        String insert = "INSERT INTO " + Model.WORDS_TABLE + " (word,senid) VALUES (?,?)";
        try (PreparedStatement wordStatement = con.prepareStatement(insert)) {
            for (String key : wordList.keySet()) {
                wordStatement.setString(1, key);
                wordStatement.setInt(2, wordList.get(key));
                wordStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void UpdateSentenceList() {
        String insert = "INSERT INTO " + Model.SENTENCES_TABLE + " (content) VALUES (?)";
        try (PreparedStatement sentenceStatement = con.prepareStatement(insert)) {
            for (String sentence : sentenceList) {
                sentenceStatement.setString(1, sentence);
                sentenceStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public boolean isInTable(String table, String column, String string) {
        String query = "SELECT " + column + " FROM " + table + " WHERE " + column + " = \"" + string + "\" ";

        try (ResultSet rs = queryDB(query)) {
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void insertInWords(String word) {
        wordList.put(word, sentenceCount);
    }

    private void insertInSentences(String content) {
        sentenceList.add(content);
    }

    public void insertInStopWords(String word) {
        try {
            if (stopwordSt == null) {
                String query = "INSERT INTO " + Model.STOPWORDS_TABLE + "(sword) VALUES(\" ? \");";
                stopwordSt = con.prepareStatement(query);
            }

            stopwordSt.setString(1, word);
            stopwordSt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void incWordCount() {
        wordCount++;
    }

    public void incrementSentenceCount() {
        sentenceCount++;
    }

    public void UpdateDatabase() {
        UpdateWordList();
        UpdateInverted();
        UpdateSentenceList();
    }
}