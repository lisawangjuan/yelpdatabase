package populate;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

import java.sql.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.FileReader;


public class Populate {

    private static final String DB_USER = "Scott";
    private static final String DB_PASS = "tiger";
    private static final String ORACLE_URL = "jdbc:oracle:thin:@localhost:1521:orcl5";
    private static final String JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";
    private static final String BUSINESS_JSON_FILE_PATH = "C:\\WangJuan\\SCU\\COEN280\\assignments\\homework3\\YelpDataset\\yelp_business.json";
    private static final String USER_JSON_FILE_PATH = "C:\\WangJuan\\SCU\\COEN280\\assignments\\homework3\\YelpDataset\\yelp_user.json";
    private static final String REVIEW_JSON_FILE_PATH = "C:\\WangJuan\\SCU\\COEN280\\assignments\\homework3\\YelpDataset\\yelp_review.json";
    private static final String CHECKIN_JSON_FILE_PATH = "C:\\WangJuan\\SCU\\COEN280\\assignments\\homework3\\YelpDataset\\yelp_checkin.json";

    /**
     *
     */
    public static HashSet<String> mainCategoriesHash = new HashSet();

    /**
     *
     */
    public static void run() {
        try {
            init();
            parseAndInsertYelpUser();
            parseAndInsertBusiness();
            parseAndInsertAttribute();
            parseAndInsertHours();
            parseAndInsertCheckIn();
            parseAndInsertReview();
            parseAndInsertMainAndSubCategory();

        } catch (SQLException ex) {
            Logger.getLogger(Populate.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Populate.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Populate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    private static void init() throws SQLException, ClassNotFoundException {
        System.out.println("START initialization");
        addMainCategories();
        cleanTable();
        System.out.println("END initialization");
    }

    private static void addMainCategories() {
        System.out.println("Set MainCategories...");
        mainCategoriesHash.add("Active Life");
        mainCategoriesHash.add("Arts & Entertainment");
        mainCategoriesHash.add("Automotive");
        mainCategoriesHash.add("Car Rental");
        mainCategoriesHash.add("Cafes");
        mainCategoriesHash.add("Beauty & Spas");
        mainCategoriesHash.add("Convenience Stores");
        mainCategoriesHash.add("Dentists");
        mainCategoriesHash.add("Doctors");
        mainCategoriesHash.add("Drugstores");
        mainCategoriesHash.add("Department Stores");
        mainCategoriesHash.add("Education");
        mainCategoriesHash.add("Event Planning & Services");
        mainCategoriesHash.add("Flowers & Gifts");
        mainCategoriesHash.add("Food");
        mainCategoriesHash.add("Health & Medical");
        mainCategoriesHash.add("Home Services");
        mainCategoriesHash.add("Home & Garden");
        mainCategoriesHash.add("Hospitals");
        mainCategoriesHash.add("Hotels & Travel");
        mainCategoriesHash.add("Hardware Stores");
        mainCategoriesHash.add("Grocery");
        mainCategoriesHash.add("Medical Centers");
        mainCategoriesHash.add("Nurseries & Gardening");
        mainCategoriesHash.add("Nightlife");
        mainCategoriesHash.add("Restaurants");
        mainCategoriesHash.add("Shopping");
        mainCategoriesHash.add("Transportation");
    }

    private static void cleanTable() throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnect()) {
            String sql;
            PreparedStatement preparedStatement;

            System.out.println("Clean YelpUser table...");
            sql = "DELETE FROM YelpUser";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();

            System.out.println("Clean Review table...");
            sql = "DELETE FROM Review";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();

            System.out.println("Clean Hours table...");
            sql = "DELETE FROM Hours";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();
            preparedStatement.close();

            System.out.println("Clean Attribute table...");
            sql = "DELETE FROM Attribute";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();
            preparedStatement.close();

            System.out.println("Clean SubCategory table...");
            sql = "DELETE FROM SubCategory";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();
            preparedStatement.close();

            System.out.println("Clean MainCategory table...");
            sql = "DELETE FROM MainCategory";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();
            preparedStatement.close();

            System.out.println("Clean Checkin table...");
            sql = "DELETE FROM Checkin";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();
            preparedStatement.close();

            System.out.println("Clean Business table...");
            sql = "DELETE FROM Business";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        }
    }

    private static void parseAndInsertBusiness() throws IOException, SQLException, ClassNotFoundException {
        System.out.println("Parsing yelp_business.json file and inserting data into Business table...");
        File file = new File(BUSINESS_JSON_FILE_PATH);
        try (FileReader fileReader = new FileReader(file);
             BufferedReader reader = new BufferedReader(fileReader);
             Connection connection = getConnect();) {
            String line;
            String sql;
            PreparedStatement preparedStatement = null;
            JSONParser parser = new JSONParser();

            // parse business data
            while ((line = reader.readLine()) != null) {
                JSONObject obj = (JSONObject) parser.parse(line);
                String business_id = (String) obj.get("business_id");
                String address = (String) obj.get("full_address");
                String city = (String) obj.get("city");
                Integer review_count = ((Long) obj.get("review_count")).intValue();
                String business_name = (String) obj.get("name");
                String b_state = (String) obj.get("state");
                double stars = (double) obj.get("stars");

                // insert business data
                sql = "INSERT INTO Business (business_id, business_name, city, b_state, address, stars, review_count) VALUES (?, ?, ?, ?, ?, ?, ?)";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, business_id);
                preparedStatement.setString(2, business_name);
                preparedStatement.setString(3, city);
                preparedStatement.setString(4, b_state);
                preparedStatement.setString(5, address);
                preparedStatement.setDouble(6, stars);
                preparedStatement.setInt(7, review_count);
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static void parseAndInsertMainAndSubCategory() throws IOException, SQLException, ClassNotFoundException {
        System.out.println("Parsing yelp_business.json file and inserting data into MainCategory and SubCategory table...");
        File file = new File(BUSINESS_JSON_FILE_PATH);
        try (FileReader fileReader = new FileReader(file);
             BufferedReader reader = new BufferedReader(fileReader);
             Connection connection = getConnect();) {
            String line;
            String sql;
            PreparedStatement preparedStatement = null;
            JSONParser parser = new JSONParser();
            // parse business data

            while ((line = reader.readLine()) != null) {
                JSONObject obj = (JSONObject) parser.parse(line);
                String business_id = (String) obj.get("business_id");
                JSONArray arr = (JSONArray) obj.get("categories");
                for (int i = 0; i < arr.size(); i++) {
                    String category = (String) arr.get(i);

                    // insert MainCategory data
                    if (mainCategoriesHash.contains(category)) {
                        String mainCategory = category;
                        sql = "INSERT INTO MainCategory (business_id, mainCategory) VALUES (?, ?)";
                        preparedStatement = connection.prepareStatement(sql);
                        preparedStatement.setString(1, business_id);
                        preparedStatement.setString(2, mainCategory);
                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                    }

                    // insert SubCategory data
                    else {
                        String subCategory = category;
                        sql = "INSERT INTO SubCategory (business_id, subCategory) VALUES (?, ?)";
                        preparedStatement = connection.prepareStatement(sql);
                        preparedStatement.setString(1, business_id);
                        preparedStatement.setString(2, subCategory);
                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static void parseAndInsertAttribute() throws IOException, SQLException, ClassNotFoundException {
        System.out.println("Parsing yelp_business.json file and inserting data into Attribute table...");
        File file = new File(BUSINESS_JSON_FILE_PATH);
        try (FileReader fileReader = new FileReader(file);
             BufferedReader reader = new BufferedReader(fileReader);
             Connection connection = getConnect();) {
            String line;
            String sql;
            PreparedStatement preparedStatement = null;
            JSONParser parser = new JSONParser();
            // parse business data

            while ((line = reader.readLine()) != null) {
                JSONObject obj = (JSONObject) parser.parse(line);
                String business_id = (String) obj.get("business_id");
                JSONObject attributes1 = (JSONObject) obj.get("attributes");
                for (Object okey1 : attributes1.keySet()) {
                    String key1 = (String) okey1;
                    StringBuilder sb1 = new StringBuilder(key1);
                    String attribute = null;
                    if (attributes1.get(key1) instanceof JSONObject) {
                        JSONObject attributes2 = (JSONObject) attributes1.get(key1);
                        for (Object okey2 :
                                attributes1.keySet()) {
                            String key2 = (String) okey2;
                            StringBuilder sb2 = new StringBuilder(key2);
                            sb2.append("_");
                            sb2.append(attributes2.get(key2));
                            attribute = sb1.toString() + "_" + sb2.toString();
                        }
                    } else {
                        sb1.append("_");
                        sb1.append(attributes1.get(key1));
                        attribute = sb1.toString();
                    }
                    // insert attribute data
                    sql = "INSERT INTO Attribute (business_id, attribute) VALUES (?, ?)";
                    preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setString(1, business_id);
                    preparedStatement.setString(2, attribute);
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                }
            }
        } catch (ParseException e1) {
            e1.printStackTrace();
        }
    }

    private static void parseAndInsertHours() throws IOException, SQLException, ClassNotFoundException {
        System.out.println("Parsing yelp_business.json file and inserting data into Hours table...");
        File file = new File(BUSINESS_JSON_FILE_PATH);
        try (FileReader fileReader = new FileReader(file);
             BufferedReader reader = new BufferedReader(fileReader);
             Connection connection = getConnect();) {
            String line;
            String sql;
            PreparedStatement preparedStatement = null;
            JSONParser parser = new JSONParser();
            // parse business data

            while ((line = reader.readLine()) != null) {
                JSONObject obj = (JSONObject) parser.parse(line);
                String business_id = (String) obj.get("business_id");
                JSONObject openHours = (JSONObject) obj.get("hours");
                if (openHours == null) {
                    return;
                }
                String query = "Parse business file and insert into Hours table....";
                for (Object okey1 : openHours.keySet()) {
                    String openDay = (String) okey1;
                    JSONObject businessTime = (JSONObject) openHours.get(openDay);
                    String openTime = (String) businessTime.get("open");
                    String closeTime = (String) businessTime.get("close");

                    // insert hours data
                    sql = "INSERT INTO Hours (business_id, openDay, openTime, closeTime) VALUES (?, ?, ?, ?)";
                    preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setString(1, business_id);
                    preparedStatement.setString(2, openDay);
                    preparedStatement.setString(3, openTime);
                    preparedStatement.setString(4, closeTime);
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                }
            }
        } catch (ParseException e1) {
            e1.printStackTrace();
        }
    }

    private static void parseAndInsertReview() throws IOException, SQLException, ClassNotFoundException {
        System.out.println("Parsing review.json file and inserting data into Review table...");
        File file = new File(REVIEW_JSON_FILE_PATH);
        try (FileReader fileReader = new FileReader(file);
             BufferedReader reader = new BufferedReader(fileReader);
             Connection connection = getConnect();) {
            String line;
            String sql;
            PreparedStatement preparedStatement = null;
            JSONParser parser = new JSONParser();

            while ((line = reader.readLine()) != null) {
                // parse review data
                JSONObject obj = (JSONObject) parser.parse(line);
                int votes_funny = ((Long) ((JSONObject) obj.get("votes")).get("funny")).intValue();
                int votes_useful = ((Long) ((JSONObject) obj.get("votes")).get("useful")).intValue();
                int votes_cool = ((Long) ((JSONObject) obj.get("votes")).get("cool")).intValue();
                String user_id = (String) obj.get("user_id");
                String review_id = (String) obj.get("review_id");
                int stars = ((Long) obj.get("stars")).intValue();
                String review_date = (String) obj.get("date");
                String text = (String) obj.get("text");
                String business_id = (String) obj.get("business_id");

                // insert review data
                sql = "INSERT INTO Review (votes_funny, votes_useful, votes_cool, user_id, review_id, stars, review_date, text, business_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setInt(1, votes_funny);
                preparedStatement.setInt(2, votes_useful);
                preparedStatement.setInt(3, votes_cool);
                preparedStatement.setString(4, user_id);
                preparedStatement.setString(5, review_id);
                preparedStatement.setInt(6, stars);
                preparedStatement.setString(7, review_date);
                preparedStatement.setBytes(8, text.getBytes());
                preparedStatement.setString(9, business_id);
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static void parseAndInsertYelpUser() throws IOException, SQLException, ClassNotFoundException {
        System.out.println("Parsing user.json file and inserting data into YelpUser table...");
        File file = new File(USER_JSON_FILE_PATH);
        try (FileReader fileReader = new FileReader(file);
             BufferedReader reader = new BufferedReader(fileReader);
             Connection connection = getConnect();) {
            String line;
            String sql;
            PreparedStatement preparedStatement = null;
            JSONParser parser = new JSONParser();
            // parse yelp_user data

            while ((line = reader.readLine()) != null) {
                JSONObject obj = (JSONObject) parser.parse(line);
                int votes_funny = ((Long) ((JSONObject) obj.get("votes")).get("funny")).intValue();
                int votes_useful = ((Long) ((JSONObject) obj.get("votes")).get("useful")).intValue();
                int votes_cool = ((Long) ((JSONObject) obj.get("votes")).get("cool")).intValue();
                String yelping_since = (String) obj.get("yelping_since");
                int review_count = ((Long) obj.get("review_count")).intValue();
                String user_name = (String) obj.get("name");
                String user_id = (String) obj.get("user_id");
                double average_stars = (double) obj.get("average_stars");

                // insert user data
                sql = "INSERT INTO YelpUser (votes_funny, votes_useful, votes_cool, yelping_since, review_count, user_name, user_id, average_stars) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setInt(1, votes_funny);
                preparedStatement.setInt(2, votes_useful);
                preparedStatement.setInt(3, votes_cool);
                preparedStatement.setString(4, yelping_since);
                preparedStatement.setInt(5, review_count);
                preparedStatement.setString(6, user_name);
                preparedStatement.setString(7, user_id);
                preparedStatement.setDouble(8, average_stars);
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static void parseAndInsertCheckIn() throws IOException, SQLException, ClassNotFoundException {
        System.out.println("Parsing yelp_checkin.json file and inserting data into checkin table...");
        File file = new File(CHECKIN_JSON_FILE_PATH);
        try (FileReader fileReader = new FileReader(file);
             BufferedReader reader = new BufferedReader(fileReader);
             Connection connection = getConnect();) {
            String line;
            String sql;
            PreparedStatement preparedStatement = null;
            JSONParser parser = new JSONParser();

            while ((line = reader.readLine()) != null) {
                // parse checkin data
                JSONObject obj = (JSONObject) parser.parse(line);
                String business_id = (String) obj.get("business_id");
                JSONObject checkin = (JSONObject) obj.get("checkin_info");
                for (Object okey1 : checkin.keySet()) {
                    String checkin_time = (String) okey1;
                    int checkin_number = ((Long) checkin.get(okey1)).intValue();

                    // insert checkin data
                    sql = "INSERT INTO CheckIn (business_id, checkin_time, checkin_number) VALUES (?, ?, ?)";
                    preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setString(1, business_id);
                    preparedStatement.setString(2, checkin_time);
                    preparedStatement.setInt(3, checkin_number);
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static Connection getConnect() throws SQLException, ClassNotFoundException {
        System.out.println("Checking JDBC...");
        Class.forName(JDBC_DRIVER);
        System.out.println("Connecting to database...");
        return DriverManager.getConnection(ORACLE_URL, DB_USER, DB_PASS);
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        new Populate().run();
    }
}