package com.javaprojects.crud.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_users")
@Data
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor
public class User {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(columnDefinition="VARCHAR(50)")
    private String email;
    @Column(columnDefinition="VARCHAR(5)")
    private String contryCode;
    @Column(columnDefinition="VARCHAR(15)")
    private String mobile;
    @Column(columnDefinition="VARCHAR(60)")
    private String password;
    @Column(columnDefinition="TINYINT(1) default 1")
    private Integer verify;
    @Column(columnDefinition="VARCHAR(15)")
    private String firstName; 
    @Column(columnDefinition="VARCHAR(15)")
    private String lastName;
}
