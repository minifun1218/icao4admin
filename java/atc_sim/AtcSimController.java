package org.icao4.eqasbackend2.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icao4.eqasbackend2.common.ApiResponse;
import org.icao4.eqasbackend2.dto.request.*;
import org.icao4.eqasbackend2.dto.response.*;
import org.icao4.eqasbackend2.entity.atc_sim.Airport;
import org.icao4.eqasbackend2.entity.atc_sim.AtcScenario;
import org.icao4.eqasbackend2.entity.atc_sim.AtcTurn;
import org.icao4.eqasbackend2.entity.atc_sim.AtcTurnResponse;
import org.icao4.eqasbackend2.service.atc_sim.AirportService;
import org.icao4.eqasbackend2.service.atc_sim.AtcScenarioService;
import org.icao4.eqasbackend2.service.atc_sim.AtcTurnResponseService;
import org.icao4.eqasbackend2.service.atc_sim.AtcTurnService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ATC模拟系统统一控制器
 * 提供机场、场景、轮次、回答的基础CRUD操作接口
 * 同时支持题库管理系统的ATC模拟题相关功能
 */
@Slf4j
@RestController
@RequestMapping("/atc-sim")
@RequiredArgsConstructor
public class AtcSimController {

    private final AirportService airportService;
    private final AtcScenarioService atcScenarioService;
    private final AtcTurnService atcTurnService;
    private final AtcTurnResponseService atcTurnResponseService;

    // ==================== 机场管理 ====================

