package addressbook;

import detailsofperson.Address;
import detailsofperson.Contact;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AddressBookDBService {
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

    public List<Contact> readData(Map<Integer, Address> addressMap) throws AddressBookDBExceptions {
        List<Contact> contactList = new ArrayList<>();
        Connection connection = this.getConnection();

        String sql_Select_query = "SELECT * FROM contact_table";
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
                Contact contact = new Contact(contactID, firstName, lastName,
                                              phoneNumber, email, addressMap.get(zip_code));
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
                contact = new Contact(contactID, firstName, lastName, phoneNumber, email, null);
            }
            return contact;
        } catch (SQLException e) {
            throw new AddressBookDBExceptions(AddressBookDBExceptions.Status.READ_FAILURE);
        }
    }
}
