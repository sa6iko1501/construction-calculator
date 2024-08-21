/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.services;

import com.turboproductions.consrtuctioncalculator.dao.UserRepository;
import com.turboproductions.consrtuctioncalculator.models.User;
import com.turboproductions.consrtuctioncalculator.models.dto.RegistrationRequestDto;
import com.turboproductions.consrtuctioncalculator.models.dto.UpdatePasswordRequestDto;
import com.turboproductions.consrtuctioncalculator.services.helpers.UserPropertiesValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {
  private final UserPropertiesValidator userPropertiesValidator;
  private final UserRepository userRepository;
  private final BCryptPasswordEncoder encoder;

  @Autowired
  public UserService(
      UserPropertiesValidator validator, UserRepository repository, BCryptPasswordEncoder encoder) {
    this.userPropertiesValidator = validator;
    this.userRepository = repository;
    this.encoder = encoder;
  }

  public String registerUser(RegistrationRequestDto requestDto) {
    String errMsg = userPropertiesValidator.validateRegistrationRequest(requestDto);
    if (errMsg == null) {
      User user = new User();
      user.setUsername(requestDto.getUsername());
      user.setPassword(encoder.encode(requestDto.getPassword()));
      try {
        userRepository.save(user);
        return null;
      } catch (DataIntegrityViolationException ex) {
        return String.format(
            "There is already a user with the name `%s`", requestDto.getUsername());
      }
    }
    return errMsg;
  }

  public String updatePassword(UpdatePasswordRequestDto requestDto, User user) {
    if (encoder.matches(requestDto.getOldPassword(), user.getPassword())) {
      String errMsg = userPropertiesValidator.validateUpdatePasswordRequest(requestDto);
      if (errMsg == null) {
        user.setPassword(encoder.encode(requestDto.getNewPassword()));
        userRepository.save(user);
        return null;
      }
      return errMsg;
    }
    return "Incorrect old password";
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findUserByUsername(username);
    if (user == null) {
      throw new UsernameNotFoundException("User not found.");
    }
    return user;
  }
}
