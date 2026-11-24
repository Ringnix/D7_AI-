package com.cyberway.mp.demo.backenddemo.domain.purchase.repository;

import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestPageDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.enums.PurchaseRequestStatus;

public class PurchaseRequestQuery {

    private String requestNoLike;

    private String titleLike;

    private PurchaseRequestStatus status;

    public String getRequestNoLike() {
        return requestNoLike;
    }

    public void setRequestNoLike(String requestNoLike) {
        this.requestNoLike = requestNoLike;
    }

    public String getTitleLike() {
        return titleLike;
    }

    public void setTitleLike(String titleLike) {
        this.titleLike = titleLike;
    }

    public PurchaseRequestStatus getStatus() {
        return status;
    }

    public void setStatus(PurchaseRequestStatus status) {
        this.status = status;
    }

    public static PurchaseRequestQuery fromPageDto(PurchaseRequestPageDto dto) {
        PurchaseRequestQuery query = new PurchaseRequestQuery();
        query.setRequestNoLike(dto.getRequestNoLike());
        query.setTitleLike(dto.getTitleLike());
        query.setStatus(dto.getStatus());
        return query;
    }
}
