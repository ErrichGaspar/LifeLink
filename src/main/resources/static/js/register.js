// =====================================================
// REGISTER FORM
// =====================================================

const registerForm =
    document.querySelector(".register-form");


// =====================================================
// FORM INPUTS
// =====================================================

const firstNameInput =
    document.querySelector("#firstName");

const lastNameInput =
    document.querySelector("#lastName");

const usernameInput =
    document.querySelector("#username");

const passwordInput =
    document.querySelector("#password");

const confirmPasswordInput =
    document.querySelector("#confirmPassword");

const phoneNumberInput =
    document.querySelector("#phoneNumber");

const bloodTypeInput =
    document.querySelector("#bloodType");

const termsInput =
    document.querySelector("#terms");


// =====================================================
// HIDDEN ADMIN CODE
// =====================================================

const adminCodeInput =
    document.querySelector("#adminCode");


// =====================================================
// ERROR MESSAGES
// =====================================================

const firstNameError =
    document.querySelector("#firstName-error");

const lastNameError =
    document.querySelector("#lastName-error");

const usernameError =
    document.querySelector("#username-error");

const passwordError =
    document.querySelector("#password-error");

const confirmPasswordError =
    document.querySelector("#confirmPassword-error");

const phoneNumberError =
    document.querySelector("#phoneNumber-error");

const bloodTypeError =
    document.querySelector("#bloodType-error");

const termsError =
    document.querySelector("#terms-error");


// =====================================================
// PASSWORD TOGGLE BUTTONS
// =====================================================

const togglePassword =
    document.querySelector("#toggle-password");

const toggleConfirmPassword =
    document.querySelector("#toggle-confirm-password");


// =====================================================
// TERMS / PRIVACY ELEMENTS
// =====================================================

const viewTerms =
    document.querySelector("#view-terms");

const viewPrivacy =
    document.querySelector("#view-privacy");

const termsModal =
    document.querySelector("#terms-modal");

const privacyModal =
    document.querySelector("#privacy-modal");

const closeTerms =
    document.querySelector("#close-terms");

const closePrivacy =
    document.querySelector("#close-privacy");


// =====================================================
// ADMIN CONFIRMATION MODAL
// =====================================================

const adminConfirmationModal =
    document.querySelector("#admin-confirmation-modal");

const adminConfirmationCode =
    document.querySelector("#admin-confirmation-code");

const adminCodeError =
    document.querySelector("#admin-code-error");

const toggleAdminCode =
    document.querySelector("#toggle-admin-code");

const cancelAdminConfirmation =
    document.querySelector("#cancel-admin-confirmation");

const confirmAdminRegistration =
    document.querySelector("#confirm-admin-registration");


// =====================================================
// REGISTRATION SUCCESS MODAL
// =====================================================

const registrationSuccessModal =
    document.querySelector("#registration-success-modal");

const goToLoginButton =
    document.querySelector("#go-to-login");


// =====================================================
// HARD-CODED ADMIN REGISTRATION CODE
// =====================================================

const ADMIN_REGISTRATION_CODE =
    "LIFELINK-ADMIN-2026";


// =====================================================
// SHOW REGISTRATION SUCCESS MODAL
// =====================================================

function openRegistrationSuccessModal() {

    if (registrationSuccessModal) {

        registrationSuccessModal.classList.add(
            "active"
        );

    }

}


// =====================================================
// GO TO LOGIN BUTTON
// =====================================================

if (goToLoginButton) {

    goToLoginButton.addEventListener(
        "click",
        function () {

            window.location.href = "/login";

        }
    );

}


// =====================================================
// CHECK IF REGISTRATION WAS SUCCESSFUL
// =====================================================

const urlParameters =
    new URLSearchParams(
        window.location.search
    );


if (
    urlParameters.get("success") === "true"
) {

    openRegistrationSuccessModal();

}


// =====================================================
// CLEAR ERRORS WHEN USER TYPES
// =====================================================

firstNameInput.addEventListener(
    "input",
    function () {

        firstNameError.textContent = "";

    }
);


lastNameInput.addEventListener(
    "input",
    function () {

        lastNameError.textContent = "";

    }
);


usernameInput.addEventListener(
    "input",
    function () {

        usernameError.textContent = "";

    }
);


passwordInput.addEventListener(
    "input",
    function () {

        passwordError.textContent = "";

    }
);


confirmPasswordInput.addEventListener(
    "input",
    function () {

        confirmPasswordError.textContent = "";

    }
);


phoneNumberInput.addEventListener(
    "input",
    function () {

        phoneNumberError.textContent = "";

    }
);


bloodTypeInput.addEventListener(
    "change",
    function () {

        bloodTypeError.textContent = "";

    }
);


termsInput.addEventListener(
    "change",
    function () {

        termsError.textContent = "";

    }
);


adminConfirmationCode.addEventListener(
    "input",
    function () {

        adminCodeError.textContent = "";

    }
);


