package com.rohan.cloudProject.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

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
    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull
    private Date uploadDate;

    @OneToOne(mappedBy = "billFile")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Bill bill;

    @JsonProperty(value = "user_id", access = JsonProperty.Access.WRITE_ONLY)
    private String userId;

    @JsonProperty(value = "file_size", access = JsonProperty.Access.WRITE_ONLY)
    private Long fileSize;

    @JsonProperty(value = "MD5_Hash", access = JsonProperty.Access.WRITE_ONLY)
    private String md5Hash;
}
