package addressbook;

import detailsofperson.Address;
import detailsofperson.Contact;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
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

    @Test
    public void givenMultipleEmployees_WhenAddedToDB_UsingMultithreading_ShouldMatchEmployeeEntries() throws AddressBookDBExceptions {
        Contact[] contacts = new Contact[] {
                new Contact(0, "Sayan", "Sarkar", 8823141599l,
                        "sayansarkar@gmail.com", LocalDate.now(),
                              new Address(	700052, "Kolkata", "West Bengal")),
                new Contact(0, "Chayan", "Ghosh", 7777723421l,
                            "chayanghost@gmail.com", LocalDate.now(),
                              new Address(735215, "Hasimara", "West Bengal")),
                new Contact(0, "Nayan", "Singh", 9897897333l,
                           "nayan@gmail.com", LocalDate.now(),
                              new Address(190001, "Srinagar", "J&K")),
                new Contact(0, "Dale", "Stein", 9234998929l,
                            "dailystoned@gmail.com", LocalDate.now(),
                              new Address(500001, "Hyderabad", "Telengana")),
                new Contact(0, "Scorpion", "Tail", 9247888899l,
                        "thescorpiontail@gmail.com", LocalDate.now(),
                              new Address(734104, "Darjeeling", "West Bengal"))
        };
        addressBookservice.readData();
        addressBookservice.writeContacts(Arrays.asList(contacts));
        long count = addressBookservice.count();
        Assert.assertEquals(10, count);
    }
}
