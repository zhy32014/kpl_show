import request from './request'
import axios from 'axios'


const aiRequest = axios.create({
  baseURL: 'http://localhost:8000',   // 你的 FastAPI 服务地址
  timeout: 60000,                     // AI 生成可能较慢，设长一点
  headers: { 'Content-Type': 'application/json' }
})
// ===================== 认证模块 =====================
export const authApi = {
  login: (data) => request.post('/auth/login', data),
  register: (data) => request.post('/auth/register', data),
  getMe: () => request.get('/auth/me')
}

// ===================== 战队模块 =====================
export const teamApi = {
  getAll: () => request.get('/teams'),
  getById: (id) => request.get(`/teams/${id}`),
  getOfficialList: () => request.get('/teams/official/list'),
  getOfficialIntro: (teamId) => request.get(`/teams/intro/${teamId}`),
  search: (q) => request.get('/teams/search', { params: { q } }),
  getChampion: () => request.get('/teams/champion'),
  getTop: () => request.get('/teams/top'),
  create: (data) => request.post('/teams', data),
  update: (id, data) => request.put(`/teams/${id}`, data),
  delete: (id) => request.delete(`/teams/${id}`)
}

// ===================== 赛事模块 =====================
export const matchApi = {
  getAll: () => request.get('/matches'),
  getById: (id) => request.get(`/matches/${id}`),
  getByStatus: (status) => request.get(`/matches/status/${status}`),
  getByTeam: (teamId) => request.get(`/matches/team/${teamId}`),
  getRecent: (size = 10) => request.get('/matches/recent', { params: { size } }),
  getFinals: () => request.get('/matches/finals'),
  syncFromOfficial: (options) => {
    if (!options) return request.post('/matches/sync')
    if (typeof options === 'string') return request.post('/matches/sync', null, { params: { seasonId: options } })
    const seasonId = options.seasonId
    const seasonIds = Array.isArray(options.seasonIds) ? options.seasonIds.join(',') : options.seasonIds
    return request.post('/matches/sync', null, { params: { seasonId, seasonIds } })
  },
  updateReplay: (id, replayUrl) =>
    request.patch(`/admin/matches/${id}/replay`, null, { params: { replayUrl } }),
  create: (data) => request.post('/matches', data),
  update: (id, data) => request.put(`/matches/${id}`, data),
  updateScore: (id, scoreA, scoreB, status, result) =>
    request.patch(`/admin/matches/${id}/score`, null, { params: { scoreA, scoreB, status, result } }),
  delete: (id) => request.delete(`/matches/${id}`)
}

// ===================== 资讯模块 =====================
export const newsApi = {
  getList: (page = 0, size = 10) => request.get('/news', { params: { page, size } }),
  getById: (id) => request.get(`/news/${id}`),
  getFeatured: () => request.get('/news/featured'),
  getByCategory: (category) => request.get(`/news/category/${category}`),
  search: (q, page = 0, size = 10) => request.get('/news/search', { params: { q, page, size } }),
  create: (data) => request.post('/news', data),
  update: (id, data) => request.put(`/news/${id}`, data),
  delete: (id) => request.delete(`/news/${id}`)
}

// ===================== 社区模块 =====================
export const communityApi = {
  getPosts: (page = 0, size = 20) => request.get('/community/posts', { params: { page, size } }),
  createPost: (data) => request.post('/community/posts', data),
  getComments: (postId) => request.get(`/community/posts/${postId}/comments`),
  addComment: (postId, content) => request.post(`/community/posts/${postId}/comments`, { content }),
  likePost: (postId) => request.post(`/community/posts/${postId}/like`),
  deletePost: (postId) => request.delete(`/community/posts/${postId}`)
}

// ===================== 管理后台 =====================
export const adminApi = {
  getDashboard: () => request.get('/admin/dashboard'),
  getUsers: (page = 0, size = 20) => request.get('/admin/users', { params: { page, size } }),
  updateUserRole: (id, role) => request.put(`/admin/users/${id}/role`, null, { params: { role } }),
  toggleUser: (id, enabled) => request.put(`/admin/users/${id}/enabled`, null, { params: { enabled } }),
  deleteUser: (id) => request.delete(`/admin/users/${id}`),
  getPosts: (page = 0, size = 20) => request.get('/admin/posts', { params: { page, size } }),
  deletePost: (id) => request.delete(`/admin/posts/${id}`)
}

// export const aiApi = {
//   tacticalAnalysis: (data) => request.post('/ai/tactical-analysis', data),
//   coachAdvice: (data) => request.post('/ai/coach-advice', data),
//   commentary: (data) => request.post('/ai/commentary', data),
//   quickChat: (question, context = '') => {
//     const safeQuestion = (question || '').trim()
//     return request.post('/ai/tactical-analysis', {
//       version: 'S39',
//       blueTeam: '用户未指定',
//       blueLineup: '用户未指定',
//       redTeam: '用户未指定',
//       redLineup: '用户未指定',
//       analysisNode: safeQuestion || '请给出一段KPL通用赛事分析',
//       nodeData: context || '这是用户的通用问答场景，请在信息不足时给出【信息缺失提示】，并基于公开常识给出可执行建议。'
//     })
//   }
// }

export const aiApi = {
  // 废弃：战术分析接口（已迁移到自然语言）
  // tacticalAnalysis: (data) => request.post('/ai/tactical-analysis', data),
  // coachAdvice: (data) => request.post('/ai/coach-advice', data),
  // commentary: (data) => request.post('/ai/commentary', data),

  /**
   * 自然语言对话接口
   * @param {string} question - 用户问题，如 "分析一下昨天AG超玩会的团战"
   * @param {string} sessionId - 会话ID，用于多轮对话（可选，默认 'default'）
   * @param {number} temperature - 温度参数，控制随机性（可选，默认 0.3）
   * @returns {Promise} 返回 AI 回答
   */
  quickChat: (question, sessionId = 'default', temperature = 0.3) => {
    const safeQuestion = (question || '').trim()
    if (!safeQuestion) {
      return Promise.reject(new Error('问题不能为空'))
    }
    return aiRequest.post('/ai/chat', {
      prompt: safeQuestion,
      temperature: temperature,
      session_id: sessionId
    })
  }
}