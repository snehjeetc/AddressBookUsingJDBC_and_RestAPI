package detailsofperson;
import scannerwrapper.ScannerWrapped;

public class Contact {
	int contactID;
	String firstName;
	String lastName;
	long phoneNumber;
	String email;
	Address address;

	public Contact(int contactID, String firstName, String lastName, long phoneNumber,
                   String email, Address address) {
        this.contactID = contactID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
    }
}
