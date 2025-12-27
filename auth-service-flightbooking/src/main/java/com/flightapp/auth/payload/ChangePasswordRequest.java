package com.flightapp.auth.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
	    @NotBlank
	    private String oldPassword;

	    @NotBlank
	    @Size(min = 8, message = "Password must be at least 8 characters")
	    private String newPassword;

	    @NotBlank
	    private String confirmPassword;
	}

