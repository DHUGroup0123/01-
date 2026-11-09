package com.team.team_work.memberA.user.dto;

import java.io.Serializable;

/** Session identity only: passwords and address details must not be stored here. */
public record SessionUser(Long userId, String loginName, String nickname) implements Serializable {
    private static final long serialVersionUID = 1L;
}
