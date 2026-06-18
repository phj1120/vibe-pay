package com.api.app.service.goods

import com.api.app.common.exception.ApiError
import com.api.app.common.exception.ApiException
import com.api.app.dto.request.goods.GoodsItemRequest
import com.api.app.dto.request.goods.GoodsModifyRequest
import com.api.app.dto.request.goods.GoodsRegisterRequest
import com.api.app.dto.request.goods.GoodsSearchRequest
import com.api.app.dto.response.goods.GoodsDetailResponse
import com.api.app.dto.response.goods.GoodsItemResponse
import com.api.app.dto.response.goods.GoodsListResponse
import com.api.app.dto.response.goods.GoodsPageResponse
import com.api.app.entity.GoodsBase
import com.api.app.entity.GoodsItem
import com.api.app.entity.GoodsItemId
import com.api.app.entity.GoodsPriceHist
import com.api.app.entity.GoodsPriceHistId
import com.api.app.repository.rodb.goods.GoodsBaseRepository
import com.api.app.repository.rodb.goods.GoodsItemRepository
import com.api.app.repository.rodb.goods.GoodsPriceHistRepository
import com.api.app.repository.rwdb.goods.GoodsBaseTrxRepository
import com.api.app.repository.rwdb.goods.GoodsItemTrxRepository
import com.api.app.repository.rwdb.goods.GoodsPriceHistTrxRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class GoodsServiceImpl(
    private val goodsBaseRepository: GoodsBaseRepository,
    private val goodsBaseTrxRepository: GoodsBaseTrxRepository,
    private val goodsItemRepository: GoodsItemRepository,
    private val goodsItemTrxRepository: GoodsItemTrxRepository,
    private val goodsPriceHistRepository: GoodsPriceHistRepository,
    private val goodsPriceHistTrxRepository: GoodsPriceHistTrxRepository
) : GoodsService {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Transactional
    override fun registerGoods(request: GoodsRegisterRequest): String {
        val goodsBase = GoodsBase().apply {
            this.goodsName = request.goodsName
            this.goodsStatusCode = request.goodsStatusCode
            this.goodsMainImageUrl = request.goodsMainImageUrl
        }
        goodsBaseTrxRepository.save(goodsBase)
        val goodsNo = goodsBase.goodsNo
        log.debug("상품번호 생성: {}", goodsNo)

        val priceHist = GoodsPriceHist().apply {
            this.id = GoodsPriceHistId(goodsNo = goodsNo, startDateTime = LocalDateTime.now())
            this.endDateTime = LocalDateTime.of(9999, 12, 31, 23, 59, 59)
            this.salePrice = request.salePrice
            this.supplyPrice = request.supplyPrice
        }
        goodsPriceHistTrxRepository.save(priceHist)

        saveGoodsItems(goodsNo, request.items)

        log.info("상품 등록 완료: goodsNo={}, goodsName={}", goodsNo, request.goodsName)
        return goodsNo
    }

    @Transactional
    override fun modifyGoods(goodsNo: String, request: GoodsModifyRequest) {
        val goodsBase = goodsBaseRepository.findById(goodsNo).orElseThrow {
            ApiException(ApiError.DATA_NOT_FOUND, "상품을 찾을 수 없습니다")
        }

        goodsBase.apply {
            this.goodsName = request.goodsName
            this.goodsStatusCode = request.goodsStatusCode
            this.goodsMainImageUrl = request.goodsMainImageUrl
        }
        goodsBaseTrxRepository.save(goodsBase)

        val currentPrice = goodsPriceHistRepository.selectCurrentPrice(goodsNo)
        if (currentPrice == null || currentPrice.salePrice != request.salePrice || currentPrice.supplyPrice != request.supplyPrice) {
            goodsPriceHistTrxRepository.updatePreviousPriceEndDateTime(goodsNo)

            val priceHist = GoodsPriceHist().apply {
                this.id = GoodsPriceHistId(goodsNo = goodsNo, startDateTime = LocalDateTime.now())
                this.endDateTime = LocalDateTime.of(9999, 12, 31, 23, 59, 59)
                this.salePrice = request.salePrice
                this.supplyPrice = request.supplyPrice
            }
            goodsPriceHistTrxRepository.save(priceHist)
        }

        goodsItemTrxRepository.deleteByGoodsNo(goodsNo)
        saveGoodsItems(goodsNo, request.items)

        log.info("상품 수정 완료: goodsNo={}", goodsNo)
    }

    override fun getGoodsList(request: GoodsSearchRequest): GoodsPageResponse {
        val offset = request.page.toLong() * request.size
        val content = goodsBaseRepository.selectGoodsList(
            request.goodsStatusCode, request.goodsName, request.size, offset
        ).map {
            GoodsListResponse(
                goodsNo = it.goodsNo,
                goodsName = it.goodsName,
                goodsStatusCode = it.goodsStatusCode,
                goodsStatusName = it.goodsStatusName,
                goodsMainImageUrl = it.goodsMainImageUrl,
                salePrice = it.salePrice,
                supplyPrice = it.supplyPrice
            )
        }

        val totalElements = goodsBaseRepository.countGoodsList(request.goodsStatusCode, request.goodsName)
        val totalPages = Math.ceil(totalElements.toDouble() / request.size).toInt()

        return GoodsPageResponse(
            content = content,
            page = request.page,
            size = request.size,
            totalElements = totalElements,
            totalPages = totalPages,
            last = request.page >= totalPages - 1
        )
    }

    override fun getGoodsDetail(goodsNo: String): GoodsDetailResponse {
        val dto = goodsBaseRepository.selectGoodsDetail(goodsNo)
            ?: throw ApiException(ApiError.DATA_NOT_FOUND, "상품을 찾을 수 없습니다")

        val items = goodsItemRepository.findByIdGoodsNoOrderByIdItemNoAsc(goodsNo).map { it.toResponse() }

        return GoodsDetailResponse(
            goodsNo = dto.goodsNo,
            goodsName = dto.goodsName,
            goodsStatusCode = dto.goodsStatusCode,
            goodsStatusName = dto.goodsStatusName,
            goodsMainImageUrl = dto.goodsMainImageUrl,
            salePrice = dto.salePrice,
            supplyPrice = dto.supplyPrice,
            registDateTime = dto.registDateTime,
            modifyDateTime = dto.modifyDateTime,
            items = items
        )
    }

    @Transactional
    override fun deleteGoods(goodsNo: String) {
        if (!goodsBaseRepository.existsById(goodsNo)) {
            throw ApiException(ApiError.DATA_NOT_FOUND, "상품을 찾을 수 없습니다")
        }

        goodsItemTrxRepository.deleteByGoodsNo(goodsNo)
        goodsBaseTrxRepository.deleteById(goodsNo)
        log.info("상품 삭제 완료: goodsNo={}", goodsNo)
    }

    private fun saveGoodsItems(goodsNo: String, items: List<GoodsItemRequest>) {
        items.forEachIndexed { index, itemRequest ->
            val itemNo = String.format("%03d", index + 1)
            val goodsItem = GoodsItem().apply {
                this.id = GoodsItemId(goodsNo = goodsNo, itemNo = itemNo)
                this.itemName = itemRequest.itemName
                this.itemPrice = itemRequest.itemPrice
                this.stock = itemRequest.stock
                this.goodsStatusCode = itemRequest.goodsStatusCode
            }
            goodsItemTrxRepository.save(goodsItem)
        }
    }

    private fun GoodsItem.toResponse() = GoodsItemResponse(
        goodsNo = goodsNo,
        itemNo = itemNo,
        itemName = itemName,
        itemPrice = itemPrice,
        stock = stock,
        goodsStatusCode = goodsStatusCode,
        goodsStatusName = null,
        isSoldOut = stock <= 0
    )
}
