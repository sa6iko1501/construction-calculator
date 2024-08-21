/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.controllers;

import com.turboproductions.consrtuctioncalculator.models.User;
import com.turboproductions.consrtuctioncalculator.models.dto.LoginRequestDto;
import com.turboproductions.consrtuctioncalculator.models.dto.RegistrationRequestDto;
import com.turboproductions.consrtuctioncalculator.models.dto.UpdatePasswordRequestDto;
import com.turboproductions.consrtuctioncalculator.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class IndexController {
  private final UserService userService;

  @GetMapping("/home")
  String getHomePage() {
    return "homepage";
  }

  @GetMapping("/register")
  String getRegisterPage(Model model) {
    model.addAttribute("registerRequest", new RegistrationRequestDto());
    return "register";
  }

  @GetMapping("/login")
  String getLoginPage(Model model) {
    model.addAttribute("loginRequest", new LoginRequestDto());
    return "login";
  }

  @GetMapping("/user")
  String getUserInfoPage(Model model, @AuthenticationPrincipal User authenticatedUser) {
    model.addAttribute("user", authenticatedUser);
    return "user-info-page";
  }

  @PostMapping("/register")
  String registerUser(
      Model model, @ModelAttribute("registerRequest") RegistrationRequestDto registrationRequest) {
    String errMsg = userService.registerUser(registrationRequest);
    if (errMsg != null) {
      model.addAttribute("message", errMsg);
      return "register";
    }
    return "redirect:/login";
  }

  @GetMapping("/user/update-password")
  String getInsertOldPassword(Model model) {
    model.addAttribute("updatePasswordRequest", new UpdatePasswordRequestDto());
    return "update-password-page";
  }

  @PostMapping("/user/update-password")
  String updatePassword(
      RedirectAttributes model,
      @ModelAttribute("updatePasswordRequest") UpdatePasswordRequestDto requestDto,
      @AuthenticationPrincipal User authenticatedUser) {
    String errMsg = userService.updatePassword(requestDto, authenticatedUser);
    if (errMsg == null) {
      model.addFlashAttribute("message", "Password changed successfully");
    } else {
      model.addFlashAttribute("message", errMsg);
    }
    return "redirect:/user";
  }
}
