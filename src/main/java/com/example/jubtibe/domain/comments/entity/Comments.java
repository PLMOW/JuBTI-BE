package com.example.jubtibe.domain.comments.entity;

import com.example.jubtibe.domain.user.entity.User;
import lombok.Getter;

import javax.persistence.*;

@Getter
@Entity
public class Comments {
    //작성자 권재현
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @Column(nullable = false)
    private String comments;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="userId")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="recipeId")
    private Recipe recipe;
}