package com.prosto.indexer.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;

@SuppressWarnings("JpaDataSourceORMInspection")
@Document(indexName = "users")
@Entity
@Table(name = "users", schema = "public")
@Getter
@Setter
@EqualsAndHashCode
public class User {
    @Id
    @Column
    String id;
    @Column
    String firstname;
    @Column
    String lastname;
    @Column
    String fullname;
    @Column
    String street;
    @Column
    String city;
    @Column
    String zip;
    @Column
    String email;
}
