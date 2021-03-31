package detailsofperson;

import scannerwrapper.ScannerWrapped;

public class Address {
	private int zip_code;
	private String city;
	private String state;

	public Address(int zip_code, String city, String state){
		this.zip_code = zip_code;
		this.city = city;
		this.state = state;
	}
}
