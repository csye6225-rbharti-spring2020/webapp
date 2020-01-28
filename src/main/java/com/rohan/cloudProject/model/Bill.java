package com.rohan.cloudProject.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * Bill Entity (Model) class for the Spring Boot Application. Uses LomBok for getters, setters and constructor
 * initialization.
 *
 * @author rohan_bharti
 */

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bills")
public class Bill {

    /**
     * Id is generated in a UUID format.
     */
    @Id
    @GeneratedValue(generator = "uuidGenerator")
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @JsonProperty(value = "id", access = JsonProperty.Access.READ_ONLY)
    private String billId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private User user;

    @Transient
    @JsonProperty(value = "owner_id", access = JsonProperty.Access.READ_ONLY)
    private String userId;

    @JsonProperty(value = "created_ts", access = JsonProperty.Access.READ_ONLY)
    @Temporal(TemporalType.TIMESTAMP)
    private Date billCreated;

    @JsonProperty(value = "updated_ts", access = JsonProperty.Access.READ_ONLY)
    @Temporal(TemporalType.TIMESTAMP)
    private Date billUpdated;

    @JsonProperty(value = "vendor")
    @NotBlank(message = "Vendor is mandatory")
    private String vendor;

    @JsonProperty(value = "bill_date")
    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull
    private Date billDate;

    @JsonProperty(value = "due_date")
    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull
    private Date dueDate;

    @JsonProperty(value = "amount_due")
    @NotNull
    @DecimalMin(value = "0.01", message = "The Amount Due has to be greater than 0.01$")
    private Double amountDue;

    @JsonProperty(value = "categories")
    @NotNull
    @ElementCollection
    private List<String> categories;

    @Enumerated(EnumType.STRING)
    @JsonProperty(value = "paymentStatus")
    @NotNull
    private PayStatus payStatus;
}
