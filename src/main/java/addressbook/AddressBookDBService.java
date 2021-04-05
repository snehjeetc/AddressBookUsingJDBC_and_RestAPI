package addressbook;

import detailsofperson.Address;
import detailsofperson.Contact;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AddressBookDBService {

    enum CountBy{
        CITY ("city"),
        STATE ("state");

        private String filler;
        CountBy(String filler){
            this.filler = filler;
        }
    }

    private static String jdbcUrl = "jdbc:mysql://localhost:3306/AddressBookServiceDB?useSSL=false";
    private static String userName = "root";
    private static String passWord = "Rooting@1";
    private static String sql_for_preparedStatement = "SELECT * FROM contact_table WHERE contactID = ?";

    private static PreparedStatement preparedStatement;
    private static AddressBookDBService addressBookDBService;

    private AddressBookDBService(){}

    public static AddressBookDBService getInstance(){
        if(addressBookDBService == null)
            addressBookDBService = new AddressBookDBService();
        return addressBookDBService;
    }

    private Connection getConnection() throws AddressBookDBExceptions {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(jdbcUrl, userName, passWord);
            return connection;
        } catch (SQLException e) {
            throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.CONNECTION_FAILURE);
        }
    }

    private static void prepareStatement(Connection connection) throws SQLException {
        preparedStatement = connection.prepareStatement(sql_for_preparedStatement);
    }

    private List<Contact> retrieveData(Map<Integer, Address> addressMap, String sql_Select_query) throws AddressBookDBExceptions {
        List<Contact> contactList = new ArrayList<>();
        Connection connection = this.getConnection();

        try(Statement statement = connection.createStatement()){
            this.readAddress(connection, addressMap);
            ResultSet resultSet = statement.executeQuery(sql_Select_query);
            while(resultSet.next()){
                int contactID = resultSet.getInt("contactID");
                String firstName = resultSet.getString("firstName");
                String lastName = resultSet.getString("lastName");
                long phoneNumber = resultSet.getLong("phoneNumber");
                String email = resultSet.getString("email");
                Integer zip_code = resultSet.getInt("zip_code");
                LocalDate added_date = resultSet.getDate("added_date").toLocalDate();
                Contact contact = new Contact(contactID, firstName, lastName,
                                              phoneNumber, email, added_date, addressMap.get(zip_code));
                contactList.add(contact);
            }
            return contactList;
        } catch (SQLException e) {
            throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.READ_FAILURE);
        }catch (AddressBookDBExceptions exceptions){
            throw exceptions;
        }finally{
            try {
                connection.close();
            } catch (SQLException e) {
                throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.CONNECTION_CLOSING_FAILURE);
            }
        }
    }

    public List<Contact> readData(Map<Integer, Address> addressMap) throws AddressBookDBExceptions{
        String sql = "SELECT * FROM contact_table";
        return this.retrieveData(addressMap, sql);
    }

    private void readAddress(Connection connection, Map<Integer, Address> addressMap) throws AddressBookDBExceptions {
        try(Statement statement = connection.createStatement()){
            String sql = "SELECT * FROM zip_table";
            ResultSet resultSet = statement.executeQuery(sql);
            while(resultSet.next()){
                Integer zip_code = resultSet.getInt("zip_code");
                String city = resultSet.getString("city");
                String state = resultSet.getString("state");
                if(!addressMap.containsKey(zip_code)){
                    Address address = new Address(zip_code, city, state);
                    addressMap.put(zip_code, address);
                }
            }
        } catch (SQLException e) {
           throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.READ_FAILURE);
        }
    }


    public void updateContact(int contactID, String firstName, String lastName,
                                 Long phoneNumber, String email, Address address) throws AddressBookDBExceptions{
        Connection connection = this.getConnection();
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            try {
                connection.close();
            } catch (SQLException throwables) {
                throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.TRANSACTION_FAILURE,
                                                  AddressBookDBExceptions.Status.CONNECTION_CLOSING_FAILURE);
            }
            throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.TRANSACTION_FAILURE);
        }
        try {
            this.updateAdderssBookTable(connection, address);
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException throwables) {
                throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.UPDATION_FAILURE,
                                                  AddressBookDBExceptions.Status.TRANSACTION_FAILURE);
            }
            try {
                connection.close();
            } catch (SQLException throwables) {
                throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.UPDATION_FAILURE,
                                                  AddressBookDBExceptions.Status.CONNECTION_CLOSING_FAILURE);
            }
            throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.UPDATION_FAILURE);
        }
        String sqlUpdate = String.format("UPDATE contact_table " +
                                         "SET firstName = '%s', lastName = '%s', phoneNumber = %s, " +
                                         "email = '%s', zip_code = %s " +
                                         "WHERE contactID = %s",
                                        firstName, lastName, phoneNumber, email, address.getZip_code(), contactID);
        try(Statement statement = connection.createStatement()){
            statement.executeUpdate(sqlUpdate);
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException throwables) {
                throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.UPDATION_FAILURE,
                                                  AddressBookDBExceptions.Status.TRANSACTION_FAILURE);
            }
            try {
                connection.close();
            } catch (SQLException throwables) {
                throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.UPDATION_FAILURE,
                                                  AddressBookDBExceptions.Status.CONNECTION_CLOSING_FAILURE);
            }
            throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.UPDATION_FAILURE);
        }
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.TRANSACTION_FAILURE);
        }finally{
            try {
                connection.close();
            } catch (SQLException e) {
                throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.CONNECTION_CLOSING_FAILURE);
            }
        }
    }

    private int updateAdderssBookTable(Connection connection, Address address) throws SQLException {
        try(Statement statement = connection.createStatement()){
            String sql = String.format("SELECT zip_code FROM zip_table WHERE zip_code = %s", address.getZip_code());
            ResultSet resultSet = statement.executeQuery(sql);
            if(resultSet.next()) return 0;
            String sqlUpdate = String.format("INSERT INTO zip_table " +
                                             "(zip_code, city, state) VALUES " +
                                             "(%s, '%s', '%s')",
                                             address.getZip_code(), address.getCity(), address.getState());
            int rowsAffected = statement.executeUpdate(sqlUpdate);
            return rowsAffected;
        }
    }

    public Contact getContact(int contactID) throws AddressBookDBExceptions {
        Connection connection = this.getConnection();
        if(preparedStatement == null) {
            try {
                prepareStatement(connection);
            } catch (SQLException e) {
                try {
                    connection.close();
                } catch (SQLException throwables) {
                    throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.READ_FAILURE,
                                                      AddressBookDBExceptions.Status.CONNECTION_CLOSING_FAILURE);
                }
                throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.READ_FAILURE);
            }
        }
        Contact contact = null;
        try {
            preparedStatement.setInt(1, contactID);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                String firstName = resultSet.getString("firstName");
                String lastName = resultSet.getString("lastName");
                long phoneNumber = resultSet.getLong("phoneNumber");
                String email = resultSet.getString("email");
                LocalDate added_date = resultSet.getDate("added_date").toLocalDate();
                contact = new Contact(contactID, firstName, lastName, phoneNumber, email, added_date,null);
            }
            return contact;
        } catch (SQLException e) {
            throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.READ_FAILURE);
        }
    }

    public List<Contact> readDataBetweenDates(Map<Integer, Address> addressMap,
                                              String from, String to)
    throws AddressBookDBExceptions{
        LocalDate fromDate = LocalDate.parse(from);
        LocalDate toDate = (to == null) ? LocalDate.now() : LocalDate.parse(to);
        String sql_select_query = String.format("SELECT * FROM contact_table " +
                                  "WHERE added_date BETWEEN '%s' AND '%s'",
                                   fromDate, toDate);
        return this.retrieveData(addressMap, sql_select_query);
    }

    public int count(CountBy param, String name) throws AddressBookDBExceptions {
       String sql = String.format("SELECT COUNT(%s) FROM " +
                                  "contact_table JOIN zip_table USING (zip_code) " +
                                  "WHERE %s = '%s' " +
                                  "GROUP BY %s",
                                   param.filler, param.filler, name, param.filler);
       int count = 0;
       Connection connection = this.getConnection();
       try( Statement statement = connection.createStatement()){
            ResultSet resultSet = statement.executeQuery(sql);
            if(resultSet.next())
                count = resultSet.getInt(1);
            return count;
       } catch (SQLException e) {
           throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.READ_FAILURE);
       }finally{
           try {
               connection.close();
           } catch (SQLException e) {
               throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.CONNECTION_CLOSING_FAILURE);
           }
       }
    }

    private int write(Connection connection, String sql) throws SQLException {
        int primaryKey = -1;
        try(Statement statement = connection.createStatement()){
            int rowsAffected = statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            if(rowsAffected == 1) {
                ResultSet resultSet = statement.getGeneratedKeys();
                if(resultSet.next()) primaryKey = resultSet.getInt(1);
            }
            return primaryKey;
        }
    }

    public Contact writeContact(String firstName, String lastName, long phoneNumber, String email) throws AddressBookDBExceptions {
        Connection connection = this.getConnection();
        LocalDate currentDate = LocalDate.now();
        String sql = String.format("INSERT INTO contact_table " +
                                   "(firstName, lastName, phoneNumber, email, added_date) VALUES " +
                                   "('%s', '%s', %s, '%s', '%s')",
                                    firstName, lastName, phoneNumber, email, currentDate);
        try {
            int contactID = this.write(connection, sql);
            return new Contact(contactID, firstName, lastName, phoneNumber, email, currentDate, null);
        } catch (SQLException e) {
            throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.UPDATION_FAILURE);
        }finally{
            try {
                connection.close();
            } catch (SQLException e) {
                throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.CONNECTION_CLOSING_FAILURE);
            }
        }
    }

    public Contact writeContact(String firstName, String lastName, long phoneNumber) throws AddressBookDBExceptions {
        Connection connection = this.getConnection();
        LocalDate currentDate = LocalDate.now();
        String sql = String.format("INSERT INTO contact_table " +
                        "(firstName, lastName, phoneNumber, added_date) VALUES " +
                        "('%s', '%s', %s, '%s')",
                firstName, lastName, phoneNumber, currentDate);
        try {
            int contactID = this.write(connection, sql);
            return new Contact(contactID, firstName, lastName, phoneNumber, null, currentDate, null);
        } catch (SQLException e) {
            throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.UPDATION_FAILURE);
        }finally{
            try {
                connection.close();
            } catch (SQLException e) {
                throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.CONNECTION_CLOSING_FAILURE);
            }
        }
    }

    public Contact writeContact(String firstName, long phoneNumber) throws AddressBookDBExceptions {
        Connection connection = this.getConnection();
        LocalDate currentDate = LocalDate.now();
        String sql = String.format("INSERT INTO contact_table " +
                        "(firstName, phoneNumber, added_date) VALUES " +
                        "('%s', %s, '%s')",
                firstName, phoneNumber, currentDate);
        try {
            int contactID = this.write(connection, sql);
            return new Contact(contactID, firstName, null, phoneNumber, null, currentDate, null);
        } catch (SQLException e) {
            throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.UPDATION_FAILURE);
        }finally{
            try {
                connection.close();
            } catch (SQLException e) {
                throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.CONNECTION_CLOSING_FAILURE);
            }
        }
    }

    private int writeContactUsingTransaction(String sqlQuery, Address address) throws AddressBookDBExceptions {
        Connection connection = this.getConnection();
        int contactID = -1;
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            try {
                connection.close();
            } catch (SQLException throwables) {
                throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.TRANSACTION_FAILURE,
                        AddressBookDBExceptions.Status.CONNECTION_CLOSING_FAILURE);
            }
            throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.TRANSACTION_FAILURE);
        }
        try {
            contactID = this.write(connection, sqlQuery);
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException throwables) {
                throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.UPDATION_FAILURE,
                        AddressBookDBExceptions.Status.TRANSACTION_FAILURE);
            }
            try {
                connection.close();
            } catch (SQLException throwables) {
                throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.UPDATION_FAILURE,
                        AddressBookDBExceptions.Status.CONNECTION_CLOSING_FAILURE);
            }
            throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.UPDATION_FAILURE);
        }
        try {
            this.updateAdderssBookTable(connection, address);
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException throwables) {
                throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.UPDATION_FAILURE,
                        AddressBookDBExceptions.Status.TRANSACTION_FAILURE);
            }
            try {
                connection.close();
            } catch (SQLException throwables) {
                throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.UPDATION_FAILURE,
                        AddressBookDBExceptions.Status.CONNECTION_CLOSING_FAILURE);
            }
            throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.UPDATION_FAILURE);
        }
        String sql_Update_Query = String.format("UPDATE contact_table " +
                                                "SET zip_code = %s " +
                                                "WHERE contactID = %s",
                                                address.getZip_code(), contactID);
        try {
            this.write(connection, sql_Update_Query);
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException throwables) {
                throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.UPDATION_FAILURE,
                        AddressBookDBExceptions.Status.TRANSACTION_FAILURE);
            }
            try {
                connection.close();
            } catch (SQLException throwables) {
                throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.UPDATION_FAILURE,
                        AddressBookDBExceptions.Status.CONNECTION_CLOSING_FAILURE);
            }
            throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.UPDATION_FAILURE);
        }
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.TRANSACTION_FAILURE);
        }finally{
            try {
                connection.close();
            } catch (SQLException e) {
                throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.CONNECTION_CLOSING_FAILURE);
            }
        }
        return contactID;
    }

    public Contact writeContact(String firstName, String lastName, long phoneNumber, String email, Address address) throws AddressBookDBExceptions {
        Contact contact;
        LocalDate currentDate = LocalDate.now();
        String sql = String.format("INSERT INTO contact_table " +
                                  "(firstName, lastName, phoneNumber, email, added_date) VALUES " +
                                  "('%s', '%s', %s, '%s', '%s')",
                                   firstName, lastName, phoneNumber, email, currentDate);
        int contactID = this.writeContactUsingTransaction(sql, address);
        contact = new Contact(contactID, firstName, lastName, phoneNumber, email, currentDate, null);
        contact.setAddress(address);
        return contact;
    }

    public Contact writeContact(String firstName, String lastName, long phoneNumber, Address address) throws AddressBookDBExceptions {
        Contact contact;
        LocalDate currentDate = LocalDate.now();
        String sql = String.format("INSERT INTO contact_table " +
                                   "(firstName, lastName, phoneNumber, added_date) VALUES " +
                                   "('%s', '%s', %s, '%s')",
                                    firstName, lastName, phoneNumber, currentDate);
        int contactID = this.writeContactUsingTransaction(sql, address);
        contact = new Contact(contactID, firstName, lastName, phoneNumber, null, currentDate, null);
        contact.setAddress(address);
        return contact;
    }

    public Contact writeContact(String firstName, long phoneNumber, Address address) throws AddressBookDBExceptions {
        Contact contact;
        LocalDate currentDate = LocalDate.now();
        String sql = String.format("INSERT INTO contact_table " +
                                   "(firstName, phoneNumber, added_date) VALUES " +
                                   "('%s', %s, '%s')",
                                    firstName, phoneNumber, currentDate);
        int contactID = this.writeContactUsingTransaction(sql, address);
        contact = new Contact(contactID, firstName, null, phoneNumber, null, currentDate, null);
        contact.setAddress(address);
        return contact;
    }

    private int writeRollBackOn(Connection connection, String sql) throws AddressBookDBExceptions {
        int primaryKey = -1;
        try(Statement statement = connection.createStatement()){
            int rowsAffected = statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            if(rowsAffected == 1) {
                ResultSet resultSet = statement.getGeneratedKeys();
                if(resultSet.next()) primaryKey = resultSet.getInt(1);
            }
            return primaryKey;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException exception) {
                throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.UPDATION_FAILURE,
                                                  AddressBookDBExceptions.Status.TRANSACTION_FAILURE);
            }
        }
        return primaryKey;
    }

    public Integer writeContact(Contact contact){
        Connection connection = null;
        try {
            connection = this.getConnection();
        } catch (AddressBookDBExceptions exceptions) {
            exceptions.printStackTrace();
        }
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                connection.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
            return null;
        }
        LocalDate currentDate = LocalDate.now();
        Integer[] contactID = new Integer[] {-1};
        Boolean[] errorInFirstTask = new Boolean[] {true};
        Boolean[] errorInSecondTask = new Boolean[] {true};
        Boolean[] isFirstTaskDone = new Boolean[] {false};
        Connection finalConnection = connection;
        Runnable taskUpdateContactTable = () -> {
            String sql = String.format("INSERT INTO contact_table " +
                            "(firstName, lastName, phoneNumber, email, added_date, zip_code) VALUES " +
                            "('%s', '%s', %s, '%s', '%s', %s)",
                             contact.getFirstName(), contact.getLastName(), contact.getPhoneNumber(),
                             contact.getEmail(), currentDate, contact.getAddress().getZip_code());
            try {
                contactID[0] = this.writeRollBackOn(finalConnection, sql);
                errorInSecondTask[0] = false;
            } catch (AddressBookDBExceptions exceptions) {
              exceptions.printStackTrace();
            }
            try {
                if(!errorInSecondTask[0])
                    finalConnection.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }finally{
                try {
                    finalConnection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };

        Runnable taskUpdateAddressTable = () -> {
            try {
                this.updateAdderssBookTable(finalConnection, contact.getAddress());
                errorInFirstTask[0] = false;
            } catch (SQLException e) {
                try {
                    finalConnection.rollback();
                } catch (SQLException exception) {
                    exception.printStackTrace();
                }finally{
                    isFirstTaskDone[0] = true;
                }
            }
        };
        Thread thread1 = new Thread(taskUpdateAddressTable, contact.getFirstName());
        Thread thread2 = new Thread(taskUpdateContactTable, contact.getLastName());
        thread1.start();
        while(thread1.isAlive()){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(errorInFirstTask[0]){
            try {
                finalConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }finally{
                return null;
            }
        }
        thread2.start();
        while(thread2.isAlive()) {}
        if(errorInSecondTask[0])
            return null;
       return contactID[0];
    }
}
