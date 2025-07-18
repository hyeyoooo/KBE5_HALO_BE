package com.kernel.common.admin.service;

import com.kernel.common.admin.dto.mapper.ServiceCategoryMapper;
import com.kernel.common.admin.dto.request.AdminServiceCategoryReqDTO;
import com.kernel.common.admin.dto.response.AdminServiceCategoryRspDTO;
import com.kernel.common.repository.ServiceCategoryRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AdminServiceCategoryServiceImpl implements AdminServiceCategoryService {

    private final ServiceCategoryRepository serviceCategoryRepository;
    private final ServiceCategoryMapper serviceCategoryMapper;

    /**
     * 서비스 카테고리 목록 조회
     * @return List<AdminServiceCategoryRspDTO>
     */
    @Transactional(readOnly = true)
    @Override
    public List<AdminServiceCategoryRspDTO>  getServiceCategories() {
        return serviceCategoryMapper.toServiceCategoriesRspDTO(serviceCategoryRepository.findAll());
    }

    /**
     * 서비스 카테고리 생성
     * @param request AdminServiceCategoryReqDTO
     */
    @Transactional
    @Override
    public void createServiceCategory(AdminServiceCategoryReqDTO request) {
        var parentCategory = serviceCategoryRepository.findById(request.getParentId())
                        .orElse(null);

        serviceCategoryRepository.save(serviceCategoryMapper.toServiceCategory(request, parentCategory));
    }

    /**
     * 서비스 카테고리 수정
     * @param serviceCategoryId Long
     * @param request AdminServiceCategoryReqDTO
     */
    @Transactional
    @Override
    public void updateServiceCategory(Long serviceCategoryId, AdminServiceCategoryReqDTO request) {
        var serviceCategory = serviceCategoryRepository.findById(serviceCategoryId)
                .orElseThrow(() -> new NoSuchElementException("서비스 카테고리를 찾을 수 없습니다."));

        var parentCategory = serviceCategoryRepository.findById(request.getParentId())
                        .orElse(null);

        serviceCategory.update(request, parentCategory);
    }

    /**
     * 서비스 카테고리 삭제
     * @param serviceCategoryId Long
     */
    @Transactional
    @Override
    public void deleteServiceCategory(Long serviceCategoryId) {
        var serviceCategory = serviceCategoryRepository.findById(serviceCategoryId)
                .orElseThrow(() -> new NoSuchElementException("서비스 카테고리를 찾을 수 없습니다."));

        serviceCategory.delete();
    }
}