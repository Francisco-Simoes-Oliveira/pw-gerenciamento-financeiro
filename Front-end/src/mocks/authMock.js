export const mockLogin = async (data) => {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      // Allow any email/password for the mock, just simulating a successful login.
      if (data.email && data.password) {
        resolve({
          data: {
            success: true,
            data: {
              token: "mock-jwt-token-12345",
              user: {
                id: "e42e4567-e89b-12d3-a456-426614174000",
                name: "Usuário de Teste (Mock)",
                email: data.email
              }
            }
          }
        });
      } else {
        reject(new Error("Credenciais inválidas."));
      }
    }, 1000);
  });
};
