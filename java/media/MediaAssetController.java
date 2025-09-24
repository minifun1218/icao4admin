package org.icao4.eqasbackend2.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icao4.eqasbackend2.common.ApiResponse;
import org.icao4.eqasbackend2.dto.request.MediaAssetUploadRequest;
import org.icao4.eqasbackend2.dto.response.MediaAssetResponse;
import org.icao4.eqasbackend2.entity.MediaAsset;
import org.icao4.eqasbackend2.service.MediaAssetService;
import org.icao4.eqasbackend2.service.ControllerStatsService;
import org.icao4.eqasbackend2.dto.response.ControllerStatsResponse;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 媒体资产控制器
 * 提供媒体文件上传、下载、管理等功能
 */
@Slf4j
@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
@CacheConfig(cacheNames = "mediaAssets")
public class MediaAssetController {

    private final MediaAssetService mediaAssetService;
    private final ControllerStatsService controllerStatsService;

    /**
     * 上传媒体文件
     */
    @PostMapping("/upload")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @CacheEvict(allEntries = true)
    public ResponseEntity<ApiResponse<MediaAssetResponse>> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "title", required = false) String title) {
        
        log.info("API - 上传媒体文件: type={}, filename={}", type, file.getOriginalFilename());
        
        try {
            // TODO: 实现文件上传逻辑
            log.warn("上传媒体文件接口未实现: 暂无对应的Service方法");
            return ResponseEntity.ok(ApiResponse.error(501, "上传功能暂未实现"));
        } catch (Exception e) {
            log.error("上传媒体文件失败: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error(500, "上传媒体文件失败: " + e.getMessage()));
        }
    }

    /**
     * 根据ID获取媒体资源
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Cacheable(key = "#id")
    public ResponseEntity<ApiResponse<MediaAssetResponse>> getMediaById(@PathVariable Long id) {
        log.info("API - 获取媒体资源: {}", id);
        try {
            return mediaAssetService.getMediaAssetById(id)
                    .map(media -> {
                        // TODO: 转换为MediaAssetResponse
                        return ResponseEntity.ok(ApiResponse.<MediaAssetResponse>success("获取媒体资源成功"));
                    })
                    .orElse(ResponseEntity.ok(ApiResponse.error(404, "媒体资源不存在")));
        } catch (Exception e) {
            log.error("获取媒体资源失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error(500, "获取媒体资源失败"));
        }
    }

    /**
     * 删除媒体资源
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(allEntries = true)
    public ResponseEntity<ApiResponse<Void>> deleteMedia(@PathVariable Long id) {
        log.info("API - 删除媒体资源: {}", id);
        try {
            mediaAssetService.deleteMediaAsset(id);
            return ResponseEntity.ok(ApiResponse.success("删除媒体资源成功"));
        } catch (Exception e) {
            log.error("删除媒体资源失败: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error(500, "删除媒体资源失败: " + e.getMessage()));
        }
    }

    /**
     * 获取所有媒体资源
     */
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Cacheable(key = "'page:' + #page + ':size:' + #size + ':sort:' + #sort")
    public ResponseEntity<ApiResponse<Page<MediaAssetResponse>>> getAllMedia(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {
        
        log.info("API - 分页获取媒体资源列表: page={}, size={}, sort={}", page, size, sort);
        
        try {
            String[] sortParams = sort.split(",");
            String sortField = sortParams[0];
            Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc") ? 
                    Sort.Direction.DESC : Sort.Direction.ASC;
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
            Page<MediaAsset> mediaPage = mediaAssetService.getAllMediaAssets(pageable);
            
            // TODO: 转换为MediaAssetResponse
            return ResponseEntity.ok(ApiResponse.success("获取媒体资源列表成功"));
        } catch (Exception e) {
            log.error("获取媒体资源列表失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取媒体资源列表失败"));
        }
    }

    /**
     * 根据类型获取媒体资源
     */
    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Cacheable(key = "'type:' + #type")
    public ResponseEntity<ApiResponse<List<MediaAssetResponse>>> getMediaByType(@PathVariable String type) {
        log.info("API - 根据类型获取媒体资源: {}", type);
        try {
            MediaAsset.MediaType mediaType = MediaAsset.MediaType.valueOf(type.toLowerCase());
            Page<MediaAsset> pageResult = mediaAssetService.getMediaAssetsByType(mediaType, Pageable.unpaged());
            List<MediaAsset> mediaList = pageResult.getContent();
            
            // TODO: 转换为MediaAssetResponse列表
            return ResponseEntity.ok(ApiResponse.success("获取指定类型媒体资源成功"));
        } catch (IllegalArgumentException ex) {
            log.warn("无效的媒体类型: {}", type);
            return ResponseEntity.ok(ApiResponse.error(400, "无效的媒体类型: " + type));
        } catch (Exception e) {
            log.error("获取指定类型媒体资源失败: type={}, error={}", type, e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error(500, "获取指定类型媒体资源失败"));
        }
    }

    /**
     * 搜索媒体资源
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Cacheable(key = "'search:' + #keyword")
    public ResponseEntity<ApiResponse<List<MediaAssetResponse>>> searchMedia(@RequestParam String keyword) {
        log.info("API - 搜索媒体资源: {}", keyword);
        try {
            Page<MediaAsset> pageResult = mediaAssetService.searchMediaAssetsByTranscript(keyword, Pageable.unpaged());
            List<MediaAsset> mediaList = pageResult.getContent();
            
            // TODO: 转换为MediaAssetResponse列表
            return ResponseEntity.ok(ApiResponse.success("搜索媒体资源成功"));
        } catch (Exception e) {
            log.error("搜索媒体资源失败: keyword={}, error={}", keyword, e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error(500, "搜索媒体资源失败"));
        }
    }

    /**
     * 获取媒体资源管理统计信息
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ControllerStatsResponse.MediaControllerStats>> getMediaStats() {
        log.info("API - 获取媒体资源管理统计信息");
        
        try {
            ControllerStatsResponse.MediaControllerStats stats = controllerStatsService.getMediaControllerStats();
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            log.error("获取媒体统计信息失败: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error(500, "获取媒体统计信息失败: " + e.getMessage()));
        }
    }
}