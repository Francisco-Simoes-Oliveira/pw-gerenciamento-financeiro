import api from "@/config/axiosConfig"

export const goalService = {
  list: () => api.get("/api/goals"),
  create: (data) => api.post("/api/goals", data),
  update: (id, data) => api.put(`/api/goals/${id}`, data),
  updateProgress: (id, currentAmount) => api.patch(`/api/goals/${id}/progress`, { currentAmount }),
  cancel: (id) => api.delete(`/api/goals/${id}`),
}
