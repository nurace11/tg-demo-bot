package com.nurasick.springtelegrambot.SpringDemoBot.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.HashMap;

@Entity(name = "users")
//@Table(name = "users")
public class User {

    @Id
    long chatId;

    double gold;
    long dollars;

    int pickAxeLevel;
    double pickAxePower;

    String firstName;
    String lastName;
    String userName;
    Timestamp registerDate;

    public User() {
        pickAxePower = 1;
        pickAxeLevel = 1;
    }

/*    public User(long chatId) {
        pickAxePower = 1;
        pickAxeLevel = 1;
        this.chatId = chatId;
    }*/

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public double getGold() {
        return gold;
    }

    public double getPickAxePower() {
        return pickAxePower;
    }

    public void setPickAxePower(double pickAxePower) {
        this.pickAxePower = pickAxePower;
    }

    public void setGold(double gold) {
        this.gold = gold;
    }

    public long getDollars() {
        return dollars;
    }

    public void setDollars(long dollars) {
        this.dollars = dollars;
    }

    public int getPickAxeLevel() {
        return pickAxeLevel;
    }

    public void setPickAxeLevel(int pickAxeLevel) {
        this.pickAxeLevel = pickAxeLevel;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Timestamp getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(Timestamp registerDate) {
        this.registerDate = registerDate;
    }
}
