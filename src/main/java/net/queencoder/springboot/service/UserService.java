package net.queencoder.springboot.service;

import java.util.List;

import net.queencoder.springboot.dto.UserDto;
import net.queencoder.springboot.model.User;

public interface UserService {
    void saveUser(UserDto userDto);

    User findUserByEmail(String email);

    List<UserDto> findAllUsers();
}