    /**
     * 获取所有机场
     */
    @GetMapping("/airports")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<AirportResponse>>> getAllAirports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        
        try {
            Sort sort = Sort.by(sortDir.equalsIgnoreCase("DESC") ? 
                               Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Airport> airports = airportService.findAll(pageable);
            // TODO: 转换为AirportResponse
            return ResponseEntity.ok(ApiResponse.success("获取机场列表成功"));
        } catch (Exception e) {
            log.error("获取机场列表失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取机场列表失败"));
        }
    }

    /**
     * 根据ID获取机场
     */
    @GetMapping("/airports/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AirportResponse>> getAirportById(@PathVariable Long id) {
        try {
            Optional<Airport> airport = airportService.findById(id);
            if (airport.isPresent()) {
                // TODO: 转换为AirportResponse
                return ResponseEntity.ok(ApiResponse.success("获取机场信息成功"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("机场不存在"));
            }
        } catch (Exception e) {
            log.error("获取机场信息失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取机场信息失败"));
        }
    }

    /**
     * 创建机场
     */
    @PostMapping("/airports")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AirportResponse>> createAirport(@Valid @RequestBody AirportCreateRequest request) {
        try {
            // TODO: 转换Request为Entity
            Airport airport = new Airport();
            // 设置字段...
            
            Airport savedAirport = airportService.save(airport);
            // TODO: 转换为AirportResponse
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("创建机场成功"));
        } catch (Exception e) {
            log.error("创建机场失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("创建机场失败: " + e.getMessage()));
        }
    }

    /**
     * 更新机场
     */
    @PutMapping("/airports/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AirportResponse>> updateAirport(
            @PathVariable Long id, 
            @Valid @RequestBody AirportUpdateRequest request) {
        try {
            if (!airportService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("机场不存在"));
            }
            
            // TODO: 转换Request为Entity并更新
            Airport airport = new Airport();
            airport.setId(id);
            // 设置字段...
            
            Airport updatedAirport = airportService.save(airport);
            // TODO: 转换为AirportResponse
            return ResponseEntity.ok(ApiResponse.success("更新机场成功"));
        } catch (Exception e) {
            log.error("更新机场失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新机场失败: " + e.getMessage()));
        }
    }

    /**
     * 删除机场
     */
    @DeleteMapping("/airports/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAirport(@PathVariable Long id) {
        try {
            if (!airportService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("机场不存在"));
            }
            
            airportService.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("删除机场成功"));
        } catch (Exception e) {
            log.error("删除机场失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除机场失败: " + e.getMessage()));
        }
    }

    /**
     * 搜索机场
     */
    @GetMapping("/airports/search")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<AirportResponse>>> searchAirports(@RequestParam String keyword) {
        try {
            List<Airport> airports = airportService.searchAirports(keyword);
            // TODO: 转换为AirportResponse列表
            return ResponseEntity.ok(ApiResponse.success("搜索机场成功"));
        } catch (Exception e) {
            log.error("搜索机场失败: keyword={}, error={}", keyword, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("搜索机场失败"));
        }
    }

    // ==================== 场景管理 ====================

    /**
     * 获取所有场景
     */
    @GetMapping("/scenarios")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<AtcScenarioResponse>>> getAllScenarios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        
        try {
            Sort sort = Sort.by(sortDir.equalsIgnoreCase("DESC") ? 
                               Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<AtcScenario> scenarios = atcScenarioService.findAll(pageable);
            // TODO: 转换为AtcScenarioResponse
            return ResponseEntity.ok(ApiResponse.success("获取场景列表成功"));
        } catch (Exception e) {
            log.error("获取场景列表失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取场景列表失败"));
        }
    }

    /**
     * 根据ID获取场景
     */
    @GetMapping("/scenarios/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AtcScenarioResponse>> getScenarioById(@PathVariable Long id) {
        try {
            Optional<AtcScenario> scenario = atcScenarioService.findById(id);
            if (scenario.isPresent()) {
                // TODO: 转换为AtcScenarioResponse
                return ResponseEntity.ok(ApiResponse.success("获取场景信息成功"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("场景不存在"));
            }
        } catch (Exception e) {
            log.error("获取场景信息失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取场景信息失败"));
        }
    }

    /**
     * 创建场景
     */
    @PostMapping("/scenarios")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AtcScenarioResponse>> createScenario(@Valid @RequestBody AtcScenarioCreateRequest request) {
        try {
            // TODO: 转换Request为Entity
            AtcScenario scenario = new AtcScenario();
            // 设置字段...
            
            AtcScenario savedScenario = atcScenarioService.save(scenario);
            // TODO: 转换为AtcScenarioResponse
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("创建场景成功"));
        } catch (Exception e) {
            log.error("创建ATC场景失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("创建ATC场景失败: " + e.getMessage()));
        }
    }

    /**
     * 更新场景
     */
    @PutMapping("/scenarios/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AtcScenarioResponse>> updateScenario(
            @PathVariable Long id, 
            @Valid @RequestBody AtcScenarioCreateRequest request) {
        try {
            if (!atcScenarioService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("场景不存在"));
            }
            
            // TODO: 转换Request为Entity并更新
            AtcScenario scenario = new AtcScenario();
            scenario.setId(id);
            // 设置字段...
            
            AtcScenario updatedScenario = atcScenarioService.save(scenario);
            // TODO: 转换为AtcScenarioResponse
            return ResponseEntity.ok(ApiResponse.success("更新场景成功"));
        } catch (Exception e) {
            log.error("更新ATC场景失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新ATC场景失败: " + e.getMessage()));
        }
    }

    /**
     * 删除场景
     */
    @DeleteMapping("/scenarios/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteScenario(@PathVariable Long id) {
        try {
            if (!atcScenarioService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("场景不存在"));
            }
            
            atcScenarioService.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("删除场景成功"));
        } catch (Exception e) {
            log.error("删除ATC场景失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除ATC场景失败: " + e.getMessage()));
        }
    }

    @PutMapping("/scenarios/{id}/activate")
    public ResponseEntity<AtcScenario> activateScenario(@PathVariable Long id) {
        try {
            AtcScenario scenario = atcScenarioService.activateScenario(id);
            return ResponseEntity.ok(scenario);
        } catch (Exception e) {
            log.error("激活ATC场景失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/scenarios/{id}/deactivate")
    public ResponseEntity<AtcScenario> deactivateScenario(@PathVariable Long id) {
        try {
            AtcScenario scenario = atcScenarioService.deactivateScenario(id);
            return ResponseEntity.ok(scenario);
        } catch (Exception e) {
            log.error("停用ATC场景失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/scenarios/{id}/copy")
    public ResponseEntity<AtcScenario> copyScenario(@PathVariable Long id) {
        try {
            AtcScenario scenario = atcScenarioService.copyScenario(id);
            return ResponseEntity.status(HttpStatus.CREATED).body(scenario);
        } catch (Exception e) {
            log.error("复制ATC场景失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/scenarios/type/{type}")
    public ResponseEntity<List<AtcScenario>> getScenariosByType(@PathVariable String type) {
        List<AtcScenario> scenarios = atcScenarioService.findByScenarioType(type);
        return ResponseEntity.ok(scenarios);
    }

    @GetMapping("/scenarios/difficulty/{level}")
    public ResponseEntity<List<AtcScenario>> getScenariosByDifficulty(@PathVariable Integer level) {
        List<AtcScenario> scenarios = atcScenarioService.findByDifficultyLevel(level);
        return ResponseEntity.ok(scenarios);
    }

    @GetMapping("/scenarios/active")
    public ResponseEntity<List<AtcScenario>> getActiveScenarios() {
        List<AtcScenario> scenarios = atcScenarioService.findActiveScenarios();
        return ResponseEntity.ok(scenarios);
    }

    @GetMapping("/scenarios/search")
    public ResponseEntity<List<AtcScenario>> searchScenarios(@RequestParam String keyword) {
        List<AtcScenario> scenarios = atcScenarioService.searchScenarios(keyword);
        return ResponseEntity.ok(scenarios);
    }

    // ==================== 轮次管理 ====================

    @GetMapping("/turns")
    public ResponseEntity<Page<AtcTurn>> getAllTurns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("DESC") ? 
                           Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<AtcTurn> turns = atcTurnService.findAll(pageable);
        return ResponseEntity.ok(turns);
    }

    @GetMapping("/turns/{id}")
    public ResponseEntity<AtcTurn> getTurnById(@PathVariable Long id) {
        Optional<AtcTurn> turn = atcTurnService.findById(id);
        return turn.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/turns")
    public ResponseEntity<AtcTurn> createTurn(@Valid @RequestBody AtcTurn turn) {
        try {
            AtcTurn savedTurn = atcTurnService.save(turn);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTurn);
        } catch (Exception e) {
            log.error("创建ATC轮次失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/turns/{id}")
    public ResponseEntity<AtcTurn> updateTurn(@PathVariable Long id, @Valid @RequestBody AtcTurn turn) {
        try {
            turn.setId(id);
            AtcTurn updatedTurn = atcTurnService.save(turn);
            return ResponseEntity.ok(updatedTurn);
        } catch (Exception e) {
            log.error("更新ATC轮次失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/turns/{id}")
    public ResponseEntity<Void> deleteTurn(@PathVariable Long id) {
        try {
            atcTurnService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("删除ATC轮次失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/turns/scenario/{scenarioId}")
    public ResponseEntity<List<AtcTurn>> getTurnsByScenario(@PathVariable Long scenarioId) {
        List<AtcTurn> turns = atcTurnService.findByScenarioId(scenarioId);
        return ResponseEntity.ok(turns);
    }

    @PutMapping("/turns/{id}/activate")
    public ResponseEntity<AtcTurn> activateTurn(@PathVariable Long id) {
        try {
            AtcTurn turn = atcTurnService.activateTurn(id);
            return ResponseEntity.ok(turn);
        } catch (Exception e) {
            log.error("激活ATC轮次失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/turns/{id}/deactivate")
    public ResponseEntity<AtcTurn> deactivateTurn(@PathVariable Long id) {
        try {
            AtcTurn turn = atcTurnService.deactivateTurn(id);
            return ResponseEntity.ok(turn);
        } catch (Exception e) {
            log.error("停用ATC轮次失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/turns/speaker/{speakerType}")
    public ResponseEntity<List<AtcTurn>> getTurnsBySpeaker(@PathVariable String speakerType) {
        List<AtcTurn> turns = atcTurnService.findBySpeakerType(speakerType);
        return ResponseEntity.ok(turns);
    }

    @GetMapping("/turns/difficulty/{level}")
    public ResponseEntity<List<AtcTurn>> getTurnsByDifficulty(@PathVariable Integer level) {
        List<AtcTurn> turns = atcTurnService.findByDifficultyLevel(level);
        return ResponseEntity.ok(turns);
    }

    @GetMapping("/turns/search")
    public ResponseEntity<List<AtcTurn>> searchTurns(@RequestParam String keyword) {
        List<AtcTurn> turns = atcTurnService.searchTurns(keyword);
        return ResponseEntity.ok(turns);
    }

    // ==================== 回答管理 ====================

    @GetMapping("/responses")
    public ResponseEntity<Page<AtcTurnResponse>> getAllResponses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("DESC") ? 
                           Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<AtcTurnResponse> responses = atcTurnResponseService.findAll(pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/responses/{id}")
    public ResponseEntity<AtcTurnResponse> getResponseById(@PathVariable Long id) {
        Optional<AtcTurnResponse> response = atcTurnResponseService.findById(id);
        return response.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/responses")
    public ResponseEntity<AtcTurnResponse> createResponse(@Valid @RequestBody AtcTurnResponse response) {
        try {
            AtcTurnResponse savedResponse = atcTurnResponseService.save(response);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedResponse);
        } catch (Exception e) {
            log.error("创建ATC轮次回答失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/responses/submit")
    public ResponseEntity<AtcTurnResponse> submitResponse(
            @RequestParam Long turnId,
            @RequestParam Long userId,
            @RequestParam String responseText,
            @RequestParam(required = false) String audioFilePath,
            @RequestParam(required = false) Integer audioDuration,
            @RequestParam(required = false) Long responseTime) {
        try {
            // 使用现有的submitResponse方法 - 只传递必要参数
            AtcTurnResponse response = atcTurnResponseService.submitResponse(
                    turnId, userId, responseText, responseTime);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("提交轮次回答失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/responses/{id}")
    public ResponseEntity<AtcTurnResponse> updateResponse(@PathVariable Long id, @Valid @RequestBody AtcTurnResponse response) {
        try {
            response.setId(id);
            AtcTurnResponse updatedResponse = atcTurnResponseService.save(response);
            return ResponseEntity.ok(updatedResponse);
        } catch (Exception e) {
            log.error("更新ATC轮次回答失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/responses/{id}")
    public ResponseEntity<Void> deleteResponse(@PathVariable Long id) {
        try {
            atcTurnResponseService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("删除ATC轮次回答失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/responses/turn/{turnId}")
    public ResponseEntity<List<AtcTurnResponse>> getResponsesByTurn(@PathVariable Long turnId) {
        List<AtcTurnResponse> responses = atcTurnResponseService.findByTurnId(turnId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/responses/user/{userId}")
    public ResponseEntity<List<AtcTurnResponse>> getResponsesByUser(@PathVariable Long userId) {
        List<AtcTurnResponse> responses = atcTurnResponseService.findByUserId(userId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/responses/search")
    public ResponseEntity<List<AtcTurnResponse>> searchResponses(@RequestParam String keyword) {
        List<AtcTurnResponse> responses = atcTurnResponseService.searchResponses(keyword);
        return ResponseEntity.ok(responses);
    }

    // ==================== 统计信息 ====================

    @GetMapping("/airports/statistics")
    public ResponseEntity<Map<String, Object>> getAirportStatistics() {
        Map<String, Object> statistics = airportService.getAirportStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/scenarios/statistics")
    public ResponseEntity<Map<String, Object>> getScenarioStatistics() {
        Map<String, Object> statistics = atcScenarioService.getScenarioStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/turns/statistics")
    public ResponseEntity<Map<String, Object>> getTurnStatistics() {
        Map<String, Object> statistics = atcTurnService.getTurnStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/responses/statistics")
    public ResponseEntity<Map<String, Object>> getResponseStatistics() {
        Map<String, Object> statistics = atcTurnResponseService.getResponseStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/responses/statistics/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserResponseStatistics(@PathVariable Long userId) {
        Map<String, Object> statistics = atcTurnResponseService.getUserResponseStatistics(userId);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getSystemStatistics() {
        Map<String, Object> systemStats = Map.of(
                "airports", airportService.getAirportStatistics(),
                "scenarios", atcScenarioService.getScenarioStatistics(),
                "turns", atcTurnService.getTurnStatistics(),
                "responses", atcTurnResponseService.getResponseStatistics(),
                "timestamp", LocalDateTime.now()
        );
        return ResponseEntity.ok(systemStats);
    }

    // ==================== 题库管理系统 - ATC模拟题相关接口 ====================

    /**
     * 获取ATC模拟题列表 (对应 questionBank.js 的 getATCQuestions)
     */
    @GetMapping("/question-bank/atc")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<AtcScenario>>> getATCQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String scenarioType,
            @RequestParam(required = false) Integer difficultyLevel,
            @RequestParam(required = false) Boolean isActive) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            // TODO: 实现带筛选条件的查询
            Page<AtcScenario> scenarios = atcScenarioService.findAll(pageable);
            return ResponseEntity.ok(ApiResponse.success(scenarios));
        } catch (Exception e) {
            log.error("获取ATC模拟题列表失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取ATC模拟题列表失败"));
        }
    }

    /**
     * 创建ATC模拟题 (对应 questionBank.js 的 createATCQuestion)
     */
    @PostMapping("/question-bank/atc")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AtcScenario>> createATCQuestion(@Valid @RequestBody AtcScenario scenario) {
        try {
            AtcScenario createdScenario = atcScenarioService.save(scenario);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(createdScenario));
        } catch (Exception e) {
            log.error("创建ATC模拟题失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("创建ATC模拟题失败: " + e.getMessage()));
        }
    }

    /**
     * 更新ATC模拟题 (对应 questionBank.js 的 updateATCQuestion)
     */
    @PutMapping("/question-bank/atc/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AtcScenario>> updateATCQuestion(
            @PathVariable Long id,
            @Valid @RequestBody AtcScenario scenario) {
        try {
            if (!atcScenarioService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("ATC模拟题不存在"));
            }
            scenario.setId(id);
            AtcScenario updatedScenario = atcScenarioService.save(scenario);
            return ResponseEntity.ok(ApiResponse.success(updatedScenario));
        } catch (Exception e) {
            log.error("更新ATC模拟题失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新ATC模拟题失败: " + e.getMessage()));
        }
    }

    /**
     * 删除ATC模拟题 (对应 questionBank.js 的 deleteATCQuestion)
     */
    @DeleteMapping("/question-bank/atc/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteATCQuestion(@PathVariable Long id) {
        try {
            if (!atcScenarioService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("ATC模拟题不存在"));
            }
            atcScenarioService.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("ATC模拟题删除成功"));
        } catch (Exception e) {
            log.error("删除ATC模拟题失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除ATC模拟题失败: " + e.getMessage()));
        }
    }

    /**
     * 获取ATC场景列表 (对应 questionBank.js 的 getATCScenarios)
     */
    @GetMapping("/question-bank/atc/scenarios")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<AtcScenario>>> getATCScenarios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer difficultyLevel) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<AtcScenario> scenarios = atcScenarioService.findAll(pageable);
            return ResponseEntity.ok(ApiResponse.success(scenarios));
        } catch (Exception e) {
            log.error("获取ATC场景列表失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取ATC场景列表失败"));
        }
    }

    /**
     * 创建ATC场景 (对应 questionBank.js 的 createATCScenario)
     */
    @PostMapping("/question-bank/atc/scenarios")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AtcScenario>> createATCScenario(@Valid @RequestBody AtcScenario scenario) {
        try {
            AtcScenario createdScenario = atcScenarioService.save(scenario);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(createdScenario));
        } catch (Exception e) {
            log.error("创建ATC场景失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("创建ATC场景失败: " + e.getMessage()));
        }
    }

    /**
     * 更新ATC场景 (对应 questionBank.js 的 updateATCScenario)
     */
    @PutMapping("/question-bank/atc/scenarios/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AtcScenario>> updateATCScenario(
            @PathVariable Long id,
            @Valid @RequestBody AtcScenario scenario) {
        try {
            if (!atcScenarioService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("ATC场景不存在"));
            }
            scenario.setId(id);
            AtcScenario updatedScenario = atcScenarioService.save(scenario);
            return ResponseEntity.ok(ApiResponse.success(updatedScenario));
        } catch (Exception e) {
            log.error("更新ATC场景失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新ATC场景失败: " + e.getMessage()));
        }
    }

    /**
     * 删除ATC场景 (对应 questionBank.js 的 deleteATCScenario)
     */
    @DeleteMapping("/question-bank/atc/scenarios/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteATCScenario(@PathVariable Long id) {
        try {
            if (!atcScenarioService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("ATC场景不存在"));
            }
            atcScenarioService.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("ATC场景删除成功"));
        } catch (Exception e) {
            log.error("删除ATC场景失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除ATC场景失败: " + e.getMessage()));
        }
    }

    /**
     * 批量删除ATC场景 (对应 questionBank.js 的 batchDeleteATCScenarios)
     */
    @DeleteMapping("/question-bank/atc/scenarios/batch")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> batchDeleteATCScenarios(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> scenarioIds = (List<Long>) request.get("scenarioIds");
            
            if (scenarioIds == null || scenarioIds.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("场景ID列表不能为空"));
            }
            
            // TODO: 实现批量删除逻辑
            for (Long id : scenarioIds) {
                if (atcScenarioService.existsById(id)) {
                    atcScenarioService.deleteById(id);
                }
            }
            
            return ResponseEntity.ok(ApiResponse.success("批量删除ATC场景成功"));
        } catch (Exception e) {
            log.error("批量删除ATC场景失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("批量删除ATC场景失败: " + e.getMessage()));
        }
    }

    /**
     * 获取ATC通讯模板 (对应 questionBank.js 的 getATCCommunicationTemplates)
     */
    @GetMapping("/question-bank/atc/communication-templates")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getATCCommunicationTemplates() {
        try {
            // TODO: 实现获取ATC通讯模板的逻辑
            List<Map<String, Object>> templates = List.of(
                    Map.of("id", 1L, "name", "标准离场", "template", "Good morning, runway 09 cleared for takeoff"),
                    Map.of("id", 2L, "name", "标准进场", "template", "Contact tower 118.1, good day"),
                    Map.of("id", 3L, "name", "紧急情况", "template", "Emergency declared, priority handling required")
            );
            return ResponseEntity.ok(ApiResponse.success(templates));
        } catch (Exception e) {
            log.error("获取ATC通讯模板失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取ATC通讯模板失败"));
        }
    }

    /**
     * 创建ATC通讯模板 (对应 questionBank.js 的 createATCCommunicationTemplate)
     */
    @PostMapping("/question-bank/atc/communication-templates")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createATCCommunicationTemplate(@RequestBody Map<String, Object> template) {
        try {
            // TODO: 实现创建ATC通讯模板的逻辑
            template.put("id", System.currentTimeMillis());
            template.put("createdAt", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(template));
        } catch (Exception e) {
            log.error("创建ATC通讯模板失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("创建ATC通讯模板失败: " + e.getMessage()));
        }
    }

    // ==================== 文件上传相关接口 ====================

    /**
     * 上传ATC题目音频 (对应 questionBank.js 的 uploadQuestionAudio)
     */
    @PostMapping("/question-bank/upload/audio")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadQuestionAudio(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(required = false) Long questionId,
            @RequestParam(required = false) String quality) {
        try {
            // TODO: 实现音频文件上传逻辑
            Map<String, Object> result = Map.of(
                    "filename", file.getOriginalFilename(),
                    "size", file.getSize(),
                    "url", "/uploads/audio/" + file.getOriginalFilename(),
                    "questionId", questionId != null ? questionId : 0L,
                    "quality", quality != null ? quality : "medium"
            );
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("上传ATC题目音频失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("上传ATC题目音频失败: " + e.getMessage()));
        }
    }

    /**
     * 删除ATC题目音频 (对应 questionBank.js 的 deleteQuestionAudio)
     */
    @DeleteMapping("/question-bank/upload/audio")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteQuestionAudio(@RequestBody Map<String, Object> request) {
        try {
            String audioUrl = (String) request.get("audioUrl");
            if (audioUrl == null || audioUrl.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("音频URL不能为空"));
            }
            
            // TODO: 实现删除音频文件的逻辑
            
            return ResponseEntity.ok(ApiResponse.success("ATC题目音频删除成功"));
        } catch (Exception e) {
            log.error("删除ATC题目音频失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除ATC题目音频失败: " + e.getMessage()));
        }
    }

    // ==================== 数据导入导出接口 ====================

    /**
     * 批量导入ATC题目 (对应 questionBank.js 的 importQuestions)
     */
    @PostMapping("/question-bank/atc/import")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> importATCQuestions(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(required = false, defaultValue = "false") Boolean skipDuplicates,
            @RequestParam(required = false, defaultValue = "false") Boolean updateExisting,
            @RequestParam(required = false, defaultValue = "true") Boolean validateData) {
        try {
            // TODO: 实现ATC题目批量导入逻辑
            Map<String, Object> result = Map.of(
                    "totalRows", 100,
                    "successCount", 95,
                    "failureCount", 5,
                    "duplicateCount", 3,
                    "message", "ATC题目导入完成"
            );
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("批量导入ATC题目失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("批量导入ATC题目失败: " + e.getMessage()));
        }
    }

    /**
     * 导出ATC题目 (对应 questionBank.js 的 exportQuestions)
     */
    @GetMapping("/question-bank/atc/export")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<byte[]> exportATCQuestions(
            @RequestParam(required = false) String scenarioType,
            @RequestParam(required = false) Integer difficultyLevel,
            @RequestParam(required = false) Boolean isActive) {
        try {
            // TODO: 实现ATC题目导出逻辑
            String csvContent = "ID,场景类型,难度等级,是否激活,创建时间\n1,departure,3,true,2024-01-01";
            
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=atc_questions.csv")
                    .header("Content-Type", "text/csv")
                    .body(csvContent.getBytes());
        } catch (Exception e) {
            log.error("导出ATC题目失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== 高级搜索和筛选接口 ====================

    /**
     * 高级搜索ATC题目 (对应 questionBank.js 的 advancedSearch)
     */
    @PostMapping("/question-bank/atc/search/advanced")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<AtcScenario>>> advancedSearchATCQuestions(@RequestBody Map<String, Object> criteria) {
        try {
            // TODO: 实现高级搜索逻辑
            Pageable pageable = PageRequest.of(0, 20);
            Page<AtcScenario> scenarios = atcScenarioService.findAll(pageable);
            return ResponseEntity.ok(ApiResponse.success(scenarios));
        } catch (Exception e) {
            log.error("高级搜索ATC题目失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("高级搜索ATC题目失败"));
        }
    }

    /**
     * 全文搜索ATC题目 (对应 questionBank.js 的 fullTextSearch)
     */
    @GetMapping("/question-bank/atc/search/fulltext")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<AtcScenario>>> fullTextSearchATCQuestions(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            // TODO: 实现全文搜索逻辑
            Pageable pageable = PageRequest.of(page, size);
            Page<AtcScenario> scenarios = atcScenarioService.findAll(pageable);
            return ResponseEntity.ok(ApiResponse.success(scenarios));
        } catch (Exception e) {
            log.error("全文搜索ATC题目失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("全文搜索ATC题目失败"));
        }
    }
}