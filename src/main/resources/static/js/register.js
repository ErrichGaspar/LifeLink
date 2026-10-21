const form = document.querySelector('.register-form');
const $ = (id) => document.querySelector(id);
const show = (id, message) => { $(id).textContent = message; };
const clear = () => document.querySelectorAll('.error-message').forEach((item) => item.textContent = '');
const password = $('#password');
const confirmPassword = $('#confirmPassword');
const viewPassword = $('#view-password');
if (viewPassword) viewPassword.addEventListener('change', () => {
    password.type = confirmPassword.type = viewPassword.checked ? 'text' : 'password';
});
form?.addEventListener('submit', (event) => {
    clear();
    const first = $('#firstName').value.trim(); const last = $('#lastName').value.trim();
    const phone = $('#phoneNumber').value.replace(/[\s-]/g, ''); const pass = password.value;
    let valid = true;
    if (!/^[A-Za-z]+(?:[ '-][A-Za-z]+)*$/.test(first)) { show('#firstName-error', 'First name must contain letters only.'); valid = false; }
    if (!/^[A-Za-z]+(?:[ '-][A-Za-z]+)*$/.test(last)) { show('#lastName-error', 'Last name must contain letters only.'); valid = false; }
    if (!/^[A-Za-z0-9._]+$/.test(pass) || pass.length < 8 || pass.length > 30 || !/[A-Z]/.test(pass) || !/[a-z]/.test(pass) || !/\d/.test(pass) || !/[._]/.test(pass)) { show('#password-error', 'Use 8–30 characters with uppercase, lowercase, number, and . or _.'); valid = false; }
    if (confirmPassword.value !== pass) { show('#confirmPassword-error', 'Passwords do not match.'); valid = false; }
    if (!/^\+639\d{9}$/.test(phone)) { show('#phoneNumber-error', 'Use a Philippine number: +639 followed by 9 digits.'); valid = false; }
    if (!valid) event.preventDefault();
    else {
        const submit = form.querySelector('button[type="submit"]');
        if (submit) { submit.disabled = true; submit.textContent = 'Creating account…'; }
    }
});
const successModal = $('#registration-success-modal');
if (successModal?.dataset.success === 'true') successModal.classList.add('active');
$('#go-to-login')?.addEventListener('click', () => window.location.href = '/login');
