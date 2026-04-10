import "./login.css";

const Login = () => {
  return (
    <div className="content">
      <form action="">
        <div>
          <span>Email</span>
          <input type="text" placeholder="Email" />
        </div>
        <div>
          <span>Senha</span>
          <input type="password" placeholder="******" />
        </div>
        <button>Entrar</button>
      </form>
    </div>
  );
};

export default Login;
