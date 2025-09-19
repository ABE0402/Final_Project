package com.example.hong.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Integer id;

    // 'tagName' 필드명을 'name'으로 변경하고,
    // @Column의 name 속성을 DB에 맞게 "name"으로 수정
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String category;
}