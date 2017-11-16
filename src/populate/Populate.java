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


class Business {
    String business_id;
    String name;
    String city;
    String state;
    String address;
    double stars;
    String open;
    String review_count;

    Business(String business_id, String name, String city, String state, String address, double stars, String open,  String review_count) {
        this.business_id = business_id;
        this.name = name;
        this.city = city;
        this.state = state;
        this.address = address;
        this.stars = stars;
        this.open = open;
        this.review_count = review_count;
    }
}

class Hours {
    String business_id;
    String openDay;
    String openTime;
    String closeTime;

    Hours(String business_id, String day, String openTime, String closeTime) {
        this.business_id = business_id;
        this.openDay = day;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }
}

class MainCategory {
    String business_id;
    String mainCategory;

    MainCategory(String business_id, String mainCategory) {
        this.business_id = business_id;
        this.mainCategory = mainCategory;
    }
}

class SubCategory {
    String business_id;
    String subcategory;

    SubCategory(String business_id, String subcategory) {
        this.business_id = business_id;
        this.subcategory = subcategory;
    }
}


class Attribute {
    String business_id;
    String attribute;

    Attribute(String business_id, String attribute) {
        this.business_id = business_id;
        this.attribute = attribute;
    }
}

public class Populate {

    private static final String DB_USER = "Scott";
    private static final String DB_PASS = "tiger";
    private static final String ORACLE_URL = "jdbc:oracle:thin:@localhost:1521:orcl5";
    private static final String JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";
    private static final String BUSINESS_JSON_FILE_PATH = "C:\\WangJuan\\SCU\\COEN280\\assignments\\homework3\\YelpDataset\\yelp_business.json";
    private static final String USER_JSON_FILE_PATH = "C:\\WangJuan\\SCU\\COEN280\\assignments\\homework3\\YelpDataset\\yelp_user.json";
    private static final String REVIEW_JSON_FILE_PATH = "C:\\WangJuan\\SCU\\COEN280\\assignments\\homework3\\YelpDataset\\yelp_review.json";

    public static List<Business> businesses = new ArrayList();
    public static List<MainCategory> mainCategories = new ArrayList();
    public static List<SubCategory> subCategories = new ArrayList();
    public static List<Attribute> attributes = new ArrayList();
    public static List<Hours> hours = new ArrayList();
    public static HashSet<String> mainCategoriesHash = new HashSet();

