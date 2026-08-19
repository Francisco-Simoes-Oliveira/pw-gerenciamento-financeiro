// Mocks para o Dashboard onde a API ainda não suporta

export const fetchDashboardChartMock = async () => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve([
        { name: "Jan", Receitas: 5000, Despesas: 3200 },
        { name: "Fev", Receitas: 6200, Despesas: 2400 },
        { name: "Mar", Receitas: 7400, Despesas: 4100 },
        { name: "Abr", Receitas: 6900, Despesas: 4300 },
        { name: "Mai", Receitas: 8200, Despesas: 3150 },
        { name: "Jun", Receitas: 7800, Despesas: 3900 },
      ]);
    }, 500);
  });
};

export const fetchCategoryDistributionMock = async () => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve([
        { name: "Alimentação", value: 40, color: "#F59E0B" },
        { name: "Transporte", value: 25, color: "#3B82F6" },
        { name: "Moradia", value: 20, color: "#EF4444" },
        { name: "Lazer", value: 15, color: "#10B981" },
      ]);
    }, 500);
  });
};

export const fetchSharedWalletsMock = async () => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve([
        {
          name: "Família Oliveira",
          type: "Ativo",
          balance: "R$ 4.200,00",
          members: [
            "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=100&h=100&q=80",
            "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=100&h=100&q=80",
            "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=100&h=100&q=80",
          ]
        },
        {
          name: "República",
          type: "Standard",
          balance: "R$ 1.850,25",
          members: [
            "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=100&h=100&q=80",
            "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=100&h=100&q=80",
            "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&w=100&h=100&q=80",
          ]
        }
      ]);
    }, 500);
  });
};
