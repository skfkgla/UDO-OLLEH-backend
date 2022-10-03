package com.udoolleh.backend.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Table(name = "likes")
@Entity
@Getter
@NoArgsConstructor
public class Likes {
    @Id
    @Column(name = "like_id")
    private String id = UUID.randomUUID().toString();

    @ManyToOne(targetEntity = User.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(targetEntity = Board.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @Builder
    public Likes(User user, Board board) {
        this.user = user;
        this.board = board;
    }

}