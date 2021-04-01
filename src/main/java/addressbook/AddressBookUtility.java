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
        contactList = addressBookDBService.readData(addressMap);
        return contactList;
    }

    public boolean isSyncWithDB(int contactID) throws AddressBookDBExceptions {
        Contact contactInDB = addressBookDBService.getContact(contactID);
        Contact contactInSys = this.getContact(contactID);
        if(contactInDB == null && contactInSys == null)
            return true;
        else if(contactInDB == null)
            return false;
        else if(contactInSys == null)
            return false;
        else
            return contactInDB.equals(contactInSys);
    }

    public void updateData(int contactID, String firstName, String lastName, Long phoneNumber,
                           String email, Address address) throws AddressBookDBExceptions {
        Contact existingContact = this.getContact(contactID);
        if(existingContact == null)
            return;
        if(firstName == null)
            firstName = existingContact.getFirstName();
        if(lastName == null)
            lastName = existingContact.getLastName();
        if(phoneNumber == null)
            phoneNumber = existingContact.getPhoneNumber();
        if(email == null)
            email = existingContact.getEmail();
        if(address == null)
            address = existingContact.getAddress();
        else
            if(addressMap.containsKey(address.getZip_code()))
                address = addressMap.get(address.getZip_code());
        this.addressBookDBService.updateContact(contactID, firstName, lastName,
                                                phoneNumber, email, address);
        existingContact.setFirstName(firstName);
        existingContact.setLastName(lastName);
        existingContact.setPhoneNumber(phoneNumber);
        existingContact.setEmail(email);
        existingContact.setAddress(address);
        if(!addressMap.containsKey(address.getZip_code()))
            addressMap.put(address.getZip_code(), address);
    }

    private Contact getContact(int contactID) {
        return contactList.stream().filter(contact -> contact.getContactID() == contactID)
                                   .findFirst()
                                   .orElse(null);
    }

    public List<Contact> readDataBetween(String from, String to) throws AddressBookDBExceptions{
        return addressBookDBService.readDataBetweenDates(addressMap ,from, to);
    }

    public int count(AddressBookDBService.CountBy param, String name) throws AddressBookDBExceptions {
        return addressBookDBService.count(param, name);
    }

    public int writeContact(String firstName, String lastName, long phoneNumber, String email) throws AddressBookDBExceptions {
        Contact contactAdded = this.addressBookDBService.writeContact(firstName, lastName, phoneNumber, email);
        contactList.add(contactAdded);
        return contactAdded.getContactID();
    }

    public int writeContact(String firstName, String lastName, long phoneNumber) throws AddressBookDBExceptions {
        Contact contactAdded = this.addressBookDBService.writeContact(firstName, lastName, phoneNumber);
        contactList.add(contactAdded);
        return contactAdded.getContactID();
    }

    public int writeContact(String firstName, long phoneNumber) throws AddressBookDBExceptions {
        Contact contactAdded = this.addressBookDBService.writeContact(firstName, phoneNumber);
        contactList.add(contactAdded);
        return contactAdded.getContactID();
    }

    public int writeContact(String firstName, String lastName,
                             long phoneNumber, String email,
                             Address address) throws AddressBookDBExceptions {
        Contact contactAdded = this.addressBookDBService.writeContact(firstName, lastName, phoneNumber, email, address);
        contactList.add(contactAdded);
        if(!addressMap.containsKey(address.getZip_code()))
            addressMap.put(address.getZip_code(), address);
        return contactAdded.getContactID();
    }

    public int writeContact(String firstName, String lastName, long phoneNumber, Address address) throws AddressBookDBExceptions {
        Contact contactAdded = this.addressBookDBService.writeContact(firstName, lastName, phoneNumber, address);
        contactList.add(contactAdded);
        if(!addressMap.containsKey(address.getZip_code()))
            addressMap.put(address.getZip_code(), address);
        return contactAdded.getContactID();
    }

    public int writeContact(String firstName, long phoneNumber, Address address) throws AddressBookDBExceptions {
        Contact contactAdded = this.addressBookDBService.writeContact(firstName, phoneNumber, address);
        contactList.add(contactAdded);
        if(!addressMap.containsKey(address.getZip_code()))
            addressMap.put(address.getZip_code(), address);
        return contactAdded.getContactID();
    }
}
