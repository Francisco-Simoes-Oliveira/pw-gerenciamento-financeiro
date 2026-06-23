


class RegisterUser{
    constructor(
        name,
        email,
        password,
        confirmPassword,
        active
    ) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.active = active;
    }
}

export default RegisterUser
