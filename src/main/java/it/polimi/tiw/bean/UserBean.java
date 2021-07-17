package it.polimi.tiw.bean;

import java.io.Serializable;

public class UserBean implements Serializable {

	private String id;
	private String email;
	private String name;
	private String surname;
	private String psw_hash;
	private String shipment_addr;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getPsw_hash() {
		return psw_hash;
	}

	public void setPsw_hash(String psw_hash) {
		this.psw_hash = psw_hash;
	}

	public String getShipment_addr() {
		return shipment_addr;
	}

	public void setShipment_addr(String shipment_addr) {
		this.shipment_addr = shipment_addr;
	}
}
