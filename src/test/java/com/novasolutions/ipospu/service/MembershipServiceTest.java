package com.novasolutions.ipospu.service;

import com.novasolutions.ipospu.db.MemberDAO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MembershipServiceTest {

    private final MembershipService membershipService = new MembershipService();
    private final MemberDAO memberDAO = new MemberDAO();
    private String testEmail;
    private String testPassword;

    @BeforeEach
    void setUp() {
        testEmail = "testuser_" + System.currentTimeMillis() + "@example.com";

        RegistrationResult result =
                membershipService.registerNonCommercial("Test User", testEmail);

        assertTrue(result.isSuccess(), "Test setup failed: could not register test member");
        assertNotNull(result.getPassword(), "Generated password should not be null");

        testPassword = result.getPassword();
    }

    @AfterEach
    void cleanup() {
        if (testEmail != null) {
            boolean deleted = memberDAO.deleteMemberByEmail(testEmail);
            assertTrue(deleted, "Cleanup failed: test member was not deleted");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Authentication tests
    // ─────────────────────────────────────────────────────────────

    @Test
    void authenticate_validCredentials_returnsSuccess() {
        LoginResult result = membershipService.authenticate(testEmail, testPassword);

        assertTrue(result.isSuccess());
        assertEquals("Login successful", result.getMessage());
        assertNotNull(result.getMember());
        assertEquals(testEmail, result.getMember().email());
    }

    @Test
    void authenticate_wrongPassword_returnsFailure() {
        LoginResult result = membershipService.authenticate(testEmail, "wrongpassword");

        assertFalse(result.isSuccess());
        assertEquals("Invalid email or password", result.getMessage());
        assertNull(result.getMember());
    }

    @Test
    void authenticate_unknownEmail_returnsFailure() {
        LoginResult result = membershipService.authenticate("nobody@example.com", testPassword);

        assertFalse(result.isSuccess());
        assertEquals("No member found with that email", result.getMessage());
        assertNull(result.getMember());
    }

    @Test
    void authenticate_blankEmail_returnsFailure() {
        LoginResult result = membershipService.authenticate("", testPassword);

        assertFalse(result.isSuccess());
        assertEquals("Email and password cannot be empty", result.getMessage());
        assertNull(result.getMember());
    }

    @Test
    void authenticate_nullPassword_returnsFailure() {
        LoginResult result = membershipService.authenticate(testEmail, null);

        assertFalse(result.isSuccess());
        assertEquals("Email and password cannot be empty", result.getMessage());
        assertNull(result.getMember());
    }

    // ─────────────────────────────────────────────────────────────
    // Non-commercial registration tests
    // ─────────────────────────────────────────────────────────────

    @Test
    void registerNonCommercial_validInput_returnsGeneratedPassword() {
        String email = "newuser_" + System.currentTimeMillis() + "@example.com";

        RegistrationResult result =
                membershipService.registerNonCommercial("New User", email);

        assertTrue(result.isSuccess());
        assertNotNull(result.getPassword());
        assertEquals(10, result.getPassword().length());
        assertNull(result.getMessage());

        String password = result.getPassword();
        String special = "!@#$%^&*";

        assertTrue(password.chars().anyMatch(Character::isLetter));
        assertTrue(password.chars().anyMatch(Character::isDigit));
        assertTrue(password.chars().anyMatch(c -> special.indexOf(c) >= 0));

        memberDAO.deleteMemberByEmail(email);
    }

    @Test
    void registerNonCommercial_duplicateEmail_returnsFailure() {
        RegistrationResult result =
                membershipService.registerNonCommercial("Test User", testEmail);

        assertFalse(result.isSuccess());
        assertEquals("Email already in use", result.getMessage());
        assertNull(result.getPassword());
    }

    @Test
    void registerNonCommercial_blankName_returnsFailure() {
        String email = "blankname_" + System.currentTimeMillis() + "@example.com";

        RegistrationResult result =
                membershipService.registerNonCommercial("", email);

        assertFalse(result.isSuccess());
        assertEquals("Name and email cannot be blank", result.getMessage());
        assertNull(result.getPassword());
    }

    @Test
    void registerNonCommercial_nullEmail_returnsFailure() {
        RegistrationResult result =
                membershipService.registerNonCommercial("Test User", null);

        assertFalse(result.isSuccess());
        assertEquals("Name and email cannot be empty", result.getMessage());
        assertNull(result.getPassword());
    }

    @Test
    void registerNonCommercial_blankEmail_returnsFailure() {
        RegistrationResult result =
                membershipService.registerNonCommercial("Test User", "");

        assertFalse(result.isSuccess());
        assertEquals("Invalid email format", result.getMessage());
        assertNull(result.getPassword());
    }

    @Test
    void registerNonCommercial_invalidEmailFormat_returnsFailure() {
        RegistrationResult result =
                membershipService.registerNonCommercial("Test User", "notanemail.com");

        assertFalse(result.isSuccess());
        assertEquals("Invalid email format", result.getMessage());
        assertNull(result.getPassword());
    }

    // ─────────────────────────────────────────────────────────────
    // Commercial registration tests
    // ─────────────────────────────────────────────────────────────

    @Test
    void submitCommercialApplication_validInput_returnsSuccess() {
        String email = "commercial_" + System.currentTimeMillis() + "@example.com";

        CommercialApplicationResult result =
                membershipService.submitCommercialApplication(
                        "Alice Director",
                        "Acme Pharmacy Ltd",
                        "CH123456",
                        "Alice Director, Bob Director",
                        "Pharmacy",
                        "1 High Street, London",
                        email
                );

        assertTrue(result.isSuccess());
        assertNotNull(result.getMessage());

        // cleanup created member/application
        memberDAO.deleteMemberByEmail(email);
    }

    @Test
    void submitCommercialApplication_nullField_returnsFailure() {
        CommercialApplicationResult result =
                membershipService.submitCommercialApplication(
                        null,
                        "Acme Pharmacy Ltd",
                        "CH123456",
                        "Alice Director",
                        "Pharmacy",
                        "1 High Street, London",
                        "commercial@example.com"
                );

        assertFalse(result.isSuccess());
        assertEquals("All fields are required", result.getMessage());
    }

    @Test
    void submitCommercialApplication_blankField_returnsFailure() {
        CommercialApplicationResult result =
                membershipService.submitCommercialApplication(
                        "",
                        "Acme Pharmacy Ltd",
                        "CH123456",
                        "Alice Director",
                        "Pharmacy",
                        "1 High Street, London",
                        "commercial@example.com"
                );

        assertFalse(result.isSuccess());
        assertEquals("All fields are required", result.getMessage());
    }

    @Test
    void submitCommercialApplication_invalidEmail_returnsFailure() {
        CommercialApplicationResult result =
                membershipService.submitCommercialApplication(
                        "Alice Director",
                        "Acme Pharmacy Ltd",
                        "CH123456",
                        "Alice Director",
                        "Pharmacy",
                        "1 High Street, London",
                        "notanemail"
                );

        assertFalse(result.isSuccess());
        assertEquals("Invalid email format", result.getMessage());
    }

    @Test
    void submitCommercialApplication_duplicateMemberEmail_returnsFailure() {
        CommercialApplicationResult result =
                membershipService.submitCommercialApplication(
                        "Alice Director",
                        "Acme Pharmacy Ltd",
                        "CH123456",
                        "Alice Director",
                        "Pharmacy",
                        "1 High Street, London",
                        testEmail
                );

        assertFalse(result.isSuccess());
        assertEquals("An account already exists for this email address", result.getMessage());
    }

    @Test
    void submitCommercialApplication_duplicatePendingApplication_returnsFailure() {
        String email = "pending_" + System.currentTimeMillis() + "@example.com";

        CommercialApplicationResult first =
                membershipService.submitCommercialApplication(
                        "Alice Director",
                        "Acme Pharmacy Ltd",
                        "CH999999",
                        "Alice Director",
                        "Pharmacy",
                        "1 High Street, London",
                        email
                );

        assertTrue(first.isSuccess());

        CommercialApplicationResult second =
                membershipService.submitCommercialApplication(
                        "Alice Director",
                        "Acme Pharmacy Ltd",
                        "CH999999",
                        "Alice Director",
                        "Pharmacy",
                        "1 High Street, London",
                        email
                );

        assertFalse(second.isSuccess());
        assertEquals("A pending application already exists for this email address", second.getMessage());

        memberDAO.deleteMemberByEmail(email);
    }
}