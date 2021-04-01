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

	public int getZip_code() {
		return zip_code;
	}

	public String getCity() {
		return city;
	}

	public String getState() {
		return state;
	}
}
