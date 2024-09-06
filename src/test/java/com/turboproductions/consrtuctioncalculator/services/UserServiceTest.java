/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.turboproductions.consrtuctioncalculator.dao.UserRepository;
import com.turboproductions.consrtuctioncalculator.models.User;
import com.turboproductions.consrtuctioncalculator.models.dto.RegistrationRequestDto;
import com.turboproductions.consrtuctioncalculator.models.dto.UpdatePasswordRequestDto;
import com.turboproductions.consrtuctioncalculator.services.helpers.UserPropertiesValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  @Mock private UserPropertiesValidator userPropertiesValidator;

  @Mock private UserRepository userRepository;

  @Mock private BCryptPasswordEncoder encoder;

  @InjectMocks private UserService userService;

  @Test
  void testRegisterUser_Success() {
    RegistrationRequestDto requestDto = new RegistrationRequestDto();
    requestDto.setUsername("validUser");
    requestDto.setPassword("Valid1@Password");
    requestDto.setConfirmedPassword("Valid1@Password");

    when(userPropertiesValidator.validateRegistrationRequest(requestDto)).thenReturn(null);
    when(encoder.encode(requestDto.getPassword())).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class))).thenReturn(new User());

    String result = userService.registerUser(requestDto);

    assertNull(result);
  }

  @Test
  void testRegisterUser_UsernameAlreadyExists() {
    RegistrationRequestDto requestDto = new RegistrationRequestDto();
    requestDto.setUsername("existingUser");
    requestDto.setPassword("Valid1@Password");
    requestDto.setConfirmedPassword("Valid1@Password");

    when(userPropertiesValidator.validateRegistrationRequest(requestDto)).thenReturn(null);
    when(encoder.encode(requestDto.getPassword())).thenReturn("encodedPassword");
    doThrow(new DataIntegrityViolationException("")).when(userRepository).save(any(User.class));

    String result = userService.registerUser(requestDto);

    assertEquals("There is already a user with the name `existingUser`", result);
  }

  @Test
  void testUpdatePassword_Success() {
    UpdatePasswordRequestDto requestDto = new UpdatePasswordRequestDto();
    requestDto.setOldPassword("oldPassword");
    requestDto.setNewPassword("New1@Password");
    requestDto.setConfirmNewPassword("New1@Password");

    User user = new User();
    user.setPassword("encodedOldPassword");

    when(encoder.matches(requestDto.getOldPassword(), user.getPassword())).thenReturn(true);
    when(userPropertiesValidator.validateUpdatePasswordRequest(requestDto)).thenReturn(null);
    when(encoder.encode(requestDto.getNewPassword())).thenReturn("encodedNewPassword");
    when(userRepository.save(user)).thenReturn(user);

    String result = userService.updatePassword(requestDto, user);

    assertNull(result);
  }

  @Test
  void testUpdatePassword_IncorrectOldPassword() {
    UpdatePasswordRequestDto requestDto = new UpdatePasswordRequestDto();
    requestDto.setOldPassword("incorrectOldPassword");
    requestDto.setNewPassword("New1@Password");
    requestDto.setConfirmNewPassword("New1@Password");

    User user = new User();
    user.setPassword("encodedOldPassword");

    when(encoder.matches(requestDto.getOldPassword(), user.getPassword())).thenReturn(false);

    String result = userService.updatePassword(requestDto, user);

    assertEquals("Incorrect old password", result);
  }

  @Test
  void testUpdatePassword_InvalidNewPassword() {
    UpdatePasswordRequestDto requestDto = new UpdatePasswordRequestDto();
    requestDto.setOldPassword("oldPassword");
    requestDto.setNewPassword("short");
    requestDto.setConfirmNewPassword("short");

    User user = new User();
    user.setPassword("encodedOldPassword");

    doReturn(true).when(encoder).matches(requestDto.getOldPassword(), user.getPassword());
    when(userPropertiesValidator.validateUpdatePasswordRequest(any(UpdatePasswordRequestDto.class)))
        .thenCallRealMethod();

    String result = userService.updatePassword(requestDto, user);

    assertEquals(
        """
                Password must be 8-20 characters long and include at least one uppercase letter,
                one lowercase letter, one number, and one special character (@, #, $, %, ^, &, +, =).
                It cannot contain any spaces.
                """,
        result);
  }

  @Test
  void testLoadUserByUsername() {
    String username = "existingUser";
    User user = new User();
    user.setUsername(username);

    when(userRepository.findUserByUsername(any(String.class))).thenReturn(user);

    UserDetails result = userService.loadUserByUsername(username);

    assertNotNull(result, "UserDetails should be returned");
    assertEquals(username, result.getUsername());
  }

  @Test
  void testLoadUserByUsernameUserNotFound() {
    assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("username"));
  }
}
