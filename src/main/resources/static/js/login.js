const password = document.querySelector('#password');
const viewPassword = document.querySelector('#view-password');
if (password && viewPassword) {
    viewPassword.addEventListener('change', () => {
        password.type = viewPassword.checked ? 'text' : 'password';
    });
}
