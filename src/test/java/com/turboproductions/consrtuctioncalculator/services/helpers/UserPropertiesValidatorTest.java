/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.services.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.turboproductions.consrtuctioncalculator.models.dto.RegistrationRequestDto;
import com.turboproductions.consrtuctioncalculator.models.dto.UpdatePasswordRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserPropertiesValidatorTest {
  private UserPropertiesValidator validator;
  private static final String INVALID_PASSWORD =
      """
                    Password must be 8-20 characters long and include at least one uppercase letter,
                    one lowercase letter, one number, and one special character (@, #, $, %, ^, &, +, =).
                    It cannot contain any spaces.
                    """;
  private static final String INVALID_USERNAME =
      """
                Username must be 5-20 characters long, starting and ending with a letter
                or number. It can include letters, numbers, and the symbols '.', '_', or '-'
                (but not consecutively).
                """;
  private static final String PASSWORDS_DONT_MATCH = "Passwords do not match.";

  @BeforeEach
  void setUp() {
    validator = new UserPropertiesValidator();
  }

  @Test
  void testValidateRegistrationRequest() {
    RegistrationRequestDto requestDto = new RegistrationRequestDto();
    requestDto.setUsername("validUser1");
    requestDto.setPassword("Valid1@Password");
    requestDto.setConfirmedPassword("Valid1@Password");

    String result = validator.validateRegistrationRequest(requestDto);
    assertNull(result);
  }

  @Test
  void testValidateRegistrationRequestInvalidUsername() {
    RegistrationRequestDto requestDto = new RegistrationRequestDto();
    requestDto.setUsername("inva!id");
    requestDto.setPassword("Valid1@Password");
    requestDto.setConfirmedPassword("Valid1@Password");

    String result = validator.validateRegistrationRequest(requestDto);
    assertEquals(INVALID_USERNAME, result);
  }

  @Test
  void testValidateRegistrationRequestInvalidPassword() {
    RegistrationRequestDto requestDto = new RegistrationRequestDto();
    requestDto.setUsername("validUser1");
    requestDto.setPassword("invalidpassword");
    requestDto.setConfirmedPassword("invalidpassword");

    String result = validator.validateRegistrationRequest(requestDto);
    assertEquals(INVALID_PASSWORD, result);
  }

  @Test
  void testValidateUpdatePasswordRequestValid() {
    UpdatePasswordRequestDto requestDto = new UpdatePasswordRequestDto();
    requestDto.setNewPassword("Valid1@NewPassword");
    requestDto.setConfirmNewPassword("Valid1@NewPassword");

    String result = validator.validateUpdatePasswordRequest(requestDto);
    assertNull(result);
  }

  @Test
  void testValidateUpdatePasswordRequestInvalid() {
    UpdatePasswordRequestDto requestDto = new UpdatePasswordRequestDto();
    requestDto.setNewPassword("short");
    requestDto.setConfirmNewPassword("short");

    String result = validator.validateUpdatePasswordRequest(requestDto);
    assertEquals(INVALID_PASSWORD, result);
  }

  @Test
  void testValidateUpdatePasswordRequestPasswordsDoNotMatch() {
    UpdatePasswordRequestDto requestDto = new UpdatePasswordRequestDto();
    requestDto.setNewPassword("Valid1@NewPassword");
    requestDto.setConfirmNewPassword("NotMatchingNewPassword");
    String result = validator.validateUpdatePasswordRequest(requestDto);
    assertEquals(PASSWORDS_DONT_MATCH, result);
  }

  @Test
  void testValidateRegistrationRequestPasswordsDoNotMatch() {
    RegistrationRequestDto requestDto = new RegistrationRequestDto();
    requestDto.setPassword("Valid1@NewPassword");
    requestDto.setConfirmedPassword("NotMatchingPassword");
    String result = validator.validateRegistrationRequest(requestDto);
    assertEquals(PASSWORDS_DONT_MATCH, result);
  }
}
