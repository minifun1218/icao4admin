import api from '@/api/index'

/**
 * 首页统计相关API
 * 基于 HomeController 的接口
 */
export const homeApi = {
  /**
   * 获取快速统计信息
   * @returns {Promise} 快速统计数据
   */
  getQuickStats() {
    return api.get('/home/quick-stats')
  },

  /**
   * 获取详细系统统计信息
   * @returns {Promise} 详细系统统计数据
   */
  getSystemStats() {
    return api.get('/home/system-stats')
  },

  /**
   * 获取仪表板数据
   * @returns {Promise} 完整仪表板数据
   */
  getDashboard() {
    return api.get('/home/dashboard')
  },

  /**
   * 系统健康检查
   * @returns {Promise} 系统健康状态
   */
  getHealth() {
    return api.get('/home/health')
  },

  /**
   * 刷新统计缓存 (需要ADMIN权限)
   * @returns {Promise} 刷新结果
   */
  refreshCache() {
    return api.post('/home/refresh-cache')
  }
}

/**
 * 用户管理统计API
 * 基于 UserController 的接口 (需要ADMIN权限)
 */
export const userStatsApi = {
  /**
   * 获取用户管理统计信息
   * @returns {Promise} 用户统计数据
   */
  getUserStats() {
    return api.get('/users/stats')
  }
}

/**
 * 媒体资源统计API
 * 基于 MediaAssetController 的接口 (需要ADMIN权限)
 */
export const mediaStatsApi = {
  /**
   * 获取媒体资源统计信息
   * @returns {Promise} 媒体统计数据
   */
  getMediaStats() {
    return api.get('/media/stats')
  }
}

/**
 * 管理员用户统计API
 * 基于 AdminUserController 的接口 (需要SUPER_ADMIN权限)
 */
export const adminStatsApi = {
  /**
   * 获取管理员用户统计信息
   * @returns {Promise} 管理员统计数据
   */
  getAdminStats() {
    return api.get('/admin/stats')
  }
}

/**
 * 词汇管理统计API
 * 基于 AvVocabController 的接口 (需要ADMIN权限)
 */
export const vocabStatsApi = {
  /**
   * 获取词汇管理统计信息
   * @returns {Promise} 词汇统计数据
   */
  getVocabStats() {
    return api.get('/home/vocab-stats')
  }
}

/**
 * 术语管理统计API
 * 基于 AvTermController 的接口 (需要ADMIN权限)
 */
export const termStatsApi = {
  /**
   * 获取术语管理统计信息
   * @returns {Promise} 术语统计数据
   */
  getTermStats() {
    return api.get('/home/terms-stats')
  }
}

/**
 * 统一的统计API集合
 * 整合所有统计接口，方便使用
 */
export const statsApi = {
  // HomeController 接口
  ...homeApi,
  
  // 其他控制器的统计接口
  getUserStats() {
    return userStatsApi.getUserStats()
  },
  getMediaStats() {
    return mediaStatsApi.getMediaStats()
  },
  getAdminStats() {
    return adminStatsApi.getAdminStats()
  },
  getVocabStats() {
    return vocabStatsApi.getVocabStats()
  },
  getTermStats() {
    return termStatsApi.getTermStats()
  },
  
  /**
   * 获取所有可用的统计数据
   * @returns {Promise} 包含所有统计数据的对象
   */
  async getAllStats() {
    try {
      const [
        quickStats,
        systemStats,
        userStats,
        mediaStats,
        vocabStats,
        termStats
      ] = await Promise.allSettled([
        this.getQuickStats(),
        this.getSystemStats(),
        this.getUserStats(),
        this.getMediaStats(),
        this.getVocabStats(),
        this.getTermStats()
      ])

      return {
        quickStats: quickStats.status === 'fulfilled' ? quickStats.value : null,
        systemStats: systemStats.status === 'fulfilled' ? systemStats.value : null,
        userStats: userStats.status === 'fulfilled' ? userStats.value : null,
        mediaStats: mediaStats.status === 'fulfilled' ? mediaStats.value : null,
        vocabStats: vocabStats.status === 'fulfilled' ? vocabStats.value : null,
        termStats: termStats.status === 'fulfilled' ? termStats.value : null,
        errors: [
          quickStats.status === 'rejected' ? { api: 'quickStats', error: quickStats.reason } : null,
          systemStats.status === 'rejected' ? { api: 'systemStats', error: systemStats.reason } : null,
          userStats.status === 'rejected' ? { api: 'userStats', error: userStats.reason } : null,
          mediaStats.status === 'rejected' ? { api: 'mediaStats', error: mediaStats.reason } : null,
          vocabStats.status === 'rejected' ? { api: 'vocabStats', error: vocabStats.reason } : null,
          termStats.status === 'rejected' ? { api: 'termStats', error: termStats.reason } : null
        ].filter(Boolean)
      }
    } catch (error) {
      console.error('获取所有统计数据失败:', error)
      throw error
    }
  }
}

// 导出默认的 homeApi 保持向后兼容
export default homeApi
