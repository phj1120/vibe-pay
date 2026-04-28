package com.api.app.service.goods

import com.api.app.common.exception.ApiError
import com.api.app.common.exception.ApiException
import com.api.app.dto.request.goods.GoodsItemRequest
import com.api.app.dto.request.goods.GoodsModifyRequest
import com.api.app.dto.request.goods.GoodsRegisterRequest
import com.api.app.dto.request.goods.GoodsSearchRequest
import com.api.app.emum.PRD001
import com.api.app.entity.GoodsBase
import com.api.app.entity.GoodsItem
import com.api.app.entity.GoodsPriceHist
import com.api.app.repository.rodb.goods.GoodsBaseRepository
import com.api.app.repository.rodb.goods.GoodsDetailProjection
import com.api.app.repository.rodb.goods.GoodsItemProjection
import com.api.app.repository.rodb.goods.GoodsItemRepository
import com.api.app.repository.rodb.goods.GoodsListProjection
import com.api.app.repository.rodb.goods.GoodsPriceHistRepository
import com.api.app.repository.rwdb.goods.GoodsBaseTrxRepository
import com.api.app.repository.rwdb.goods.GoodsItemTrxRepository
import com.api.app.repository.rwdb.goods.GoodsPriceHistTrxRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class GoodsServiceImplTest {

    @InjectMocks
    private lateinit var goodsService: GoodsServiceImpl

    @Mock
    private lateinit var goodsBaseRepository: GoodsBaseRepository

    @Mock
    private lateinit var goodsBaseTrxRepository: GoodsBaseTrxRepository

    @Mock
    private lateinit var goodsItemRepository: GoodsItemRepository

    @Mock
    private lateinit var goodsItemTrxRepository: GoodsItemTrxRepository

    @Mock
    private lateinit var goodsPriceHistRepository: GoodsPriceHistRepository

    @Mock
    private lateinit var goodsPriceHistTrxRepository: GoodsPriceHistTrxRepository

    @Test
    @DisplayName("상품 등록 성공")
    fun registerGoodsSuccess() {
        val request = createRegisterRequest()
        given(goodsBaseTrxRepository.save(any(GoodsBase::class.java))).willAnswer { invocation ->
            (invocation.arguments[0] as GoodsBase).apply { goodsNo = "G00000000000001" }
        }

        val goodsNo = goodsService.registerGoods(request)

        assertThat(goodsNo).isEqualTo("G00000000000001")
        val priceCaptor = ArgumentCaptor.forClass(GoodsPriceHist::class.java)
        verify(goodsPriceHistTrxRepository).save(priceCaptor.capture())
        assertThat(priceCaptor.value.goodsNo).isEqualTo(goodsNo)
        assertThat(priceCaptor.value.salePrice).isEqualTo(request.salePrice)
        assertThat(priceCaptor.value.supplyPrice).isEqualTo(request.supplyPrice)

        val itemCaptor = ArgumentCaptor.forClass(GoodsItem::class.java)
        verify(goodsItemTrxRepository, times(2)).save(itemCaptor.capture())
        assertThat(itemCaptor.allValues.map { it.itemNo }).containsExactly("001", "002")
    }

    @Test
    @DisplayName("상품 수정 성공 - 가격 변경 포함")
    fun modifyGoodsSuccessWithPriceChange() {
        val goodsNo = "G00000000000001"
        val request = createModifyRequest(salePrice = 25000L, supplyPrice = 18000L)
        val goodsBase = createGoodsBase(goodsNo)
        given(goodsBaseRepository.findById(goodsNo)).willReturn(Optional.of(goodsBase))
        given(goodsPriceHistRepository.selectCurrentPrice(goodsNo)).willReturn(
            GoodsPriceHist().apply {
                this.id = this.id.copy(goodsNo = goodsNo)
                this.salePrice = 20000L
                this.supplyPrice = 15000L
            }
        )

        goodsService.modifyGoods(goodsNo, request)

        assertThat(goodsBase.goodsName).isEqualTo(request.goodsName)
        assertThat(goodsBase.goodsStatusCode).isEqualTo(request.goodsStatusCode)
        verify(goodsPriceHistTrxRepository).updatePreviousPriceEndDateTime(goodsNo)
        verify(goodsPriceHistTrxRepository, times(1)).save(any(GoodsPriceHist::class.java))
        verify(goodsItemTrxRepository).deleteByGoodsNo(goodsNo)
        verify(goodsItemTrxRepository, times(2)).save(any(GoodsItem::class.java))
    }

    @Test
    @DisplayName("상품 수정 성공 - 가격 변경 없음")
    fun modifyGoodsSuccessWithoutPriceChange() {
        val goodsNo = "G00000000000001"
        val request = createModifyRequest()
        given(goodsBaseRepository.findById(goodsNo)).willReturn(Optional.of(createGoodsBase(goodsNo)))
        given(goodsPriceHistRepository.selectCurrentPrice(goodsNo)).willReturn(
            GoodsPriceHist().apply {
                this.id = this.id.copy(goodsNo = goodsNo)
                this.salePrice = request.salePrice
                this.supplyPrice = request.supplyPrice
            }
        )

        goodsService.modifyGoods(goodsNo, request)

        verify(goodsPriceHistTrxRepository, never()).updatePreviousPriceEndDateTime(goodsNo)
        verify(goodsPriceHistTrxRepository, never()).save(any(GoodsPriceHist::class.java))
        verify(goodsItemTrxRepository).deleteByGoodsNo(goodsNo)
    }

    @Test
    @DisplayName("상품 수정 실패 - 상품 없음")
    fun modifyGoodsFailWhenMissing() {
        given(goodsBaseRepository.findById("G00000000000001")).willReturn(Optional.empty())

        assertThatThrownBy { goodsService.modifyGoods("G00000000000001", createModifyRequest()) }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.DATA_NOT_FOUND)
    }

    @Test
    @DisplayName("상품 목록 조회 성공")
    fun getGoodsListSuccess() {
        val request = GoodsSearchRequest(goodsStatusCode = PRD001.ON_SALE.code, goodsName = "테스트", page = 1, size = 2)
        val goods1 = createGoodsListProjection(goodsNo = "G001", goodsName = "테스트 1")
        val goods2 = createGoodsListProjection(goodsNo = "G002", goodsName = "테스트 2")
        given(goodsBaseRepository.selectGoodsList(request.goodsStatusCode, request.goodsName, 2, 2L))
            .willReturn(listOf(goods1, goods2))
        given(goodsBaseRepository.countGoodsList(request.goodsStatusCode, request.goodsName)).willReturn(5L)

        val response = goodsService.getGoodsList(request)

        assertThat(response.content).hasSize(2)
        assertThat(response.totalElements).isEqualTo(5L)
        assertThat(response.totalPages).isEqualTo(3)
        assertThat(response.last).isFalse()
    }

    @Test
    @DisplayName("상품 상세 조회 성공")
    fun getGoodsDetailSuccess() {
        val goodsNo = "G00000000000001"
        val detail = createGoodsDetailProjection(goodsNo)
        val item1 = createGoodsItemProjection(goodsNo = goodsNo, itemNo = "001", stock = 5L)
        val item2 = createGoodsItemProjection(goodsNo = goodsNo, itemNo = "002", stock = 0L)
        given(goodsBaseRepository.selectGoodsDetail(goodsNo)).willReturn(detail)
        given(goodsItemRepository.selectGoodsItemsByGoodsNo(goodsNo))
            .willReturn(listOf(item1, item2))

        val response = goodsService.getGoodsDetail(goodsNo)

        assertThat(response.goodsNo).isEqualTo(goodsNo)
        assertThat(response.items).hasSize(2)
        assertThat(response.items.last().isSoldOut).isTrue()
    }

    @Test
    @DisplayName("상품 상세 조회 실패 - 상품 없음")
    fun getGoodsDetailFailWhenMissing() {
        given(goodsBaseRepository.selectGoodsDetail("G00000000000001")).willReturn(null)

        assertThatThrownBy { goodsService.getGoodsDetail("G00000000000001") }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.DATA_NOT_FOUND)
    }

    @Test
    @DisplayName("상품 삭제 성공")
    fun deleteGoodsSuccess() {
        val goodsNo = "G00000000000001"
        given(goodsBaseRepository.existsById(goodsNo)).willReturn(true)

        goodsService.deleteGoods(goodsNo)

        verify(goodsItemTrxRepository).deleteByGoodsNo(goodsNo)
        verify(goodsBaseTrxRepository).deleteById(goodsNo)
    }

    @Test
    @DisplayName("상품 삭제 실패 - 상품 없음")
    fun deleteGoodsFailWhenMissing() {
        given(goodsBaseRepository.existsById("G00000000000001")).willReturn(false)

        assertThatThrownBy { goodsService.deleteGoods("G00000000000001") }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.DATA_NOT_FOUND)
    }

    private fun createRegisterRequest() = GoodsRegisterRequest(
        goodsName = "테스트 상품",
        goodsStatusCode = PRD001.ON_SALE.code,
        goodsMainImageUrl = "https://cdn.example.com/goods.jpg",
        salePrice = 20000L,
        supplyPrice = 15000L,
        items = listOf(
            GoodsItemRequest("옵션 1", 0L, 10L, PRD001.ON_SALE.code),
            GoodsItemRequest("옵션 2", 1000L, 0L, PRD001.ON_SALE.code)
        )
    )

    private fun createModifyRequest(
        salePrice: Long = 20000L,
        supplyPrice: Long = 15000L
    ) = GoodsModifyRequest(
        goodsName = "수정된 상품",
        goodsStatusCode = PRD001.ON_SALE.code,
        goodsMainImageUrl = "https://cdn.example.com/goods-updated.jpg",
        salePrice = salePrice,
        supplyPrice = supplyPrice,
        items = listOf(
            GoodsItemRequest("수정 옵션 1", 0L, 5L, PRD001.ON_SALE.code),
            GoodsItemRequest("수정 옵션 2", 1000L, 2L, PRD001.ON_SALE.code)
        )
    )

    private fun createGoodsBase(goodsNo: String) = GoodsBase().apply {
        this.goodsNo = goodsNo
        this.goodsName = "기존 상품"
        this.goodsStatusCode = PRD001.ON_SALE.code
        this.goodsMainImageUrl = "https://cdn.example.com/original.jpg"
    }

    private fun createGoodsListProjection(goodsNo: String, goodsName: String): GoodsListProjection {
        val projection = mock(GoodsListProjection::class.java)
        doReturn(goodsNo).`when`(projection).getGoodsNo()
        doReturn(goodsName).`when`(projection).getGoodsName()
        doReturn(PRD001.ON_SALE.code).`when`(projection).getGoodsStatusCode()
        doReturn("판매중").`when`(projection).getGoodsStatusName()
        doReturn("https://cdn.example.com/goods.jpg").`when`(projection).getGoodsMainImageUrl()
        doReturn(20000L).`when`(projection).getSalePrice()
        doReturn(15000L).`when`(projection).getSupplyPrice()
        return projection
    }

    private fun createGoodsDetailProjection(goodsNo: String): GoodsDetailProjection {
        val projection = mock(GoodsDetailProjection::class.java)
        doReturn(goodsNo).`when`(projection).getGoodsNo()
        doReturn("테스트 상품").`when`(projection).getGoodsName()
        doReturn(PRD001.ON_SALE.code).`when`(projection).getGoodsStatusCode()
        doReturn("판매중").`when`(projection).getGoodsStatusName()
        doReturn("https://cdn.example.com/goods.jpg").`when`(projection).getGoodsMainImageUrl()
        doReturn(20000L).`when`(projection).getSalePrice()
        doReturn(15000L).`when`(projection).getSupplyPrice()
        doReturn(LocalDateTime.of(2026, 4, 28, 0, 0)).`when`(projection).getRegistDateTime()
        doReturn(LocalDateTime.of(2026, 4, 28, 1, 0)).`when`(projection).getModifyDateTime()
        return projection
    }

    private fun createGoodsItemProjection(goodsNo: String, itemNo: String, stock: Long): GoodsItemProjection {
        val projection = mock(GoodsItemProjection::class.java)
        doReturn(goodsNo).`when`(projection).getGoodsNo()
        doReturn(itemNo).`when`(projection).getItemNo()
        doReturn("옵션$itemNo").`when`(projection).getItemName()
        doReturn(1000L).`when`(projection).getItemPrice()
        doReturn(stock).`when`(projection).getStock()
        doReturn(PRD001.ON_SALE.code).`when`(projection).getGoodsStatusCode()
        return projection
    }
}
