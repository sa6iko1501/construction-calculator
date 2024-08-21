/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.models.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationRequestDto {
  private String username;
  private String password;
  private String confirmedPassword;
}
