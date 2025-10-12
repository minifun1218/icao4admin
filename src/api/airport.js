import request from '@/utils/request'

/**
 * 机场管理API
 * 基于 Airport.java 实体类
 */

// ==================== 机场基础CRUD操作 ====================

/**
 * 获取所有机场（分页）
 * GET /airports
 */
export const getAirports = (params = {}) => {
  return request.get('/airports', { params })
}

/**
 * 根据ID获取机场详情
 * GET /airports/{id}
 */
export const getAirportById = (id) => {
  return request.get(`/airports/${id}`)
}

/**
 * 创建机场
 * POST /airports
 */
export const createAirport = (airportData) => {
  return request.post('/airports', airportData)
}

/**
 * 更新机场
 * PUT /airports/{id}
 */
export const updateAirport = (id, airportData) => {
  return request.put(`/airports/${id}`, airportData)
}

/**
 * 删除机场
 * DELETE /airports/{id}
 */
export const deleteAirport = (id) => {
  return request.delete(`/airports/${id}`)
}

// ==================== 机场扩展功能 ====================

/**
 * 根据ICAO代码获取机场
 * GET /airports/by-icao/{icao}
 */
export const getAirportByIcao = (icao) => {
  return request.get(`/airports/by-icao/${icao}`)
}

/**
 * 根据IATA代码获取机场
 * GET /airports/by-iata/{iata}
 */
export const getAirportByIata = (iata) => {
  return request.get(`/airports/by-iata/${iata}`)
}

/**
 * 根据城市获取机场列表
 * GET /airports/by-city/{city}
 */
export const getAirportsByCity = (city, params = {}) => {
  return request.get(`/airports/by-city/${city}`, { params })
}

/**
 * 根据国家获取机场列表
 * GET /airports/by-country/{country}
 */
export const getAirportsByCountry = (country, params = {}) => {
  return request.get(`/airports/by-country/${country}`, { params })
}

/**
 * 搜索机场
 * GET /airports/search
 */
export const searchAirports = (keyword, params = {}) => {
  return request.get('/airports/search', {
    params: { keyword, ...params }
  })
}

/**
 * 检查ICAO代码是否可用
 * GET /airports/check-icao/{icao}
 */
export const checkIcaoAvailability = (icao) => {
  return request.get(`/airports/check-icao/${icao}`)
}

/**
 * 检查IATA代码是否可用
 * GET /airports/check-iata/{iata}
 */
export const checkIataAvailability = (iata) => {
  return request.get(`/airports/check-iata/${iata}`)
}

/**
 * 复制机场
 * POST /airports/{id}/copy
 */
export const copyAirport = (id) => {
  return request.post(`/airports/${id}/copy`)
}

// ==================== 批量操作接口 ====================

/**
 * 批量删除机场
 * DELETE /airports/batch
 */
export const batchDeleteAirports = (airportIds) => {
  return request.delete('/airports/batch', { data: airportIds })
}

/**
 * 批量更新机场信息
 * PUT /airports/batch
 */
export const batchUpdateAirports = (updateData) => {
  return request.put('/airports/batch', updateData)
}

/**
 * 批量操作机场
 * POST /airports/batch-operation
 */
export const batchOperateAirports = (operation, airportIds, data = {}) => {
  return request.post('/airports/batch-operation', {
    operation,
    airportIds,
    data
  })
}

// ==================== 统计相关接口 ====================

/**
 * 获取机场统计信息
 * GET /airports/statistics
 */
export const getAirportStatistics = () => {
  return request.get('/airports/statistics')
}

/**
 * 获取国家统计信息
 * GET /airports/statistics/by-country
 */
export const getCountryStatistics = () => {
  return request.get('/airports/statistics/by-country')
}

/**
 * 获取城市统计信息
 * GET /airports/statistics/by-city
 */
export const getCityStatistics = () => {
  return request.get('/airports/statistics/by-city')
}

// ==================== 导入导出接口 ====================

/**
 * 导出机场数据
 * GET /airports/export
 */
export const exportAirports = (params = {}) => {
  return request.get('/airports/export', {
    params,
    responseType: 'blob'
  })
}

/**
 * 批量导入机场
 * POST /airports/import
 */
