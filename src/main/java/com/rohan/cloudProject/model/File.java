package com.rohan.cloudProject.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * File Entity (Model) class for the Spring Boot Application. Uses LomBok for getters, setters and constructor
 * initialization.
 *
 * @author rohan_bharti
 */

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "file")
public class File {

    /**
     * Id is generated in a UUID format.
     */
    @Id
    @GeneratedValue(generator = "uuidGenerator")
    @GenericGenerator(name = "uuidGenerator", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonProperty(value = "id", access = JsonProperty.Access.READ_ONLY)
    private String fileId;

    @JsonProperty(value = "file_name", access = JsonProperty.Access.READ_ONLY)
    private String fileName;

    @JsonProperty(value = "url", access = JsonProperty.Access.READ_ONLY)
    private String storageUrl;

    @JsonProperty(value = "upload_date", access = JsonProperty.Access.READ_ONLY)
    @Temporal(TemporalType.TIMESTAMP)
    private String uploadDate;

    @OneToOne(mappedBy = "billFile", cascade = CascadeType.ALL)
    private Bill bill;
}
