# 听力简答考试组件使用说明

## 概述

听力简答考试组件为ICAO4民航英语考试系统提供专业的听力简答部分考试功能。该系统严格按照民航英语考试要求设计，支持完整的考试流程管理和实时考试执行。

## 考试流程说明

听力简答部分包含2段对话，每段对话后设3个问题，总共6道题目，考试时间约5分钟，具体流程如下：

1. **对话播放阶段**：播放对话音频（100-120词），仅播放一遍
2. **问题回答阶段**：依次回答3个问题，每个问题20秒回答时间
3. **录音功能**：系统对考生回答进行录音
4. **自动跳转**：超时自动跳转下一题
5. **流程重复**：完成第1段对话后，重复流程处理第2段对话

## 组件架构

### 1. ListeningComprehensionExam.vue - 主考试组件

**功能特性：**
- 完整的考试流程控制
- 音频播放管理
- 实时计时器显示
- 录音功能集成
- 考试进度跟踪
- 自动流程控制

**考试阶段：**
- `playing`: 音频播放阶段
- `answering`: 问题回答阶段
- `completed`: 对话完成阶段

### 2. ListeningComprehensionConfig.vue - 配置展示组件

**功能特性：**
- 考试配置参数展示
- 考试流程图解
- 时间分配说明
- 内容要求显示

### 3. ListeningComprehensionScoring.vue - 评分标准组件

**功能特性：**
- 评分标准详细说明
- 分数等级划分
- 评分示例展示
- 评分建议提供

## 数据结构

### 听力简答考试数据模型

```javascript
{
  id: String,                    // 考试ID
  title: String,                 // 考试标题
  dialogs: [                     // 对话数组（2段）
    {
      id: String,                // 对话ID
      title: String,             // 对话标题
      content: String,           // 对话内容（100-120词）
      audioUrl: String,          // 音频文件URL
      audioDuration: Number,     // 音频时长（秒）
      questions: [               // 问题数组（3个）
        {
          id: String,            // 问题ID
          questionText: String,  // 问题内容（≤15词）
          order: Number          // 问题顺序
        }
      ]
    }
  ]
}
```

### 考试结果数据模型

```javascript
{
  examId: String,                // 考试ID
  startTime: Number,             // 开始时间戳
  endTime: Number,               // 结束时间戳
  duration: Number,              // 考试时长（秒）
  answers: [                     // 答案数组
    {
      dialogIndex: Number,       // 对话索引
      questionIndex: Number,     // 问题索引
      questionId: String,        // 问题ID
      questionText: String,      // 问题内容
      recordingUrl: String,      // 录音文件URL
      answeredAt: Number,        // 回答时间戳
      duration: Number           // 回答时长
    }
  ],
  completedDialogs: Number,      // 完成对话数
  answeredQuestions: Number,     // 回答问题数
  totalQuestions: Number         // 总问题数
}
```

## API接口

### 考试配置相关

```javascript
// 获取听力简答考试配置
questionBankApi.getListeningComprehensionExamConfig()

// 验证考试数据
questionBankApi.validateListeningComprehensionExam(examData)

// 获取评分标准
questionBankApi.getListeningComprehensionScoringCriteria()

// 计算考试统计
questionBankApi.calculateListeningComprehensionStats(examData, results)
```

### 工具方法

```javascript
// 计算词数
questionBankApi.getWordCount(text)

// 计算平均问题词数
questionBankApi.calculateAverageQuestionWordCount(dialogs)

// 计算总考试时长
questionBankApi.calculateTotalExamDuration(dialogs, config)
```

## 使用指南

### 1. 基本使用

```vue
<template>
  <ListeningComprehensionExam 
    :exam-data="examData"
    :show-admin-preview="true"
    :auto-start="false"
    @exam-started="handleExamStarted"
    @exam-completed="handleExamCompleted"
    @answer-recorded="handleAnswerRecorded"
  />
</template>

<script setup>
import ListeningComprehensionExam from './components/ListeningComprehensionExam.vue'

const examData = {
  id: 'listening-exam-001',
  title: '听力简答考试',
  dialogs: [
    {
      id: 'dialog-1',
      title: '机场延误情况',
      content: 'Air traffic control to Flight 123...',
      audioUrl: '/audio/dialog1.mp3',
      audioDuration: 45,
      questions: [
        {
          id: 'q1-1',
          questionText: 'What is the main reason for the flight delay?',
          order: 1
        }
        // ... 更多问题
      ]
    }
    // ... 更多对话
  ]
}

const handleExamStarted = (data) => {
  console.log('考试开始:', data)
}

const handleExamCompleted = (result) => {
  console.log('考试完成:', result)
  // 保存考试结果
}

const handleAnswerRecorded = (answer) => {
  console.log('录音答案:', answer)
  // 处理录音数据
}
</script>
```

### 2. 管理员预览模式

