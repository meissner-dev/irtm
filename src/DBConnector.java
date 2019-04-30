import java.sql.*;
import java.util.*;

public class DBConnector {
    Connection con;
    private ArrayList<String> sentenceList;
    private ArrayList<Word> wordList, stemmedWordList;
    private PreparedStatement stopwordSt;
    private Model model;

    public DBConnector() {

        String url = "jdbc:mysql://localhost:3306/IRTM?useLegacyDatetimeCode=false&serverTimezone=GMT";
        String user = "root";
        String password = "";
        try {
            con = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        wordList = new ArrayList<>();
        stemmedWordList = new ArrayList<>();
        sentenceList = new ArrayList<>();

        resetDB();
    }

    public void setModel(Model model)
    {
        this.model = model;
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

    public void insertInDB(String... args) {
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
            case Model.WORDS_STEMMED_TABLE:
                insertInStemmedWords(args[1]);
                break;
            default:
                System.err.println("Non-valid tablename");
        }
    }

    private void createInvertedList()
    {
        int posCount = 0;
        Map<String, Integer> invertedList = new TreeMap<>();
        Map<Integer,String> posList = new TreeMap<>();

        for (Word word : wordList) {
            if(invertedList.containsKey(word.getText()))
            {
                int posId = invertedList.get(word.getText());
                posList.put(posId, posList.get(posId) + " " + word.getPosition());
            }
            else
            {
                invertedList.put(word.getText(), posCount);
                posList.put(posCount, String.valueOf(word.getPosition()));
                posCount++;
            }
        }

        String insert = "INSERT INTO " + Model.WORDS_INVERTED_TABLE + " (word,posId) VALUES (?,?)";
        try (PreparedStatement invertedStatement = con.prepareStatement(insert)) {
            for (String word : invertedList.keySet()) {
                invertedStatement.setString(1, word);
                invertedStatement.setInt(2, invertedList.get(word));
                invertedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        insert = "INSERT INTO " + Model.WORDS_POSITIONS + " (posId,positions) VALUES (?,?)";
        try (PreparedStatement positionStatement = con.prepareStatement(insert)) {
            for (Integer posId : posList.keySet()) {
                positionStatement.setInt(1, posId);
                positionStatement.setString(2, posList.get(posId));
                positionStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateWordList() {
        String insert = "INSERT INTO " + Model.WORDS_TABLE + " (word,position,senid) VALUES (?,?,?)";

        try (PreparedStatement wordStatement = con.prepareStatement(insert)) {
            for (Word word : wordList)
            {
                wordStatement.setString(1, word.getText());
                wordStatement.setInt(2, word.getPosition());
                wordStatement.setInt(3, word.getSentenceId());
                wordStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateStemmedWordList() {
        String insert = "INSERT INTO " + Model.WORDS_STEMMED_TABLE + " (word,position,senid) VALUES (?,?,?)";

        try (PreparedStatement wordStatement = con.prepareStatement(insert)) {
            for (Word word : stemmedWordList)
            {
                wordStatement.setString(1, word.getText());
                wordStatement.setInt(2, word.getPosition());
                wordStatement.setInt(3, word.getSentenceId());
                wordStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateSentenceList() {
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
        wordList.add(new Word(word, model.getWordCount(), model.getSentenceCount()));
    }

    private void insertInStemmedWords(String word) {
        stemmedWordList.add(new Word(word, model.getWordCount(), model.getSentenceCount()));
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

    public void UpdateDatabase() {
        updateWordList();
        updateStemmedWordList();
        createInvertedList();
        updateSentenceList();
    }
}