export const importAirports = (file, options = {}) => {
  const formData = new FormData()
  formData.append('file', file)
  
  if (options.skipDuplicates !== undefined) {
    formData.append('skipDuplicates', options.skipDuplicates)
  }
  if (options.updateExisting !== undefined) {
    formData.append('updateExisting', options.updateExisting)
  }
  if (options.validateData !== undefined) {
    formData.append('validateData', options.validateData)
  }
  
  return request.post('/airports/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 导出机场模板
 * GET /airports/export/template
 */
export const exportAirportTemplate = () => {
  return request.get('/airports/export/template', {
    responseType: 'blob'
  })
}

/**
 * 验证导入文件
 * POST /airports/import/validate
 */
export const validateImportFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request.post('/airports/import/validate', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// ==================== 高级搜索接口 ====================

/**
 * 高级搜索机场
 * POST /airports/advanced-search
 */
export const advancedSearchAirports = (criteria) => {
  return request.post('/airports/advanced-search', criteria)
}

/**
 * 全文搜索机场
 * GET /airports/search/fulltext
 */
export const fullTextSearchAirports = (query, params = {}) => {
  return request.get('/airports/search/fulltext', {
    params: { query, ...params }
  })
}

// ==================== 工具函数 ====================

/**
 * 验证ICAO代码格式
 */
export const validateIcaoCode = (icao) => {
  // ICAO代码通常是4个字母
  const icaoPattern = /^[A-Z]{4}$/
  return icaoPattern.test(icao)
}

/**
 * 验证IATA代码格式
 */
export const validateIataCode = (iata) => {
  // IATA代码通常是3个字母
  const iataPattern = /^[A-Z]{3}$/
  return iataPattern.test(iata)
}

/**
 * 验证机场数据
 */
export const validateAirportData = (airport) => {
  const errors = []
  
  if (!airport.icao || airport.icao.trim() === '') {
    errors.push('ICAO代码不能为空')
  } else if (!validateIcaoCode(airport.icao)) {
    errors.push('ICAO代码格式不正确（应为4个大写字母）')
  }
  
  if (airport.iata && !validateIataCode(airport.iata)) {
    errors.push('IATA代码格式不正确（应为3个大写字母）')
  }
  
  if (!airport.name || airport.name.trim() === '') {
    errors.push('机场名称不能为空')
  }
  
  if (airport.name && airport.name.length > 200) {
    errors.push('机场名称长度不能超过200个字符')
  }
  
  if (airport.city && airport.city.length > 100) {
    errors.push('城市名称长度不能超过100个字符')
  }
  
  if (airport.country && airport.country.length > 100) {
    errors.push('国家名称长度不能超过100个字符')
  }
  
  return errors
}

/**
 * 生成机场模板
 */
export const generateAirportTemplate = () => {
  return {
    id: null,
    icao: '',
    iata: '',
    name: '',
    city: '',
    country: '',
    createdAt: null
  }
}

/**
 * 格式化机场显示名称
 */
export const formatAirportDisplayName = (airport) => {
  if (!airport) return ''
  
  let displayName = airport.name
  
  if (airport.icao) {
    displayName += ` (${airport.icao}`
    if (airport.iata) {
      displayName += `/${airport.iata}`
    }
    displayName += ')'
  } else if (airport.iata) {
    displayName += ` (${airport.iata})`
  }
  
  if (airport.city) {
    displayName += ` - ${airport.city}`
  }
  
  return displayName
}

/**
 * 获取机场完整信息
 */
export const getAirportFullInfo = (airport) => {
  if (!airport) return ''
  
  const parts = []
  
  if (airport.name) parts.push(airport.name)
  if (airport.city) parts.push(airport.city)
  if (airport.country) parts.push(airport.country)
  
  let info = parts.join(', ')
  
  const codes = []
  if (airport.icao) codes.push(`ICAO: ${airport.icao}`)
  if (airport.iata) codes.push(`IATA: ${airport.iata}`)
  
  if (codes.length > 0) {
    info += ` (${codes.join(', ')})`
  }
  
  return info
}

/**
 * 获取国家选项（常用国家列表）
 */
export const getCountryOptions = () => {
  return [
    { value: 'China', label: '中国' },
    { value: 'United States', label: '美国' },
    { value: 'United Kingdom', label: '英国' },
    { value: 'Germany', label: '德国' },
    { value: 'France', label: '法国' },
    { value: 'Japan', label: '日本' },
    { value: 'South Korea', label: '韩国' },
    { value: 'Australia', label: '澳大利亚' },
    { value: 'Canada', label: '加拿大' },
    { value: 'Singapore', label: '新加坡' },
    { value: 'Thailand', label: '泰国' },
    { value: 'Malaysia', label: '马来西亚' },
    { value: 'Indonesia', label: '印度尼西亚' },
    { value: 'India', label: '印度' },
    { value: 'Russia', label: '俄罗斯' },
    { value: 'Brazil', label: '巴西' },
    { value: 'Argentina', label: '阿根廷' },
    { value: 'South Africa', label: '南非' },
    { value: 'Egypt', label: '埃及' },
    { value: 'UAE', label: '阿联酋' },
    { value: 'Saudi Arabia', label: '沙特阿拉伯' },
    { value: 'Turkey', label: '土耳其' },
    { value: 'Italy', label: '意大利' },
    { value: 'Spain', label: '西班牙' },
    { value: 'Netherlands', label: '荷兰' },
    { value: 'Switzerland', label: '瑞士' },
    { value: 'Sweden', label: '瑞典' },
    { value: 'Norway', label: '挪威' },
    { value: 'Denmark', label: '丹麦' },
    { value: 'Finland', label: '芬兰' }
  ]
}

/**
 * 生成排序选项
 */
export const getSortOptions = () => {
  return [
    { value: 'icao', label: 'ICAO代码' },
    { value: 'iata', label: 'IATA代码' },
    { value: 'name', label: '机场名称' },
    { value: 'city', label: '城市' },
    { value: 'country', label: '国家' },
    { value: 'createdAt', label: '创建时间' }
  ]
}

/**
 * 生成机场代码建议
 */
export const suggestAirportCodes = (name, city) => {
  if (!name && !city) return { icao: '', iata: '' }
  
  const text = (name || city).toUpperCase().replace(/[^A-Z]/g, '')
  
  // 简单的代码生成逻辑（实际应用中可能需要更复杂的算法）
  let icao = ''
  let iata = ''
  
  if (text.length >= 4) {
    icao = text.substring(0, 4)
  } else {
    icao = text.padEnd(4, 'X')
  }
  
  if (text.length >= 3) {
    iata = text.substring(0, 3)
  } else {
    iata = text.padEnd(3, 'X')
  }
  
  return { icao, iata }
}
