package com.englishmemory.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CATEGORIES")
@SequenceGenerator(name = "default_seq", sequenceName = "SEQ_CATEGORIES", allocationSize = 1)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @Column(name = "COLOR", length = 7)
    @Builder.Default
    private String color = "#6366F1";

    @Column(name = "ICON", length = 50)
    @Builder.Default
    private String icon = "book";
}
