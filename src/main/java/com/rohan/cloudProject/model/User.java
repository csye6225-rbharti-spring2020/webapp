package com.rohan.cloudProject.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

/**
 * User Entity (Model) class for the Spring Boot Application. Uses LomBok for getters, setters and constructor
 * initialization.
 *
 * @author rohan_bharti
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User implements Serializable {

    /**
     * Id is generated in a UUID format.
     */
    @Id
    @GeneratedValue(generator = "uuidGenerator")
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @JsonProperty(value = "id", access = JsonProperty.Access.READ_ONLY)
    private String id;

    @JsonProperty(value = "first_name")
    @NotBlank(message = "First Name is mandatory")
    private String firstName;

    @JsonProperty(value = "last_name")
    @NotBlank(message = "Last Name is mandatory")
    private String lastName;

    @JsonProperty(value = "email_address")
    @NotBlank(message = "Email is mandatory")
    private String email;

    @JsonProperty(value = "password", access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "Password is mandatory")
    private String password;

    /**
     * Stores the Java Date Object in the Timestamp form in the database.
     */
    @JsonProperty(value = "account_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date accountCreated;

    /**
     * Stores the Java Date Object in the Timestamp form in the database.
     */
    @JsonProperty(value = "account_updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date accountUpdated;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    @JsonProperty(value = "bills", access = JsonProperty.Access.WRITE_ONLY)
    private Set<Bill> bills;

    public User(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }

    public String getAccountUpdated() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");
        return simpleDateFormat.format(accountUpdated);
    }

    public String getAccountCreated() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");
        return simpleDateFormat.format(accountCreated);
    }
}
