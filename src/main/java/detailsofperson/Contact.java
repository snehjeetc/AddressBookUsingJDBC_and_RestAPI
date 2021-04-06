package detailsofperson;

import java.time.LocalDate;
import java.util.Objects;

public class Contact {
	int id;             //Note for JSON object's first property should be "id"
                        //Or else it the status cod will be 500
                        //TypeError: Cannot read property 'id' of undefined
	String firstName;
	String lastName;
	long phoneNumber;
	String email;
	LocalDate added_date;
	Address address;

	public Contact(){

    }

	public Contact(int contactID, String firstName, String lastName, long phoneNumber,
                   String email, LocalDate added_date, Address address) {
        this.id = contactID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.added_date = added_date;
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public long getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public LocalDate getDate() { return added_date; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;
        return id == contact.id &&
                phoneNumber == contact.phoneNumber &&
                Objects.equals(firstName, contact.firstName) &&
                Objects.equals(lastName, contact.lastName) &&
                Objects.equals(email, contact.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, phoneNumber);
    }

    @Override
    public String toString(){
	    return id + " " + firstName + " " + phoneNumber;
    }


}
