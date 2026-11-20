package com.pgms.model;

import com.pgms.util.Check;

/**
 * OOPS: ABSTRACT CLASS + ENCAPSULATION.
 * Common details of anyone the PG keeps a record of.
 * Fields are private and reached only through getters / setters.
 */
public abstract class Person {

    private int id;
    private String name;
    private String phone;
    private String email;
    private Gender gender;

    protected Person(int id, String name, String phone, String email, Gender gender) {
        this.id = id;
        setName(name);
        setPhone(phone);
        setEmail(email);
        this.gender = gender;
    }

    /** Each kind of person says what they are (polymorphism). */
    public abstract String role();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = Check.text(name, 60, "Name", true);
    }

    public String getPhone() {
        return phone;
    }

    public final void setPhone(String phone) {
        if (phone == null || !phone.matches("[6-9][0-9]{9}")) {
            throw new IllegalArgumentException("Phone must be a 10-digit Indian mobile number");
        }
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public final void setEmail(String email) {
        this.email = Check.email(email);
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return role() + " #" + id + " " + name + " (" + phone + ")";
    }
}
