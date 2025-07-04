package com.kernel.common.manager.entity;

import com.kernel.common.global.entity.BaseEntity;
import com.kernel.common.reservation.entity.Reservation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name="cleaning_log")
@Getter
@SuperBuilder
@NoArgsConstructor
public class CleaningLog extends BaseEntity {

    // 체크ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long checkId;

    // 예약
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    private Reservation reservation;

    // 체크인 일시 (INSERT 시 자동 생성)
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime inTime;

    // 체크인 첨부파일
    private Long inFileId;

    // 체크아웃 일시
    private LocalDateTime outTime;

    // 체크아웃 첨부파일
    private Long outFileId;

    /**
     * 체크아웃 처리
     * @param outFileId 체크아웃파일ID
     */
    public void checkOut(Long outFileId) {
        this.outTime = LocalDateTime.now();
        this.outFileId = outFileId;
    }
}
