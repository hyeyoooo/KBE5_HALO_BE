package com.kernel.admin.service;

import com.kernel.admin.repository.AdminRepository;
import com.kernel.admin.service.dto.request.AdminSearchReqDTO;
import com.kernel.admin.service.dto.response.AdminDetailRspDTO;
import com.kernel.admin.service.dto.response.AdminSearchRspDTO;
import com.kernel.global.common.enums.UserRole;
import com.kernel.global.common.enums.UserStatus;
import com.kernel.global.domain.entity.User;
import com.kernel.global.domain.info.AdminUserSearchInfo;
import com.kernel.global.service.dto.condition.AdminUserSearchCondition;
import com.kernel.member.service.common.UserService;
import com.kernel.member.service.common.info.UserAccountInfo;
import com.kernel.member.service.common.request.UserSignupReqDTO;
import com.kernel.member.service.common.request.UserUpdateReqDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserService userService;
    private final AdminRepository adminRepository;

    /**
    * 관리자 회원가입
    *
    * @param signupReqDTO 관리자회원가입 요청 DTO
    */
    @Override
    @Transactional
    public void signup(UserSignupReqDTO signupReqDTO) {
        // 1. phone 중복 검사
        userService.validateDuplicatePhone(signupReqDTO.getPhone());

        // 2. user 저장
        User savedUser = userService.createUser(signupReqDTO, UserRole.ADMIN);

        adminRepository.save(savedUser);
    }


    /**
    * 관리자 목록 조회
    *
    * @param request  검색 조건을 포함하는 요청 DTO
    * @param pageable 페이징 정보
    * @return 검색된 관리자 목록과 페이징 정보가 포함된 Page 객체
    */
    @Override
    @Transactional(readOnly = true)
    public Page<AdminSearchRspDTO> searchAdminList(AdminSearchReqDTO request, Pageable pageable) {
        AdminUserSearchCondition adminUserSearchCondition = request.toCondition();

        Page<AdminUserSearchInfo> adminPage = adminRepository.searchByConditionsWithPaging(adminUserSearchCondition, pageable);


        return AdminSearchRspDTO.fromInfo(adminPage);
    }

    /**
    * 관리자 정보 수정
    *
    * @param userId 관리자 ID
    * @param updateReqDTO 관리자 정보 수정 요청 DTO
    * @return 수정된 관리자 상세 정보 DTO
    */
    @Override
    @Transactional
    public AdminDetailRspDTO updateAdmin(Long userId, UserUpdateReqDTO updateReqDTO) {

        // 1. admin User 조회
        User foundAdmin = userService.getByUserIdAndStatus(userId, UserStatus.ACTIVE);

        // User 수정
        foundAdmin.updateEmail(updateReqDTO.getEmail());

        // DTO 변환 후 return
        return AdminDetailRspDTO.fromInfo(
            UserAccountInfo.fromEntity(foundAdmin)
        );
    }

    /**
    * 관리자 삭제
    *
    * @param userId 관리자 ID
    */
    @Override
    @Transactional
    public void deleteAdmin(Long userId) {
         // 1. admin User 조회
         User foundAdmin = userService.getByUserIdAndStatus(userId, UserStatus.ACTIVE);

         // 2. User 삭제
         foundAdmin.delete();

         // 3. User 저장
         adminRepository.save(foundAdmin);
     }

}