// =====================================================
// SHOW / HIDE PASSWORD
// =====================================================

togglePassword.addEventListener(
    "click",
    function () {

        if (
            passwordInput.type ===
            "password"
        ) {

            passwordInput.type = "text";

            togglePassword.textContent =
                "⌣";

            togglePassword.setAttribute(
                "aria-label",
                "Hide password"
            );

        } else {

            passwordInput.type =
                "password";

            togglePassword.textContent =
                "👁";

            togglePassword.setAttribute(
                "aria-label",
                "Show password"
            );

        }

    }
);


// =====================================================
// SHOW / HIDE CONFIRM PASSWORD
// =====================================================

toggleConfirmPassword.addEventListener(
    "click",
    function () {

        if (
            confirmPasswordInput.type ===
            "password"
        ) {

            confirmPasswordInput.type =
                "text";

            toggleConfirmPassword.textContent =
                "⌣";

            toggleConfirmPassword.setAttribute(
                "aria-label",
                "Hide confirm password"
            );

        } else {

            confirmPasswordInput.type =
                "password";

            toggleConfirmPassword.textContent =
                "👁";

            toggleConfirmPassword.setAttribute(
                "aria-label",
                "Show confirm password"
            );

        }

    }
);


// =====================================================
// SHOW / HIDE ADMIN CODE
// =====================================================

toggleAdminCode.addEventListener(
    "click",
    function () {

        if (
            adminConfirmationCode.type ===
            "password"
        ) {

            adminConfirmationCode.type =
                "text";

            toggleAdminCode.textContent =
                "⌣";

            toggleAdminCode.setAttribute(
                "aria-label",
                "Hide admin code"
            );

        } else {

            adminConfirmationCode.type =
                "password";

            toggleAdminCode.textContent =
                "👁";

            toggleAdminCode.setAttribute(
                "aria-label",
                "Show admin code"
            );

        }

    }
);


// =====================================================
// OPEN TERMS AND CONDITIONS
// =====================================================

viewTerms.addEventListener(
    "click",
    function (event) {

        event.preventDefault();

        termsModal.classList.add(
            "active"
        );

    }
);


// =====================================================
// OPEN PRIVACY POLICY
// =====================================================

viewPrivacy.addEventListener(
    "click",
    function (event) {

        event.preventDefault();

        privacyModal.classList.add(
            "active"
        );

    }
);


// =====================================================
// CLOSE TERMS
// =====================================================

closeTerms.addEventListener(
    "click",
    function () {

        termsModal.classList.remove(
            "active"
        );

    }
);


// =====================================================
// CLOSE PRIVACY
// =====================================================

closePrivacy.addEventListener(
    "click",
    function () {

        privacyModal.classList.remove(
            "active"
        );

    }
);


// =====================================================
// CLOSE TERMS WHEN CLICKING OUTSIDE
// =====================================================

termsModal.addEventListener(
    "click",
    function (event) {

        if (
            event.target === termsModal
        ) {

            termsModal.classList.remove(
                "active"
            );

        }

    }
);


// =====================================================
// CLOSE PRIVACY WHEN CLICKING OUTSIDE
// =====================================================

privacyModal.addEventListener(
    "click",
    function (event) {

        if (
            event.target === privacyModal
        ) {

            privacyModal.classList.remove(
                "active"
            );

        }

    }
);


// =====================================================
// OPEN ADMIN CONFIRMATION MODAL
// =====================================================

function openAdminConfirmationModal() {

    adminConfirmationCode.value = "";

    adminCodeError.textContent = "";

    adminConfirmationCode.type =
        "password";

    toggleAdminCode.textContent =
        "👁";

    adminConfirmationModal.classList.add(
        "active"
    );

    setTimeout(
        function () {

            adminConfirmationCode.focus();

        },
        100
    );

}


// =====================================================
// CLOSE ADMIN CONFIRMATION MODAL
// =====================================================

function closeAdminConfirmationModal() {

    adminConfirmationModal.classList.remove(
        "active"
    );

    adminConfirmationCode.value = "";

    adminCodeError.textContent = "";

}


// =====================================================
// CANCEL ADMIN REGISTRATION
// =====================================================

cancelAdminConfirmation.addEventListener(
    "click",
    function () {

        closeAdminConfirmationModal();

        adminCodeInput.value = "";

    }
);


// =====================================================
// CLOSE ADMIN MODAL WHEN CLICKING OUTSIDE
// =====================================================

adminConfirmationModal.addEventListener(
    "click",
    function (event) {

        if (
            event.target ===
            adminConfirmationModal
        ) {

            closeAdminConfirmationModal();

            adminCodeInput.value = "";

        }

    }
);


// =====================================================
// CONFIRM ADMIN REGISTRATION
// =====================================================