    public static void run() {
        try {
            init();
            parseBusiness();
            parseAndInsertYelpUser();
            parseAndInsertReview();

        } catch (SQLException ex) {
            Logger.getLogger(Populate.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Populate.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Populate.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    private static void init() throws SQLException, ClassNotFoundException {
        System.out.println("+++++++++++++ START initialization +++++++++++++");
        addMainCategories();
        cleanTable();
        System.out.println("------------- END initialization -------------");
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

            System.out.println("Clean populate.Hours table...");
            sql = "DELETE FROM populate.Hours";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();
            preparedStatement.close();

            System.out.println("Clean populate.Attribute table...");
            sql = "DELETE FROM populate.Attribute";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();
            preparedStatement.close();

            System.out.println("Clean populate.SubCategory table...");
            sql = "DELETE FROM populate.SubCategory";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();
            preparedStatement.close();

            System.out.println("Clean populate.MainCategory table...");
            sql = "DELETE FROM populate.MainCategory";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();
            preparedStatement.close();

            System.out.println("Clean populate.Business table...");
            sql = "DELETE FROM populate.Business";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        }
    }

    private static void parseBusiness() throws ParseException, IOException, SQLException, ClassNotFoundException {
        File file = new File(BUSINESS_JSON_FILE_PATH);
        JSONParser parser = new JSONParser();

        try (FileReader fileReader = new FileReader(file);
             BufferedReader reader = new BufferedReader(fileReader);
             Connection connection = getConnect()) {
            String line;
            while ((line = reader.readLine()) != null) {
                JSONObject obj = (JSONObject) parser.parse(line);
                String business_id = (String) obj.get("business_id");
                String address = (String) obj.get("full_address");
                String open = (String) obj.get("open");
                String city = (String) obj.get("city");
                String review_count = (String) obj.get("review_count");
                String name = (String) obj.get("name");
                String state = (String) obj.get("state");
                double stars = (double) obj.get("stars");
                JSONArray arr = (JSONArray) obj.get("categories");
                for (int i = 0; i < arr.size(); i++) {
                    String category = (String) arr.get(i);
                    if (mainCategoriesHash.contains(category)) {
                        mainCategories.add(new MainCategory(business_id, category));
                    } else {
                        subCategories.add(new SubCategory(business_id, category));
                    }
                }
                businesses.add(new Business(business_id, name, city, state, address, stars, open, review_count));

                JSONObject attributes1 = (JSONObject) obj.get("attributes");
                for (Object okey1 : attributes1.keySet()) {
                    String key1 = (String) okey1;
                    StringBuilder sb1 = new StringBuilder(key1);
                    if (attributes1.get(key1) instanceof JSONObject) {
                        JSONObject attributes2 = (JSONObject) attributes1.get(key1);
                        for (Object okey2 :
                                attributes1.keySet()) {
                            String key2 = (String) okey2;
                            StringBuilder sb2 = new StringBuilder(key2);
                            sb2.append("_");
                            sb2.append(attributes2.get(key2));
                            attributes.add(new Attribute(business_id, sb1.toString() + "_" + sb2.toString()));
                        }
                    } else {
                        sb1.append("_");
                        sb1.append(attributes1.get(key1));
                        attributes.add(new Attribute(business_id, sb1.toString()));
                    }
                }

                JSONObject openHours = (JSONObject) obj.get("hours");
                if (openHours == null) {
                    return;
                }
                String query = "Parse business file and insert into populate.Hours table....";
                for (Object okey1 : attributes1.keySet()) {
                    String day = (String) okey1;
                    JSONObject businessTime = (JSONObject) openHours.get(day);
                    String openTime = (String) businessTime.get("open");
                    String closeTime = (String) businessTime.get("close");
                    hours.add(new Hours(business_id, day, openTime, closeTime));
                }
            }
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
                long votes_funny = (long) ((JSONObject) obj.get("votes")).get("funny");
                long votes_useful = (long) ((JSONObject) obj.get("votes")).get("useful");
                long votes_cool = (long) ((JSONObject) obj.get("votes")).get("cool");
                String user_id = (String) obj.get("user_id");
                String review_id = (String) obj.get("review_id");
                long stars = (long) obj.get("stars");
                String review_date = (String) obj.get("date");
                String text = (String) obj.get("text");
                String business_id = (String) obj.get("business_id");
                // insert review data
                sql = "INSERT INTO Review (votes_funny, votes_useful, votes_cool, user_id, review_id, stars, review_date, text, business_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setLong(1, votes_funny);
                preparedStatement.setLong(2, votes_useful);
                preparedStatement.setLong(3, votes_cool);
                preparedStatement.setString(4, user_id);
                preparedStatement.setString(5, review_id);
                preparedStatement.setLong(6, stars);
                preparedStatement.setString(7, review_date);
                preparedStatement.setString(8, text);
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
            // parse review data

            while ((line = reader.readLine()) != null) {
                // parse user data
                JSONObject obj = (JSONObject) parser.parse(line);
                int votes_funny = (int) ((JSONObject) obj.get("votes")).get("funny");
                int votes_useful = (int) ((JSONObject) obj.get("votes")).get("useful");
                int votes_cool = (int) ((JSONObject) obj.get("votes")).get("cool");
                String yelping_since = (String) obj.get("yelping_since");
                int review_count = (int) obj.get("review_count");
                String name = (String) obj.get("name");
                String user_id = (String) obj.get("user_id");
                double average_stars = (double) obj.get("average_stars");
                // insert user data
                sql = "INSERT INTO YelpUser (votes_funny, votes_useful, votes_cool, yelping_since, review_count, name, user_id, average_stars) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setInt(1, votes_funny);
                preparedStatement.setInt(2, votes_useful);
                preparedStatement.setInt(3, votes_cool);
                preparedStatement.setString(4, yelping_since);
                preparedStatement.setInt(5, review_count);
                preparedStatement.setString(6, name);
                preparedStatement.setString(7, user_id);
                preparedStatement.setDouble(8, average_stars);
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnect() throws SQLException, ClassNotFoundException {
        System.out.println("Checking JDBC...");
        Class.forName(JDBC_DRIVER);
        System.out.println("Connecting to database...");
        return DriverManager.getConnection(ORACLE_URL, DB_USER, DB_PASS);
    }

    public static void main(String[] args) {
        new Populate().run();
    }
}