package com.api.app.service;

import com.api.app.entity.codedetail.CodeDetailDto;
import com.api.app.entity.pointhistory.PointHistoryDto;
import com.api.app.mapper.codedetail.CodeDetailMapper;
import com.api.app.mapper.pointhistory.PointHistoryMapper;
import com.api.app.point.request.PointTransactionRequestDto;
import com.api.app.point.response.PointHistoryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointHistoryMapper pointHistoryMapper;
    private final CodeDetailMapper codeDetailMapper;

    // 포인트 적립
    @Transactional
    public void earnPoints(String memberNo, PointTransactionRequestDto request) {
        CodeDetailDto validityPeriodCode = codeDetailMapper.selectCodeDetail("ME003", "001"); // Assuming 001 is the code for validity days
        int validityDays = Integer.parseInt(validityPeriodCode.getReferenceValue1());

        PointHistoryDto pointHistory = new PointHistoryDto();
        pointHistory.setPointHistoryNo(UUID.randomUUID().toString());
        pointHistory.setMemberNo(memberNo);
        pointHistory.setAmount(request.getAmount());
        pointHistory.setPointTransactionCode("001"); // 적립
        pointHistory.setPointTransactionReasonCode(request.getPointTransactionReasonCode());
        pointHistory.setPointTransactionReasonNo(request.getPointTransactionReasonNo());
        pointHistory.setStartDateTime(LocalDateTime.now());
        pointHistory.setEndDateTime(LocalDateTime.now().plusDays(validityDays));
        pointHistory.setRemainPoint(request.getAmount());
        pointHistory.setRegistId(memberNo);
        pointHistory.setRegistDateTime(LocalDateTime.now());
        pointHistory.setModifyId(memberNo);
        pointHistory.setModifyDateTime(LocalDateTime.now());

        pointHistoryMapper.insertPointHistory(pointHistory);
    }

    // 포인트 사용
    @Transactional
    public void usePoints(String memberNo, PointTransactionRequestDto request) {
        Long totalAvailablePoints = pointHistoryMapper.findTotalAvailablePointsByMemberNo(memberNo);
        if (totalAvailablePoints < request.getAmount()) {
            throw new IllegalArgumentException("Not enough points available.");
        }

        List<PointHistoryDto> availablePoints = pointHistoryMapper.findAvailablePointsByMemberNo(memberNo);
        Long amountToUse = request.getAmount();

        for (PointHistoryDto point : availablePoints) {
            if (amountToUse <= 0) break;

            Long usableAmount = Math.min(amountToUse, point.getRemainPoint());

            // Update the remaining points of the original accumulation
            point.setRemainPoint(point.getRemainPoint() - usableAmount);
            point.setModifyId(memberNo);
            point.setModifyDateTime(LocalDateTime.now());
            pointHistoryMapper.updatePointHistory(point);

            // Create a new usage history record
            PointHistoryDto usageHistory = new PointHistoryDto();
            usageHistory.setPointHistoryNo(UUID.randomUUID().toString());
            usageHistory.setMemberNo(memberNo);
            usageHistory.setAmount(usableAmount);
            usageHistory.setPointTransactionCode("002"); // 사용
            usageHistory.setPointTransactionReasonCode(request.getPointTransactionReasonCode());
            usageHistory.setPointTransactionReasonNo(request.getPointTransactionReasonNo());
            usageHistory.setUpperPointHistoryNo(point.getPointHistoryNo()); // Link to the accumulation record
            usageHistory.setRegistId(memberNo);
            usageHistory.setRegistDateTime(LocalDateTime.now());
            usageHistory.setModifyId(memberNo);
            usageHistory.setModifyDateTime(LocalDateTime.now());
            // startDateTime and endDateTime are not set for usage records as they refer to the parent accumulation

            pointHistoryMapper.insertPointHistory(usageHistory);

            amountToUse -= usableAmount;
        }
    }

    // 포인트 사용/적립 내역 조회
    public List<PointHistoryResponseDto> getPointHistory(String memberNo) {
        return pointHistoryMapper.selectPointHistoryList(memberNo).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    // 총 사용 가능 포인트 조회
    public Long getTotalAvailablePoints(String memberNo) {
        return pointHistoryMapper.findTotalAvailablePointsByMemberNo(memberNo);
    }

    private PointHistoryResponseDto convertToResponseDto(PointHistoryDto dto) {
        PointHistoryResponseDto response = new PointHistoryResponseDto();
        response.setPointHistoryNo(dto.getPointHistoryNo());
        response.setMemberNo(dto.getMemberNo());
        response.setAmount(dto.getAmount());
        response.setPointTransactionCode(dto.getPointTransactionCode());
        response.setPointTransactionReasonCode(dto.getPointTransactionReasonCode());
        response.setPointTransactionReasonNo(dto.getPointTransactionReasonNo());
        response.setStartDateTime(dto.getStartDateTime());
        response.setEndDateTime(dto.getEndDateTime());
        response.setUpperPointHistoryNo(dto.getUpperPointHistoryNo());
        response.setRemainPoint(dto.getRemainPoint());
        response.setRegistDateTime(dto.getRegistDateTime());
        return response;
    }
}