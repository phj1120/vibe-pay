package com.vibepay.goods.service

import com.api.app.common.exception.ApiError
import com.api.app.common.exception.ApiException
import com.vibepay.goods.dto.internal.GoodsItemSnapshot
import com.vibepay.goods.dto.internal.GoodsValidateRequest
import com.vibepay.goods.dto.internal.GoodsValidateResponse
import com.vibepay.goods.dto.request.GoodsItemRequest
import com.vibepay.goods.dto.request.GoodsModifyRequest
import com.vibepay.goods.dto.request.GoodsRegisterRequest
import com.vibepay.goods.dto.request.GoodsSearchRequest
import com.vibepay.goods.dto.response.GoodsDetailResponse
import com.vibepay.goods.dto.response.GoodsItemResponse
import com.vibepay.goods.dto.response.GoodsListResponse
import com.vibepay.goods.dto.response.GoodsPageResponse
import com.vibepay.goods.entity.GoodsBase
import com.vibepay.goods.entity.GoodsItem
import com.vibepay.goods.entity.GoodsItemId
import com.vibepay.goods.entity.GoodsPriceHist
import com.vibepay.goods.entity.GoodsPriceHistId
import com.vibepay.goods.repository.GoodsBaseRepository
import com.vibepay.goods.repository.GoodsItemProjection
import com.vibepay.goods.repository.GoodsItemRepository
import com.vibepay.goods.repository.GoodsPriceHistRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.math.ceil

