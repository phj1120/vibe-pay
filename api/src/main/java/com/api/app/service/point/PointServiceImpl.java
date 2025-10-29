package com.api.app.service.point;

import com.api.app.common.exception.ApiError;
import com.api.app.common.exception.ApiException;
import com.api.app.dto.request.point.PointHistoryRequest;
import com.api.app.dto.request.point.PointTransactionRequest;
import com.api.app.dto.response.point.PointBalanceResponse;
import com.api.app.dto.response.point.PointHistoryListResponse;
import com.api.app.dto.response.point.PointHistoryResponse;
import com.api.app.emum.MEM002;
import com.api.app.emum.MEM003;
import com.api.app.entity.MemberBase;
import com.api.app.entity.PointHistory;
import com.api.app.repository.member.MemberBaseMapper;
import com.api.app.repository.point.PointHistoryMapper;
import com.api.app.repository.point.PointHistoryTrxMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 포인트 서비스 구현
 *
 * @author system
 * @version 1.0
 * @since 2025-10-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private final MemberBaseMapper memberBaseMapper;
    private final PointHistoryMapper pointHistoryMapper;
    private final PointHistoryTrxMapper pointHistoryTrxMapper;

    @Override
    @Transactional
    public void processPointTransaction(String email, PointTransactionRequest request) {
        log.debug("포인트 처리 시작: email={}, code={}, amount={}",
                  email, request.getPointTransactionCode(), request.getAmount());

        // 회원 조회
        MemberBase member = memberBaseMapper.selectMemberBaseByEmail(email);
        if (member == null) {
            throw new ApiException(ApiError.DATA_NOT_FOUND, "회원 정보를 찾을 수 없습니다");
        }

        // 적립 또는 사용 처리
        if (MEM002.EARN.getCode().equals(request.getPointTransactionCode())) {
            processEarn(member.getMemberNo(), request);
        } else if (MEM002.USE.getCode().equals(request.getPointTransactionCode())) {
            processUse(member.getMemberNo(), request);
        } else {
            throw new ApiException(ApiError.INVALID_PARAMETER, "잘못된 포인트 거래 코드입니다");
        }

        log.info("포인트 처리 완료: memberNo={}, code={}, amount={}",
                 member.getMemberNo(), request.getPointTransactionCode(), request.getAmount());
    }

    /**
     * 포인트 적립 처리
     */
    private void processEarn(String memberNo, PointTransactionRequest request) {
        // 유효기간 계산 (MEM003의 referenceValue1 사용)
        int validityDays = Integer.parseInt(MEM003.ETC.getReferenceValue1());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDateTime = now.toLocalDate().atStartOfDay();
        LocalDateTime endDateTime = startDateTime.plusDays(validityDays);

        // 포인트 기록번호 생성
        String pointHistoryNo = pointHistoryTrxMapper.generatePointHistoryNo();

        // 포인트 내역 생성
        PointHistory pointHistory = new PointHistory();
        pointHistory.setPointHistoryNo(pointHistoryNo);
        pointHistory.setMemberNo(memberNo);
        pointHistory.setAmount(request.getAmount());
        pointHistory.setPointTransactionCode(request.getPointTransactionCode());
        pointHistory.setPointTransactionReasonCode(request.getPointTransactionReasonCode());
        pointHistory.setPointTransactionReasonNo(request.getPointTransactionReasonNo());
        pointHistory.setStartDateTime(startDateTime);
        pointHistory.setEndDateTime(endDateTime);
        pointHistory.setRemainPoint(request.getAmount());
        pointHistory.setRegistId(memberNo);
        pointHistory.setRegistDateTime(now);
        pointHistory.setModifyId(memberNo);
        pointHistory.setModifyDateTime(now);

        // 포인트 내역 등록
        int result = pointHistoryTrxMapper.insertPointHistory(pointHistory);
        if (result != 1) {
            throw new ApiException(ApiError.INTERNAL_SERVER_ERROR, "포인트 적립에 실패했습니다");
        }
    }

    /**
     * 포인트 사용 처리
     */
    private void processUse(String memberNo, PointTransactionRequest request) {
        Long useAmount = request.getAmount();
        LocalDateTime now = LocalDateTime.now();

        // 사용 가능한 포인트 조회
        PointBalanceResponse balance = pointHistoryMapper.selectPointBalance(memberNo);
        if (balance.getTotalPoint() < useAmount) {
            throw new ApiException(ApiError.INVALID_PARAMETER,
                    "사용 가능한 포인트가 부족합니다. 보유: " + balance.getTotalPoint() + ", 요청: " + useAmount);
        }

        // 종료일시가 가까운 순서로 포인트 사용
        List<PointHistory> availablePoints = pointHistoryMapper.selectAvailablePointHistory(memberNo);
        Long remainingAmount = useAmount;

        for (PointHistory earnHistory : availablePoints) {
            if (remainingAmount <= 0) {
                break;
            }

            Long currentRemain = earnHistory.getRemainPoint();
            Long deductAmount = Math.min(currentRemain, remainingAmount);

            // 적립 내역의 잔여 포인트 차감
            PointHistory updateEarn = new PointHistory();
            updateEarn.setPointHistoryNo(earnHistory.getPointHistoryNo());
            updateEarn.setRemainPoint(currentRemain - deductAmount);
            updateEarn.setModifyId(memberNo);
            updateEarn.setModifyDateTime(now);
            pointHistoryTrxMapper.updateRemainPoint(updateEarn);

            // 사용 내역 등록
            String useHistoryNo = pointHistoryTrxMapper.generatePointHistoryNo();
            PointHistory useHistory = new PointHistory();
            useHistory.setPointHistoryNo(useHistoryNo);
            useHistory.setMemberNo(memberNo);
            useHistory.setAmount(deductAmount);
            useHistory.setPointTransactionCode(request.getPointTransactionCode());
            useHistory.setPointTransactionReasonCode(request.getPointTransactionReasonCode());
            useHistory.setPointTransactionReasonNo(request.getPointTransactionReasonNo());
            useHistory.setUpperPointHistoryNo(earnHistory.getPointHistoryNo());
            useHistory.setRemainPoint(0L);
            useHistory.setRegistId(memberNo);
            useHistory.setRegistDateTime(now);
            useHistory.setModifyId(memberNo);
            useHistory.setModifyDateTime(now);
            pointHistoryTrxMapper.insertPointHistory(useHistory);

            remainingAmount -= deductAmount;
        }
    }

    @Override
    public PointBalanceResponse getPointBalance(String email) {
        log.debug("보유 포인트 조회: email={}", email);

        // 회원 조회
        MemberBase member = memberBaseMapper.selectMemberBaseByEmail(email);
        if (member == null) {
            throw new ApiException(ApiError.DATA_NOT_FOUND, "회원 정보를 찾을 수 없습니다");
        }

        // 보유 포인트 조회
        PointBalanceResponse response = pointHistoryMapper.selectPointBalance(member.getMemberNo());
        if (response == null) {
            response = new PointBalanceResponse();
            response.setTotalPoint(0L);
        }

        return response;
    }

    @Override
    public PointHistoryListResponse getPointHistoryList(String email, PointHistoryRequest request) {
        log.debug("포인트 내역 조회: email={}, page={}, size={}",
                  email, request.getPage(), request.getSize());

        // 회원 조회
        MemberBase member = memberBaseMapper.selectMemberBaseByEmail(email);
        if (member == null) {
            throw new ApiException(ApiError.DATA_NOT_FOUND, "회원 정보를 찾을 수 없습니다");
        }

        // 회원번호 설정
        request.setMemberNo(member.getMemberNo());

        // 포인트 내역 목록 조회
        List<PointHistoryResponse> content = pointHistoryMapper.selectPointHistoryList(request);
        Long totalElements = pointHistoryMapper.countPointHistory(request);

        // 응답 생성
        PointHistoryListResponse response = new PointHistoryListResponse();
        response.setContent(content);
        response.setPage(request.getPage());
        response.setSize(request.getSize());
        response.setTotalElements(totalElements);
        response.setTotalPages((int) Math.ceil((double) totalElements / request.getSize()));

        return response;
    }
}
