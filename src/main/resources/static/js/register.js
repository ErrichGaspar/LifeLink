const form = document.querySelector('.register-form');
const $ = (selector) => document.querySelector(selector);
const show = (selector, message) => { const target = $(selector); if (target) target.textContent = message; };
const clear = () => document.querySelectorAll('.error-message').forEach((item) => { item.textContent = ''; });
const password = $('#password');
const confirmPassword = $('#confirmPassword');
const username = $('#username');
const adminModal = $('#admin-confirmation-modal');
const adminCodeInput = $('#admin-confirmation-code');
const submitButton = form?.querySelector('button[type="submit"]');

const openAdminModal = () => {
    if (!adminModal) return;
    adminModal.classList.add('active');
    adminCodeInput?.focus();
};
const closeAdminModal = () => {
    adminModal?.classList.remove('active');
    if (adminCodeInput) adminCodeInput.value = '';
    form?.querySelector('input[name="adminCode"]')?.remove();
    if (form) delete form.dataset.adminConfirmed;
    submitButton?.focus();
};

const viewPassword = $('#view-password');
if (viewPassword) viewPassword.addEventListener('change', () => {
    password.type = confirmPassword.type = viewPassword.checked ? 'text' : 'password';
});

form?.addEventListener('submit', (event) => {
    clear();
    const first = $('#firstName').value.trim();
    const last = $('#lastName').value.trim();
    const phone = $('#phoneNumber').value.replace(/[\s-]/g, '');
    const pass = password.value;
    let valid = true;
    if (!/^[A-Za-z]+(?:[ '-][A-Za-z]+)*$/.test(first)) { show('#firstName-error', 'First name must contain letters only.'); valid = false; }
    if (!/^[A-Za-z]+(?:[ '-][A-Za-z]+)*$/.test(last)) { show('#lastName-error', 'Last name must contain letters only.'); valid = false; }
    if (!/^[A-Za-z0-9._]+$/.test(pass) || pass.length < 8 || pass.length > 30 || !/[A-Z]/.test(pass) || !/[a-z]/.test(pass) || !/\d/.test(pass) || !/[._]/.test(pass)) { show('#password-error', 'Use 8–30 characters with uppercase, lowercase, number, and . or _.'); valid = false; }
    if (confirmPassword.value !== pass) { show('#confirmPassword-error', 'Passwords do not match.'); valid = false; }
    if (!/^\+639\d{9}$/.test(phone)) { show('#phoneNumber-error', 'Use a Philippine number: +639 followed by 9 digits.'); valid = false; }
    if (!valid) {
        event.preventDefault();
        return;
    }

    const requiresAdminConfirmation = /\d/.test(username?.value || '');
    if (requiresAdminConfirmation && form.dataset.adminConfirmed !== 'true') {
        event.preventDefault();
        show('#admin-code-error', '');
        openAdminModal();
        return;
    }

    if (submitButton) { submitButton.disabled = true; submitButton.textContent = 'Creating account…'; }
});

$('#confirm-admin-registration')?.addEventListener('click', () => {
    const code = adminCodeInput?.value || '';
    if (!code.trim()) {
        show('#admin-code-error', 'Enter the admin confirmation code to continue.');
        adminCodeInput?.focus();
        return;
    }
    let submittedCode = form.querySelector('input[name="adminCode"]');
    if (!submittedCode) {
        submittedCode = document.createElement('input');
        submittedCode.type = 'hidden';
        submittedCode.name = 'adminCode';
        form.appendChild(submittedCode);
    }
    submittedCode.value = code;
    form.dataset.adminConfirmed = 'true';
    form.requestSubmit(submitButton);
});

$('#cancel-admin-confirmation')?.addEventListener('click', closeAdminModal);
adminModal?.addEventListener('click', (event) => {
    if (event.target === adminModal) closeAdminModal();
});
document.addEventListener('keydown', (event) => {
    if (event.key === 'Escape' && adminModal?.classList.contains('active')) closeAdminModal();
});
if (adminModal?.dataset.error === 'true') openAdminModal();

const successModal = $('#registration-success-modal');
if (successModal?.dataset.success === 'true') successModal.classList.add('active');
$('#go-to-login')?.addEventListener('click', () => { window.location.href = '/login'; });
