package com.example.jubtibe.domain.recipe.dto;

import com.example.jubtibe.domain.like.image.entity.Images;
import com.example.jubtibe.domain.recipe.entity.Recipe;
import com.example.jubtibe.domain.user.entity.UserMbti;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class RecipeResponseDto {
    //    작성자 박성민,권재현
    //    이미지파일 받아오기
    private Long id;
    private List image;
    private String nickname;
    private String title;
    private String material;
    private String content;
    private UserMbti mbti;
    private Integer recipeLike;
    private int hasLike;
    private List comments;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public RecipeResponseDto(Recipe recipe, List comments, Integer recipeLike, int hasLike,List image) {
        this.image=image;
        this.id = recipe.getId();
        this.nickname = recipe.getUser().getNickname();
        this.title = recipe.getTitle();
        this.material = recipe.getMaterial();
        this.content = recipe.getContent();
        this.mbti = recipe.getMbti();
        this.recipeLike = recipeLike;
        this.hasLike = hasLike;
        this.comments = comments;
        this.createdAt = recipe.getCreatedAt();
        this.modifiedAt = recipe.getModifiedAt();
    }
}
