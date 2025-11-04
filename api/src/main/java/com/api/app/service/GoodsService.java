package com.api.app.service;

import com.api.app.entity.goodsbase.GoodsBaseDto;
import com.api.app.entity.goodsitem.GoodsItemDto;
import com.api.app.entity.goodspricehist.GoodsPriceHistDto;
import com.api.app.goods.request.GoodsItemRequestDto;
import com.api.app.goods.request.GoodsModifyRequestDto;
import com.api.app.goods.request.GoodsRegisterRequestDto;
import com.api.app.goods.response.GoodsDetailDto;
import com.api.app.mapper.goodsbase.GoodsBaseMapper;
import com.api.app.mapper.goodsitem.GoodsItemMapper;
import com.api.app.mapper.goodspricehist.GoodsPriceHistMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoodsService {

    private final GoodsBaseMapper goodsBaseMapper;
    private final GoodsItemMapper goodsItemMapper;
    private final GoodsPriceHistMapper goodsPriceHistMapper;

    @Transactional
    public void registerGoods(GoodsRegisterRequestDto request) {
        String goodsNo = UUID.randomUUID().toString();

        // 1. Insert into goods_base
        GoodsBaseDto goodsBase = new GoodsBaseDto();
        goodsBase.setGoodsNo(goodsNo);
        goodsBase.setGoodsName(request.getGoodsName());
        goodsBase.setGoodsStatusCode("001"); // Default to selling
        goodsBase.setGoodsMainImageUrl(request.getGoodsMainImageUrl());
        goodsBase.setRegistId("SYSTEM"); // TODO: Replace with actual user ID
        goodsBase.setRegistDateTime(LocalDateTime.now());
        goodsBase.setModifyId("SYSTEM"); // TODO: Replace with actual user ID
        goodsBase.setModifyDateTime(LocalDateTime.now());
        goodsBaseMapper.insertGoodsBase(goodsBase);

        // 2. Insert into goods_price_hist
        GoodsPriceHistDto goodsPriceHist = new GoodsPriceHistDto();
        goodsPriceHist.setGoodsNo(goodsNo);
        goodsPriceHist.setStartDateTime(LocalDateTime.now().toLocalDate());
        goodsPriceHist.setEndDateTime(LocalDateTime.now().toLocalDate().plusYears(100)); // Long validity
        goodsPriceHist.setSalePrice(request.getSalePrice());
        goodsPriceHist.setSupplyPrice(request.getSupplyPrice());
        goodsPriceHist.setRegistId("SYSTEM"); // TODO: Replace with actual user ID
        goodsPriceHist.setRegistDateTime(LocalDateTime.now());
        goodsPriceHist.setModifyId("SYSTEM"); // TODO: Replace with actual user ID
        goodsPriceHist.setModifyDateTime(LocalDateTime.now());
        goodsPriceHistMapper.insertGoodsPriceHist(goodsPriceHist);

        // 3. Insert into goods_item
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (GoodsItemRequestDto itemRequest : request.getItems()) {
                GoodsItemDto goodsItem = new GoodsItemDto();
                goodsItem.setGoodsNo(goodsNo);
                goodsItem.setItemNo(UUID.randomUUID().toString()); // Generate itemNo
                goodsItem.setItemName(itemRequest.getItemName());
                goodsItem.setItemPrice(itemRequest.getItemPrice());
                goodsItem.setStock(itemRequest.getStock());
                goodsItem.setGoodsStatusCode(itemRequest.getGoodsStatusCode());
                goodsItem.setRegistId("SYSTEM"); // TODO: Replace with actual user ID
                goodsItem.setRegistDateTime(LocalDateTime.now());
                goodsItem.setModifyId("SYSTEM"); // TODO: Replace with actual user ID
                goodsItem.setModifyDateTime(LocalDateTime.now());
                goodsItemMapper.insertGoodsItem(goodsItem);
            }
        }
    }

    @Transactional
    public void modifyGoods(GoodsModifyRequestDto request) {
        // 1. Update goods_base
        GoodsBaseDto goodsBase = goodsBaseMapper.selectGoodsBase(request.getGoodsNo());
        if (goodsBase == null) {
            throw new IllegalArgumentException("Goods not found.");
        }
        goodsBase.setGoodsName(request.getGoodsName());
        goodsBase.setGoodsStatusCode(request.getGoodsStatusCode());
        goodsBase.setGoodsMainImageUrl(request.getGoodsMainImageUrl());
        goodsBase.setModifyId("SYSTEM"); // TODO: Replace with actual user ID
        goodsBase.setModifyDateTime(LocalDateTime.now());
        goodsBaseMapper.updateGoodsBase(goodsBase);

        // 2. Update goods_price_hist (or insert new if price changed)
        // For simplicity, we'll just update the current active price history. In a real app,
        // you'd check if price changed and create a new history record with new dates.
        GoodsPriceHistDto currentPriceHist = goodsPriceHistMapper.selectCurrentGoodsPriceHist(request.getGoodsNo());
        if (currentPriceHist == null) {
            // This should not happen if goods was registered correctly
            throw new IllegalStateException("Current price history not found for goods: " + request.getGoodsNo());
        }
        currentPriceHist.setSalePrice(request.getSalePrice());
        currentPriceHist.setSupplyPrice(request.getSupplyPrice());
        currentPriceHist.setModifyId("SYSTEM"); // TODO: Replace with actual user ID
        currentPriceHist.setModifyDateTime(LocalDateTime.now());
        goodsPriceHistMapper.updateGoodsPriceHist(currentPriceHist);

        // 3. Update goods_item (delete existing and insert new, or update)
        // For simplicity, we'll delete all existing items and re-insert. In a real app,
        // you'd compare and update/insert/delete selectively.
        goodsItemMapper.deleteGoodsItemsByGoodsNo(request.getGoodsNo());
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (GoodsItemRequestDto itemRequest : request.getItems()) {
                GoodsItemDto goodsItem = new GoodsItemDto();
                goodsItem.setGoodsNo(request.getGoodsNo());
                goodsItem.setItemNo(UUID.randomUUID().toString()); // Generate new itemNo
                goodsItem.setItemName(itemRequest.getItemName());
                goodsItem.setItemPrice(itemRequest.getItemPrice());
                goodsItem.setStock(itemRequest.getStock());
                goodsItem.setGoodsStatusCode(itemRequest.getGoodsStatusCode());
                goodsItem.setRegistId("SYSTEM"); // TODO: Replace with actual user ID
                goodsItem.setRegistDateTime(LocalDateTime.now());
                goodsItem.setModifyId("SYSTEM"); // TODO: Replace with actual user ID
                goodsItem.setModifyDateTime(LocalDateTime.now());
                goodsItemMapper.insertGoodsItem(goodsItem);
            }
        }
    }

    public List<GoodsDetailDto> getGoodsList() {
        return goodsBaseMapper.selectGoodsDetailList();
    }
}
