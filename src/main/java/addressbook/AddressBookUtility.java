package addressbook;

import detailsofperson.Address;
import detailsofperson.Contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddressBookUtility {
    private List<Contact> contactList;
    private Map<Integer, Address> addressMap;
    private AddressBookDBService addressBookDBService;

    public AddressBookUtility(){
        addressBookDBService = AddressBookDBService.getInstance();
        this.contactList = new ArrayList<>();
        this.addressMap = new HashMap<>();
    }

    public List<Contact> readData() throws AddressBookDBExceptions {
        return addressBookDBService.readData(addressMap);
    }
}
