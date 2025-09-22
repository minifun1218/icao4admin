# 故事复述题目管理组件使用说明

## 概述

故事复述题目管理系统为ICAO4民航英语考试系统提供专业的故事复述题目管理功能。该系统严格按照民航英语考试要求设计，支持完整的考试流程管理。

## 考试流程说明

故事复述部分采用听说结合的形式进行考核，考试时间约为5分钟，具体流程如下：

1. **音频播放阶段**：播放与民航管制工作相关的非正常或特情情景短文（100-120词）
2. **播放设置**：录音播放2遍，两遍之间设有5秒间隔
3. **准备时间**：考生收听录音后，有300秒的准备时间
4. **复述阶段**：考生用自己的语言进行口头复述
5. **自动跳转**：300秒结束后，系统自动跳转至下一题

## 组件架构

### 1. StoryRetellPanel.vue - 主管理组件

**功能特性：**
- 故事复述题目的增删改查
- 支持批量操作（删除、状态更新）
- 高级搜索和筛选功能
- 音频文件上传和管理
- 考试参数配置
- 实时预览功能

**主要操作：**
- 创建故事：点击"添加故事复述题"按钮
- 编辑故事：点击列表中的"编辑"按钮
- 删除故事：支持单个删除和批量删除
- 复制故事：快速创建相似题目
- 预览故事：查看完整的考试流程

### 2. StoryRetellPreview.vue - 预览组件

**功能特性：**
- 故事内容展示
- 音频播放控制
- 考试流程演示
- 配置参数显示
- 评分标准展示

**演示功能：**
- 模拟完整考试流程
- 实时计时器显示
- 阶段进度指示
- 自动流程控制

## 数据结构

### 故事复述题目数据模型

```javascript
{
  id: String,                    // 题目ID
  title: String,                 // 故事标题
  content: String,               // 故事内容（100-120词）
  audioUrl: String,              // 音频文件URL
  difficulty: Number,            // 难度等级（1-6）
  displayOrder: Number,          // 显示顺序
  
  // 考试配置
  preparationTime: Number,       // 准备时间（默认300秒）
  responseTime: Number,          // 回答时间（默认300秒）
  playCount: Number,             // 播放次数（默认2次）
  playInterval: Number,          // 播放间隔（默认5秒）
  autoNext: Boolean,             // 自动跳转（默认true）
  showTimer: Boolean,            // 显示计时器（默认true）
  
  // 元数据
  enabled: Boolean,              // 启用状态
  remark: String,                // 备注信息
  createdAt: Date,               // 创建时间
  updatedAt: Date                // 更新时间
}
```

## API接口

### 基础CRUD操作

```javascript
// 获取故事列表
questionBankApi.getStoryRetellItems(params)

// 获取单个故事
questionBankApi.getStoryRetellItemById(id)

// 创建故事
questionBankApi.createStoryRetellItem(data)

// 更新故事
questionBankApi.updateStoryRetellItem(id, data)

// 删除故事
questionBankApi.deleteStoryRetellItem(id)

// 复制故事
questionBankApi.copyStoryRetellItem(id)
```

### 批量操作

```javascript
// 批量删除
questionBankApi.batchDeleteStoryRetellItems(itemIds)

// 批量更新状态
questionBankApi.batchUpdateStoryRetellItemsStatus(itemIds, enabled)
```

### 工具方法

```javascript
// 数据验证
questionBankApi.validateStoryRetellItem(data)

// 获取默认配置
questionBankApi.getStoryRetellDefaultConfig()

// 获取评分标准
questionBankApi.getStoryRetellScoringCriteria()
```

## 使用指南

### 1. 创建故事复述题

1. 点击"添加故事复述题"按钮
2. 填写故事标题和内容（建议100-120词）
3. 上传音频文件（支持MP3、WAV等格式）
4. 设置难度等级
5. 配置考试参数：
   - 准备时间：建议300秒
   - 回答时间：建议300秒
   - 播放次数：建议2次
   - 播放间隔：建议5秒
6. 设置其他选项（自动跳转、显示计时器等）
7. 点击"创建"保存

### 2. 编辑故事

1. 在故事列表中找到要编辑的题目
2. 点击"编辑"按钮
3. 修改相应内容
4. 点击"更新"保存更改

### 3. 预览故事

1. 点击故事列表中的"预览"按钮
2. 查看故事内容和音频
3. 点击"开始演示"体验完整考试流程
4. 观察各个阶段的时间控制和流程转换

### 4. 批量管理

1. 使用表格左侧的复选框选择多个故事
2. 点击"批量删除"进行批量删除操作
3. 或使用"批量编辑"进行批量状态更新

## 配置说明

### 默认考试配置

```javascript
{
  preparationTime: 300,    // 准备时间300秒
  responseTime: 300,       // 回答时间300秒
  playCount: 2,           // 播放次数2次
  playInterval: 5,        // 播放间隔5秒
  autoNext: true,         // 时间结束自动跳转
  allowPause: false,      // 不允许暂停
  showTimer: true         // 显示计时器
}
```

### 评分标准

| 评分项目 | 权重 | 描述 |
|---------|------|------|
| 内容准确性 | 30% | 复述内容与原文的符合程度 |
| 语言流利性 | 25% | 表达的流畅程度和连贯性 |
| 发音准确性 | 20% | 发音的清晰度和准确性 |
| 语法运用 | 15% | 语法结构的正确使用 |
| 词汇运用 | 10% | 词汇的准确性和丰富性 |

## 注意事项

### 内容要求
- 故事内容应为100-120词的民航管制相关情景
- 内容应涉及非正常或特情情况
- 语言难度应适中，符合ICAO4级别要求

### 音频要求
- 音频格式：MP3、WAV、OGG、M4A、AAC
- 文件大小：不超过50MB
- 音质要求：清晰，无杂音
- 语速：适中，便于理解

### 时间设置
- 准备时间：建议300秒（5分钟）
- 回答时间：建议300秒（5分钟）
- 播放间隔：建议5秒
- 总考试时间：约5分钟

### 技术要求
- 支持现代浏览器的音频播放
- 响应式设计，支持移动设备
- 实时数据同步和状态管理

## 故障排除

### 常见问题

1. **音频上传失败**
   - 检查文件格式是否支持
   - 确认文件大小不超过限制
   - 检查网络连接状态

2. **预览功能异常**
   - 确认音频文件已正确上传
   - 检查浏览器音频播放权限
   - 刷新页面重试

3. **数据保存失败**
   - 检查必填字段是否完整
   - 确认网络连接正常
   - 查看控制台错误信息

### 性能优化

- 音频文件建议压缩后上传
- 大量数据时使用分页加载
- 定期清理无用的音频文件
- 使用CDN加速音频文件访问

## 更新日志

### v1.0.0
- 初始版本发布
- 支持基础的故事复述题目管理
- 实现完整的考试流程预览
- 集成音频上传和播放功能
- 提供批量操作支持
