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
}
