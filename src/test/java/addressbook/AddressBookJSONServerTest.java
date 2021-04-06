package addressbook;

import com.google.gson.Gson;
import detailsofperson.Address;
import detailsofperson.Contact;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;

public class AddressBookJSONServerTest {
    @Before
    public void setup(){
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 3000;
    }

    @Test
    public void givenContactsInJSONServer_WhenRetrieved_ShouldMatchTheCount(){
        Contact[] contactList = getContactList();
        AddressBookUtility addressBookService = new AddressBookUtility(Arrays.asList(contactList));
        for(Contact contact : contactList )
            System.out.println(contact);
        long entries = addressBookService.count();
        Assert.assertEquals(5, entries);
    }

    @Test
    public void givenList_Of_New_Employees_WhenAdded_ShouldMatch201ResponseCode_AndTheTotalEntries(){
        Contact[] contacts = new Contact[]{
                new Contact(0, "Raman", "Bharali", 8823112349l,
                        "ramanbharali@gmail.com", LocalDate.now(),
                        new Address(370001, "Bhuj", "Gujarat")),
                new Contact(0, "Lal", "Badshah", 7772345421l,
                        "lalbadshah@gmail.com", LocalDate.now(),
                        new Address(492001, "Ranchi", "Chattishgarh")),
                new Contact(0, "Shekhar", "Faker", 9865432133l,
                        "fakeit@gmail.com", LocalDate.now(),
                        new Address(110001, "Delhi", "Delhi"))
        };
        Contact[] contactList = this.getContactList();
        AddressBookUtility addressBookService = new AddressBookUtility(Arrays.asList(contactList));
        for(Contact contact : contacts){
            Response response = addContactToJsonServer(contact);
            Assert.assertEquals(201, response.getStatusCode());
            Contact contactAdded = new Gson().fromJson(response.asString(), Contact.class);
            addressBookService.addContact(contactAdded);
        }
        long entries = addressBookService.count();
        Assert.assertEquals(8, entries);
    }

    @Test
    public void givenNewPhoneNumberOfAContact_WhenUpdated_ShouldMatch200_Response(){
        Contact[] contactList = this.getContactList();
        AddressBookUtility addressBookService = new AddressBookUtility(Arrays.asList(contactList));
        addressBookService.updateData("Lal", "Badshah", 7700000000l);
        Contact contact = addressBookService.getContact("Lal", "Badshah");

        String contactJson = new Gson().toJson(contact);
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        request.body(contactJson);
        Response response = request.put("/contactlist/"+contact.getId());
        int statusCode = response.getStatusCode();
        Assert.assertEquals(200, statusCode);
    }

    private Response addContactToJsonServer(Contact contact) {
        String contactJson = new Gson().toJson(contact);
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        request.body(contactJson);
        return request.post("/contactlist");
    }

    private Contact[] getContactList() {
        Response response = RestAssured.get("/contactlist");
        System.out.println("Contacts in JSON Server: \n" + response.asString());
        Contact[] contactArray = new Gson().fromJson(response.asString(), Contact[].class);
        return contactArray;
    }
}
