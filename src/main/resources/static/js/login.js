const registerForm = document.querySelector(".register-form");

const firstNameInput = document.querySelector("#firstName");
const lastNameInput = document.querySelector("#lastName");
const usernameInput = document.querySelector("#username");
const passwordInput = document.querySelector("#password");
const confirmPasswordInput = document.querySelector("#confirmPassword");
const phoneNumberInput = document.querySelector("#phoneNumber");
const bloodTypeInput = document.querySelector("#bloodType");
const termsInput = document.querySelector("#terms");

const firstNameError = document.querySelector("#firstName-error");
const lastNameError = document.querySelector("#lastName-error");
const usernameError = document.querySelector("#username-error");
const passwordError = document.querySelector("#password-error");
const confirmPasswordError = document.querySelector("#confirmPassword-error");
const phoneNumberError = document.querySelector("#phoneNumber-error");
const bloodTypeError = document.querySelector("#bloodType-error");
const termsError = document.querySelector("#terms-error");

const togglePassword = document.querySelector("#toggle-password");
const toggleConfirmPassword = document.querySelector("#toggle-confirm-password");


// =====================================================
// CLEAR ERRORS WHEN USER TYPES
// =====================================================

firstNameInput.addEventListener("input", function () {
    firstNameError.textContent = "";
});

lastNameInput.addEventListener("input", function () {
    lastNameError.textContent = "";
});

usernameInput.addEventListener("input", function () {
    usernameError.textContent = "";
});

passwordInput.addEventListener("input", function () {
    passwordError.textContent = "";
});

confirmPasswordInput.addEventListener("input", function () {
    confirmPasswordError.textContent = "";
});

phoneNumberInput.addEventListener("input", function () {
    phoneNumberError.textContent = "";
});

termsInput.addEventListener("change", function () {
    termsError.textContent = "";
});


// =====================================================
// SHOW / HIDE PASSWORD
// =====================================================

togglePassword.addEventListener("click", function () {

    if (passwordInput.type === "password") {

        passwordInput.type = "text";

        togglePassword.textContent = "⌣";

        togglePassword.setAttribute(
            "aria-label",
            "Hide password"
        );

    } else {

        passwordInput.type = "password";

        togglePassword.textContent = "👁";

        togglePassword.setAttribute(
            "aria-label",
            "Show password"
        );

    }

});


// =====================================================
// SHOW / HIDE CONFIRM PASSWORD
// =====================================================

toggleConfirmPassword.addEventListener("click", function () {

    if (confirmPasswordInput.type === "password") {

        confirmPasswordInput.type = "text";

        toggleConfirmPassword.textContent = "⌣";

        toggleConfirmPassword.setAttribute(
            "aria-label",
            "Hide password"
        );

    } else {

        confirmPasswordInput.type = "password";

        toggleConfirmPassword.textContent = "👁";

        toggleConfirmPassword.setAttribute(
            "aria-label",
            "Show password"
        );

    }

});


// =====================================================
// REGISTRATION FORM VALIDATION
// =====================================================

registerForm.addEventListener("submit", function (event) {

    event.preventDefault();

    // Clear all errors
    firstNameError.textContent = "";
    lastNameError.textContent = "";
    usernameError.textContent = "";
    passwordError.textContent = "";
    confirmPasswordError.textContent = "";
    phoneNumberError.textContent = "";
    termsError.textContent = "";


    // =================================================
    // FIRST NAME
    // =================================================

    if (firstNameInput.value.trim() === "") {

        firstNameError.textContent =
            "Please enter your first name.";

        return;
    }


    // =================================================
    // LAST NAME
    // =================================================

    if (lastNameInput.value.trim() === "") {

        lastNameError.textContent =
            "Please enter your last name.";

        return;
    }


    // =================================================
    // USERNAME
    // =================================================

    if (usernameInput.value.trim() === "") {

        usernameError.textContent =
            "Please enter a username.";

        return;
    }


    // =================================================
    // PASSWORD
    // =================================================

    const password = passwordInput.value;

    // 8–30 characters
    if (password.length < 8 || password.length > 30) {

        passwordError.textContent =
            "Password must be 8–30 characters long.";

        return;
    }


    // Must contain lowercase
    if (!/[a-z]/.test(password)) {

        passwordError.textContent =
            "Password must contain a lowercase letter.";

        return;
    }


    // Must contain uppercase
    if (!/[A-Z]/.test(password)) {

        passwordError.textContent =
            "Password must contain an uppercase letter.";

        return;
    }


    // Only letters, numbers, period and underscore
    if (!/^[A-Za-z0-9._]+$/.test(password)) {

        passwordError.textContent =
            "Password may only contain letters, numbers, . and _.";

        return;
    }


    // =================================================
    // CONFIRM PASSWORD
    // =================================================

    if (confirmPasswordInput.value !== passwordInput.value) {

        confirmPasswordError.textContent =
            "Password is invalid or incorrect.";

        return;
    }


    // =================================================
    // PHONE NUMBER
    // =================================================

    if (phoneNumberInput.value.trim() === "") {

        phoneNumberError.textContent =
            "Please enter your phone number.";

        return;
    }


    // =================================================
    // BLOOD TYPE
    // =================================================

    if (bloodTypeInput.value === "") {

        alert("Please select your blood type.");

        return;
    }


    // =================================================
    // TERMS AND CONDITIONS
    // =================================================

    if (!termsInput.checked) {

        termsError.textContent =
            "You must agree to the Terms and Conditions and Privacy Policy.";

        return;
    }


    // =================================================
    // VALIDATION SUCCESSFUL
    // =================================================

    console.log("Registration validation successful!");

    // Allow the form to submit to Spring Boot
    registerForm.submit();

});