package com.vinniesnp.nightpatrol.api.model;

public class UserChange {

    public String getFirstName() {
        return firstName;
    }

    final String firstName;
    final String lastName;
    final String email;
    final String phone;
//    final String teamId;

    public UserChange(String fName, String lName, String email, String phone) {
        this.firstName = fName;
        this.lastName = lName;
        this.email = email;
        this.phone = phone;
//        this.teamId = teamId;
    }
}
