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

    public AddressBookUtility(List<Contact> contactList) {
        this();
        this.contactList = new ArrayList<>(contactList);
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
        return contactList.stream().filter(contact -> contact.getId() == contactID)
                                   .findFirst()
                                   .orElse(null);
    }

    public List<Contact> readDataBetween(String from, String to) throws AddressBookDBExceptions{
        return addressBookDBService.readDataBetweenDates(addressMap ,from, to);
    }

    public int count(){
        return contactList.size();
    }

    public int count(AddressBookDBService.CountBy param, String name) throws AddressBookDBExceptions {
        return addressBookDBService.count(param, name);
    }

    public int writeContact(String firstName, String lastName, long phoneNumber, String email) throws AddressBookDBExceptions {
        Contact contactAdded = this.addressBookDBService.writeContact(firstName, lastName, phoneNumber, email);
        contactList.add(contactAdded);
        return contactAdded.getId();
    }

    public int writeContact(String firstName, String lastName, long phoneNumber) throws AddressBookDBExceptions {
        Contact contactAdded = this.addressBookDBService.writeContact(firstName, lastName, phoneNumber);
        contactList.add(contactAdded);
        return contactAdded.getId();
    }

    public int writeContact(String firstName, long phoneNumber) throws AddressBookDBExceptions {
        Contact contactAdded = this.addressBookDBService.writeContact(firstName, phoneNumber);
        contactList.add(contactAdded);
        return contactAdded.getId();
    }

    public int writeContact(String firstName, String lastName,
                             long phoneNumber, String email,
                             Address address) throws AddressBookDBExceptions {
        Contact contactAdded = this.addressBookDBService.writeContact(firstName, lastName, phoneNumber, email, address);
        contactList.add(contactAdded);
        if(!addressMap.containsKey(address.getZip_code()))
            addressMap.put(address.getZip_code(), address);
        return contactAdded.getId();
    }

    public int writeContact(String firstName, String lastName, long phoneNumber, Address address) throws AddressBookDBExceptions {
        Contact contactAdded = this.addressBookDBService.writeContact(firstName, lastName, phoneNumber, address);
        contactList.add(contactAdded);
        if(!addressMap.containsKey(address.getZip_code()))
            addressMap.put(address.getZip_code(), address);
        return contactAdded.getId();
    }

    public int writeContact(String firstName, long phoneNumber, Address address) throws AddressBookDBExceptions {
        Contact contactAdded = this.addressBookDBService.writeContact(firstName, phoneNumber, address);
        contactList.add(contactAdded);
        if(!addressMap.containsKey(address.getZip_code()))
            addressMap.put(address.getZip_code(), address);
        return contactAdded.getId();
    }

    public void writeContacts(List<Contact> contactList) {
        Map<Integer, Boolean> additionStatusOfContact = new HashMap<>();
        contactList.forEach(contact -> {
            Runnable task = () -> {
                additionStatusOfContact.put(contact.hashCode(), false);
                Integer contactID = addressBookDBService.writeContact(contact);
                if(contactID != null) {
                    /*
                    System.out.println(contact);
                    Contact contactAdded = contact;
                    contactAdded.setContactID(contactID);
                    System.out.println(contact);

                    //If you do this then the changes also reflects in your contact class
                    //causing your program to have an infinite loop
                    //as the hashcode of the contact object will change when the thread is going
                    //to end
                     */
                    Contact contactAdded = new Contact(contactID, contact.getFirstName(), contact.getLastName(),
                                                       contact.getPhoneNumber(), contact.getEmail(), contact.getDate(),
                                                       contact.getAddress());
                    this.addContact(contactAdded);
                    if (!addressMap.containsKey(contactAdded.getAddress().getZip_code()))
                        addressMap.put(contactAdded.getAddress().getZip_code(), contact.getAddress());
                }
                additionStatusOfContact.put(contact.hashCode(), true);
            };
            Thread thread = new Thread(task);
            thread.start();
        });
        while(additionStatusOfContact.containsValue(false)){
            try {
                Thread.sleep(10);
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void addContact(Contact contact) {
        this.contactList.add(contact);
    }
}
