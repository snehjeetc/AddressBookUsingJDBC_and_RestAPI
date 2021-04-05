package addressbook;

import com.google.gson.Gson;
import detailsofperson.Address;
import detailsofperson.Contact;
import io.restassured.RestAssured;
import io.restassured.response.Response;
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

    private Contact[] getContactList() {
        Response response = RestAssured.get("/contactlist");
        System.out.println("Contacts in JSON Server: \n" + response.asString());
        Contact[] contactArray = new Gson().fromJson(response.asString(), Contact[].class);
        return contactArray;
    }
}
