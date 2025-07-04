package com.kernel.reservation.controller;


import com.kernel.global.security.CustomUserDetails;
import com.kernel.global.service.dto.response.ApiResponse;
import com.kernel.reservation.service.MatchService;
import com.kernel.reservation.service.request.ChooseManagerReqDTO;
import com.kernel.reservation.service.response.common.MatchedManagersRspDTO;
import com.kernel.reservation.service.response.common.ReservationMatchedRspDTO;
import com.kernel.reservation.service.response.common.ServiceCategoryTreeDTO;
import com.kernel.sharedDomain.common.enums.ReservationStatus;
import com.kernel.reservation.service.CustomerReservationService;
import com.kernel.reservation.service.ServiceCategoryService;
import com.kernel.reservation.service.request.ReservationCancelReqDTO;
import com.kernel.reservation.service.request.ReservationReqDTO;
import com.kernel.reservation.service.response.*;
import com.kernel.reservation.service.response.common.ReservationRspDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers/reservations")
@RequiredArgsConstructor
public class CustomerReservationController {

    private final CustomerReservationService customerReservationService;
    private final ServiceCategoryService serviceCategoryService;
    private final MatchService matchServiceService;

    /**
     * 예약 요청
     * @param user 로그인한 유저
     * @param reservationReqDTO 수요자 예약 요청 DTO
     * @return 예약 요청 내용 + 매칭 매니저 리스트
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReservationMatchedRspDTO>> makeReservationByCustomer(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody ReservationReqDTO reservationReqDTO
    ){
        // 1. 예약 요청 저장
        ReservationRspDTO requestedReservation = customerReservationService.makeReservationByCustomer(user.getUserId(), reservationReqDTO);

        // 2. 예약 요청 서비스 카테고리 조회
        ServiceCategoryTreeDTO requestCategory
                = serviceCategoryService.getRequestServiceCategory(reservationReqDTO.getMainServiceId(), reservationReqDTO.getAdditionalServiceIds());

        // 3. 매칭 매니저 조회
        List<MatchedManagersRspDTO> matchedManagers = matchServiceService.getMatchingManagers(reservationReqDTO, user.getUserId());

        // 4. 예약ID + 매칭 매니저 리스트
        ReservationMatchedRspDTO result = ReservationMatchedRspDTO.builder()
                .reservation(requestedReservation)
                .requestCategory(requestCategory)
                .matchedManagers(matchedManagers)
                .build();


        return ResponseEntity.ok(new ApiResponse<>(true, "수요자 예약 요청 성공", result));
    }

    /**
     * 예약 매칭 저장
     * @param reservationId 예약ID
     * @param user 로그인한 유저
     * @param chooseReqDTO 예약 매칭 매니저 ID
     * @return 확정한 예약 정보
     */
    @PatchMapping("/{reservation-id}/confirm")
    public ResponseEntity<ApiResponse<CustomerReservationSummaryRspDTO>> chooseManager(
            @PathVariable("reservation-id") Long reservationId,
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody ChooseManagerReqDTO chooseReqDTO
    ){
        // 1. 예약 매칭 저장
        matchServiceService.saveReservationMatch(user.getUserId(), reservationId, chooseReqDTO);

        // 2. 예약 요청 조회
        //CustomerReservationSummaryRspDTO rspDTO = customerReservationService;

        return ResponseEntity.ok(new ApiResponse<>(true, "수요자 예약 확정 성공", null));
    }


    /**
     * 예약 내역 조회
     * @param status 예약 상태
     * @param user 로그인한 유저
     * @param pageable 페이징 정보
     * @return 검색 조건에 따른 예약 목록(페이징 포함)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CustomerReservationSummaryRspDTO>>> getCustomerReservations(
            @RequestParam(required = false) ReservationStatus status,
            @PageableDefault(size = 5) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Page<CustomerReservationSummaryRspDTO> rspDTOPage
                = customerReservationService.getCustomerReservations(user.getUserId(), status, pageable);

        return ResponseEntity.ok(new ApiResponse<>(true,  "수요자 예약 내역 조회 성공", rspDTOPage));
    }

    /**
     * 예약 내역 상세 조회
     * @param reservationId 예약 ID
     * @param user 로그인한 유저
     * @return 조회된 예약 상세 정보
     */
    @GetMapping("{reservation-id}")
    public ResponseEntity<ApiResponse<CustomerReservationDetailRspDTO>> getCustomerReservationDetail(
            @PathVariable("reservation-id") Long reservationId,
            @AuthenticationPrincipal CustomUserDetails user
    ){
        CustomerReservationDetailRspDTO detailRspDTO
                = customerReservationService.getCustomerReservationDetail(user.getUserId(),reservationId);

        return ResponseEntity.ok(new ApiResponse<>(true, "수요자 예약 내역 상세 조회 성공", detailRspDTO));
    }

    /**
     * 예약 취소
     * @param user 로그인한 유저
     * @param cancelReqDTO 예약 취소 요청 DTO
     */
    @PatchMapping("/{reservation_id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelReservationByCustomer(
            @RequestBody ReservationCancelReqDTO cancelReqDTO,
            @AuthenticationPrincipal CustomUserDetails user
    ){

        // 1. 예약 상태 변경 및 예약 취소 저장
        customerReservationService.cancelReservationByCustomer(user.getUserId(), user.getRole(), cancelReqDTO);

        // 2. 매칭 상태 변경
        matchServiceService.changeStatus(cancelReqDTO.getReservationId());

        return ResponseEntity.ok(new ApiResponse<Void>(true, "수요자 예약 취소 성공", null));
    }

    /**
     * 예약 확정 전 취소
     * @param reservationId 예약ID
     * @param user 수요자ID
     */
    @PatchMapping("/{reservation-id}/pre-cancel")
    public ResponseEntity<ApiResponse<Void>> cancelBeforeConfirmReservation(
            @PathVariable("reservation-id") Long reservationId,
            @AuthenticationPrincipal CustomUserDetails user
    ){
        customerReservationService.cancelBeforeConfirmReservation(user.getUserId(), user.getRole(), reservationId);
        return ResponseEntity.ok(new ApiResponse<>(true, "수요자 예약 확정 전 취소 성공", null));
    }
}
