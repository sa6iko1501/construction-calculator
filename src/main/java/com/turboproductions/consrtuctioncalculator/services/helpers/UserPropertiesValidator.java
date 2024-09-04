/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.services.helpers;

import com.turboproductions.consrtuctioncalculator.models.dto.RegistrationRequestDto;
import com.turboproductions.consrtuctioncalculator.models.dto.UpdatePasswordRequestDto;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@NoArgsConstructor
@Component
public class UserPropertiesValidator {
  private static final String PW_PATTERN =
      "^[a-zA-Z0-9]([._-](?![._-])|[a-zA-Z0-9]){3,18}[a-zA-Z0-9]$";
  private static final String PASSWORD_PATTERN =
      "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,20}$";

  public String validateRegistrationRequest(RegistrationRequestDto requestDto) {
    String errMsg =
        confirmPasswordsMatch(requestDto.getPassword(), requestDto.getConfirmedPassword());
    if (errMsg == null) {
      errMsg = validateUsername(requestDto.getUsername());
      if (errMsg == null) {
        errMsg = validatePassword(requestDto.getPassword());
      }
    }
    return errMsg;
  }

  public String validateUpdatePasswordRequest(UpdatePasswordRequestDto requestDto) {
    String errMsg =
        confirmPasswordsMatch(requestDto.getNewPassword(), requestDto.getConfirmNewPassword());
    if (errMsg == null) {
      errMsg = validatePassword(requestDto.getNewPassword());
    }
    return errMsg;
  }

  private String validateUsername(String username) {
    Pattern usernamePattern = Pattern.compile(PW_PATTERN);
    Matcher matcher = usernamePattern.matcher(username);
    if (!matcher.matches()) {
      return """
                    Username must be 5-20 characters long, starting and ending with a letter
                    or number. It can include letters, numbers, and the symbols '.', '_', or '-'
                    (but not consecutively).
                    """;
    }
    return null;
  }

  private String validatePassword(String password) {
    Pattern passwordPattern = Pattern.compile(PASSWORD_PATTERN, Pattern.CASE_INSENSITIVE);
    Matcher matcher = passwordPattern.matcher(password);
    if (!matcher.matches()) {
      return """
                    Password must be 8-20 characters long and include at least one uppercase letter,
                    one lowercase letter, one number, and one special character (@, #, $, %, ^, &, +, =).
                    It cannot contain any spaces.
                    """;
    }
    return null;
  }

  private String confirmPasswordsMatch(String password, String confirmPassword) {
    return password.equals(confirmPassword) ? null : "Passwords do not match.";
  }
}