confirmAdminRegistration.addEventListener(
    "click",
    function () {

        const enteredCode =
            adminConfirmationCode.value.trim();


        // EMPTY CODE

        if (
            enteredCode === ""
        ) {

            adminCodeError.textContent =
                "Please enter the Admin Registration Code.";

            adminConfirmationCode.focus();

            return;

        }


        // WRONG CODE

        if (
            enteredCode !==
            ADMIN_REGISTRATION_CODE
        ) {

            adminCodeError.textContent =
                "Incorrect Admin Registration Code.";

            adminConfirmationCode.focus();

            return;

        }


        // CORRECT CODE

        adminCodeInput.value =
            enteredCode;


        closeAdminConfirmationModal();


        // SUBMIT FORM

        HTMLFormElement.prototype.submit.call(
            registerForm
        );

    }
);


// =====================================================
// ALLOW ENTER KEY FOR ADMIN CONFIRMATION
// =====================================================

adminConfirmationCode.addEventListener(
    "keydown",
    function (event) {

        if (
            event.key === "Enter"
        ) {

            event.preventDefault();

            confirmAdminRegistration.click();

        }

    }
);


// =====================================================
// CLOSE MODALS WITH ESCAPE
// =====================================================

document.addEventListener(
    "keydown",
    function (event) {

        if (
            event.key === "Escape"
        ) {

            termsModal.classList.remove(
                "active"
            );

            privacyModal.classList.remove(
                "active"
            );

            closeAdminConfirmationModal();

            adminCodeInput.value = "";

        }

    }
);


// =====================================================
// REGISTRATION FORM VALIDATION
// =====================================================

registerForm.addEventListener(
    "submit",
    function (event) {

        event.preventDefault();


        // CLEAR ERRORS

        firstNameError.textContent = "";

        lastNameError.textContent = "";

        usernameError.textContent = "";

        passwordError.textContent = "";

        confirmPasswordError.textContent = "";

        phoneNumberError.textContent = "";

        bloodTypeError.textContent = "";

        termsError.textContent = "";


        // RESET ADMIN CODE

        adminCodeInput.value = "";


        // FIRST NAME

        if (
            firstNameInput.value.trim() === ""
        ) {

            firstNameError.textContent =
                "Please enter your first name.";

            firstNameInput.focus();

            return;

        }


        // LAST NAME

        if (
            lastNameInput.value.trim() === ""
        ) {

            lastNameError.textContent =
                "Please enter your last name.";

            lastNameInput.focus();

            return;

        }


        // USERNAME

        const username =
            usernameInput.value.trim();


        if (
            username === ""
        ) {

            usernameError.textContent =
                "Please enter a username.";

            usernameInput.focus();

            return;

        }


        // PASSWORD

        const password =
            passwordInput.value;


        if (
            password.length < 8 ||
            password.length > 30
        ) {

            passwordError.textContent =
                "Password must be 8–30 characters long.";

            passwordInput.focus();

            return;

        }


        // LOWERCASE LETTER

        if (
            !/[a-z]/.test(password)
        ) {

            passwordError.textContent =
                "Password must contain a lowercase letter.";

            passwordInput.focus();

            return;

        }


        // UPPERCASE LETTER

        if (
            !/[A-Z]/.test(password)
        ) {

            passwordError.textContent =
                "Password must contain an uppercase letter.";

            passwordInput.focus();

            return;

        }


        // ALLOWED CHARACTERS

        if (
            !/^[A-Za-z0-9._]+$/.test(
                password
            )
        ) {

            passwordError.textContent =
                "Password may only contain letters, numbers, . and _.";

            passwordInput.focus();

            return;

        }


        // CONFIRM PASSWORD

        if (
            confirmPasswordInput.value !==
            passwordInput.value
        ) {

            confirmPasswordError.textContent =
                "Password is invalid or incorrect.";

            confirmPasswordInput.focus();

            return;

        }


        // PHONE NUMBER

        if (
            phoneNumberInput.value.trim() === ""
        ) {

            phoneNumberError.textContent =
                "Please enter your phone number.";

            phoneNumberInput.focus();

            return;

        }


        // BLOOD TYPE

        if (
            bloodTypeInput.value === ""
        ) {

            bloodTypeError.textContent =
                "Please select your blood type.";

            bloodTypeInput.focus();

            return;

        }


        // TERMS AND CONDITIONS

        if (
            !termsInput.checked
        ) {

            termsError.textContent =
                "You must agree to the Terms and Conditions and Privacy Policy.";

            termsInput.focus();

            return;

        }


        // DETERMINE ACCOUNT TYPE

        const usernameContainsNumber =
            /\d/.test(username);


        // ADMIN ACCOUNT

        if (
            usernameContainsNumber
        ) {

            openAdminConfirmationModal();

            return;

        }


        // DONOR ACCOUNT

        adminCodeInput.value = "";


        // SUBMIT DONOR REGISTRATION

        HTMLFormElement.prototype.submit.call(
            registerForm
        );

    }
);