```vue
<ListeningComprehensionExam 
  :exam-data="examData"
  :show-admin-preview="true"
/>
```

管理员预览模式包含：
- 对话内容预览
- 考试配置展示
- 评分标准说明

### 3. 自动开始考试

```vue
<ListeningComprehensionExam 
  :exam-data="examData"
  :auto-start="true"
/>
```

## 配置说明

### 默认考试配置

```javascript
{
  totalDialogs: 2,           // 总对话数：2段
  questionsPerDialog: 3,     // 每段对话问题数：3个
  totalQuestions: 6,         // 总问题数：6道
  totalExamTime: 300,        // 总考试时间：5分钟（300秒）
  dialogWordCount: {         // 对话词数要求
    min: 100,
    max: 120
  },
  questionWordLimit: 15,     // 问题字数限制：15词以内
  answerTimePerQuestion: 20, // 每个问题回答时间：20秒
  playCount: 1,              // 音频播放次数：仅1次
  autoNext: true,            // 超时自动跳转
  recordResponse: true,      // 录音回答
  showTimer: true            // 显示计时器
}
```

### 评分标准

| 评分维度 | 权重 | 描述 |
|---------|------|------|
| 内容准确性 | 35% | 回答内容与问题的相关性和准确性 |
| 语言流利性 | 25% | 表达的流畅程度和自然性 |
| 发音清晰度 | 20% | 发音的清晰度和可理解性 |
| 语法正确性 | 12% | 语法结构的正确使用 |
| 回答时效性 | 8% | 在规定时间内完成回答的能力 |

## 事件说明

### exam-started
考试开始时触发
```javascript
{
  startTime: Number,         // 开始时间戳
  examData: Object          // 考试数据
}
```

### exam-completed
考试完成时触发
```javascript
{
  examId: String,           // 考试ID
  startTime: Number,        // 开始时间
  endTime: Number,          // 结束时间
  duration: Number,         // 总时长
  answers: Array,           // 所有答案
  completedDialogs: Number, // 完成对话数
  answeredQuestions: Number // 回答问题数
}
```

### answer-recorded
录音答案时触发
```javascript
{
  dialogIndex: Number,      // 对话索引
  questionIndex: Number,    // 问题索引
  questionId: String,       // 问题ID
  action: String,          // 'start' 或 'stop'
  recordingUrl: String,    // 录音URL（仅stop时）
  duration: Number         // 录音时长（仅stop时）
}
```

## 样式定制

### CSS变量

```css
:root {
  --exam-primary-color: #409EFF;
  --exam-success-color: #67C23A;
  --exam-warning-color: #E6A23C;
  --exam-danger-color: #F56C6C;
  --exam-border-radius: 8px;
  --exam-box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
```

### 自定义样式类

- `.listening-comprehension-exam`: 主容器
- `.exam-header`: 考试头部
- `.exam-progress`: 进度条区域
- `.dialog-card`: 对话卡片
- `.audio-stage`: 音频播放阶段
- `.question-stage`: 问题回答阶段
- `.recording-section`: 录音控制区域

## 技术要求

### 浏览器支持
- Chrome 60+
- Firefox 55+
- Safari 12+
- Edge 79+

### 音频支持
- 支持 MP3、WAV、OGG、M4A、AAC 格式
- 需要浏览器音频播放权限
- 需要麦克风录音权限

### 录音功能
- 基于 MediaRecorder API
- 需要 HTTPS 环境（生产环境）
- 支持 WebM、MP4 录音格式

## 注意事项

### 音频文件要求
- 文件大小：建议不超过 10MB
- 音频时长：建议 30-90 秒
- 音质要求：清晰，无杂音
- 语速：适中，便于理解

### 考试内容要求
- 对话内容：100-120词，民航相关情景
- 问题内容：15词以内，简洁明了
- 语言难度：符合ICAO4级别要求

### 网络要求
- 稳定的网络连接
- 音频文件预加载
- 录音数据实时上传

## 故障排除

### 常见问题

1. **音频无法播放**
   - 检查浏览器音频权限
   - 确认音频文件格式支持
   - 检查网络连接状态

2. **录音功能异常**
   - 检查麦克风权限
   - 确认浏览器支持录音API
   - 检查HTTPS环境

3. **考试流程卡死**
   - 检查JavaScript控制台错误
   - 确认考试数据格式正确
   - 刷新页面重新开始

4. **时间计算错误**
   - 检查系统时间设置
   - 确认时区设置正确
   - 验证考试配置参数

### 性能优化

- 音频文件预加载
- 组件懒加载
- 录音数据分片上传
- 考试状态本地缓存

## 更新日志

### v1.0.0
- 初始版本发布
- 支持完整的听力简答考试流程
- 实现音频播放和录音功能
- 集成实时计时器和进度跟踪
- 提供管理员预览模式
- 支持考试结果统计分析
