package com.gdc.tripmate.domain.tag.entity;

import com.gdc.tripmate.global.TimeStamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag extends TimeStamp {

    /**
     * 컬럼 - 연관관계 컬럼을 제외한 컬럼을 정의합니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    // 태그 카테고리 (MBTI, HOBBY, INTEREST 등)
    @Column
    private String category;

    /**
     * 생성자
     */
    public Tag(String name, String category) {
        this.name = name;
        this.category = category;
    }
    
    public Tag(String name) {
        this.name = name;
        this.category = "DEFAULT";
    }
}