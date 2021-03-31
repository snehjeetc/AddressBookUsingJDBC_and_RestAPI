package addressbook;

import detailsofperson.Contact;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class AddressBookDBTest {
    private AddressBookUtility addressBookservice;

    @Before
    public void init(){ this.addressBookservice = new AddressBookUtility(); }

    @Test
    public void whenContactsRetrieved_FromDB_ShouldMatchTheContactCount() throws AddressBookDBExceptions {
        List<Contact> contactList = addressBookservice.readData();
        int contact_count = 4;
        Assert.assertEquals(contact_count, contactList.size());
    }
}