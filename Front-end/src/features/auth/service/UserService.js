import BaseService from "@/service/BaseService";

class UserService extends BaseService {
    constructor() {
        super('/api/users');
    }

    async login(data) {
        return await this.api.post(`${this.endPoint}/login`, data);
    }

    async register(data) {
        return await this.api.post(this.endPoint, data);
    }
}

export default UserService;