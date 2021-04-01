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

    @Test
    public void whenContactsUpdated_InDB_ShouldSyncWithSystem() throws AddressBookDBExceptions {
        addressBookservice.readData();
        addressBookservice.updateData(2, null, null,
                                      92357896211l, "abhishekBhaskar@gmail.com", null);
        boolean result = addressBookservice.isSyncWithDB(2);
        Assert.assertTrue(result);
    }

    @Test
    public void whenContactRetrieved_FromDB_ThatWereAdded_InParticularPeriod_ShouldMatchTheNumberOfEntries()
    throws AddressBookDBExceptions{
        String from = "2019-01-01";
        String to = null;
        List<Contact> contactList = addressBookservice.readDataBetween(from, to);
        int entries = 2;
        Assert.assertEquals(entries, contactList.size());
    }

    @Test
    public void whenCountedContactsInTheDatabase_ByCityAndState_ShouldMatchTheNumberOfCounts() throws AddressBookDBExceptions {
        String cityName = "Lucknow";
        String stateName = "UP";
        Integer countCity = addressBookservice.count(AddressBookDBService.CountBy.CITY, cityName);
        Integer countState= addressBookservice.count(AddressBookDBService.CountBy.STATE, stateName);
        Assert.assertTrue(countCity.equals(Integer.valueOf(1)) &&
                countState.equals(Integer.valueOf(1)));
    }

    @Test
    public void whenAddedAContactToDB_ShouldSyncWithThe_System() throws AddressBookDBExceptions {
        addressBookservice.readData();
        int contactID = addressBookservice.writeContact("Some", "Girl", 9232314568l, "someGirl@gmail.com");
        boolean result = addressBookservice.isSyncWithDB(contactID);
        Assert.assertTrue(result);
    }
}
