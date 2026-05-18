package com.agileforge.domain.port.in;

import com.agileforge.domain.model.User;

public interface RegisterUseCase {

    User register(String email, String password, String firstName, String lastName);
}