@Service
@Transactional(readOnly = true)
class GoodsServiceImpl(
    private val goodsBaseRepository: GoodsBaseRepository,
    private val goodsItemRepository: GoodsItemRepository,
    private val goodsPriceHistRepository: GoodsPriceHistRepository
) : GoodsService {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Transactional
    override fun registerGoods(request: GoodsRegisterRequest): String {
        val goodsBase = GoodsBase().apply {
            goodsName = request.goodsName
            goodsStatusCode = request.goodsStatusCode
            goodsMainImageUrl = request.goodsMainImageUrl
        }
        goodsBaseRepository.save(goodsBase)
        val goodsNo = goodsBase.goodsNo
        log.debug("상품번호 생성: {}", goodsNo)

        val priceHist = GoodsPriceHist().apply {
            id = GoodsPriceHistId(goodsNo = goodsNo, startDateTime = LocalDateTime.now())
            endDateTime = LocalDateTime.of(9999, 12, 31, 23, 59, 59)
            salePrice = request.salePrice
            supplyPrice = request.supplyPrice
        }
        goodsPriceHistRepository.save(priceHist)
        saveGoodsItems(goodsNo, request.items)
        log.info("상품 등록 완료: goodsNo={}, goodsName={}", goodsNo, request.goodsName)
        return goodsNo
    }

    @Transactional
    override fun modifyGoods(goodsNo: String, request: GoodsModifyRequest) {
        val goodsBase = goodsBaseRepository.findById(goodsNo)
            .orElseThrow { ApiException(ApiError.DATA_NOT_FOUND, "상품을 찾을 수 없습니다") }

        goodsBase.apply {
            goodsName = request.goodsName
            goodsStatusCode = request.goodsStatusCode
            goodsMainImageUrl = request.goodsMainImageUrl
        }
        goodsBaseRepository.save(goodsBase)

        val currentPrice = goodsPriceHistRepository.selectCurrentPrice(goodsNo)
        if (currentPrice == null || currentPrice.salePrice != request.salePrice || currentPrice.supplyPrice != request.supplyPrice) {
            goodsPriceHistRepository.updatePreviousPriceEndDateTime(goodsNo)
            val priceHist = GoodsPriceHist().apply {
                id = GoodsPriceHistId(goodsNo = goodsNo, startDateTime = LocalDateTime.now())
                endDateTime = LocalDateTime.of(9999, 12, 31, 23, 59, 59)
                salePrice = request.salePrice
                supplyPrice = request.supplyPrice
            }
            goodsPriceHistRepository.save(priceHist)
        }

        goodsItemRepository.deleteByGoodsNo(goodsNo)
        saveGoodsItems(goodsNo, request.items)
        log.info("상품 수정 완료: goodsNo={}", goodsNo)
    }

    override fun getGoodsList(request: GoodsSearchRequest): GoodsPageResponse {
        val offset = request.page.toLong() * request.size
        val content = goodsBaseRepository.selectGoodsList(request.goodsStatusCode, request.goodsName, request.size, offset)
            .map {
                GoodsListResponse(
                    goodsNo = it.getGoodsNo(), goodsName = it.getGoodsName(),
                    goodsStatusCode = it.getGoodsStatusCode(), goodsStatusName = it.getGoodsStatusName(),
                    goodsMainImageUrl = it.getGoodsMainImageUrl(), salePrice = it.getSalePrice(),
                    supplyPrice = it.getSupplyPrice()
                )
            }
        val totalElements = goodsBaseRepository.countGoodsList(request.goodsStatusCode, request.goodsName)
        val totalPages = ceil(totalElements.toDouble() / request.size).toInt()
        return GoodsPageResponse(
            content = content, page = request.page, size = request.size,
            totalElements = totalElements, totalPages = totalPages,
            last = request.page >= totalPages - 1
        )
    }

    override fun getGoodsDetail(goodsNo: String): GoodsDetailResponse {
        val projection = goodsBaseRepository.selectGoodsDetail(goodsNo)
            ?: throw ApiException(ApiError.DATA_NOT_FOUND, "상품을 찾을 수 없습니다")
        val items = goodsItemRepository.selectGoodsItemsByGoodsNo(goodsNo).map { it.toResponse() }
        return GoodsDetailResponse(
            goodsNo = projection.getGoodsNo(), goodsName = projection.getGoodsName(),
            goodsStatusCode = projection.getGoodsStatusCode(), goodsStatusName = projection.getGoodsStatusName(),
            goodsMainImageUrl = projection.getGoodsMainImageUrl(), salePrice = projection.getSalePrice(),
            supplyPrice = projection.getSupplyPrice(), registDateTime = projection.getRegistDateTime(),
            modifyDateTime = projection.getModifyDateTime(), items = items
        )
    }

    @Transactional
    override fun deleteGoods(goodsNo: String) {
        if (!goodsBaseRepository.existsById(goodsNo)) throw ApiException(ApiError.DATA_NOT_FOUND, "상품을 찾을 수 없습니다")
        goodsItemRepository.deleteByGoodsNo(goodsNo)
        goodsBaseRepository.deleteById(goodsNo)
        log.info("상품 삭제 완료: goodsNo={}", goodsNo)
    }

    override fun validateGoods(request: GoodsValidateRequest): GoodsValidateResponse {
        val snapshots = mutableListOf<GoodsItemSnapshot>()
        for (validateItem in request.items) {
            val item = goodsItemRepository.findByIdGoodsNoAndIdItemNo(validateItem.goodsNo, validateItem.itemNo)
                ?: return GoodsValidateResponse(valid = false, errorMessage = "상품 정보를 찾을 수 없습니다: ${validateItem.goodsNo}-${validateItem.itemNo}")

            if (item.stock < validateItem.quantity) {
                return GoodsValidateResponse(valid = false, errorMessage = "재고가 부족합니다: ${validateItem.goodsNo}-${validateItem.itemNo} (재고: ${item.stock}, 요청: ${validateItem.quantity})")
            }

            val price = goodsPriceHistRepository.selectCurrentPrice(validateItem.goodsNo)
                ?: return GoodsValidateResponse(valid = false, errorMessage = "가격 정보를 찾을 수 없습니다: ${validateItem.goodsNo}")

            if (price.salePrice != validateItem.expectedSalePrice) {
                return GoodsValidateResponse(valid = false, errorMessage = "가격이 변경되었습니다: ${validateItem.goodsNo} (DB: ${price.salePrice}, 요청: ${validateItem.expectedSalePrice})")
            }

            val goodsBase = goodsBaseRepository.findById(validateItem.goodsNo).orElse(null)
            snapshots.add(GoodsItemSnapshot(
                goodsNo = validateItem.goodsNo, itemNo = validateItem.itemNo,
                goodsName = goodsBase?.goodsName ?: "", itemName = item.itemName,
                salePrice = price.salePrice, supplyPrice = price.supplyPrice, stock = item.stock
            ))
        }
        return GoodsValidateResponse(valid = true, items = snapshots)
    }

    private fun saveGoodsItems(goodsNo: String, items: List<GoodsItemRequest>) {
        items.forEachIndexed { index, itemRequest ->
            val itemNo = String.format("%03d", index + 1)
            val goodsItem = GoodsItem().apply {
                id = GoodsItemId(goodsNo = goodsNo, itemNo = itemNo)
                itemName = itemRequest.itemName
                itemPrice = itemRequest.itemPrice
                stock = itemRequest.stock
                goodsStatusCode = itemRequest.goodsStatusCode
            }
            goodsItemRepository.save(goodsItem)
        }
    }

    private fun GoodsItemProjection.toResponse() = GoodsItemResponse(
        goodsNo = getGoodsNo(), itemNo = getItemNo(), itemName = getItemName(),
        itemPrice = getItemPrice(), stock = getStock(), goodsStatusCode = getGoodsStatusCode(),
        goodsStatusName = null, isSoldOut = getStock()?.let { it <= 0 } ?: false
    )
}
